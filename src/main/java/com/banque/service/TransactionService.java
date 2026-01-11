package com.banque.service;

import com.banque.transaction.builder.TransactionDirector;
import com.banque.transaction.model.Commission;
import com.banque.transaction.model.Transaction;
import com.banque.transaction.model.TransactionBuilder;
import com.banque.transaction.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service métier pour la gestion des transactions.
 *
 * OBJECTIF 3: Ce service utilise le pattern Builder (via TransactionDirector
 * et TransactionBuilder) pour créer différentes variantes de transactions.
 */
@Service
public class TransactionService {

    private final TransactionDirector director;

    // Stockage en mémoire des transactions (en production: base de données)
    private final List<Transaction> transactionHistory = new ArrayList<>();

    public TransactionService(TransactionDirector director) {
        this.director = director;
    }

    /**
     * Crée et exécute une transaction courte (simple).
     */
    public Transaction.TransactionResult executeQuickTransfer(
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount) {

        Transaction transaction = director.buildQuickTransaction(
            sourceAccount, destinationAccount, amount
        );

        Transaction.TransactionResult result = transaction.execute();
        transactionHistory.add(transaction);
        return result;
    }

    /**
     * Crée et exécute une transaction complète (avec toutes les vérifications).
     */
    public Transaction.TransactionResult executeFullTransfer(
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount) {

        Transaction transaction = director.buildFullTransaction(
            sourceAccount, destinationAccount, amount
        );

        Transaction.TransactionResult result = transaction.execute();
        transactionHistory.add(transaction);
        return result;
    }

    /**
     * Crée et exécute un transfert inter-opérateurs.
     */
    public Transaction.TransactionResult executeInterOperatorTransfer(
            String sourceAccount,
            String sourceOperator,
            String destinationAccount,
            String destinationOperator,
            BigDecimal amount) {

        Transaction transaction = director.buildInterOperatorTransfer(
            sourceAccount, sourceOperator,
            destinationAccount, destinationOperator,
            amount
        );

        Transaction.TransactionResult result = transaction.execute();
        transactionHistory.add(transaction);
        return result;
    }

    /**
     * Crée et exécute un transfert international.
     */
    public Transaction.TransactionResult executeInternationalTransfer(
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount,
            String sourceCurrency,
            String targetCurrency,
            BigDecimal exchangeRate) {

        Transaction transaction = director.buildInternationalTransfer(
            sourceAccount, destinationAccount,
            amount, sourceCurrency, targetCurrency, exchangeRate
        );

        Transaction.TransactionResult result = transaction.execute();
        transactionHistory.add(transaction);
        return result;
    }

    /**
     * Crée une transaction personnalisée via le builder.
     */
    public Transaction.TransactionResult executeCustomTransaction(
            TransactionType type,
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount,
            boolean withVerification,
            boolean withFraudCheck,
            boolean withLogging,
            boolean withNotification,
            List<Commission> commissions) {

        TransactionBuilder builder = new TransactionBuilder()
            .type(type)
            .from(sourceAccount)
            .to(destinationAccount)
            .amount(amount);

        if (withVerification) builder.withVerification();
        if (withFraudCheck) builder.withFraudCheck();
        if (withLogging) builder.withLogging();
        if (withNotification) builder.withNotification();

        if (commissions != null) {
            for (Commission commission : commissions) {
                builder.withCommission(commission);
            }
        }

        Transaction transaction = builder.build();
        Transaction.TransactionResult result = transaction.execute();
        transactionHistory.add(transaction);
        return result;
    }

    /**
     * Retourne l'historique des transactions.
     */
    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    /**
     * Recherche une transaction par référence.
     */
    public Transaction findByReference(String reference) {
        return transactionHistory.stream()
            .filter(t -> t.getReference().equals(reference))
            .findFirst()
            .orElse(null);
    }

    /**
     * Retourne le Director pour un accès direct si besoin.
     */
    public TransactionDirector getDirector() {
        return director;
    }
}
