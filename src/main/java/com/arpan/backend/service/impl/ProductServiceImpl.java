package com.arpan.backend.service.impl;

import com.arpan.backend.dto.product.ProductRequest;
import com.arpan.backend.dto.product.ProductResponse;
import com.arpan.backend.dto.product.ProductSearchResponse;
import com.arpan.backend.entity.Products;
import com.arpan.backend.entity.Users;
import com.arpan.backend.exception.AuthException;
import com.arpan.backend.exception.ProductNotFoundException;
import com.arpan.backend.repository.ProductRepo;
import com.arpan.backend.repository.UserRepo;
import com.arpan.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final UserRepo userRepository;

    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        String username = auth.getName();


        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public Page<ProductSearchResponse> searchProducts(String keyword, Pageable pageable) {

        if (keyword == null || keyword.isBlank()) {

            return productRepo.findAll(pageable)
                    .map(product -> new ProductSearchResponse(
                            product.getProdId(),
                            product.getName(),
                            product.getDescription(),
                            product.getBrand(),
                            product.getCategory(),
                            product.getPrice(),
                            product.getStockQuantity(),
                            product.isProductAvailable(),
                            product.getReleaseDate()
                    ));
        }

        return productRepo.searchProducts(keyword, pageable);
    }

    // 🔐 Helper: ownership + admin check
    private void validateOwnershipOrAdmin(Products product, Users currentUser, boolean isAdmin) {
        if (!product.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new AuthException("You are not allowed to perform this action");
        }
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepo.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public ProductResponse getProductById(Long prodId) {
        Products product = productRepo.findById(prodId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        return mapToResponse(product);
    }

    @Override
    public void deleteProduct(Long prodId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = isAdmin(auth);

        Users currentUser = getCurrentUser();

        Products product = productRepo.findById(prodId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        validateOwnershipOrAdmin(product, currentUser, admin);

        productRepo.delete(product);
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {

        Users currentUser = getCurrentUser();

        Products product = mapToEntity(request);

        // 🔐 set owner
        product.setUser(currentUser);

        Products savedProduct = productRepo.save(product);

        return mapToResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        boolean admin = isAdmin(auth);

        Users currentUser = getCurrentUser();

        Products existingProduct = productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        validateOwnershipOrAdmin(existingProduct, currentUser, admin);

        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setCategory(request.getCategory());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setProductAvailable(request.isProductAvailable());
        existingProduct.setReleaseDate(request.getReleaseDate());

        MultipartFile file = request.getImage();

        if (file != null && !file.isEmpty()) {
            try {
                existingProduct.setImageName(file.getOriginalFilename());
                existingProduct.setImageType(file.getContentType());
                existingProduct.setImageData(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Image update failed");
            }
        }

        Products updatedProduct = productRepo.save(existingProduct);

        return mapToResponse(updatedProduct);
    }

    @Override
    public Products getProductEntity(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    private ProductResponse mapToResponse(Products product) {
        ProductResponse response = new ProductResponse();

        response.setProdId(product.getProdId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setBrand(product.getBrand());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setProductAvailable(product.isProductAvailable());
        response.setReleaseDate(product.getReleaseDate());
        response.setOwnerId(product.getUser().getId());

        response.setImageUrl("/api/products/" + product.getProdId() + "/image");

        return response;
    }

    private Products mapToEntity(ProductRequest request) {

        Products product = new Products();

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setProductAvailable(request.isProductAvailable());
        product.setReleaseDate(request.getReleaseDate());

        MultipartFile file = request.getImage();

        if (file != null && !file.isEmpty()) {
            try {
                product.setImageName(file.getOriginalFilename());
                product.setImageType(file.getContentType());
                product.setImageData(file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed");
            }
        }

        return product;
    }
}