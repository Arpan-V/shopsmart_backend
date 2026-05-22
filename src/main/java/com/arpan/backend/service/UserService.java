package com.arpan.backend.service;

import com.arpan.backend.dto.UserRequest;
import com.arpan.backend.dto.UserResponse;

public interface UserService {
    UserResponse getCurrentUser();


    void addAddress(UserRequest request);
}
