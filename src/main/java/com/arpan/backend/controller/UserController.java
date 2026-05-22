package com.arpan.backend.controller;

import com.arpan.backend.dto.UserRequest;
import com.arpan.backend.dto.UserResponse;
import com.arpan.backend.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/users/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }


    @GetMapping("/api/users/me/addAddress")
    public  ResponseEntity<String> addAddress(UserRequest request){
        userService.addAddress(request);
        return ResponseEntity.ok( "Address added");
    }
}
