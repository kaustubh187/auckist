package com.pbop.dtos.product;

import com.pbop.enums.ProductCategory;

import java.util.Set;


public record GetProductDto(
        Long productId,
        String name,
        ProductCategory category,
        String description,
        Set<Long> imageIds,
        Long ownerId,
        String ownerUsername

) {
}