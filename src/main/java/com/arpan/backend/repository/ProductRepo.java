package com.arpan.backend.repository;

import com.arpan.backend.dto.product.ProductSearchResponse;
import com.arpan.backend.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends JpaRepository<Products, Long> {

    @Query("""
SELECT new com.arpan.backend.dto.product.ProductSearchResponse(
    p.prodId,
    p.name,
    p.description,
    p.brand,
    p.category,
    p.price,
    p.stockQuantity,
    p.productAvailable,
    p.releaseDate
)
FROM Products p
WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
   OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))
   OR LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    Page<ProductSearchResponse> searchProducts(@Param("keyword") String keyword, Pageable pageable);

}
