package com.maju.useraccountservice.service;

import com.maju.useraccountservice.dto.AccountDTO;
import com.maju.useraccountservice.dto.ApiResponse;
import com.maju.useraccountservice.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {

    ApiResponse<AccountDTO> getMyAccount();

    ApiResponse<AccountDTO> getAccountNumber(String accountNumber);

    ApiResponse<AccountDTO> changeAccountStatus(String accountNumber, AccountStatus status);

    ApiResponse<Page<AccountDTO>> getAllAccounts(Pageable pageable);
}
