package com.pbop.dtos.product;

import com.pbop.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record CreateProductDto(
        @NotBlank String name,
        @NotNull ProductCategory category,
        @NotBlank String description,
        Long ownerId
) {
}
