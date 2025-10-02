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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

}
