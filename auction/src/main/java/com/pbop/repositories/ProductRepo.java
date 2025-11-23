package com.pbop.repositories;

import com.pbop.enums.ProductCategory;
import com.pbop.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepo extends JpaRepository<Product,Long> {
    Page<Product> findByCategory(ProductCategory category, Pageable pageable);

    // Fetch All (with pagination)
    Page<Product> findAll(Pageable pageable);

    // For Keyword Search (This requires an actual Full-Text Search index,
    // or a simple LIKE query, see Service note below)
    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByDescriptionContainingIgnoreCase(String keyword);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.owner LEFT JOIN FETCH p.images",
            countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findAllWithDetails(Pageable pageable);
}
