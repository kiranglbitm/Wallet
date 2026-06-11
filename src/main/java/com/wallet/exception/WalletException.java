package com.wallet.exception;

public class WalletException extends RuntimeException {

    public WalletException(String message) {
        super(message);
    }

    // Specific subclasses

    public static class WalletNotFoundException extends WalletException {
        public WalletNotFoundException(Long id) {
            super("Wallet not found with id: " + id);
        }
        public WalletNotFoundException(String email) {
            super("Wallet not found with email: " + email);
        }
    }

    public static class DuplicateWalletException extends WalletException {
        public DuplicateWalletException(String field, String value) {
            super("Wallet already exists with " + field + ": " + value);
        }
    }

    public static class InsufficientBalanceException extends WalletException {
        public InsufficientBalanceException(Long walletId) {
            super("Insufficient balance in wallet id: " + walletId);
        }
    }

    public static class SelfTransferException extends WalletException {
        public SelfTransferException() {
            super("Cannot transfer money to the same wallet");
        }
    }

    public static class WalletNotActiveException extends WalletException {
        public WalletNotActiveException(Long walletId) {
            super("Wallet id " + walletId + " is not active");
        }
    }
}
