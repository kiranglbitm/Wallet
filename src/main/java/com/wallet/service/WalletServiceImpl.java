package com.wallet.service;

import com.wallet.dto.WalletDto.*;
import com.wallet.exception.WalletException.*;
import com.wallet.model.Transaction;
import com.wallet.model.Transaction.TransactionType;
import com.wallet.model.Wallet;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    // Create Wallet

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        log.debug("Creating wallet for: {}", request.getEmail());

        if (walletRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateWalletException("email", request.getEmail());
        }
        if (walletRepository.existsByOwnerName(request.getOwnerName())) {
            throw new DuplicateWalletException("ownerName", request.getOwnerName());
        }

        Wallet wallet = Wallet.builder()
                .ownerName(request.getOwnerName())
                .email(request.getEmail())
                .balance(BigDecimal.ZERO)
                .build();
        wallet = walletRepository.save(wallet);

        if (request.getInitialDeposit() != null
                && request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0) {
            wallet.setBalance(request.getInitialDeposit());
            recordTransaction(wallet, request.getInitialDeposit(),
                    wallet.getBalance(), TransactionType.CREDIT,
                    null, null, "Initial deposit");
            wallet = walletRepository.save(wallet);
        }

        log.info("Wallet created: id={}, owner={}", wallet.getId(), wallet.getOwnerName());
        return WalletResponse.from(wallet);
    }

    // Get Balance

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(Long walletId) {
        Wallet wallet = findWalletById(walletId);
        return BalanceResponse.builder()
                .walletId(wallet.getId())
                .ownerName(wallet.getOwnerName())
                .balance(wallet.getBalance())
                .currency("INR")
                .asOf(LocalDateTime.now())
                .build();
    }

    // Add Money

    @Override
    @Transactional
    public TransactionResponse addMoney(Long walletId, AddMoneyRequest request) {
        log.debug("Adding {} to wallet {}", request.getAmount(), walletId);

        Wallet wallet = findActiveWalletById(walletId);
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));

        Transaction txn = recordTransaction(wallet, request.getAmount(),
                wallet.getBalance(), TransactionType.CREDIT,
                null, null,
                request.getDescription() != null ? request.getDescription() : "Deposit");

        walletRepository.save(wallet);
        log.info("Added {} to wallet {}. New balance: {}", request.getAmount(), walletId, wallet.getBalance());
        return TransactionResponse.from(txn);
    }

    // Transfer Money

    @Override
    @Transactional
    public TransferResponse transferMoney(Long sourceWalletId, TransferRequest request) {
        log.debug("Transfer {} from wallet {} to wallet {}",
                request.getAmount(), sourceWalletId, request.getTargetWalletId());

        if (sourceWalletId.equals(request.getTargetWalletId())) {
            throw new SelfTransferException();
        }

        Wallet source = findActiveWalletById(sourceWalletId);
        Wallet target = findActiveWalletById(request.getTargetWalletId());

        if (source.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(sourceWalletId);
        }

        String desc = request.getDescription() != null ? request.getDescription() : "Transfer";

        source.setBalance(source.getBalance().subtract(request.getAmount()));
        Transaction debitTxn = recordTransaction(source, request.getAmount(),
                source.getBalance(), TransactionType.TRANSFER_OUT,
                target.getId(), target.getOwnerName(), desc);

        target.setBalance(target.getBalance().add(request.getAmount()));
        Transaction creditTxn = recordTransaction(target, request.getAmount(),
                target.getBalance(), TransactionType.TRANSFER_IN,
                source.getId(), source.getOwnerName(), desc);

        walletRepository.save(source);
        walletRepository.save(target);

        log.info("Transferred {} from wallet {} to wallet {}",
                request.getAmount(), sourceWalletId, request.getTargetWalletId());

        return TransferResponse.builder()
                .debit(TransactionResponse.from(debitTxn))
                .credit(TransactionResponse.from(creditTxn))
                .message(String.format("Successfully transferred %.2f from %s to %s",
                        request.getAmount(), source.getOwnerName(), target.getOwnerName()))
                .build();
    }

    // Transaction History

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(Long walletId) {
        findWalletById(walletId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId)
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }

    // Helpers

    private Wallet findWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }

    private Wallet findActiveWalletById(Long id) {
        Wallet wallet = findWalletById(id);
        if (wallet.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw new WalletNotActiveException(id);
        }
        return wallet;
    }

    private Transaction recordTransaction(Wallet wallet, BigDecimal amount,
                                          BigDecimal balanceAfter, TransactionType type,
                                          Long relatedWalletId, String relatedWalletOwner,
                                          String description) {
        Transaction txn = Transaction.builder()
                .wallet(wallet)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .type(type)
                .relatedWalletId(relatedWalletId)
                .relatedWalletOwner(relatedWalletOwner)
                .description(description)
                .build();
        return transactionRepository.save(txn);
    }
}
