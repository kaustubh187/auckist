package com.pbop.services;

import com.pbop.models.Product;
import com.pbop.models.ProductImage;
import com.pbop.repositories.ProductImageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductImageService {
    private final ProductImageRepo imageRepository; // Assume a JPA repo for ProductImage

    @Autowired
    public ProductImageService(ProductImageRepo imageRepository) {
        this.imageRepository = imageRepository;
    }

    public Set<ProductImage> saveImagesForProduct(List<MultipartFile> files, Product product) {
        if (files == null || files.isEmpty()) {
            return Set.of();
        }

        Set<ProductImage> savedImages = new HashSet<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            try {
                ProductImage image = new ProductImage();
                image.setData(file.getBytes());
                image.setMimeType(file.getContentType());
                image.setProduct(product);

                savedImages.add(imageRepository.save(image));
            } catch (IOException e) {
                throw new RuntimeException("Failed to read image file data: " + file.getOriginalFilename(), e);
            }
        }
        return savedImages;
    }
}
