package com.mysite.sbb.product;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.category.Category;
import com.mysite.sbb.category.CategoryRepository;
import com.mysite.sbb.producer.Producer;
import com.mysite.sbb.producer.ProducerRepository;
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
public class ProductService {

    //file.upload-dir=/home/web/upload/product
    //file.docs-dir=/home/web/upload/docs
    @Value("${file.upload-dir}")
    private String uploadDir; // 이미지 저장 루트

    @Value("${file.catalog-dir}")
    private String catalogDir; // 카달로그 이미지 저장 루트

    @Value("${file.docs-dir}") // 문서 저장 루트
    private String docsDir;


    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProducerRepository producerRepository;

    // (옵션) orphanRemoval=false 라면 주입해서 사용하세요.
    private final ProductImageRepository productImageRepository; // 없다면 생성해 두세요.

    public Page<ProductDTO> getList(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        return productMapper.toDTO(productRepository.findAll(pageable));
    }

    public ProductDTO getProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        return productMapper.toDTO(product);
    }

    /** 수정 폼에서 기존 이미지 렌더용 */
    @Transactional(readOnly = true)
    public List<ProductImage> findImagesByProductId(Integer productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        // 정렬된 리스트 반환 (엔티티 그대로 사용)
        return p.getImages().stream()
                .sorted(Comparator.comparing(img -> Optional.ofNullable(img.getSortOrder()).orElse(0)))
                .collect(Collectors.toList());
    }

    /** 수정 폼에서 기존 카달로그 이미지 렌더용 */
    @Transactional(readOnly = true)
    public List<CatalogImage> findCatalogImagesByProductId(Integer productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        // 정렬된 리스트 반환 (엔티티 그대로 사용)
        return p.getCatalogImages().stream()
                .sorted(Comparator.comparing(img -> Optional.ofNullable(img.getSortOrder()).orElse(0)))
                .collect(Collectors.toList());
    }

    /** 등록: 생성 후 새로 생성된 id 반환 */
    @Transactional
    public Integer create(ProductDTO dto) {
        SiteUser author = userRepository.findByusername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Producer producer = producerRepository.findById(dto.getProducerId())
                .orElseThrow(() -> new IllegalArgumentException("Producer not found"));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Product product = Product.builder()
                .name(dto.getName())
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .applicationArea(dto.getApplicationArea())
                .advantages(dto.getAdvantages())
                .specifications(dto.getSpecifications())
                .approval(dto.getApproval())
                .displayControl(dto.getDisplayControl())
                .createDate(LocalDateTime.now())
                .author(author)
                .category(category)
                .producer(producer)
                .images(new ArrayList<>())
                .featured(dto.getFeatured())
                .build();

        // 새 이미지 저장
        appendImages(product, dto.getImages());

        // 새 카달로그 이미지 저장
        appendCatalogImages(product, dto.getCatalogImages());

        // 문서 저장 (있을 때만)
        if (dto.getSpecDoc() != null && !dto.getSpecDoc().isEmpty()) {
            String path = saveDoc(dto.getSpecDoc());
            product.setSpecDocPath(path); // 예: /uploadDocs/uuid.pdf
        }
        if (dto.getOperatingDoc() != null && !dto.getOperatingDoc().isEmpty()) {
            String path = saveDoc(dto.getOperatingDoc());
            product.setOperatingDocPath(path);
        }

        Product saved = productRepository.save(product);
        return saved.getId(); // Integer
    }

    /** 수정: 필드/연관관계 갱신 + 이미지 삭제/추가 + 정렬 */
    @Transactional
    public void update(ProductDTO dto, List<Long> deleteImageIds, List<Long> deleteCatalogImageIds,boolean deleteSpecDoc, boolean deleteOperatingDoc) {
        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        // 필드 갱신
        product.setName(dto.getName());
        product.setTitle(dto.getTitle());
        product.setSubtitle(dto.getSubtitle());
        product.setApplicationArea(dto.getApplicationArea());
        product.setAdvantages(dto.getAdvantages());
        product.setSpecifications(dto.getSpecifications());
        product.setApproval(dto.getApproval());
        product.setDisplayControl(dto.getDisplayControl());
        product.setConfigLink(dto.getConfigLink());
        product.setFeatured(dto.getFeatured());

        // 연관 갱신
        if (dto.getProducerId() != null) {
            Producer producer = producerRepository.findById(dto.getProducerId())
                    .orElseThrow(() -> new IllegalArgumentException("Producer not found"));
            product.setProducer(producer);
        }
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }

        // 이미지 삭제
        if (deleteCatalogImageIds != null && !deleteCatalogImageIds.isEmpty()) {
            // 1) 물리 파일 삭제 시도
            for (Iterator<CatalogImage> it = product.getCatalogImages().iterator(); it.hasNext(); ) {
                CatalogImage img = it.next();
                if (deleteCatalogImageIds.contains(img.getId())) {
                    deletePhysicalFileQuietly(img.getImagePath());
                    it.remove(); // orphanRemoval=true면 이걸로 DB 삭제됨
                }
            }
        }

        // 새 이미지 추가
        appendImages(product, dto.getImages());


        // 카달로그 이미지 삭제
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            // 1) 물리 파일 삭제 시도
            for (Iterator<ProductImage> it = product.getImages().iterator(); it.hasNext(); ) {
                ProductImage img = it.next();
                if (deleteImageIds.contains(img.getId())) {
                    deletePhysicalFileQuietly(img.getImagePath());
                    it.remove(); // orphanRemoval=true면 이걸로 DB 삭제됨
                }
            }
        }

        // 카달로그  새 이미지 추가
        appendCatalogImages(product, dto.getCatalogImages());


        // sortOrder 정렬(0..n-1)
        resequenceSortOrders(product);

        // ... 텍스트/연관 갱신, 이미지 삭제/추가 ...
        // 문서 삭제 플래그 처리
        if (deleteSpecDoc && product.getSpecDocPath() != null) {
            deletePhysicalDoc(product.getSpecDocPath());
            product.setSpecDocPath(null);
        }
        if (deleteOperatingDoc && product.getOperatingDocPath() != null) {
            deletePhysicalDoc(product.getOperatingDocPath());
            product.setOperatingDocPath(null);
        }

        // 새 문서 업로드가 있으면 교체
        if (dto.getSpecDoc() != null && !dto.getSpecDoc().isEmpty()) {
            deletePhysicalDoc(product.getSpecDocPath());
            product.setSpecDocPath(saveDoc(dto.getSpecDoc()));
        }
        if (dto.getOperatingDoc() != null && !dto.getOperatingDoc().isEmpty()) {
            deletePhysicalDoc(product.getOperatingDocPath());
            product.setOperatingDocPath(saveDoc(dto.getOperatingDoc()));
        }




        // 변경 저장
        productRepository.save(product);
    }

    /* ---------- 내부 유틸 ---------- */

    /** 이미지 리스트를 product에 이어붙임 (파일 저장 포함) */
    private void appendImages(Product product, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        ensureUploadDir();

        // 기존 마지막 인덱스 다음부터
        int startIndex = product.getImages().stream()
                .map(img -> Optional.ofNullable(img.getSortOrder()).orElse(0))
                .max(Integer::compareTo).orElse(-1) + 1;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String savedRelPath = saveFile(file); // "/uploadImages/xxxx.png"
            ProductImage image = ProductImage.builder()
                    .imagePath(savedRelPath)
                    .product(product)
                    .sortOrder(startIndex++)
                    .build();
            product.getImages().add(image);
        }
    }

    /** 업로드 디렉토리 보장 */
    private void ensureUploadDir() {
        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create upload directory: " + uploadDir);
        }
    }

    /** 물리 파일 저장 후, 웹 경로 반환 (예: "/uploadImages/uuid.png") */
    private String saveFile(MultipartFile file) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String ext = "";
        int pos = originalFilename.lastIndexOf('.');
        if (pos >= 0) ext = originalFilename.substring(pos);
        String saveName = UUID.randomUUID().toString() + ext;

        Path savePath = Paths.get(uploadDir).resolve(saveName);
        try {
            file.transferTo(savePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + originalFilename, e);
        }
        // 웹에서 접근할 상대 경로 규칙에 맞춰 반환
        return "/uploadImages/" + saveName;
    }

    private String saveCatalogFile(MultipartFile file) {
        String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String ext = "";
        int pos = originalFilename.lastIndexOf('.');
        if (pos >= 0) ext = originalFilename.substring(pos);
        String saveName = UUID.randomUUID().toString() + ext;

        Path savePath = Paths.get(catalogDir).resolve(saveName);
        try {
            file.transferTo(savePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + originalFilename, e);
        }
        // 웹에서 접근할 상대 경로 규칙에 맞춰 반환
        return "/catalogImages/" + saveName;
    }


    /** 물리 파일 삭제(있으면) */
    private void deletePhysicalFileQuietly(String imagePath) {
        try {
            // imagePath가 "/uploadImages/파일명" 형태라면, 파일명만 추출해서 uploadDir와 매핑
            if (imagePath == null) return;
            String fileName = imagePath;
            int idx = imagePath.lastIndexOf('/');
            if (idx >= 0) fileName = imagePath.substring(idx + 1);
            File f = Paths.get(uploadDir).resolve(fileName).toFile();
            if (f.exists()) f.delete();
        } catch (Exception ignore) {}
    }

    /** sortOrder를 0..n-1로 재배치 */
    private void resequenceSortOrders(Product product) {
        List<ProductImage> imgs = product.getImages();
        imgs.sort(Comparator.comparing(img -> Optional.ofNullable(img.getSortOrder()).orElse(0)));
        for (int i = 0; i < imgs.size(); i++) {
            imgs.get(i).setSortOrder(i);
        }
    }

    /* ------- 문서 저장 유틸 ------- */

    private void ensureDocsDir() {
        File dir = new File(docsDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create docs directory: " + docsDir);
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

        Path savePath = Paths.get(docsDir).resolve(saveName);
        try {
            file.transferTo(savePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("문서 저장 실패: " + original, e);
        }
        // 브라우저에서 접근할 URL 경로 반환
        return "/uploadDocs/" + saveName;
    }

    private void deletePhysicalDoc(String urlPath) {
        if (urlPath == null) return;
        String fileName = urlPath.substring(urlPath.lastIndexOf('/') + 1);
        File f = Paths.get(docsDir).resolve(fileName).toFile();
        if (f.exists()) { try { f.delete(); } catch (Exception ignore) {} }
    }

    @Transactional
    public void deleteProducts(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        // 0) 파일 경로 미리 수집 (DB 삭제 이전에)
        // 0-1) 이미지 파일 경로
        List<String> imageUrlPaths = productImageRepository.findPathsByProductIdIn(productIds);

        // 0-2) 문서 파일 경로 (specDoc/operatingDoc)
        List<Product> productsForDocs = productRepository.findAllForDocPaths(productIds);

        List<Path> fileTargets = new ArrayList<>();
        // 이미지
        for (String urlPath : imageUrlPaths) {
            Path p = toLocalImagePath(urlPath);
            if (p != null) fileTargets.add(p);
        }
        // 문서
        for (Product p : productsForDocs) {
            Path spec = toLocalDocPath(p.getSpecDocPath());
            if (spec != null) fileTargets.add(spec);
            Path oper = toLocalDocPath(p.getOperatingDocPath());
            if (oper != null) fileTargets.add(oper);
        }

        // 1) 자식(이미지) 먼저 벌크 삭제
        productImageRepository.deleteByProductIdIn(productIds);

        // 2) 부모(제품) 벌크 삭제
        productRepository.deleteAllByIdInBatch(productIds);

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
        if (!p.startsWith("/uploadImages/")) return null;
        String rel = p.substring("/uploadImages/".length());
        Path resolved = Paths.get(uploadDir, rel).normalize();
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        return resolved.toAbsolutePath().startsWith(base) ? resolved : null;
    }

    private Path toLocalDocPath(String urlPath) {
        if (urlPath == null || urlPath.isBlank()) return null;
        String p = urlPath.trim().replace('\\', '/');
        if (!p.startsWith("/uploadDocs/")) return null;
        String rel = p.substring("/uploadDocs/".length());
        Path resolved = Paths.get(docsDir, rel).normalize();
        Path base = Paths.get(docsDir).toAbsolutePath().normalize();
        return resolved.toAbsolutePath().startsWith(base) ? resolved : null;
    }

    public List<Product> getFeatured() {
        var ids = productRepository.findFeaturedIds();
        if (ids.isEmpty()) return List.of();

        var products = productRepository.findByIdInWithAll(ids);

        // ids 순서대로 정렬 보존
        Map<Integer, Integer> order = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) order.put(ids.get(i), i);
        products.sort(Comparator.comparingInt(p -> order.getOrDefault(p.getId(), Integer.MAX_VALUE)));

        return products;
    }


    /** 이미지 리스트를 product에 이어붙임 (파일 저장 포함) */
    private void appendCatalogImages(Product product, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        ensureUploadDir();

        // 기존 마지막 인덱스 다음부터
        int startIndex = product.getImages().stream()
                .map(img -> Optional.ofNullable(img.getSortOrder()).orElse(0))
                .max(Integer::compareTo).orElse(-1) + 1;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            String savedRelPath = saveCatalogFile(file); // "/uploadImages/xxxx.png"
            CatalogImage image = CatalogImage.builder()
                    .imagePath(savedRelPath)
                    .product(product)
                    .sortOrder(startIndex++)
                    .build();
            product.getCatalogImages().add(image);
        }
    }

}
