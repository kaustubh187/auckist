package com.pbop.mappers;

import com.pbop.dtos.product.CreateProductDto;
import com.pbop.dtos.product.GetProductDto;
import com.pbop.models.Product;
import com.pbop.models.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper{
    @Mapping(target = "productId", ignore = true) // Never map the ID on creation
    @Mapping(target = "images", ignore = true) // Images handled separately or ignored for creation
    @Mapping(target = "owner", ignore = true) // Owner mapped in Service
    Product toEntity(CreateProductDto dto);


    @Mapping(target = "imageIds", expression = "java(mapImageIds(product.getImages()))")
    @Mapping(target = "ownerId", source = "owner.userId")
    @Mapping(target = "ownerUsername", source = "owner.username")
    GetProductDto toDto(Product product);

    // Helper to convert Set<ProductImage> to Set<Long> (imageIds)
    default Set<Long> mapImageIds(Set<ProductImage> images) {
        if (images == null) return Set.of();
        System.out.println("Mapper: Found " + images.size() + " images.");
        return images.stream()
                .map(ProductImage::getImageId)
                .collect(Collectors.toSet());
    }

    default Page<GetProductDto> toDtoPage(Page<Product> products) {
        if (products == null) {
            return Page.empty();
        }


        List<GetProductDto> dtoList = new ArrayList<>();

        for (Product product : products.getContent()) {
            try {
                // Attempt to map the single entity
                GetProductDto dto = this.toDto(product);
                dtoList.add(dto);

            } catch (Exception e) { // Catch all exceptions during mapping
                // CRITICAL STEP: Log the ID of the entity that failed
                System.out.println("Massive fatal mapping error: " + e);

                // Re-throw the original exception to stop the process and see the full stack trace
                throw new RuntimeException("Mapping failed for Product ID: " + product.getProductId(), e);
            }
        }


        Pageable pageable = products.getPageable();
        long total = products.getTotalElements();


        return new PageImpl<>(dtoList, pageable, total);
    }
}
