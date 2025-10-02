package com.mysite.sbb.study;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyService {
    @Value("${file.upload-study-dir}")
    private String uploadStudyDir; // 이미지 저장 루트


    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final StudyMapper studyMapper;
    private final StudyImageRepository studyImageRepository; // 없다면 생성해 두세요.


    public Page<StudyDTO> getList(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        return studyMapper.toDTO(studyRepository.findAll(pageable));
    }

    public StudyDTO getStudy(Integer id) {
        Study study = studyRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Study not found"));
        return studyMapper.toDTO(study);
    }

    /** 수정 폼에서 기존 이미지 렌더용 */
    @Transactional(readOnly = true)
    public List<StudyImage> findImagesByStudyId(Integer studyId) {
        Study p = studyRepository.findById(studyId)
                .orElseThrow(() -> new DataNotFoundException("Study not found"));
        // 정렬된 리스트 반환 (엔티티 그대로 사용)
        return p.getImages().stream()
                .sorted(Comparator.comparing(img -> Optional.ofNullable(img.getSortOrder()).orElse(0)))
                .collect(Collectors.toList());
    }


    /** 등록: 생성 후 새로 생성된 id 반환 */
    @Transactional
    public Integer create(StudyDTO dto) {
        SiteUser author = userRepository.findByusername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


        Study study = Study.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .createDate(LocalDateTime.now())
                .author(author)
                .images(new ArrayList<>())
                .build();

        // 새 이미지 저장
        appendImages(study, dto.getImages());
        Study saved = studyRepository.save(study);
        return saved.getId(); // Integer
    }


    /** 수정: 필드/연관관계 갱신 + 이미지 삭제/추가 + 정렬 */
    @Transactional
    public void update(StudyDTO dto, List<Long> deleteImageIds) {
        Study study = studyRepository.findById(dto.getId())
                .orElseThrow(() -> new DataNotFoundException("Study not found"));

        // 필드 갱신
        study.setTitle(dto.getTitle());
        study.setDescription(dto.getDescription());


        // 이미지 삭제
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            // 1) 물리 파일 삭제 시도
            for (Iterator<StudyImage> it = study.getImages().iterator(); it.hasNext(); ) {
                StudyImage img = it.next();
                if (deleteImageIds.contains(img.getId())) {
                    deletePhysicalFileQuietly(img.getImagePath());
                    it.remove(); // orphanRemoval=true면 이걸로 DB 삭제됨
                }
            }
        }

        // 새 이미지 추가
        appendImages(study, dto.getImages());

        // sortOrder 정렬(0..n-1)
        resequenceSortOrders(study);

        // 변경 저장
        studyRepository.save(study);
    }


    @Transactional
    public void deleteStudy(List<Integer> studyIds) {
        if (studyIds == null || studyIds.isEmpty()) return;

        // 0) 파일 경로 미리 수집 (DB 삭제 이전에)
        // 0-1) 이미지 파일 경로
        List<String> imageUrlPaths = studyImageRepository.findPathsByStudyIdIn(studyIds);


        List<Path> fileTargets = new ArrayList<>();
        // 이미지
        for (String urlPath : imageUrlPaths) {
            Path p = toLocalImagePath(urlPath);
            if (p != null) fileTargets.add(p);
        }


        // 1) 자식(이미지) 먼저 벌크 삭제
        studyImageRepository.deleteByStudyIdIn(studyIds);

        // 2) 부모(제품) 벌크 삭제
        studyRepository.deleteAllByIdInBatch(studyIds);

        // 3) 트랜잭션 커밋 이후 실제 파일 삭제 (DB 롤백 시 파일 보존)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                for (Path path : fileTargets) {
                    try { Files.deleteIfExists(path); } catch (Exception ignore) { /* 로그 원하면 기록 */ }
                }
            }
        });
    }

    private Path toLocalImagePath(String urlPath) {
        if (urlPath == null || urlPath.isBlank()) return null;
        String p = urlPath.trim().replace('\\', '/');
        if (!p.startsWith("/uploadStudyImages/")) return null;
        String rel = p.substring("/uploadStudyImages/".length());
        Path resolved = Paths.get(uploadStudyDir, rel).normalize();
        Path base = Paths.get(uploadStudyDir).toAbsolutePath().normalize();
        return resolved.toAbsolutePath().startsWith(base) ? resolved : null;
    }


    /* ---------- 내부 유틸 ---------- */

    /** 이미지 리스트를 study에 이어붙임 (파일 저장 포함) */
    private void appendImages(Study study, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        ensureUploadDir();

        // 기존 마지막 인덱스 다음부터
        int startIndex = study.getImages().stream()
                .map(img -> Optional.ofNullable(img.getSortOrder()).orElse(0))
                .max(Integer::compareTo).orElse(-1) + 1;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String savedRelPath = saveFile(file); // "/uploadImages/xxxx.png"
            StudyImage image = StudyImage.builder()
                    .imagePath(savedRelPath)
                    .study(study)
                    .sortOrder(startIndex++)
                    .build();
            study.getImages().add(image);
        }
    }

    /** 업로드 디렉토리 보장 */
    private void ensureUploadDir() {
        File dir = new File(uploadStudyDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create upload Study directory: " + uploadStudyDir);
        }
    }


    /** 물리 파일 저장 후, 웹 경로 반환 (예: "/uploadImages/uuid.png") */
    private String saveFile(MultipartFile file) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String ext = "";
        int pos = originalFilename.lastIndexOf('.');
        if (pos >= 0) ext = originalFilename.substring(pos);
        String saveName = UUID.randomUUID().toString() + ext;

        Path savePath = Paths.get(uploadStudyDir).resolve(saveName);
        try {
            file.transferTo(savePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + originalFilename, e);
        }
        // 웹에서 접근할 상대 경로 규칙에 맞춰 반환
        return "/uploadStudyImages/" + saveName;
    }

    /** 물리 파일 삭제(있으면) */
    private void deletePhysicalFileQuietly(String imagePath) {
        try {
            // imagePath가 "/uploadImages/파일명" 형태라면, 파일명만 추출해서 uploadDir와 매핑
            if (imagePath == null) return;
            String fileName = imagePath;
            int idx = imagePath.lastIndexOf('/');
            if (idx >= 0) fileName = imagePath.substring(idx + 1);
            File f = Paths.get(uploadStudyDir).resolve(fileName).toFile();
            if (f.exists()) f.delete();
        } catch (Exception ignore) {}
    }

    /** sortOrder를 0..n-1로 재배치 */
    private void resequenceSortOrders(Study study) {
        List<StudyImage> imgs = study.getImages();
        imgs.sort(Comparator.comparing(img -> Optional.ofNullable(img.getSortOrder()).orElse(0)));
        for (int i = 0; i < imgs.size(); i++) {
            imgs.get(i).setSortOrder(i);
        }
    }

}


