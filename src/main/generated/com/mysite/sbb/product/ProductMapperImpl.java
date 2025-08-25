package com.mysite.sbb.product;

import com.mysite.sbb.category.Category;
import com.mysite.sbb.producer.Producer;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserMapper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-25T17:08:59+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 11.0.19 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ProductDTO toDTO(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductDTO.ProductDTOBuilder productDTO = ProductDTO.builder();

        productDTO.username( productAuthorUsername( product ) );
        productDTO.categoryName( productCategoryName( product ) );
        productDTO.producerName( productProducerName( product ) );
        productDTO.id( product.getId() );
        productDTO.name( product.getName() );
        productDTO.title( product.getTitle() );
        productDTO.subtitle( product.getSubtitle() );
        productDTO.applicationArea( product.getApplicationArea() );
        productDTO.advantages( product.getAdvantages() );
        productDTO.specifications( product.getSpecifications() );
        productDTO.certification( product.getCertification() );
        productDTO.displayControl( product.getDisplayControl() );
        productDTO.createDate( product.getCreateDate() );
        productDTO.specDocPath( product.getSpecDocPath() );
        productDTO.operatingDocPath( product.getOperatingDocPath() );

        productDTO.imagePaths( mapImagePaths(product) );

        return productDTO.build();
    }

    @Override
    public Product toEntity(ProductDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.author( userMapper.nameToSiteUser( dto.getUsername() ) );
        product.id( dto.getId() );
        product.name( dto.getName() );
        product.title( dto.getTitle() );
        product.subtitle( dto.getSubtitle() );
        product.applicationArea( dto.getApplicationArea() );
        product.advantages( dto.getAdvantages() );
        product.specifications( dto.getSpecifications() );
        product.certification( dto.getCertification() );
        product.displayControl( dto.getDisplayControl() );
        product.createDate( dto.getCreateDate() );
        product.images( multipartFileListToProductImageList( dto.getImages() ) );
        product.specDocPath( dto.getSpecDocPath() );
        product.operatingDocPath( dto.getOperatingDocPath() );

        return product.build();
    }

    @Override
    public List<ProductDTO> toDtoList(List<Product> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductDTO> list1 = new ArrayList<ProductDTO>( list.size() );
        for ( Product product : list ) {
            list1.add( toDTO( product ) );
        }

        return list1;
    }

    private String productAuthorUsername(Product product) {
        if ( product == null ) {
            return null;
        }
        SiteUser author = product.getAuthor();
        if ( author == null ) {
            return null;
        }
        String username = author.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    private String productCategoryName(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String productProducerName(Product product) {
        if ( product == null ) {
            return null;
        }
        Producer producer = product.getProducer();
        if ( producer == null ) {
            return null;
        }
        String name = producer.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    protected ProductImage multipartFileToProductImage(MultipartFile multipartFile) {
        if ( multipartFile == null ) {
            return null;
        }

        ProductImage.ProductImageBuilder productImage = ProductImage.builder();

        return productImage.build();
    }

    protected List<ProductImage> multipartFileListToProductImageList(List<MultipartFile> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductImage> list1 = new ArrayList<ProductImage>( list.size() );
        for ( MultipartFile multipartFile : list ) {
            list1.add( multipartFileToProductImage( multipartFile ) );
        }

        return list1;
    }
}
