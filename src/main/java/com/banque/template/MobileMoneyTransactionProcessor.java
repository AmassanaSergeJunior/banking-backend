package com.banque.template;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * PROCESSEUR MOBILE MONEY - Implementation Template Method
 *
 * OBJECTIF 6: Implementation concrete du Template Method pour Mobile Money.
 *
 * CARACTERISTIQUES MOBILE MONEY:
 * - Frais par paliers (selon le montant)
 * - Limites moderees (transactions quotidiennes)
 * - Validation numero de telephone
 * - Traitement instantane (temps reel)
 * - Notifications SMS obligatoires
 */
public class MobileMoneyTransactionProcessor extends TransactionProcessor {

    // Paliers de frais Mobile Money
    private static final BigDecimal[][] FEE_TIERS = {
        {new BigDecimal("0"), new BigDecimal("5000"), new BigDecimal("50")},
        {new BigDecimal("5001"), new BigDecimal("25000"), new BigDecimal("150")},
        {new BigDecimal("25001"), new BigDecimal("100000"), new BigDecimal("350")},
        {new BigDecimal("100001"), new BigDecimal("500000"), new BigDecimal("750")},
        {new BigDecimal("500001"), new BigDecimal("1000000"), new BigDecimal("1500")}
    };

    // Limites Mobile Money
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("1000000"); // 1 million
    private static final BigDecimal SINGLE_TRANSACTION_LIMIT = new BigDecimal("500000"); // 500k
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("100"); // Minimum 100

    // Suivi des limites quotidiennes
    private BigDecimal dailyProcessed = BigDecimal.ZERO;
    private int dailyTransactionCount = 0;
    private static final int MAX_DAILY_TRANSACTIONS = 20;

    @Override
    public String getOperatorType() {
        return "MOBILE_MONEY";
    }

    /**
     * ETAPE 1: Validation Mobile Money (numero de telephone).
     */
    @Override
    protected ValidationResult validateTransaction(TransactionRequest request) {
        // Verification du montant minimum
        if (request.getAmount() == null || request.getAmount().compareTo(MIN_AMOUNT) < 0) {
            return ValidationResult.invalid(
                "Montant minimum: " + MIN_AMOUNT + " XAF"
            );
        }

        // Verification numero source (format telephone)
        if (!isValidPhoneNumber(request.getSourceAccount())) {
            return ValidationResult.invalid(
                "Numero de telephone invalide. Format: +237XXXXXXXXX ou 6XXXXXXXX"
            );
        }

        // Pour les transferts, verifier le numero destination
        if (request.getTransactionType() == TransactionType.TRANSFER) {
            if (request.getDestinationAccount() == null || request.getDestinationAccount().isEmpty()) {
                return ValidationResult.invalid("Numero destinataire obligatoire");
            }
            if (!isValidPhoneNumber(request.getDestinationAccount())) {
                return ValidationResult.invalid("Numero destinataire invalide");
            }
            if (normalizePhone(request.getSourceAccount())
                .equals(normalizePhone(request.getDestinationAccount()))) {
                return ValidationResult.invalid("Impossible d'envoyer a soi-meme");
            }
        }

        // Verification du nombre de transactions quotidiennes
        if (dailyTransactionCount >= MAX_DAILY_TRANSACTIONS) {
            return ValidationResult.invalid(
                "Nombre maximum de transactions atteint: " + MAX_DAILY_TRANSACTIONS + "/jour"
            );
        }

        // Seul XAF supporte
        if (!"XAF".equals(request.getCurrency())) {
            return ValidationResult.invalid("Seule la devise XAF est supportee");
        }

        return ValidationResult.valid();
    }

    /**
     * ETAPE 2: Calcul des frais par paliers.
     */
    @Override
    protected BigDecimal calculateFees(BigDecimal amount, TransactionType type) {
        // Depot gratuit
        if (type == TransactionType.DEPOSIT) {
            return BigDecimal.ZERO;
        }

        // Trouver le palier correspondant
        for (BigDecimal[] tier : FEE_TIERS) {
            if (amount.compareTo(tier[0]) >= 0 && amount.compareTo(tier[1]) <= 0) {
                return tier[2];
            }
        }

        // Au-dela du dernier palier: 0.2% du montant
        return amount.multiply(new BigDecimal("0.002"))
            .setScale(0, RoundingMode.CEILING);
    }

    /**
     * ETAPE 3: Verification des limites Mobile Money.
     */
    @Override
    protected LimitCheckResult checkLimits(TransactionRequest request) {
        BigDecimal amount = request.getAmount();

        // Limite par transaction
        if (amount.compareTo(SINGLE_TRANSACTION_LIMIT) > 0) {
            return LimitCheckResult.exceeded(
                "Montant maximum par transaction: " + SINGLE_TRANSACTION_LIMIT + " XAF"
            );
        }

        // Limite quotidienne
        BigDecimal projectedDaily = dailyProcessed.add(amount);
        if (projectedDaily.compareTo(DAILY_LIMIT) > 0) {
            BigDecimal remaining = DAILY_LIMIT.subtract(dailyProcessed);
            return LimitCheckResult.exceeded(
                "Limite quotidienne depassee. Disponible: " + remaining + " XAF"
            );
        }

        // Tout est OK
        String message = String.format(
            "OK - Utilise: %s/%s XAF (%d/%d transactions)",
            dailyProcessed.add(amount), DAILY_LIMIT,
            dailyTransactionCount + 1, MAX_DAILY_TRANSACTIONS
        );

        return LimitCheckResult.ok(message);
    }

    /**
     * ETAPE 4: Execution instantanee Mobile Money.
     */
    @Override
    protected ExecutionResult executeTransaction(TransactionRequest request, BigDecimal fees) {
        // Simulation du traitement Mobile Money (instantane)
        try {
            Thread.sleep(50); // Traitement rapide
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generer une reference Mobile Money
        String reference = "MM" + System.currentTimeMillis() +
                          UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Mettre a jour les compteurs
        dailyProcessed = dailyProcessed.add(request.getAmount());
        dailyTransactionCount++;

        return ExecutionResult.success(reference);
    }

    /**
     * HOOK: Audit Mobile Money simplifie.
     */
    @Override
    protected void recordAudit(String transactionId, TransactionRequest request, ExecutionResult result) {
        System.out.println("[AUDIT MOBILE] " + transactionId + " - " +
            request.getAmount() + " XAF - " + result.getReference());
    }

    /**
     * HOOK: Notifications SMS obligatoires.
     */
    @Override
    protected void sendNotifications(TransactionRequest request, ExecutionResult result) {
        String phone = normalizePhone(request.getSourceAccount());
        System.out.println("[SMS] Envoi a " + phone + ": Transaction " + result.getReference() +
            " de " + request.getAmount() + " XAF effectuee");

        // Notifier aussi le destinataire pour les transferts
        if (request.getTransactionType() == TransactionType.TRANSFER) {
            String destPhone = normalizePhone(request.getDestinationAccount());
            System.out.println("[SMS] Envoi a " + destPhone + ": Vous avez recu " +
                request.getAmount() + " XAF de " + maskPhone(phone));
        }
    }

    // ==================== METHODES SPECIFIQUES MOBILE MONEY ====================

    /**
     * Valide le format du numero de telephone.
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        String normalized = normalizePhone(phone);
        // Format: 237 + 9 chiffres commencant par 6
        return normalized.matches("237[6][0-9]{8}");
    }

    /**
     * Normalise un numero de telephone au format 237XXXXXXXXX.
     */
    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String cleaned = phone.replaceAll("[^0-9]", "");

        if (cleaned.startsWith("237") && cleaned.length() == 12) {
            return cleaned;
        }
        if (cleaned.length() == 9 && cleaned.startsWith("6")) {
            return "237" + cleaned;
        }
        return cleaned;
    }

    /**
     * Masque un numero pour l'affichage.
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "****";
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 3);
    }

    /**
     * Reinitialise les compteurs quotidiens.
     */
    public void resetDailyCounters() {
        this.dailyProcessed = BigDecimal.ZERO;
        this.dailyTransactionCount = 0;
    }

    /**
     * Retourne les informations sur les limites.
     */
    public MobileMoneyLimitInfo getLimitInfo() {
        return new MobileMoneyLimitInfo(
            dailyProcessed,
            DAILY_LIMIT,
            dailyTransactionCount,
            MAX_DAILY_TRANSACTIONS,
            SINGLE_TRANSACTION_LIMIT
        );
    }

    /**
     * Informations sur les limites Mobile Money.
     */
    public static class MobileMoneyLimitInfo {
        private final BigDecimal dailyUsed;
        private final BigDecimal dailyLimit;
        private final int transactionsUsed;
        private final int maxTransactions;
        private final BigDecimal maxPerTransaction;

        public MobileMoneyLimitInfo(BigDecimal used, BigDecimal limit, int txUsed, int txMax, BigDecimal maxPer) {
            this.dailyUsed = used;
            this.dailyLimit = limit;
            this.transactionsUsed = txUsed;
            this.maxTransactions = txMax;
            this.maxPerTransaction = maxPer;
        }

        public BigDecimal getDailyUsed() { return dailyUsed; }
        public BigDecimal getDailyLimit() { return dailyLimit; }
        public BigDecimal getDailyRemaining() { return dailyLimit.subtract(dailyUsed); }
        public int getTransactionsUsed() { return transactionsUsed; }
        public int getMaxTransactions() { return maxTransactions; }
        public int getTransactionsRemaining() { return maxTransactions - transactionsUsed; }
        public BigDecimal getMaxPerTransaction() { return maxPerTransaction; }
    }
}
