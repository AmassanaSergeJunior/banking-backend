package com.banque.template;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * PROCESSEUR MICROFINANCE - Implementation Template Method
 *
 * OBJECTIF 6: Implementation concrete du Template Method pour Microfinance.
 *
 * CARACTERISTIQUES MICROFINANCE:
 * - Frais tres bas (inclusion financiere)
 * - Limites basses (petites transactions)
 * - Validation souple (numero membre)
 * - Processus simplifie
 * - Focus sur l'epargne et le microcredit
 */
public class MicrofinanceTransactionProcessor extends TransactionProcessor {

    // Frais Microfinance (tres bas pour inclusion)
    private static final BigDecimal FIXED_FEE = new BigDecimal("25"); // 25 XAF fixe
    private static final BigDecimal PERCENTAGE_FEE = new BigDecimal("0.05"); // 0.05%
    private static final BigDecimal MAX_FEE = new BigDecimal("500"); // Plafonne a 500 XAF

    // Limites Microfinance (accessibles)
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("200000"); // 200k
    private static final BigDecimal SINGLE_TRANSACTION_LIMIT = new BigDecimal("100000"); // 100k
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("50"); // Minimum 50 XAF

    // Suivi des limites
    private BigDecimal dailyProcessed = BigDecimal.ZERO;
    private int dailyTransactionCount = 0;
    private static final int MAX_DAILY_TRANSACTIONS = 10;

    // Nom de l'institution
    private final String institutionName;

    public MicrofinanceTransactionProcessor() {
        this("Microfinance Solidaire");
    }

    public MicrofinanceTransactionProcessor(String institutionName) {
        this.institutionName = institutionName;
    }

    @Override
    public String getOperatorType() {
        return "MICROFINANCE";
    }

    /**
     * ETAPE 1: Validation Microfinance (souple).
     */
    @Override
    protected ValidationResult validateTransaction(TransactionRequest request) {
        // Verification du montant minimum (tres bas)
        if (request.getAmount() == null || request.getAmount().compareTo(MIN_AMOUNT) < 0) {
            return ValidationResult.invalid(
                "Montant minimum: " + MIN_AMOUNT + " XAF"
            );
        }

        // Verification numero membre (format simplifie)
        if (request.getSourceAccount() == null || request.getSourceAccount().isEmpty()) {
            return ValidationResult.invalid("Numero de membre obligatoire");
        }

        if (!isValidMemberNumber(request.getSourceAccount())) {
            return ValidationResult.invalid(
                "Numero de membre invalide. Format: MFI + 8 chiffres (ex: MFI12345678)"
            );
        }

        // Pour les transferts entre membres
        if (request.getTransactionType() == TransactionType.TRANSFER) {
            if (request.getDestinationAccount() == null) {
                return ValidationResult.invalid("Numero membre destinataire obligatoire");
            }
            if (!isValidMemberNumber(request.getDestinationAccount())) {
                return ValidationResult.invalid("Numero membre destinataire invalide");
            }
            if (request.getSourceAccount().equals(request.getDestinationAccount())) {
                return ValidationResult.invalid("Transfert vers soi-meme non autorise");
            }
        }

        // Seul XAF supporte
        if (request.getCurrency() != null && !"XAF".equals(request.getCurrency())) {
            return ValidationResult.invalid("Seule la devise XAF est acceptee");
        }

        return ValidationResult.valid();
    }

    /**
     * ETAPE 2: Calcul des frais (tres bas pour inclusion financiere).
     */
    @Override
    protected BigDecimal calculateFees(BigDecimal amount, TransactionType type) {
        // Depots et epargne gratuits (encourager l'epargne)
        if (type == TransactionType.DEPOSIT) {
            return BigDecimal.ZERO;
        }

        // Calcul: fixe + pourcentage
        BigDecimal percentagePart = amount.multiply(PERCENTAGE_FEE)
            .divide(CENT, 0, RoundingMode.CEILING);

        BigDecimal totalFee = FIXED_FEE.add(percentagePart);

        // Plafonner les frais
        if (totalFee.compareTo(MAX_FEE) > 0) {
            totalFee = MAX_FEE;
        }

        // Reduction pour les petites transactions (moins de 10000 XAF)
        if (amount.compareTo(new BigDecimal("10000")) < 0) {
            totalFee = FIXED_FEE; // Juste les frais fixes
        }

        // Transferts entre membres: -50%
        if (type == TransactionType.TRANSFER) {
            totalFee = totalFee.divide(new BigDecimal("2"), 0, RoundingMode.CEILING);
        }

        return totalFee;
    }

    /**
     * ETAPE 3: Verification des limites (accessibles).
     */
    @Override
    protected LimitCheckResult checkLimits(TransactionRequest request) {
        BigDecimal amount = request.getAmount();

        // Limite par transaction
        if (amount.compareTo(SINGLE_TRANSACTION_LIMIT) > 0) {
            return LimitCheckResult.exceeded(
                "Montant maximum: " + SINGLE_TRANSACTION_LIMIT + " XAF. " +
                "Pour des montants superieurs, contactez votre agence."
            );
        }

        // Limite quotidienne
        BigDecimal projectedDaily = dailyProcessed.add(amount);
        if (projectedDaily.compareTo(DAILY_LIMIT) > 0) {
            return LimitCheckResult.exceeded(
                "Limite quotidienne atteinte. Disponible: " +
                DAILY_LIMIT.subtract(dailyProcessed) + " XAF"
            );
        }

        // Nombre de transactions
        if (dailyTransactionCount >= MAX_DAILY_TRANSACTIONS) {
            return LimitCheckResult.exceeded(
                "Maximum " + MAX_DAILY_TRANSACTIONS + " transactions par jour"
            );
        }

        return LimitCheckResult.ok(
            String.format("OK - %s/%s XAF utilises",
                         dailyProcessed.add(amount), DAILY_LIMIT)
        );
    }

    /**
     * ETAPE 4: Execution Microfinance (processus simplifie).
     */
    @Override
    protected ExecutionResult executeTransaction(TransactionRequest request, BigDecimal fees) {
        // Traitement simplifie (pas de delai bancaire)
        try {
            Thread.sleep(30); // Tres rapide
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generer une reference Microfinance
        String reference = "MFI" + System.currentTimeMillis() +
                          UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // Mettre a jour les compteurs
        dailyProcessed = dailyProcessed.add(request.getAmount());
        dailyTransactionCount++;

        return ExecutionResult.success(reference);
    }

    /**
     * HOOK: Audit simplifie (registre local).
     */
    @Override
    protected void recordAudit(String transactionId, TransactionRequest request, ExecutionResult result) {
        System.out.println("[REGISTRE " + institutionName.toUpperCase() + "] " +
            transactionId + " - Membre " + request.getSourceAccount() +
            " - " + request.getAmount() + " XAF");
    }

    /**
     * HOOK: Notifications adaptees (SMS simple ou recepisse).
     */
    @Override
    protected void sendNotifications(TransactionRequest request, ExecutionResult result) {
        // Pour Microfinance: option SMS ou recepisse papier
        System.out.println("[RECEPISSE] Transaction " + result.getReference());
        System.out.println("  Institution: " + institutionName);
        System.out.println("  Membre: " + request.getSourceAccount());
        System.out.println("  Type: " + request.getTransactionType().getLabel());
        System.out.println("  Montant: " + request.getAmount() + " XAF");
        System.out.println("  Merci de votre confiance!");
    }

    // ==================== METHODES SPECIFIQUES MICROFINANCE ====================

    /**
     * Valide le format du numero de membre.
     */
    private boolean isValidMemberNumber(String memberNumber) {
        // Format: MFI + 8 chiffres
        return memberNumber != null && memberNumber.matches("MFI[0-9]{8}");
    }

    /**
     * Reinitialise les compteurs quotidiens.
     */
    public void resetDailyCounters() {
        this.dailyProcessed = BigDecimal.ZERO;
        this.dailyTransactionCount = 0;
    }

    /**
     * Retourne le nom de l'institution.
     */
    public String getInstitutionName() {
        return institutionName;
    }

    /**
     * Retourne les informations sur les limites.
     */
    public MicrofinanceLimitInfo getLimitInfo() {
        return new MicrofinanceLimitInfo(
            dailyProcessed,
            DAILY_LIMIT,
            dailyTransactionCount,
            MAX_DAILY_TRANSACTIONS,
            SINGLE_TRANSACTION_LIMIT,
            MIN_AMOUNT
        );
    }

    /**
     * Informations sur les limites Microfinance.
     */
    public static class MicrofinanceLimitInfo {
        private final BigDecimal dailyUsed;
        private final BigDecimal dailyLimit;
        private final int transactionsUsed;
        private final int maxTransactions;
        private final BigDecimal maxPerTransaction;
        private final BigDecimal minAmount;

        public MicrofinanceLimitInfo(BigDecimal used, BigDecimal limit, int txUsed,
                                    int txMax, BigDecimal maxPer, BigDecimal min) {
            this.dailyUsed = used;
            this.dailyLimit = limit;
            this.transactionsUsed = txUsed;
            this.maxTransactions = txMax;
            this.maxPerTransaction = maxPer;
            this.minAmount = min;
        }

        public BigDecimal getDailyUsed() { return dailyUsed; }
        public BigDecimal getDailyLimit() { return dailyLimit; }
        public BigDecimal getDailyRemaining() { return dailyLimit.subtract(dailyUsed); }
        public int getTransactionsUsed() { return transactionsUsed; }
        public int getMaxTransactions() { return maxTransactions; }
        public BigDecimal getMaxPerTransaction() { return maxPerTransaction; }
        public BigDecimal getMinAmount() { return minAmount; }
    }
}
