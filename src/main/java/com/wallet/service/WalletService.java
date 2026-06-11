package com.wallet.service;

import com.wallet.dto.WalletDto.*;

import java.util.List;

public interface WalletService {
    WalletResponse createWallet(CreateWalletRequest request);
    BalanceResponse getBalance(Long walletId);
    TransactionResponse addMoney(Long walletId, AddMoneyRequest request);
    TransferResponse transferMoney(Long sourceWalletId, TransferRequest request);
    List<TransactionResponse> getTransactionHistory(Long walletId);
}
