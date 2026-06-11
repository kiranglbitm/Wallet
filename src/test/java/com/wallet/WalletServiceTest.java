package com.wallet;

import com.wallet.dto.WalletDto.*;
import com.wallet.exception.WalletException.*;
import com.wallet.model.Transaction;
import com.wallet.model.Wallet;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.WalletServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock WalletRepository walletRepository;
    @Mock TransactionRepository transactionRepository;
    @InjectMocks WalletServiceImpl walletService;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = Wallet.builder()
                .id(1L)
                .ownerName("Test User")
                .email("test@example.com")
                .balance(new BigDecimal("1000.00"))
                .status(Wallet.WalletStatus.ACTIVE)
                .build();
    }

    // ── Create wallet ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createWallet: success with zero initial deposit")
    void createWallet_success() {
        when(walletRepository.existsByEmail(any())).thenReturn(false);
        when(walletRepository.existsByOwnerName(any())).thenReturn(false);
        when(walletRepository.save(any())).thenReturn(testWallet);

        WalletResponse result = walletService.createWallet(
                new CreateWalletRequest("Test User", "test@example.com", BigDecimal.ZERO));

        assertThat(result.getOwnerName()).isEqualTo("Test User");
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("createWallet: throws on duplicate email")
    void createWallet_duplicateEmail() {
        when(walletRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() ->
                walletService.createWallet(
                        new CreateWalletRequest("Test User", "test@example.com", BigDecimal.ZERO)))
                .isInstanceOf(DuplicateWalletException.class);
    }

    // ── Add money ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addMoney: increases balance correctly")
    void addMoney_success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        Transaction mockTxn = Transaction.builder()
                .id(1L).wallet(testWallet)
                .amount(new BigDecimal("500.00"))
                .balanceAfter(new BigDecimal("1500.00"))
                .type(Transaction.TransactionType.CREDIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();
        when(transactionRepository.save(any())).thenReturn(mockTxn);
        when(walletRepository.save(any())).thenReturn(testWallet);

        TransactionResponse result = walletService.addMoney(1L,
                new AddMoneyRequest(new BigDecimal("500.00"), "Test"));

        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
        assertThat(testWallet.getBalance()).isEqualByComparingTo("1500.00");
    }

    // ── Transfer ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("transferMoney: self transfer throws SelfTransferException")
    void transfer_selfTransfer_throws() {
        assertThatThrownBy(() ->
                walletService.transferMoney(1L,
                        new TransferRequest(1L, new BigDecimal("100"), null)))
                .isInstanceOf(SelfTransferException.class);
    }

    @Test
    @DisplayName("transferMoney: insufficient balance throws")
    void transfer_insufficientBalance_throws() {
        Wallet target = Wallet.builder().id(2L).status(Wallet.WalletStatus.ACTIVE)
                .balance(BigDecimal.ZERO).build();
        when(walletRepository.findById(1L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() ->
                walletService.transferMoney(1L,
                        new TransferRequest(2L, new BigDecimal("9999.00"), null)))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("transferMoney: valid transfer updates both wallets")
    void transfer_success() {
        Wallet source = Wallet.builder().id(1L).ownerName("Alice").email("a@a.com")
                .balance(new BigDecimal("1000.00")).status(Wallet.WalletStatus.ACTIVE).build();
        Wallet target = Wallet.builder().id(2L).ownerName("Bob").email("b@b.com")
                .balance(new BigDecimal("500.00")).status(Wallet.WalletStatus.ACTIVE).build();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(source));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(target));

        Transaction debitTxn = Transaction.builder().id(1L).wallet(source)
                .amount(new BigDecimal("200")).balanceAfter(new BigDecimal("800"))
                .type(Transaction.TransactionType.TRANSFER_OUT)
                .status(Transaction.TransactionStatus.SUCCESS).build();
        Transaction creditTxn = Transaction.builder().id(2L).wallet(target)
                .amount(new BigDecimal("200")).balanceAfter(new BigDecimal("700"))
                .type(Transaction.TransactionType.TRANSFER_IN)
                .status(Transaction.TransactionStatus.SUCCESS).build();

        when(transactionRepository.save(any()))
                .thenReturn(debitTxn)
                .thenReturn(creditTxn);
        when(walletRepository.save(any())).thenReturn(source);

        TransferResponse result = walletService.transferMoney(1L,
                new TransferRequest(2L, new BigDecimal("200.00"), "Test transfer"));

        assertThat(source.getBalance()).isEqualByComparingTo("800.00");
        assertThat(target.getBalance()).isEqualByComparingTo("700.00");
        assertThat(result.getMessage()).contains("Alice").contains("Bob");
    }

    // ── Get wallet not found ──────────────────────────────────────────────────

    @Test
    @DisplayName("getWallet: throws WalletNotFoundException for unknown id")
    void getWallet_notFound() {
        when(walletRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> walletService.getWallet(99L))
                .isInstanceOf(WalletNotFoundException.class);
    }
}
