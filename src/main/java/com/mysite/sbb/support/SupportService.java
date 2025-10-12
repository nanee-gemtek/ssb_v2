package com.mysite.sbb.support;

import com.mysite.sbb.DataNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportService {

    //file.upload-dir=/home/web/upload/product
    //file.docs-dir=/home/web/upload/docs

    @Value("${file.upload-support-dir}")
    private String uploadSupportDir; // 이미지 저장 루트





    private final SupportRepository supportRepository;
    private final SupportMapper supportMapper;
    private final UserRepository userRepository;



    public Page<SupportDTO> getList(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        return supportMapper.toDTO(supportRepository.findAll(pageable));
    }

    public SupportDTO getSupport(Integer id) {
        Support support = supportRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Support not found"));
        return supportMapper.toDTO(support);
    }

    @Transactional
    public void deleteSupports(List<Integer> supportIds) {
        if (supportIds == null || supportIds.isEmpty()) return;

        // 0) 파일 경로 미리 수집 (DB 삭제 이전에)


        // 0-1) 문서 파일 경로 (specDoc/operatingDoc)
        List<Support> supportsForDocs = supportRepository.findAllForDocPaths(supportIds);

        List<Path> fileTargets = new ArrayList<>();

        // 문서
        for (Support p : supportsForDocs) {
            Path path = toLocalDocPath(p.getAttachmentPath());
            if (path != null) fileTargets.add(path);
        }

        // 2) 부모(제품) 벌크 삭제
        supportRepository.deleteAllByIdInBatch(supportIds);

        // 3) 트랜잭션 커밋 이후 실제 파일 삭제 (DB 롤백 시 파일 보존)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                for (Path path : fileTargets) {
                    try { Files.deleteIfExists(path); } catch (Exception ignore) { /* 로그 원하면 기록 */ }
                }
            }
        });
    }

    private Path toLocalDocPath(String urlPath) {
        if (urlPath == null || urlPath.isBlank()) return null;
        String p = urlPath.trim().replace('\\', '/');
        if (!p.startsWith("/uploadSupportDocs/")) return null;
        String rel = p.substring("/uploadSupportDocs/".length());
        Path resolved = Paths.get(uploadSupportDir, rel).normalize();
        Path base = Paths.get(uploadSupportDir).toAbsolutePath().normalize();
        return resolved.toAbsolutePath().startsWith(base) ? resolved : null;
    }


    @Transactional
    public Integer create(SupportDTO dto) {


        Support support = Support.builder()
                .name(dto.getName())
                .companyName(dto.getCompanyName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .message(dto.getMessage())
                .privacyConsent(dto.isPrivacyConsent())
                .createDate(LocalDateTime.now())
                .build();

        // 문서 저장 (있을 때만)
        if (dto.getAttachment() != null && !dto.getAttachment().isEmpty()) {
            MultipartFile file = dto.getAttachment();
            String path = saveDoc(file);
            support.setAttachmentPath(path);

            // 파일 크기 및 원본 파일명 저장
            support.setAttachmentSize(file.getSize());
            support.setAttachmentOriginal(file.getOriginalFilename());
        }

        Support saved = supportRepository.save(support);
        return saved.getId(); // Integer
    }


    /* ------- 문서 저장 유틸 ------- */

    private void ensureDocsDir() {
        File dir = new File(uploadSupportDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create docs directory: " + uploadSupportDir);
        }
    }

    private String saveDoc(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        // (선택) MIME/확장자 검증
        // String ct = file.getContentType(); // application/pdf, ...
        // 허용 확장자: pdf/doc/docx 등 정책에 맞춰 검사 가능

        ensureDocsDir();
        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String ext = "";
        int pos = original.lastIndexOf('.');
        if (pos >= 0) ext = original.substring(pos);
        String saveName = UUID.randomUUID() + ext;

        Path savePath = Paths.get(uploadSupportDir).resolve(saveName);
        try {
            file.transferTo(savePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("문서 저장 실패: " + original, e);
        }
        // 브라우저에서 접근할 URL 경로 반환
        return "/uploadSupportDocs/" + saveName;
    }
}
