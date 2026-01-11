package com.banque.template;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * PROCESSEUR BANCAIRE - Implementation Template Method
 *
 * OBJECTIF 6: Implementation concrete du Template Method pour les banques.
 *
 * CARACTERISTIQUES BANQUE:
 * - Frais fixes + pourcentage
 * - Limites elevees (transactions importantes)
 * - Validation stricte (IBAN, BIC)
 * - Audit complet (conformite reglementaire)
 * - Delai de traitement (D+1 pour virements)
 */
public class BankTransactionProcessor extends TransactionProcessor {

    // Configuration specifique banque
    private static final BigDecimal FIXED_FEE = new BigDecimal("2.50");
    private static final BigDecimal PERCENTAGE_FEE = new BigDecimal("0.15"); // 0.15%
    private static final BigDecimal MIN_FEE = new BigDecimal("1.00");
    private static final BigDecimal MAX_FEE = new BigDecimal("50.00");

    // Limites
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("10000000"); // 10 millions
    private static final BigDecimal SINGLE_TRANSACTION_LIMIT = new BigDecimal("5000000"); // 5 millions
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("100"); // Minimum 100

    // Suivi des limites quotidiennes (simplifie)
    private BigDecimal dailyProcessed = BigDecimal.ZERO;

    @Override
    public String getOperatorType() {
        return "BANK";
    }

    /**
     * ETAPE 1: Validation bancaire stricte.
     */
    @Override
    protected ValidationResult validateTransaction(TransactionRequest request) {
        // Verification du montant minimum
        if (request.getAmount() == null || request.getAmount().compareTo(MIN_AMOUNT) < 0) {
            return ValidationResult.invalid(
                "Montant minimum requis: " + MIN_AMOUNT + " " + request.getCurrency()
            );
        }

        // Verification compte source
        if (request.getSourceAccount() == null || request.getSourceAccount().isEmpty()) {
            return ValidationResult.invalid("Compte source obligatoire");
        }

        // Validation format compte (simule IBAN)
        if (!isValidBankAccount(request.getSourceAccount())) {
            return ValidationResult.invalid(
                "Format de compte invalide. Utilisez le format: CMXXXXXXXXXXXX"
            );
        }

        // Pour les transferts, verifier le compte destination
        if (request.getTransactionType() == TransactionType.TRANSFER) {
            if (request.getDestinationAccount() == null || request.getDestinationAccount().isEmpty()) {
                return ValidationResult.invalid("Compte destination obligatoire pour un transfert");
            }
            if (!isValidBankAccount(request.getDestinationAccount())) {
                return ValidationResult.invalid("Format compte destination invalide");
            }
            if (request.getSourceAccount().equals(request.getDestinationAccount())) {
                return ValidationResult.invalid("Compte source et destination identiques");
            }
        }

        // Verification devise
        if (!"XAF".equals(request.getCurrency()) && !"EUR".equals(request.getCurrency())) {
            return ValidationResult.invalid("Devise non supportee. Utilisez XAF ou EUR");
        }

        return ValidationResult.valid();
    }

    /**
     * ETAPE 2: Calcul des frais bancaires (fixe + pourcentage).
     */
    @Override
    protected BigDecimal calculateFees(BigDecimal amount, TransactionType type) {
        BigDecimal percentagePart = amount.multiply(PERCENTAGE_FEE)
            .divide(CENT, 2, RoundingMode.HALF_UP);

        BigDecimal totalFee = FIXED_FEE.add(percentagePart);

        // Appliquer les bornes
        if (totalFee.compareTo(MIN_FEE) < 0) {
            totalFee = MIN_FEE;
        } else if (totalFee.compareTo(MAX_FEE) > 0) {
            totalFee = MAX_FEE;
        }

        // Frais reduits pour les depots
        if (type == TransactionType.DEPOSIT) {
            totalFee = totalFee.multiply(new BigDecimal("0.5"))
                .setScale(2, RoundingMode.HALF_UP);
        }

        // Frais majores pour les retraits importants
        if (type == TransactionType.WITHDRAWAL && amount.compareTo(new BigDecimal("1000000")) > 0) {
            totalFee = totalFee.multiply(new BigDecimal("1.5"))
                .setScale(2, RoundingMode.HALF_UP);
        }

        return totalFee;
    }

    /**
     * ETAPE 3: Verification des limites bancaires (elevees).
     */
    @Override
    protected LimitCheckResult checkLimits(TransactionRequest request) {
        BigDecimal amount = request.getAmount();

        // Limite par transaction
        if (amount.compareTo(SINGLE_TRANSACTION_LIMIT) > 0) {
            return LimitCheckResult.exceeded(
                "Montant depasse la limite par transaction: " + SINGLE_TRANSACTION_LIMIT
            );
        }

        // Limite quotidienne
        BigDecimal projectedDaily = dailyProcessed.add(amount);
        if (projectedDaily.compareTo(DAILY_LIMIT) > 0) {
            return LimitCheckResult.exceeded(
                "Limite quotidienne atteinte. Restant: " +
                DAILY_LIMIT.subtract(dailyProcessed)
            );
        }

        // Verification supplementaire pour gros montants
        if (amount.compareTo(new BigDecimal("2000000")) > 0) {
            return LimitCheckResult.ok(
                "Transaction importante - Verification supplementaire effectuee"
            );
        }

        return LimitCheckResult.ok("Limites respectees");
    }

    /**
     * ETAPE 4: Execution via le systeme bancaire.
     */
    @Override
    protected ExecutionResult executeTransaction(TransactionRequest request, BigDecimal fees) {
        // Simulation du traitement bancaire
        try {
            Thread.sleep(100); // Simule le delai de traitement
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generer une reference bancaire
        String reference = "BNK" + System.currentTimeMillis() +
                          UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // Mettre a jour le compteur quotidien
        dailyProcessed = dailyProcessed.add(request.getAmount());

        return ExecutionResult.success(reference);
    }

    /**
     * HOOK: Audit bancaire complet (conformite).
     */
    @Override
    protected void recordAudit(String transactionId, TransactionRequest request, ExecutionResult result) {
        // Audit bancaire detaille (conformite CEMAC)
        System.out.println("[AUDIT BANQUE] Transaction: " + transactionId);
        System.out.println("  - Type: " + request.getTransactionType());
        System.out.println("  - Montant: " + request.getAmount() + " " + request.getCurrency());
        System.out.println("  - Source: " + maskAccount(request.getSourceAccount()));
        System.out.println("  - Reference: " + result.getReference());
        System.out.println("  - Conformite: CEMAC/COBAC OK");
    }

    /**
     * HOOK: Notifications bancaires formelles.
     */
    @Override
    protected void sendNotifications(TransactionRequest request, ExecutionResult result) {
        // Email formel + SMS
        System.out.println("[NOTIF BANQUE] Email envoye a " + request.getSourceAccount());
        System.out.println("[NOTIF BANQUE] Confirmation SMS pour transaction " + result.getReference());
    }

    // ==================== METHODES SPECIFIQUES BANQUE ====================

    /**
     * Valide le format du compte bancaire (simplifie).
     */
    private boolean isValidBankAccount(String account) {
        // Format simplifie: CM + 12 caracteres alphanumeriques
        return account != null && account.matches("CM[A-Z0-9]{12}");
    }

    /**
     * Masque un numero de compte pour l'affichage.
     */
    private String maskAccount(String account) {
        if (account == null || account.length() < 8) return "****";
        return account.substring(0, 4) + "****" + account.substring(account.length() - 4);
    }

    /**
     * Reinitialise le compteur quotidien.
     */
    public void resetDailyLimit() {
        this.dailyProcessed = BigDecimal.ZERO;
    }

    /**
     * Retourne le montant traite aujourd'hui.
     */
    public BigDecimal getDailyProcessed() {
        return dailyProcessed;
    }

    /**
     * Retourne la limite quotidienne.
     */
    public BigDecimal getDailyLimit() {
        return DAILY_LIMIT;
    }
}
