package com.arpan.backend.controller;
import com.arpan.backend.dto.product.ProductRequest;
import com.arpan.backend.dto.product.ProductResponse;
import com.arpan.backend.dto.product.ProductSearchResponse;
import com.arpan.backend.entity.Products;
import com.arpan.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(Pageable pageable){
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/products/search")
    public ResponseEntity<Page<ProductSearchResponse>> searchProducts(
            @RequestParam String keyword,
            Pageable pageable) {

        return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
    }

    @GetMapping("/products/{prodId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long prodId){
        return ResponseEntity.ok(productService.getProductById(prodId));
    }

    @DeleteMapping("/products/{prodId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long prodId){
        productService.deleteProduct(prodId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> addProduct(
            @ModelAttribute ProductRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @GetMapping("/products/{id}/image")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable Long id){

        Products product = productService.getProductEntity(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(product.getImageType()))
                .body(product.getImageData());
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @ModelAttribute ProductRequest request) {

        ProductResponse response = productService.updateProduct(id, request);

        return ResponseEntity.ok(response);
    }
}
