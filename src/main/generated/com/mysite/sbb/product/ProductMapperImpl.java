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
        productDTO.categoryId( productCategoryId( product ) );
        productDTO.categoryParentName( productCategoryParentName( product ) );
        Long id1 = productCategoryParentId( product );
        if ( id1 != null ) {
            productDTO.categoryParentId( String.valueOf( id1 ) );
        }
        productDTO.producerName( productProducerName( product ) );
        productDTO.producerId( productProducerId( product ) );
        productDTO.configLink( product.getConfigLink() );
        productDTO.id( product.getId() );
        productDTO.name( product.getName() );
        productDTO.title( product.getTitle() );
        productDTO.subtitle( product.getSubtitle() );
        productDTO.applicationArea( product.getApplicationArea() );
        productDTO.advantages( product.getAdvantages() );
        productDTO.specifications( product.getSpecifications() );
        productDTO.approval( product.getApproval() );
        productDTO.displayControl( product.getDisplayControl() );
        productDTO.createDate( product.getCreateDate() );
        productDTO.featured( product.isFeatured() );
        productDTO.specDocPath( product.getSpecDocPath() );
        productDTO.operatingDocPath( product.getOperatingDocPath() );

        productDTO.imagePaths( mapImagePaths(product) );
        productDTO.catalogImagePaths( mapCatalogImagePaths(product) );

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
        product.approval( dto.getApproval() );
        product.displayControl( dto.getDisplayControl() );
        product.createDate( dto.getCreateDate() );
        product.images( multipartFileListToProductImageList( dto.getImages() ) );
        product.catalogImages( multipartFileListToCatalogImageList( dto.getCatalogImages() ) );
        product.specDocPath( dto.getSpecDocPath() );
        product.operatingDocPath( dto.getOperatingDocPath() );
        if ( dto.getFeatured() != null ) {
            product.featured( dto.getFeatured() );
        }
        product.configLink( dto.getConfigLink() );

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

    private Long productCategoryId(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        Long id = category.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String productCategoryParentName(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        Category parent = category.getParent();
        if ( parent == null ) {
            return null;
        }
        String name = parent.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private Long productCategoryParentId(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        Category parent = category.getParent();
        if ( parent == null ) {
            return null;
        }
        Long id = parent.getId();
        if ( id == null ) {
            return null;
        }
        return id;
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

    private Long productProducerId(Product product) {
        if ( product == null ) {
            return null;
        }
        Producer producer = product.getProducer();
        if ( producer == null ) {
            return null;
        }
        Long id = producer.getId();
        if ( id == null ) {
            return null;
        }
        return id;
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

    protected CatalogImage multipartFileToCatalogImage(MultipartFile multipartFile) {
        if ( multipartFile == null ) {
            return null;
        }

        CatalogImage.CatalogImageBuilder catalogImage = CatalogImage.builder();

        return catalogImage.build();
    }

    protected List<CatalogImage> multipartFileListToCatalogImageList(List<MultipartFile> list) {
        if ( list == null ) {
            return null;
        }

        List<CatalogImage> list1 = new ArrayList<CatalogImage>( list.size() );
        for ( MultipartFile multipartFile : list ) {
            list1.add( multipartFileToCatalogImage( multipartFile ) );
        }

        return list1;
    }
}
