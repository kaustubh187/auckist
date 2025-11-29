package com.pbop.controllers;

import com.pbop.dtos.product.CreateProductDto;
import com.pbop.dtos.product.GetProductDto;
import com.pbop.enums.ProductCategory;
import com.pbop.mappers.ProductMapper;
import com.pbop.models.Product;
import com.pbop.models.User;
import com.pbop.services.JwtService;
import com.pbop.services.ProductServiceImpl;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JwtService jwtService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<GetProductDto> uploadProduct(@ModelAttribute @Valid CreateProductDto dto, @RequestParam("files") List<MultipartFile> files, @AuthenticationPrincipal UserDetails userDetails) {

        Product savedProduct = productService.saveProduct(dto,files,userDetails.getUsername());
        return new ResponseEntity<>(productMapper.toDto(savedProduct), HttpStatus.CREATED);
    }


    @GetMapping("/{productId}")
    public ResponseEntity<GetProductDto> getProductById(@PathVariable Long productId) {
        GetProductDto productDto = productService.getProductById(productId);
        return ResponseEntity.ok(productDto);
    }


    @GetMapping
    public ResponseEntity<Page<GetProductDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GetProductDto> products = productService.getAllProducts(page, size);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/category/{category}")
    public ResponseEntity<Page<GetProductDto>> getProductsByCategory(
            @PathVariable ProductCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GetProductDto> products = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(products);
    }


    @GetMapping("/search")
    public ResponseEntity<Page<GetProductDto>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<GetProductDto> results = productService.searchProductsByKeyword(keyword, page, size).getContent();

        return ResponseEntity.ok(new PageImpl<>(results));
    }
}

