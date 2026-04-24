import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SmartWallet — Suite de Pruebas")
public class SmartWalletTest {

    private SmartWallet standardWallet;
    private SmartWallet premiumWallet;

    @BeforeEach
    void setUp() {
        standardWallet = new SmartWallet(UserType.STANDARD);
        premiumWallet = new SmartWallet(UserType.PREMIUM);
    }

    @Test
    @DisplayName("TC-01 | Depósito válido de $50 → saldo = $50 (sin cashback)")
    void depositValidAmountUnderCashbackThreshold() {
        // Arrange
        double amount = 50.0;
        double expectedFinalBalance = standardWallet.getBalance() + amount; // $0 + $50 = $50

        // Act
        boolean result = standardWallet.deposit(amount);

        // Assert
        assertTrue(result);
        assertEquals(expectedFinalBalance, standardWallet.getBalance());
    }

    @Test
    @DisplayName("TC-02 | Depósito de $200 → saldo = $202 (cashback 1%)")
    void depositAboveCashbackThresholdAppliesCashback() {
        // Arrange
        double amount = 200.0;
        double expectedFinalBalance = standardWallet.getBalance()
                + amount + getAmountCashback(amount); // $0 + $200 + $2 = $202

        // Act
        boolean result = standardWallet.deposit(amount);

        // Assert
        assertTrue(result);
        assertEquals(expectedFinalBalance, standardWallet.getBalance());
    }

    @Test
    @DisplayName("TC-03 | Depósito exacto de $100 → saldo = $100 (sin cashback)")
    void depositExactlyAtCashbackThresholdNoBonus() {
        // Arrange
        double amount = 100.0;
        double expectedFinalBalance = standardWallet.getBalance() + amount; // $0 + $100 = $100

        // Act
        boolean result = standardWallet.deposit(amount);

        // Assert
        assertTrue(result);
        assertEquals(expectedFinalBalance, standardWallet.getBalance());
    }

    @Test
    @DisplayName("TC-04 | Usuario Standard alcanza exactamente $5,000 → permitido")
    void depositStandardUserReachesMaxBalanceAllowed() {
        // Arrange
        // $4,900 + cashback $49 = $4,949; then $4,949 + $51 = $5,000 (no cashback)
        double initialDeposit = 4900.0;
        standardWallet.deposit(initialDeposit);
        double finalDeposit = 51.0;
        double expectedFinalBalance = initialDeposit + getAmountCashback(initialDeposit) + finalDeposit; // $49 + $4,900
                                                                                                         // + $51 =
                                                                                                         // $5,000

        // Act
        boolean result = standardWallet.deposit(finalDeposit);

        // Assert
        assertTrue(result);
        assertEquals(expectedFinalBalance, standardWallet.getBalance());
    }

    @Test
    @DisplayName("TC-05 | Usuario Standard supera $5,000 → depósito rechazado")
    void depositStandardUserExceedsMaxBalance() {
        // Arrange
        double initialDeposit = 4000.0;
        standardWallet.deposit(initialDeposit); // $4,000 + cashback $40 = $4,040
        double expectedFinalBalance = initialDeposit + getAmountCashback(initialDeposit); // $40 + $4,000 = $4,040
        double finalDeposit = 1000.0;

        // Act
        boolean result = standardWallet.deposit(finalDeposit); // $4,040 + $1,010 > $5,000

        // Assert
        assertFalse(result);
        assertEquals(expectedFinalBalance, standardWallet.getBalance());
    }

    @Test
    @DisplayName("TC-06 | Usuario Premium supera $5,000 → depósito permitido")
    void depositPremiumUserExceedsMaxBalance() {
        // Arrange
        double initialDeposit = 4000.0;
        premiumWallet.deposit(initialDeposit); // $4,000 + cashback $40 = $4,040
        double secondDeposit = 2000.0;
        double expectedFinalBalance = initialDeposit + getAmountCashback(initialDeposit)
                + secondDeposit + getAmountCashback(secondDeposit);
        // $4,000 + $40 + $2,000 + $20 = $6,060

        // Act
        boolean result = premiumWallet.deposit(secondDeposit); // $4,040 + $2,020 > $5,000 but es Premium

        // Assert
        assertTrue(result);
        assertEquals(expectedFinalBalance, premiumWallet.getBalance());
    }

    @Test
    @DisplayName("TC-07 | Retiro valido de $30 → saldo disminuye en $30")
    void withdrawValidAmountDecreasesBalance() {
        // Arrange
        double initialBalance = 100.0;
        standardWallet.setBalance(initialBalance);
        double withdrawAmount = 30.0;
        double expectedFinalBalance = initialBalance - withdrawAmount; // $100 - $30 = $70

        // Act
        boolean result = standardWallet.withdraw(withdrawAmount);

        // Assert
        assertTrue(result);
        assertEquals(expectedFinalBalance, standardWallet.getBalance());
    }

    @Test
    @DisplayName("TC-08 | Depósito con monto negativo → rechazado")
    void depositNegativeAmountIsRejected() {
        // Arrange
        double expectedFinalBalance = standardWallet.getBalance(); // $0
        double negativeAmount = -50.0;

        // Act
        boolean result = standardWallet.deposit(negativeAmount);

        // Assert
        assertFalse(result);
        assertEquals(expectedFinalBalance, standardWallet.getBalance());
    }

    @Test
    @DisplayName("TC-09 | Retiro mayor al saldo → rechazado (saldo insuficiente)")
    void withdrawMoreThanBalanceIsRejected() {
        // Arrange
        double initialBalance = 50.0;
        standardWallet.setBalance(initialBalance);
        double withdrawAmount = 100.0;
        double expectedFinalBalance = initialBalance;

        // Act
        boolean result = standardWallet.withdraw(withdrawAmount);

        // Assert
        assertFalse(result);
        assertEquals(expectedFinalBalance, standardWallet.getBalance());
    }

    @Test
    @DisplayName("TC-10 | Retiro total deja saldo en $0 → cuenta pasa a 'Inactiva'")
    void withdrawAllFundsSetsAccountInactive() {
        // Arrange
        double initialBalance = 100.0;
        standardWallet.setBalance(initialBalance);
        double withdrawAmount = initialBalance;

        // Act
        boolean result = standardWallet.withdraw(withdrawAmount);

        // Assert
        assertTrue(result);
        assertEquals(0.0, standardWallet.getBalance());
        assertEquals(AccountStatus.INACTIVE, standardWallet.getStatus());
    }

    @Test
    @DisplayName("TC-11 | Retiro con monto negativo → rechazado")
    void withdrawNegativeAmountIsRejected() {
        // Arrange
        double initialBalance = 100.0;
        standardWallet.setBalance(initialBalance);
        double negativeAmount = -50.0;
        double expectedFinalBalance = initialBalance;

        // Act
        boolean result = standardWallet.withdraw(negativeAmount);

        // Assert
        assertFalse(result);
        assertEquals(expectedFinalBalance, standardWallet.getBalance());
    }

    private double getAmountCashback(double amount) {
        if (amount > Constants.CASHBACK_THRESHOLD) {
            return amount * Constants.CASHBACK_RATE;
        }
        return 0.0;
    }
}
