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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Value("${file.upload-dir}")
    private String uploadDir; // 예: /var/www/uploadImages

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
                .certification(dto.getCertification())
                .displayControl(dto.getDisplayControl())
                .createDate(LocalDateTime.now())
                .author(author)
                .category(category)
                .producer(producer)
                .images(new ArrayList<>())
                .build();

        // 새 이미지 저장
        appendImages(product, dto.getImages());

        Product saved = productRepository.save(product);
        return saved.getId(); // Integer
    }

    /** 수정: 필드/연관관계 갱신 + 이미지 삭제/추가 + 정렬 */
    @Transactional
    public void update(ProductDTO dto, List<Long> deleteImageIds) {
        Product product = productRepository.findById(dto.getId())
                .orElseThrow(() -> new DataNotFoundException("Product not found"));

        // 필드 갱신
        product.setName(dto.getName());
        product.setTitle(dto.getTitle());
        product.setSubtitle(dto.getSubtitle());
        product.setApplicationArea(dto.getApplicationArea());
        product.setAdvantages(dto.getAdvantages());
        product.setSpecifications(dto.getSpecifications());
        product.setCertification(dto.getCertification());
        product.setDisplayControl(dto.getDisplayControl());

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
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            // 1) 물리 파일 삭제 시도
            for (Iterator<ProductImage> it = product.getImages().iterator(); it.hasNext(); ) {
                ProductImage img = it.next();
                if (deleteImageIds.contains(img.getId())) {
                    deletePhysicalFileQuietly(img.getImagePath());
                    it.remove(); // orphanRemoval=true면 이걸로 DB 삭제됨
                }
            }
            // 2) orphanRemoval=false 환경 대비: 레포지토리로 직접 삭제
//            if (productImageRepository != null) {
//                productImageRepository.deleteAllByIdInBatch(deleteImageIds);
//            }
        }

        // 새 이미지 추가
        appendImages(product, dto.getImages());

        // sortOrder 정렬(0..n-1)
        resequenceSortOrders(product);

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
}
