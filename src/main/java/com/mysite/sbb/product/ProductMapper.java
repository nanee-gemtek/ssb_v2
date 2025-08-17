package com.mysite.sbb.product;

import com.mysite.sbb.user.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "author.username", target = "username")
    @Mapping(target = "imagePaths", expression = "java(mapImagePaths(product))") // ✅ 이 줄 추가
    @Mapping(target = "images", ignore = true) // ✅ 이 줄 추가: MultipartFile 무시
    @Mapping(target = "categoryName", source = "category.name")  // ✅ 카테고리명 매핑
    ProductDTO toDTO(Product product);

    // imagePath 추출용 default 메서드
    default List<String> mapImagePaths(Product product) {
        if (product.getImages() == null) return Collections.emptyList();
        return product.getImages().stream()
                .map(ProductImage::getImagePath)
                .collect(Collectors.toList());
    }

    @Mapping(source = "username", target = "author", qualifiedByName = "nameToSiteUser")
    Product toEntity(ProductDTO dto);

    List<ProductDTO> toDtoList(List<Product> list);

    default Page<ProductDTO> toDTO(Page<Product> products) {
        return products.map(this::toDTO);
    }
}
