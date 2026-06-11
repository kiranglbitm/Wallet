package com.wallet.controller;

import com.wallet.dto.WalletDto.*;
import com.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    // Create a new wallet
    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(
            @Valid @RequestBody CreateWalletRequest request) {
        WalletResponse wallet = walletService.createWallet(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Wallet created successfully", wallet));
    }
    // Check wallet balance
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(
            @PathVariable Long walletId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Balance fetched", walletService.getBalance(walletId)));
    }
    // Add money to wallet
    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> addMoney(
            @PathVariable Long walletId,
            @Valid @RequestBody AddMoneyRequest request) {
        TransactionResponse txn = walletService.addMoney(walletId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Money added successfully", txn));
    }

    // Transfer money to another wallet
    @PostMapping("/{walletId}/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transferMoney(
            @PathVariable Long walletId,
            @Valid @RequestBody TransferRequest request) {
        TransferResponse result = walletService.transferMoney(walletId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transfer successful", result));
    }

    // Get transaction history
    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @PathVariable Long walletId) {
        return ResponseEntity.ok(
                ApiResponse.ok("Transactions fetched", walletService.getTransactionHistory(walletId)));
    }
}
