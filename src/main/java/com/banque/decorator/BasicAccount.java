package com.banque.decorator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * PATTERN DECORATOR - ConcreteComponent
 *
 * OBJECTIF 8: Compte bancaire de base sans fonctionnalites supplementaires.
 * C'est le composant concret qui sera decore par les decorateurs.
 *
 * CARACTERISTIQUES:
 * - Depot et retrait simples
 * - Pas de decouvert autorise
 * - Pas d'interets
 * - Pas de frais
 * - Limite de retrait standard
 */
public class BasicAccount implements Account {

    private final String accountNumber;
    private final String accountHolder;
    private final AccountType accountType;
    private BigDecimal balance;
    private final List<Transaction> transactionHistory;
    private boolean active;
    private final BigDecimal withdrawalLimit;

    public BasicAccount(String accountNumber, String accountHolder, AccountType accountType) {
        this(accountNumber, accountHolder, accountType, BigDecimal.ZERO);
    }

    public BasicAccount(String accountNumber, String accountHolder, AccountType accountType,
                        BigDecimal initialBalance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.accountType = accountType;
        this.balance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        this.transactionHistory = new ArrayList<>();
        this.active = true;
        this.withdrawalLimit = new BigDecimal("500000"); // 500,000 FCFA par defaut

        // Enregistrer le depot initial si > 0
        if (this.balance.compareTo(BigDecimal.ZERO) > 0) {
            String txId = generateTransactionId("DEP");
            transactionHistory.add(new Transaction(txId, TransactionType.DEPOT,
                this.balance, BigDecimal.ZERO, this.balance, "Depot initial"));
        }
    }

    @Override
    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public String getAccountHolder() {
        return accountHolder;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public TransactionResult deposit(BigDecimal amount) {
        String txId = generateTransactionId("DEP");

        // Validation
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return TransactionResult.failure(txId, "Montant invalide");
        }

        if (!active) {
            return TransactionResult.failure(txId, "Compte inactif");
        }

        // Effectuer le depot
        balance = balance.add(amount);

        // Enregistrer la transaction
        Transaction tx = new Transaction(txId, TransactionType.DEPOT,
            amount, BigDecimal.ZERO, balance, "Depot standard");
        transactionHistory.add(tx);

        System.out.println("[COMPTE] Depot de " + amount + " FCFA sur " + accountNumber);

        return TransactionResult.success(txId, "Depot effectue avec succes",
            amount, BigDecimal.ZERO, balance);
    }

    @Override
    public TransactionResult withdraw(BigDecimal amount) {
        String txId = generateTransactionId("RET");

        // Validation
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return TransactionResult.failure(txId, "Montant invalide");
        }

        if (!active) {
            return TransactionResult.failure(txId, "Compte inactif");
        }

        // Verifier la limite
        if (amount.compareTo(withdrawalLimit) > 0) {
            return TransactionResult.failure(txId,
                "Montant depasse la limite de retrait (" + withdrawalLimit + " FCFA)");
        }

        // Verifier le solde (pas de decouvert pour BasicAccount)
        if (amount.compareTo(balance) > 0) {
            return TransactionResult.failure(txId, "Solde insuffisant");
        }

        // Effectuer le retrait
        balance = balance.subtract(amount);

        // Enregistrer la transaction
        Transaction tx = new Transaction(txId, TransactionType.RETRAIT,
            amount, BigDecimal.ZERO, balance, "Retrait standard");
        transactionHistory.add(tx);

        System.out.println("[COMPTE] Retrait de " + amount + " FCFA sur " + accountNumber);

        return TransactionResult.success(txId, "Retrait effectue avec succes",
            amount, BigDecimal.ZERO, balance);
    }

    @Override
    public TransactionResult transfer(Account target, BigDecimal amount) {
        String txId = generateTransactionId("TRF");

        // Validation
        if (target == null) {
            return TransactionResult.failure(txId, "Compte destinataire invalide");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return TransactionResult.failure(txId, "Montant invalide");
        }

        if (!active) {
            return TransactionResult.failure(txId, "Compte emetteur inactif");
        }

        // Verifier le solde
        if (amount.compareTo(balance) > 0) {
            return TransactionResult.failure(txId, "Solde insuffisant pour le transfert");
        }

        // Effectuer le transfert
        balance = balance.subtract(amount);

        // Enregistrer la transaction sortante
        Transaction txOut = new Transaction(txId, TransactionType.TRANSFERT_EMIS,
            amount, BigDecimal.ZERO, balance,
            "Transfert vers " + target.getAccountNumber());
        transactionHistory.add(txOut);

        // Deposer sur le compte destinataire
        target.deposit(amount);

        System.out.println("[COMPTE] Transfert de " + amount + " FCFA de " +
            accountNumber + " vers " + target.getAccountNumber());

        return TransactionResult.success(txId, "Transfert effectue avec succes",
            amount, BigDecimal.ZERO, balance);
    }

    @Override
    public String getDescription() {
        return accountType.getLabel() + " - " + accountHolder;
    }

    @Override
    public AccountType getAccountType() {
        return accountType;
    }

    @Override
    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    @Override
    public BigDecimal getMonthlyFees() {
        return BigDecimal.ZERO; // Pas de frais pour le compte de base
    }

    @Override
    public BigDecimal getMonthlyBonus() {
        return BigDecimal.ZERO; // Pas de bonus pour le compte de base
    }

    @Override
    public BigDecimal getWithdrawalLimit() {
        return withdrawalLimit;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * Active ou desactive le compte.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Ajoute une transaction a l'historique (utilise par les decorateurs).
     */
    protected void addTransaction(Transaction transaction) {
        transactionHistory.add(transaction);
    }

    /**
     * Modifie le solde directement (utilise par les decorateurs).
     */
    protected void setBalance(BigDecimal newBalance) {
        this.balance = newBalance;
    }

    /**
     * Genere un ID de transaction unique.
     */
    protected String generateTransactionId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public String toString() {
        return String.format("BasicAccount[numero=%s, titulaire=%s, type=%s, solde=%.0f FCFA]",
            accountNumber, accountHolder, accountType.getCode(), balance);
    }
}
