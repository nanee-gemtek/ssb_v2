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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProducerRepository producerRepository;


    public Page<ProductDTO> getList(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        return productMapper.toDTO(productRepository.findAll(pageable));
    }

    public ProductDTO getProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Product not found"));
        return productMapper.toDTO(product);
    }

    public void create(ProductDTO dto) {
        SiteUser author = userRepository.findByusername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 🔽 선택한 producerId Producer 엔티티 조회
        Producer producer = producerRepository.findById(dto.getProducerId())
                .orElseThrow(() -> new IllegalArgumentException("Producer not found"));

        // 🔽 선택한 categoryId로 Category 엔티티 조회
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
                .category(category) // ✅ 카테고리 설정
                .producer(producer)
                .images(new ArrayList<>())  // ✅ 반드시 추가!
                .build();

        // 이미지 저장 처리
        List<MultipartFile> imageFiles = dto.getImages();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            File uploadDirPath = new File(uploadDir);
            if (!uploadDirPath.exists()) {
                uploadDirPath.mkdirs(); // 업로드 디렉토리 없으면 생성
            }

            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    try {
                        String originalFilename = file.getOriginalFilename();
                        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                        String saveName = UUID.randomUUID().toString() + extension; // 영문 UUID + 확장자만 유지
                        Path savePath = Paths.get(uploadDir).resolve(saveName);
                        file.transferTo(savePath.toFile());

                        ProductImage image = ProductImage.builder()
                                .imagePath("/uploadImages/" + saveName) // 웹에서 접근할 수 있는 경로
                                .product(product)
                                .build();

                        product.getImages().add(image);

                    } catch (IOException e) {
                        throw new RuntimeException("이미지 저장 실패: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }

        productRepository.save(product);
    }



}
