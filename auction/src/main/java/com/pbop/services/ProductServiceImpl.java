package com.pbop.services;

import com.pbop.config.GlobalExceptionHandler;
import com.pbop.dtos.product.CreateProductDto;
import com.pbop.dtos.product.GetProductDto;
import com.pbop.enums.ProductCategory;
import com.pbop.mappers.ProductMapper;
import com.pbop.models.Product;
import com.pbop.repositories.ProductRepo;
import com.pbop.repositories.UserRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepo repository;
    private final UserRepo userRepository; // For fetching owner
    private final ProductMapper mapper;
    private final ProductImageService imageService;

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    // Example Exception (You should define this custom exception)
    // For simplicity here, assume this maps to 404 Not Found
    // e.g., public class ProductNotFoundException extends RuntimeException {}

    @Autowired
    public ProductServiceImpl(ProductRepo repository, UserRepo userRepository, ProductMapper mapper, ProductImageService imageService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.imageService = imageService;
    }

    @Override
    public Product saveProduct(CreateProductDto dto, List<MultipartFile> files, String email) {

        Product product = mapper.toEntity(dto);
        product.setOwner(userRepository.findByEmail(email));

        Product savedProduct = repository.save(product);
        imageService.saveImagesForProduct(files, savedProduct);

        try {
            product = repository.save(product);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return product;
    }

    @Override
    @Transactional
    public GetProductDto getProductById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found")); // Replace with ProductNotFoundException (404)
        log.debug("Fetched Product: {}", product.getName());
        return mapper.toDto(product);
    }

    @Override
    @Transactional
    public Page<GetProductDto> getProductsByCategory(ProductCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> entityPage = repository.findByCategory(category, pageable);
        log.debug("Fetched Products in Category {}: {}", category, entityPage.getContent().stream().map(Product::getName).collect(Collectors.toList()));
        return mapper.toDtoPage(entityPage);
    }

    @Override
    @Transactional
    public Page<GetProductDto> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> entityPage = repository.findAllWithDetails(pageable);
        log.debug("Fetched Products: {}", entityPage.getContent().stream().map(Product::getName).collect(Collectors.toList()));
        return mapper.toDtoPage(entityPage);
    }

    @Override
    @Transactional
    public Page<GetProductDto> searchProductsByKeyword(String keyword, int page, int size) {

        List<Product> nameMatches = repository.findByNameContainingIgnoreCase(keyword);
        List<Product> descMatches = repository.findByDescriptionContainingIgnoreCase(keyword);

        log.debug("Name Matches: {}" , nameMatches.size());
        log.debug("Description Matches: {}", descMatches.size());
        Set<Product> combinedResults = new HashSet<>(nameMatches);
        combinedResults.addAll(descMatches);

        log.debug("Combined Matches: {}", combinedResults.size());
        log.debug("Combined Matches Details: {}", combinedResults.stream().map(Product::getName).collect(Collectors.toList()));

        return new PageImpl<>(
                combinedResults.stream()
                        .map(mapper::toDto)
                        .collect(Collectors.toList())
        );
    }
}