package com.pbop.mappers;

import com.pbop.dtos.product.CreateProductDto;
import com.pbop.dtos.product.GetProductDto;
import com.pbop.enums.ProductCategory;
import com.pbop.models.Product;
import com.pbop.models.User;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T23:52:57+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.5 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toEntity(CreateProductDto dto) {
        if ( dto == null ) {
            return null;
        }

        Product product = new Product();

        product.setName( dto.name() );
        product.setCategory( dto.category() );
        product.setDescription( dto.description() );

        return product;
    }

    @Override
    public GetProductDto toDto(Product product) {
        if ( product == null ) {
            return null;
        }

        Long ownerId = null;
        String ownerUsername = null;
        Long productId = null;
        String name = null;
        ProductCategory category = null;
        String description = null;

        ownerId = productOwnerUserId( product );
        ownerUsername = productOwnerUsername( product );
        productId = product.getProductId();
        name = product.getName();
        category = product.getCategory();
        description = product.getDescription();

        Set<Long> imageIds = mapImageIds(product.getImages());

        GetProductDto getProductDto = new GetProductDto( productId, name, category, description, imageIds, ownerId, ownerUsername );

        return getProductDto;
    }

    private Long productOwnerUserId(Product product) {
        if ( product == null ) {
            return null;
        }
        User owner = product.getOwner();
        if ( owner == null ) {
            return null;
        }
        Long userId = owner.getUserId();
        if ( userId == null ) {
            return null;
        }
        return userId;
    }

    private String productOwnerUsername(Product product) {
        if ( product == null ) {
            return null;
        }
        User owner = product.getOwner();
        if ( owner == null ) {
            return null;
        }
        String username = owner.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }
}
