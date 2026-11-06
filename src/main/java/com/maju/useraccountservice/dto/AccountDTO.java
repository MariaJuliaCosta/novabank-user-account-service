package com.maju.useraccountservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.maju.useraccountservice.enums.AccountStatus;
import com.maju.useraccountservice.enums.AccountType;
import com.maju.useraccountservice.enums.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDTO {

    private Long id;

    private String accountNumber;

    private BigDecimal balance;

    private Currency currency; //USD, EURO

    private AccountType accountType; //SAVINGS, CURRENT

    private AccountStatus accountStatus; //ACTIVE, CLOSED, FROZEN

    private String ownerEmail;

    private LocalDateTime createdAt;
}
