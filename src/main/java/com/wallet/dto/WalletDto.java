package com.wallet.dto;

import com.wallet.model.Transaction;
import com.wallet.model.Wallet;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class WalletDto {

    // Requests

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateWalletRequest {
        @NotBlank(message = "Owner name is required")
        @Size(min = 2, max = 100, message = "Owner name must be 2–100 characters")
        private String ownerName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @DecimalMin(value = "0.0", inclusive = true, message = "Initial deposit cannot be negative")
        private BigDecimal initialDeposit = BigDecimal.ZERO;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddMoneyRequest {
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 15, fraction = 4, message = "Invalid amount format")
        private BigDecimal amount;

        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferRequest {
        @NotNull(message = "Target wallet ID is required")
        private Long targetWalletId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 15, fraction = 4, message = "Invalid amount format")
        private BigDecimal amount;

        private String description;
    }

    // Responses

    @Data
    @Builder
    public static class WalletResponse {
        private Long id;
        private String ownerName;
        private String email;
        private BigDecimal balance;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static WalletResponse from(Wallet w) {
            return WalletResponse.builder()
                    .id(w.getId())
                    .ownerName(w.getOwnerName())
                    .email(w.getEmail())
                    .balance(w.getBalance())
                    .status(w.getStatus().name())
                    .createdAt(w.getCreatedAt())
                    .updatedAt(w.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class TransactionResponse {
        private Long id;
        private Long walletId;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private String type;
        private String status;
        private Long relatedWalletId;
        private String relatedWalletOwner;
        private String description;
        private LocalDateTime createdAt;

        public static TransactionResponse from(Transaction t) {
            return TransactionResponse.builder()
                    .id(t.getId())
                    .walletId(t.getWallet().getId())
                    .amount(t.getAmount())
                    .balanceAfter(t.getBalanceAfter())
                    .type(t.getType().name())
                    .status(t.getStatus().name())
                    .relatedWalletId(t.getRelatedWalletId())
                    .relatedWalletOwner(t.getRelatedWalletOwner())
                    .description(t.getDescription())
                    .createdAt(t.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class TransferResponse {
        private TransactionResponse debit;
        private TransactionResponse credit;
        private String message;
    }

    @Data
    @Builder
    public static class BalanceResponse {
        private Long walletId;
        private String ownerName;
        private BigDecimal balance;
        private String currency;
        private LocalDateTime asOf;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }

        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
}
