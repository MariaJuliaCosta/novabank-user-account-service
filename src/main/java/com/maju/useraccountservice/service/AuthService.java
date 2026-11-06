package com.maju.useraccountservice.service;

import com.maju.useraccountservice.dto.ApiResponse;
import com.maju.useraccountservice.dto.AuthResponse;
import com.maju.useraccountservice.dto.LoginRequest;
import com.maju.useraccountservice.dto.RegistrationRequest;

public interface AuthService {

    ApiResponse<AuthResponse> registerUser(RegistrationRequest registrationRequest);

    ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest);
}
