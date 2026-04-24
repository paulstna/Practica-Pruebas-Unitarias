public class SmartWallet {

    private double balance;
    private UserType userType;
    private AccountStatus status;

    public SmartWallet(UserType userType) {
        this.balance = 0.0;
        this.userType = userType;
        this.status = AccountStatus.ACTIVE;
    }

    /**
     * Deposita un monto en la billetera.
     * - El monto debe ser > 0.
     * - Si el usuario es "Standard", el saldo no puede superar $5,000.
     * - Si el monto es > $100, se aplica un cashback del 1%.
     */
    public boolean deposit(double amount) {
        // Validación: monto debe ser mayor a 0
        if (amount <= 0) {
            return false;
        }

        // Validación: límite de saldo para usuarios Standard
        if (userType == UserType.STANDARD) {
            double cashback = (amount > Constants.CASHBACK_THRESHOLD) ? amount * Constants.CASHBACK_RATE : 0;
            if (balance + amount + cashback > Constants.MAX_BALANCE) {
                return false;
            }
        }

        // Acreditar monto
        balance += amount;

        // Aplicar cashback si el monto supera $100
        if (amount > Constants.CASHBACK_THRESHOLD) {
            balance += amount * Constants.CASHBACK_RATE;
        }

        return true;
    }

    /**
     * Retira un monto de la billetera.
     * - No se permite monto <= 0.
     * - No se puede retirar más de lo que hay en saldo.
     * - Si el saldo queda en exactamente 0, la cuenta se marca como Inactiva.
     */
    public boolean withdraw(double amount) {
        // Validación: monto debe ser mayor a 0
        if (amount <= 0) {
            return false;
        }

        // Validación: fondos suficientes
        if (amount > balance) {
            return false;
        }

        balance -= amount;

        // Marcar como inactiva si el saldo queda en exactamente 0
        if (balance == 0.0) {
            status = AccountStatus.INACTIVE;
        }

        return true;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public UserType getUserType() {
        return userType;
    }

    public AccountStatus getStatus() {
        return status;
    }
}
