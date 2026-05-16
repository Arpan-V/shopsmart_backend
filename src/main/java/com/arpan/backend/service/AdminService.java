package com.arpan.backend.service;

import com.arpan.backend.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AdminService {
    Page<UserResponse> getAllUsers(Pageable pageable);

    void blockUser(Long id);

    void unblockUser(Long id);
}
