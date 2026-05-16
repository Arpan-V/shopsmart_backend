package com.arpan.backend.controller;
import com.arpan.backend.dto.UserResponse;
import com.arpan.backend.dto.product.ProductResponseAdmin;
import com.arpan.backend.service.AdminService;
import com.arpan.backend.service.ProductServiceAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AdminController {

    private final AdminService adminService;
    private final ProductServiceAdmin productServiceAdmin;


    @GetMapping("/admin/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @PutMapping("/admin/users/{id}/block")
    public ResponseEntity<String> blockUser(@PathVariable Long id) {
        adminService.blockUser(id);
        return ResponseEntity.ok("User blocked");
    }

    @PutMapping("/admin/users/{id}/unblock")
    public ResponseEntity<String> unblockUser(@PathVariable Long id) {
        adminService.unblockUser(id);
        return ResponseEntity.ok("User unblocked");
    }

    @GetMapping("/admin/products")
    public ResponseEntity<Page<ProductResponseAdmin>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(productServiceAdmin.getAllProducts(pageable));
    }
}
