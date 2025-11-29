package com.pbop.services;

import com.pbop.dtos.product.CreateProductDto;
import com.pbop.dtos.product.GetProductDto;
import com.pbop.enums.ProductCategory;
import com.pbop.mappers.ProductMapper;
import com.pbop.models.Product;
import com.pbop.models.User;
import com.pbop.repositories.ProductRepo;
import com.pbop.repositories.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductImageService productImageService;

    @InjectMocks
    private ProductServiceImpl productService;

    // ---------- saveProduct tests ----------

    @Test
    void saveProduct_success() {
        String email = "owner@example.com";
        CreateProductDto dto = new CreateProductDto(
                "Test Product",
                ProductCategory.Jewel,
                "Nice product",
                null
        );

        User owner = new User();
        owner.setUserId(1L);
        owner.setUsername("owner");

        Product mappedProduct = new Product();
        mappedProduct.setName("Test Product");

        Product savedProduct = new Product();
        savedProduct.setName("Test Product");
        savedProduct.setOwner(owner);

        when(userRepo.findByEmail(email)).thenReturn(owner);
        when(productMapper.toEntity(dto)).thenReturn(mappedProduct);
        // two saves are called in service; keep it simple and return savedProduct both times
        when(productRepo.save(any(Product.class))).thenReturn(savedProduct);

        MultipartFile file = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file);

        Product result = productService.saveProduct(dto, files, email);

        // owner set correctly before save
        assertEquals(owner, mappedProduct.getOwner());
        // final returned product is the one from second save
        assertEquals(savedProduct, result);

        // verify interactions
        verify(userRepo).findByEmail(email);
        verify(productMapper).toEntity(dto);
        verify(productRepo, times(2)).save(mappedProduct);
        verify(productImageService).saveImagesForProduct(files, savedProduct);
    }

    @Test
    void saveProduct_secondSaveThrows_wrapsException() {
        String email = "owner@example.com";
        CreateProductDto dto = new CreateProductDto(
                "Test Product",
                ProductCategory.Jewel,
                "Nice product",
                null
        );

        User owner = new User();
        owner.setUserId(1L);

        Product mappedProduct = new Product();
        mappedProduct.setName("Test Product");

        Product firstSaved = new Product();
        firstSaved.setName("Test Product");

        when(userRepo.findByEmail(email)).thenReturn(owner);
        when(productMapper.toEntity(dto)).thenReturn(mappedProduct);
        // first save ok, second save throws
        when(productRepo.save(mappedProduct))
                .thenReturn(firstSaved)
                .thenThrow(new RuntimeException("DB error"));

        MultipartFile file = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.saveProduct(dto, files, email));

        assertTrue(ex.getMessage().contains("java.lang.RuntimeException"));

        verify(productImageService).saveImagesForProduct(files, firstSaved);
    }

    // ---------- getProductById tests ----------

    @Test
    void getProductById_success() {
        Long productId = 1L;

        Product product = new Product();
        product.setProductId(productId);
        product.setName("Prod1");
        product.setCategory(ProductCategory.Jewel);
        product.setDescription("desc");

        GetProductDto dto = new GetProductDto(
                productId,
                "Prod1",
                ProductCategory.Jewel,
                "desc",
                Set.of(),
                10L,
                "ownerUser"
        );

        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(dto);

        GetProductDto result = productService.getProductById(productId);

        assertSame(dto, result);
        verify(productRepo).findById(productId);
        verify(productMapper).toDto(product);
    }

    @Test
    void getProductById_notFound_throwsRuntimeException() {
        Long productId = 99L;
        when(productRepo.findById(productId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> productService.getProductById(productId));

        verify(productMapper, never()).toDto(any());
    }

    // ---------- getProductsByCategory tests ----------

    @Test
    void getProductsByCategory_success() {
        ProductCategory category = ProductCategory.Jewel;
        int page = 0, size = 10;
        Pageable pageable = PageRequest.of(page, size);

        Product p1 = new Product();
        p1.setProductId(1L);
        p1.setName("P1");
        p1.setCategory(category);

        Product p2 = new Product();
        p2.setProductId(2L);
        p2.setName("P2");
        p2.setCategory(category);

        Page<Product> entityPage = new PageImpl<>(List.of(p1, p2), pageable, 2);

        GetProductDto dto1 = new GetProductDto(
                1L, "P1", category, "d1", Set.of(), 10L, "u1");
        GetProductDto dto2 = new GetProductDto(
                2L, "P2", category, "d2", Set.of(), 11L, "u2");

        Page<GetProductDto> dtoPage = new PageImpl<>(List.of(dto1, dto2), pageable, 2);

        when(productRepo.findByCategory(category, pageable)).thenReturn(entityPage);
        when(productMapper.toDtoPage(entityPage)).thenReturn(dtoPage);

        Page<GetProductDto> result = productService.getProductsByCategory(category, page, size);

        assertEquals(2, result.getTotalElements());
        assertSame(dtoPage, result);
        verify(productRepo).findByCategory(category, pageable);
        verify(productMapper).toDtoPage(entityPage);
    }

    // ---------- getAllProducts tests ----------

    @Test
    void getAllProducts_success() {
        int page = 0, size = 5;
        Pageable pageable = PageRequest.of(page, size);

        Product p1 = new Product();
        Product p2 = new Product();
        Page<Product> entityPage = new PageImpl<>(List.of(p1, p2), pageable, 2);

        GetProductDto dto1 = new GetProductDto(
                1L, "P1", ProductCategory.Collectible, "d1", Set.of(), 10L, "u1");
        GetProductDto dto2 = new GetProductDto(
                2L, "P2", ProductCategory.Vehicle, "d2", Set.of(), 11L, "u2");

        Page<GetProductDto> dtoPage = new PageImpl<>(List.of(dto1, dto2), pageable, 2);

        when(productRepo.findAllWithDetails(pageable)).thenReturn(entityPage);
        when(productMapper.toDtoPage(entityPage)).thenReturn(dtoPage);

        Page<GetProductDto> result = productService.getAllProducts(page, size);

        assertEquals(2, result.getTotalElements());
        assertSame(dtoPage, result);
        verify(productRepo).findAllWithDetails(pageable);
        verify(productMapper).toDtoPage(entityPage);
    }

    // ---------- searchProductsByKeyword tests ----------

    @Test
    void searchProductsByKeyword_combinesNameAndDescriptionMatches() {
        String keyword = "ring";
        int page = 0, size = 10; // not used inside, but method requires them

        Product p1 = new Product();
        p1.setProductId(1L);
        p1.setName("Ring 1");

        Product p2 = new Product();
        p2.setProductId(2L);
        p2.setName("Golden Ring");

        // p1 appears in both name and description results (dedup via Set)
        List<Product> nameMatches = List.of(p1, p2);
        List<Product> descMatches = List.of(p1);

        GetProductDto dto1 = new GetProductDto(
                1L, "Ring 1", ProductCategory.Jewel, "d1", Set.of(), 10L, "u1");
        GetProductDto dto2 = new GetProductDto(
                2L, "Golden Ring", ProductCategory.Jewel, "d2", Set.of(), 11L, "u2");

        when(productRepo.findByNameContainingIgnoreCase(keyword)).thenReturn(nameMatches);
        when(productRepo.findByDescriptionContainingIgnoreCase(keyword)).thenReturn(descMatches);
        when(productMapper.toDto(p1)).thenReturn(dto1);
        when(productMapper.toDto(p2)).thenReturn(dto2);

        Page<GetProductDto> result = productService.searchProductsByKeyword(keyword, page, size);

        // combinedResults should be 2 unique products -> 2 DTOs
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().contains(dto1));
        assertTrue(result.getContent().contains(dto2));

        verify(productRepo).findByNameContainingIgnoreCase(keyword);
        verify(productRepo).findByDescriptionContainingIgnoreCase(keyword);
        verify(productMapper).toDto(p1);
        verify(productMapper).toDto(p2);
    }
}
