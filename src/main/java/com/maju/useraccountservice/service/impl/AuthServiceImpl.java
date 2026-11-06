package com.maju.useraccountservice.service.impl;

import com.maju.useraccountservice.dto.*;
import com.maju.useraccountservice.entity.Account;
import com.maju.useraccountservice.entity.Role;
import com.maju.useraccountservice.entity.User;
import com.maju.useraccountservice.enums.AccountStatus;
import com.maju.useraccountservice.enums.AccountType;
import com.maju.useraccountservice.enums.Currency;
import com.maju.useraccountservice.exceptions.BadRequestException;
import com.maju.useraccountservice.exceptions.NotFoundException;
import com.maju.useraccountservice.kafka.dto.UserRegistrationEvent;
import com.maju.useraccountservice.kafka.service.AccountEventPublisher;
import com.maju.useraccountservice.repository.AccountRepository;
import com.maju.useraccountservice.repository.RoleRepository;
import com.maju.useraccountservice.repository.UserRepository;
import com.maju.useraccountservice.security.JwtService;
import com.maju.useraccountservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final AccountRepository accountRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final ModelMapper modelMapper;

    private final AccountEventPublisher accountEventPublisher;

    @Override
    public ApiResponse<AuthResponse> registerUser(RegistrationRequest registrationRequest) {
        log.info("We are inside the register user service method");
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("Account exists for this email");
        }

        Set<Role> roles = new HashSet<>();

        String roleName = (registrationRequest.getRole() != null && !registrationRequest.getRole().isBlank())
                ? registrationRequest.getRole().toUpperCase()
                : "CUSTOMER";

        Role databaseRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new BadRequestException("Role with name " + roleName + " not found"));

        roles.add(databaseRole);

        User userToSave = User.builder()
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .firsName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .enabled(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(userToSave);

        //Generate a unique account number for the user
        String accountNumber = generateUniqueAccountNumber();
        Account accountToSaveToDb = Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency(Currency.USD)
                .accountType(AccountType.SAVINGS)
                .accountStatus(AccountStatus.ACTIVE)
                .user(savedUser)
                .build();

        accountRepository.save(accountToSaveToDb);

        //Publish event out the notification service
        UserRegistrationEvent userRegistrationEvent = UserRegistrationEvent.builder()
                .email(registrationRequest.getEmail())
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .accountNumber(accountNumber)
                .bankName("NOVA BANK")
                .build();

        accountEventPublisher.publishedUserRegistrationEvent(userRegistrationEvent);

        //convert to dto
        UserDTO userDTO = modelMapper.map(savedUser, UserDTO.class);

        AuthResponse authResponse = AuthResponse.builder()
                .user(userDTO)
                .build();

        return  new ApiResponse<>(HttpStatus.CONTINUE.value(), "User account created successfully", authResponse);
    }

    @Override
    public ApiResponse<AuthResponse> loginUser(LoginRequest loginRequest) {
        log.info("Inside the login user service method");
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadRequestException("Password does not match");
        }

        if (!user.isEnabled()) {
            throw new BadRequestException("User is not enabled");
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        String token = jwtService.generateToken(user.getEmail(), roles);

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .user(userDTO)
                .build();

        return  new ApiResponse<>(
                HttpStatus.OK.value(),
                "Login successful",
                authResponse
        );
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        ThreadLocalRandom random = ThreadLocalRandom.current();

        do{
            //it will generate random 8 digits (between 0000000 and 99999999)
            int randomPart = random.nextInt(100_000_000);
            accountNumber = String.format("00%08d", randomPart);

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}
