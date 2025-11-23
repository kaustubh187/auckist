package com.pbop.services;

import com.pbop.dtos.product.CreateProductDto;
import com.pbop.dtos.product.GetProductDto;
import com.pbop.enums.ProductCategory;
import com.pbop.models.Product;
import com.pbop.models.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    Product saveProduct(CreateProductDto dto, List<MultipartFile> files, String user);
    GetProductDto getProductById(Long id);
    Page<GetProductDto> getProductsByCategory(ProductCategory category, int page, int size);
    Page<GetProductDto> searchProductsByKeyword(String keyword, int page, int size);
    Page<GetProductDto> getAllProducts(int page, int size);
}
