package com.banque.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * PATTERN DECORATOR - ConcreteDecorator (Decouvert)
 *
 * OBJECTIF 8: Ajoute la possibilite de decouvert au compte.
 * Permet des retraits meme si le solde est insuffisant,
 * jusqu'a une limite configurable.
 *
 * CARACTERISTIQUES:
 * - Limite de decouvert configurable
 * - Frais d'utilisation du decouvert
 * - Taux d'interet sur le decouvert
 */
public class OverdraftDecorator extends AccountDecorator {

    private final BigDecimal overdraftLimit;
    private final BigDecimal overdraftFeeRate; // Pourcentage de frais sur utilisation
    private final BigDecimal overdraftInterestRate; // Taux d'interet mensuel sur decouvert

    /**
     * Cree un decorateur de decouvert avec limite par defaut (100,000 FCFA).
     */
    public OverdraftDecorator(Account account) {
        this(account, new BigDecimal("100000"));
    }

    /**
     * Cree un decorateur de decouvert avec limite personnalisee.
     *
     * @param account Compte a decorer
     * @param overdraftLimit Limite de decouvert autorise
     */
    public OverdraftDecorator(Account account, BigDecimal overdraftLimit) {
        this(account, overdraftLimit, new BigDecimal("1.5"), new BigDecimal("2.0"));
    }

    /**
     * Cree un decorateur de decouvert complet.
     *
     * @param account Compte a decorer
     * @param overdraftLimit Limite de decouvert
     * @param overdraftFeeRate Frais d'utilisation en pourcentage
     * @param overdraftInterestRate Taux d'interet mensuel sur decouvert
     */
    public OverdraftDecorator(Account account, BigDecimal overdraftLimit,
                              BigDecimal overdraftFeeRate, BigDecimal overdraftInterestRate) {
        super(account);
        this.overdraftLimit = overdraftLimit;
        this.overdraftFeeRate = overdraftFeeRate;
        this.overdraftInterestRate = overdraftInterestRate;
    }

    @Override
    public String getDescription() {
        return wrappedAccount.getDescription() + " + Decouvert " +
               overdraftLimit + " FCFA";
    }

    @Override
    protected String getDecoratorName() {
        return "Decouvert(" + overdraftLimit + " FCFA)";
    }

    @Override
    public TransactionResult withdraw(BigDecimal amount) {
        BigDecimal currentBalance = getBalance();
        BigDecimal availableTotal = currentBalance.add(overdraftLimit);

        // Verifier si le retrait est possible avec le decouvert
        if (amount.compareTo(availableTotal) > 0) {
            String txId = "RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return TransactionResult.failure(txId,
                "Montant depasse le solde + decouvert autorise (" + availableTotal + " FCFA)");
        }

        // Si on utilise le decouvert, calculer les frais
        BigDecimal fees = BigDecimal.ZERO;
        if (amount.compareTo(currentBalance) > 0) {
            BigDecimal overdraftUsed = amount.subtract(currentBalance);
            fees = overdraftUsed.multiply(overdraftFeeRate)
                .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);

            System.out.println("[DECOUVERT] Utilisation de " + overdraftUsed +
                " FCFA de decouvert, frais: " + fees + " FCFA");
        }

        // Effectuer le retrait via le compte enveloppe
        // On doit modifier le comportement car BasicAccount n'autorise pas le decouvert
        return withdrawWithOverdraft(amount, fees);
    }

    private TransactionResult withdrawWithOverdraft(BigDecimal amount, BigDecimal fees) {
        String txId = "RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (!isActive()) {
            return TransactionResult.failure(txId, "Compte inactif");
        }

        // Verifier la limite de retrait
        if (amount.compareTo(getWithdrawalLimit()) > 0) {
            return TransactionResult.failure(txId,
                "Montant depasse la limite de retrait");
        }

        // Calculer le nouveau solde (peut etre negatif)
        BigDecimal totalDebit = amount.add(fees);
        BigDecimal newBalance = getBalance().subtract(totalDebit);

        // Verifier qu'on ne depasse pas la limite de decouvert
        if (newBalance.compareTo(overdraftLimit.negate()) < 0) {
            return TransactionResult.failure(txId,
                "Depassement de la limite de decouvert");
        }

        // Mettre a jour le solde directement sur le compte de base
        Account base = getBaseAccount();
        if (base instanceof BasicAccount) {
            ((BasicAccount) base).setBalance(newBalance);

            // Enregistrer la transaction
            Transaction tx = new Transaction(txId, TransactionType.RETRAIT,
                amount, fees, newBalance, "Retrait avec decouvert");
            ((BasicAccount) base).addTransaction(tx);
        }

        System.out.println("[COMPTE] Retrait de " + amount + " FCFA (frais: " + fees +
            "), nouveau solde: " + newBalance + " FCFA");

        return TransactionResult.success(txId, "Retrait effectue avec decouvert",
            amount, fees, newBalance);
    }

    @Override
    public BigDecimal getMonthlyFees() {
        BigDecimal baseFees = wrappedAccount.getMonthlyFees();

        // Ajouter les interets sur le decouvert si solde negatif
        BigDecimal balance = getBalance();
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal overdraftAmount = balance.negate();
            BigDecimal overdraftInterest = overdraftAmount
                .multiply(overdraftInterestRate)
                .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            return baseFees.add(overdraftInterest);
        }

        return baseFees;
    }

    /**
     * Retourne le montant de decouvert actuellement utilise.
     */
    public BigDecimal getOverdraftUsed() {
        BigDecimal balance = getBalance();
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            return balance.negate();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Retourne le decouvert disponible.
     */
    public BigDecimal getAvailableOverdraft() {
        return overdraftLimit.subtract(getOverdraftUsed());
    }

    /**
     * Retourne le solde disponible total (solde + decouvert).
     */
    public BigDecimal getTotalAvailable() {
        return getBalance().add(overdraftLimit);
    }

    /**
     * Verifie si le compte est actuellement en decouvert.
     */
    public boolean isInOverdraft() {
        return getBalance().compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public BigDecimal getOverdraftFeeRate() {
        return overdraftFeeRate;
    }

    public BigDecimal getOverdraftInterestRate() {
        return overdraftInterestRate;
    }
}
