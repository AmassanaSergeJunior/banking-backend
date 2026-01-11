package com.banque.transaction.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PRODUIT DU BUILDER - Classe Transaction
 *
 * OBJECTIF 3: Cette classe représente une transaction bancaire complexe
 * qui peut avoir plusieurs étapes optionnelles et paramétrables.
 *
 * Une transaction est construite via le TransactionBuilder et peut inclure:
 * - Vérifications (solde, limites, fraude)
 * - Conversion de devise
 * - Application de commissions multiples
 * - Journalisation
 * - Notifications
 */
public class Transaction {

    // Identifiants
    private final String transactionId;
    private final String reference;

    // Informations de base
    private final TransactionType type;
    private final String sourceAccount;
    private final String destinationAccount;
    private final BigDecimal amount;
    private final String currency;

    // Montants calculés
    private BigDecimal convertedAmount;
    private BigDecimal totalCommissions;
    private BigDecimal finalAmount;

    // Conversion de devise
    private String targetCurrency;
    private BigDecimal exchangeRate;

    // Commissions appliquées
    private final List<Commission> commissions;

    // Étapes configurées
    private boolean verificationEnabled;
    private boolean currencyConversionEnabled;
    private boolean loggingEnabled;
    private boolean notificationEnabled;
    private boolean fraudCheckEnabled;

    // Métadonnées
    private TransactionStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String description;
    private final List<String> logs;

    // Informations d'opérateur
    private String sourceOperator;
    private String destinationOperator;

    /**
     * Constructeur privé - utilisé uniquement par le Builder
     */
    Transaction(TransactionBuilder builder) {
        this.transactionId = UUID.randomUUID().toString();
        this.reference = builder.reference != null ? builder.reference : generateReference();
        this.type = builder.type;
        this.sourceAccount = builder.sourceAccount;
        this.destinationAccount = builder.destinationAccount;
        this.amount = builder.amount;
        this.currency = builder.currency != null ? builder.currency : "XAF";

        this.convertedAmount = builder.amount;
        this.totalCommissions = BigDecimal.ZERO;
        this.finalAmount = builder.amount;

        this.targetCurrency = builder.targetCurrency;
        this.exchangeRate = builder.exchangeRate;

        this.commissions = new ArrayList<>(builder.commissions);

        this.verificationEnabled = builder.verificationEnabled;
        this.currencyConversionEnabled = builder.currencyConversionEnabled;
        this.loggingEnabled = builder.loggingEnabled;
        this.notificationEnabled = builder.notificationEnabled;
        this.fraudCheckEnabled = builder.fraudCheckEnabled;

        this.status = TransactionStatus.CREATED;
        this.createdAt = LocalDateTime.now();
        this.description = builder.description;
        this.logs = new ArrayList<>();

        this.sourceOperator = builder.sourceOperator;
        this.destinationOperator = builder.destinationOperator;

        // Calculer les montants
        calculateAmounts();

        // Log initial
        if (loggingEnabled) {
            addLog("Transaction créée: " + type.getDescription() + " de " + amount + " " + currency);
        }
    }

    /**
     * Calcule les montants finaux (conversion + commissions)
     */
    private void calculateAmounts() {
        // Conversion de devise si activée
        if (currencyConversionEnabled && exchangeRate != null) {
            convertedAmount = amount.multiply(exchangeRate);
            if (loggingEnabled) {
                addLog(String.format("Conversion: %s %s -> %s %s (taux: %s)",
                    amount, currency, convertedAmount, targetCurrency, exchangeRate));
            }
        } else {
            convertedAmount = amount;
        }

        // Calcul des commissions
        totalCommissions = BigDecimal.ZERO;
        for (Commission commission : commissions) {
            BigDecimal commissionAmount = commission.calculate(convertedAmount);
            totalCommissions = totalCommissions.add(commissionAmount);
            if (loggingEnabled) {
                addLog(String.format("Commission '%s': %s %s",
                    commission.getName(), commissionAmount, currency));
            }
        }

        // Montant final
        finalAmount = convertedAmount.add(totalCommissions);
        if (loggingEnabled && !commissions.isEmpty()) {
            addLog(String.format("Montant final (avec commissions): %s %s", finalAmount,
                targetCurrency != null ? targetCurrency : currency));
        }
    }

    /**
     * Exécute la transaction avec toutes les étapes configurées.
     */
    public TransactionResult execute() {
        List<String> executionLogs = new ArrayList<>();

        try {
            // Étape 1: Vérification
            if (verificationEnabled) {
                status = TransactionStatus.PENDING_VERIFICATION;
                executionLogs.add("Vérification du compte source...");
                // Simulation de vérification
                if (sourceAccount == null || sourceAccount.isEmpty()) {
                    throw new TransactionException("Compte source invalide");
                }
                executionLogs.add("Vérification OK");
                status = TransactionStatus.VERIFIED;
            }

            // Étape 2: Vérification anti-fraude
            if (fraudCheckEnabled) {
                executionLogs.add("Analyse anti-fraude en cours...");
                // Simulation: montants > 5M sont suspects
                if (amount.compareTo(new BigDecimal("5000000")) > 0) {
                    executionLogs.add("ALERTE: Montant élevé détecté - vérification manuelle requise");
                }
                executionLogs.add("Analyse anti-fraude OK");
            }

            // Étape 3: Traitement
            status = TransactionStatus.PROCESSING;
            executionLogs.add("Traitement de la transaction...");

            // Simulation du traitement
            Thread.sleep(100); // Simule un délai de traitement

            // Étape 4: Finalisation
            status = TransactionStatus.COMPLETED;
            processedAt = LocalDateTime.now();
            executionLogs.add("Transaction complétée avec succès");

            // Étape 5: Notification
            if (notificationEnabled) {
                executionLogs.add("Envoi des notifications...");
                executionLogs.add("Notification envoyée au compte source");
                if (destinationAccount != null) {
                    executionLogs.add("Notification envoyée au compte destination");
                }
            }

            // Logging final
            if (loggingEnabled) {
                for (String log : executionLogs) {
                    addLog(log);
                }
            }

            return new TransactionResult(true, "Transaction exécutée avec succès", this, executionLogs);

        } catch (TransactionException e) {
            status = TransactionStatus.FAILED;
            executionLogs.add("ERREUR: " + e.getMessage());
            return new TransactionResult(false, e.getMessage(), this, executionLogs);
        } catch (InterruptedException e) {
            status = TransactionStatus.FAILED;
            Thread.currentThread().interrupt();
            return new TransactionResult(false, "Transaction interrompue", this, executionLogs);
        }
    }

    private String generateReference() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    public void addLog(String message) {
        logs.add(LocalDateTime.now() + " - " + message);
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getReference() { return reference; }
    public TransactionType getType() { return type; }
    public String getSourceAccount() { return sourceAccount; }
    public String getDestinationAccount() { return destinationAccount; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public BigDecimal getTotalCommissions() { return totalCommissions; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public String getTargetCurrency() { return targetCurrency; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public List<Commission> getCommissions() { return new ArrayList<>(commissions); }
    public boolean isVerificationEnabled() { return verificationEnabled; }
    public boolean isCurrencyConversionEnabled() { return currencyConversionEnabled; }
    public boolean isLoggingEnabled() { return loggingEnabled; }
    public boolean isNotificationEnabled() { return notificationEnabled; }
    public boolean isFraudCheckEnabled() { return fraudCheckEnabled; }
    public TransactionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public String getDescription() { return description; }
    public List<String> getLogs() { return new ArrayList<>(logs); }
    public String getSourceOperator() { return sourceOperator; }
    public String getDestinationOperator() { return destinationOperator; }

    /**
     * Retourne un résumé de la transaction.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Transaction ").append(reference).append(" ===\n");
        sb.append("Type: ").append(type.getDescription()).append("\n");
        sb.append("Montant: ").append(amount).append(" ").append(currency).append("\n");
        if (currencyConversionEnabled) {
            sb.append("Converti: ").append(convertedAmount).append(" ").append(targetCurrency).append("\n");
        }
        if (!commissions.isEmpty()) {
            sb.append("Commissions: ").append(totalCommissions).append("\n");
        }
        sb.append("Montant final: ").append(finalAmount).append("\n");
        sb.append("Statut: ").append(status.getDescription()).append("\n");
        sb.append("Étapes: ");
        List<String> steps = new ArrayList<>();
        if (verificationEnabled) steps.add("Vérification");
        if (fraudCheckEnabled) steps.add("Anti-fraude");
        if (currencyConversionEnabled) steps.add("Conversion");
        if (!commissions.isEmpty()) steps.add("Commissions(" + commissions.size() + ")");
        if (loggingEnabled) steps.add("Logging");
        if (notificationEnabled) steps.add("Notification");
        sb.append(steps.isEmpty() ? "Aucune" : String.join(" -> ", steps));
        return sb.toString();
    }

    /**
     * Exception pour les erreurs de transaction.
     */
    public static class TransactionException extends RuntimeException {
        public TransactionException(String message) {
            super(message);
        }
    }

    /**
     * Résultat d'exécution d'une transaction.
     */
    public static class TransactionResult {
        private final boolean success;
        private final String message;
        private final Transaction transaction;
        private final List<String> executionLogs;

        public TransactionResult(boolean success, String message, Transaction transaction, List<String> logs) {
            this.success = success;
            this.message = message;
            this.transaction = transaction;
            this.executionLogs = logs;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Transaction getTransaction() { return transaction; }
        public List<String> getExecutionLogs() { return executionLogs; }
    }
}
