package com.maju.useraccountservice.service;

import com.maju.useraccountservice.dto.ApiResponse;
import com.maju.useraccountservice.dto.UserDTO;
import com.maju.useraccountservice.dto.UserStatisticsDTO;
import com.maju.useraccountservice.dto.UserWithAccountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    ApiResponse<UserWithAccountDTO> getMyDetails();

    ApiResponse<UserWithAccountDTO> searchUser(String email, String accountNumber);

    ApiResponse<Page<UserDTO>> getAllUser(String roleName, Pageable pageable);

    ApiResponse<UserStatisticsDTO> getUserStatistics();

    ApiResponse<String> toggleUserStatus(Long userId);
}
