package com.maju.useraccountservice.service.impl;

import com.maju.useraccountservice.dto.AccountDTO;
import com.maju.useraccountservice.dto.ApiResponse;
import com.maju.useraccountservice.entity.Account;
import com.maju.useraccountservice.entity.User;
import com.maju.useraccountservice.enums.AccountStatus;
import com.maju.useraccountservice.exceptions.NotFoundException;
import com.maju.useraccountservice.repository.AccountRepository;
import com.maju.useraccountservice.repository.UserRepository;
import com.maju.useraccountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    @Override
    public ApiResponse<AccountDTO> getMyAccount() {
        log.info("Fetching account for logged in user");

        String userEmail = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        AccountDTO accountDTO = modelMapper.map(account, AccountDTO.class);

        accountDTO.setOwnerEmail(account.getUser().getEmail());

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account retrieved",
                accountDTO
        );
    }

    @Override
    public ApiResponse<AccountDTO> getAccountNumber(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        AccountDTO accountDTO = modelMapper.map(account, AccountDTO.class);

        accountDTO.setOwnerEmail(account.getUser().getEmail());

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account retrieved",
                accountDTO
        );
    }

    @Override
    public ApiResponse<AccountDTO> changeAccountStatus(String accountNumber, AccountStatus status) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        account.setAccountStatus(status);

        Account savedAccount = accountRepository.save(account);

        AccountDTO accountDTO = modelMapper.map(savedAccount, AccountDTO.class);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Account status updated successfully",
                accountDTO
        );
    }

    @Override
    public ApiResponse<Page<AccountDTO>> getAllAccounts(Pageable pageable) {

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Page<Account> accounts = accountRepository.findAll(sortedPageable);

        Page<AccountDTO> dtoPage = accounts.map(account -> modelMapper.map(account, AccountDTO.class));

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "Accounts retrieved",
                dtoPage
        );
    }
}
