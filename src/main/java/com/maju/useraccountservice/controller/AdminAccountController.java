package com.maju.useraccountservice.controller;

import com.maju.useraccountservice.dto.AccountDTO;
import com.maju.useraccountservice.dto.ApiResponse;
import com.maju.useraccountservice.enums.AccountStatus;
import com.maju.useraccountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminAccountController {

    private final AccountService accountService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<AccountDTO>>> listAllAccounts(
            @PageableDefault(page = 0, size = 100) Pageable pageable
    ) {
        return ResponseEntity.ok(accountService.getAllAccounts(pageable));
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<AccountDTO>> changeAccountStatus(
            @RequestParam String accountNumber,
            @RequestParam AccountStatus status
    ) {
        return ResponseEntity.ok(accountService.changeAccountStatus(accountNumber, status));
    }
}
