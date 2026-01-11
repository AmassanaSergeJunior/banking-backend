package com.banque.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * PATTERN DECORATOR - ConcreteDecorator (Frais)
 *
 * OBJECTIF 8: Ajoute des frais de gestion au compte.
 * Permet de configurer differents types de frais.
 *
 * CARACTERISTIQUES:
 * - Frais de tenue de compte mensuels
 * - Frais par transaction
 * - Frais de retrait
 * - Frais minimums et maximums
 */
public class FeeDecorator extends AccountDecorator {

    private final BigDecimal monthlyMaintenanceFee;
    private final BigDecimal transactionFeeRate; // Pourcentage
    private final BigDecimal withdrawalFee;
    private final BigDecimal minimumFee;
    private final BigDecimal maximumFee;
    private final FeeType feeType;

    /**
     * Types de structure de frais.
     */
    public enum FeeType {
        BASIC("Basique", new BigDecimal("500"), new BigDecimal("0"), new BigDecimal("100")),
        STANDARD("Standard", new BigDecimal("1000"), new BigDecimal("0.5"), new BigDecimal("200")),
        BUSINESS("Business", new BigDecimal("2500"), new BigDecimal("0.25"), new BigDecimal("500"));

        private final String label;
        private final BigDecimal defaultMonthly;
        private final BigDecimal defaultRate;
        private final BigDecimal defaultWithdrawal;

        FeeType(String label, BigDecimal monthly, BigDecimal rate, BigDecimal withdrawal) {
            this.label = label;
            this.defaultMonthly = monthly;
            this.defaultRate = rate;
            this.defaultWithdrawal = withdrawal;
        }

        public String getLabel() { return label; }
        public BigDecimal getDefaultMonthly() { return defaultMonthly; }
        public BigDecimal getDefaultRate() { return defaultRate; }
        public BigDecimal getDefaultWithdrawal() { return defaultWithdrawal; }
    }

    /**
     * Cree un decorateur de frais basiques.
     */
    public FeeDecorator(Account account) {
        this(account, FeeType.BASIC);
    }

    /**
     * Cree un decorateur de frais avec type specifie.
     */
    public FeeDecorator(Account account, FeeType feeType) {
        this(account, feeType,
             feeType.getDefaultMonthly(),
             feeType.getDefaultRate(),
             feeType.getDefaultWithdrawal());
    }

    /**
     * Cree un decorateur de frais personnalise.
     */
    public FeeDecorator(Account account, FeeType feeType,
                        BigDecimal monthlyFee, BigDecimal txRate, BigDecimal withdrawalFee) {
        super(account);
        this.feeType = feeType;
        this.monthlyMaintenanceFee = monthlyFee;
        this.transactionFeeRate = txRate;
        this.withdrawalFee = withdrawalFee;
        this.minimumFee = new BigDecimal("50");
        this.maximumFee = new BigDecimal("10000");
    }

    @Override
    public String getDescription() {
        return wrappedAccount.getDescription() + " + Frais " + feeType.getLabel();
    }

    @Override
    protected String getDecoratorName() {
        return "Frais(" + feeType.getLabel() + ")";
    }

    @Override
    public BigDecimal getMonthlyFees() {
        return wrappedAccount.getMonthlyFees().add(monthlyMaintenanceFee);
    }

    @Override
    public TransactionResult withdraw(BigDecimal amount) {
        // Calculer les frais de retrait
        BigDecimal fees = calculateWithdrawalFees(amount);
        BigDecimal totalDebit = amount.add(fees);

        // Verifier si le solde est suffisant pour le retrait + frais
        if (totalDebit.compareTo(getBalance()) > 0) {
            // Essayer le retrait normal (peut utiliser le decouvert si disponible)
            TransactionResult result = wrappedAccount.withdraw(amount);

            if (result.isSuccess()) {
                // Prelever les frais separement
                applyFees(fees, "Frais de retrait");
            }

            return result;
        }

        // Effectuer le retrait avec frais integres
        return withdrawWithFees(amount, fees);
    }

    private TransactionResult withdrawWithFees(BigDecimal amount, BigDecimal fees) {
        // Effectuer le retrait de base
        TransactionResult result = wrappedAccount.withdraw(amount);

        if (result.isSuccess() && fees.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("[FRAIS] Application de " + fees + " FCFA de frais de retrait");
            applyFees(fees, "Frais de retrait");
        }

        return result;
    }

    /**
     * Applique des frais au compte.
     */
    private void applyFees(BigDecimal fees, String description) {
        if (fees.compareTo(BigDecimal.ZERO) <= 0) return;

        Account base = getBaseAccount();
        if (base instanceof BasicAccount) {
            BasicAccount basicAccount = (BasicAccount) base;
            BigDecimal newBalance = basicAccount.getBalance().subtract(fees);
            basicAccount.setBalance(newBalance);

            String txId = "FEE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Transaction tx = new Transaction(txId, TransactionType.FRAIS,
                fees, BigDecimal.ZERO, newBalance, description);
            basicAccount.addTransaction(tx);
        }
    }

    /**
     * Calcule les frais de retrait.
     */
    public BigDecimal calculateWithdrawalFees(BigDecimal amount) {
        // Frais fixe + pourcentage
        BigDecimal percentageFee = amount.multiply(transactionFeeRate)
            .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);

        BigDecimal totalFees = withdrawalFee.add(percentageFee);

        // Appliquer min et max
        if (totalFees.compareTo(minimumFee) < 0) {
            totalFees = minimumFee;
        }
        if (totalFees.compareTo(maximumFee) > 0) {
            totalFees = maximumFee;
        }

        return totalFees;
    }

    /**
     * Calcule les frais sur une transaction generique.
     */
    public BigDecimal calculateTransactionFees(BigDecimal amount) {
        BigDecimal fees = amount.multiply(transactionFeeRate)
            .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);

        if (fees.compareTo(minimumFee) < 0) {
            fees = minimumFee;
        }
        if (fees.compareTo(maximumFee) > 0) {
            fees = maximumFee;
        }

        return fees;
    }

    /**
     * Retourne le resume des frais.
     */
    public String getFeeSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== STRUCTURE DE FRAIS (").append(feeType.getLabel()).append(") ===\n");
        sb.append("Tenue de compte mensuelle: ").append(monthlyMaintenanceFee).append(" FCFA\n");
        sb.append("Taux par transaction: ").append(transactionFeeRate).append("%\n");
        sb.append("Frais de retrait fixe: ").append(withdrawalFee).append(" FCFA\n");
        sb.append("Frais minimum: ").append(minimumFee).append(" FCFA\n");
        sb.append("Frais maximum: ").append(maximumFee).append(" FCFA\n");
        return sb.toString();
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public BigDecimal getMonthlyMaintenanceFee() {
        return monthlyMaintenanceFee;
    }

    public BigDecimal getTransactionFeeRate() {
        return transactionFeeRate;
    }

    public BigDecimal getWithdrawalFee() {
        return withdrawalFee;
    }
}
