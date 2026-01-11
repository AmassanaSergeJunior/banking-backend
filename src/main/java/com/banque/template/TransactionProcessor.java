package com.banque.template;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PATTERN TEMPLATE METHOD - Processeur de Transactions
 *
 * OBJECTIF 6: Cette classe abstraite definit le squelette de l'algorithme
 * de traitement des transactions. Les sous-classes implementent les etapes
 * specifiques a chaque type d'operateur.
 *
 * POURQUOI TEMPLATE METHOD?
 * - Definir la structure commune du traitement (validation, calcul, execution)
 * - Permettre aux operateurs de personnaliser certaines etapes
 * - Eviter la duplication de code
 * - Garantir que toutes les etapes sont executees dans le bon ordre
 *
 * ALGORITHME (Template Method):
 * 1. Valider la transaction (abstract)
 * 2. Calculer les frais (abstract)
 * 3. Verifier les limites (abstract)
 * 4. Executer la transaction (abstract)
 * 5. Enregistrer l'audit (hook - optionnel)
 * 6. Envoyer les notifications (hook - optionnel)
 */
public abstract class TransactionProcessor {

    // Constantes communes
    protected static final BigDecimal CENT = new BigDecimal("100");

    // Historique des transactions traitees
    protected final List<TransactionRecord> processedTransactions;

    protected TransactionProcessor() {
        this.processedTransactions = new ArrayList<>();
    }

    // ==================== TEMPLATE METHOD ====================

    /**
     * TEMPLATE METHOD - Definit le squelette de l'algorithme.
     * Cette methode est finale pour empecher la modification de l'ordre des etapes.
     *
     * @param request La demande de transaction
     * @return Le resultat du traitement
     */
    public final TransactionResult processTransaction(TransactionRequest request) {
        String transactionId = generateTransactionId();
        LocalDateTime startTime = LocalDateTime.now();
        List<String> logs = new ArrayList<>();

        logs.add("Debut du traitement - " + getOperatorType());

        try {
            // ETAPE 1: Validation (abstraite)
            logs.add("Etape 1: Validation...");
            ValidationResult validation = validateTransaction(request);
            if (!validation.isValid()) {
                logs.add("Validation echouee: " + validation.getErrorMessage());
                return TransactionResult.failure(transactionId, validation.getErrorMessage(), logs);
            }
            logs.add("Validation OK");

            // ETAPE 2: Calcul des frais (abstraite)
            logs.add("Etape 2: Calcul des frais...");
            BigDecimal fees = calculateFees(request.getAmount(), request.getTransactionType());
            logs.add("Frais calcules: " + fees + " " + request.getCurrency());

            // ETAPE 3: Verification des limites (abstraite)
            logs.add("Etape 3: Verification des limites...");
            LimitCheckResult limitCheck = checkLimits(request);
            if (!limitCheck.isWithinLimits()) {
                logs.add("Limite depassee: " + limitCheck.getMessage());
                return TransactionResult.failure(transactionId, limitCheck.getMessage(), logs);
            }
            logs.add("Limites OK - " + limitCheck.getMessage());

            // ETAPE 4: Execution (abstraite)
            logs.add("Etape 4: Execution de la transaction...");
            ExecutionResult execution = executeTransaction(request, fees);
            if (!execution.isSuccess()) {
                logs.add("Execution echouee: " + execution.getErrorMessage());
                return TransactionResult.failure(transactionId, execution.getErrorMessage(), logs);
            }
            logs.add("Execution OK - Reference: " + execution.getReference());

            // ETAPE 5: Audit (hook)
            logs.add("Etape 5: Enregistrement audit...");
            recordAudit(transactionId, request, execution);
            logs.add("Audit enregistre");

            // ETAPE 6: Notifications (hook)
            logs.add("Etape 6: Envoi notifications...");
            sendNotifications(request, execution);
            logs.add("Notifications envoyees");

            // Succes
            BigDecimal totalAmount = request.getAmount().add(fees);
            logs.add("Transaction terminee avec succes");

            TransactionResult result = TransactionResult.success(
                transactionId,
                execution.getReference(),
                request.getAmount(),
                fees,
                totalAmount,
                logs
            );

            // Enregistrer dans l'historique
            processedTransactions.add(new TransactionRecord(
                transactionId,
                request,
                result,
                startTime,
                LocalDateTime.now()
            ));

            return result;

        } catch (Exception e) {
            logs.add("ERREUR INATTENDUE: " + e.getMessage());
            return TransactionResult.failure(transactionId, "Erreur systeme: " + e.getMessage(), logs);
        }
    }

    // ==================== METHODES ABSTRAITES (a implementer) ====================

    /**
     * Retourne le type d'operateur.
     */
    public abstract String getOperatorType();

    /**
     * ETAPE 1: Valide la transaction selon les regles de l'operateur.
     */
    protected abstract ValidationResult validateTransaction(TransactionRequest request);

    /**
     * ETAPE 2: Calcule les frais selon la politique de l'operateur.
     */
    protected abstract BigDecimal calculateFees(BigDecimal amount, TransactionType type);

    /**
     * ETAPE 3: Verifie les limites de l'operateur.
     */
    protected abstract LimitCheckResult checkLimits(TransactionRequest request);

    /**
     * ETAPE 4: Execute la transaction (transfert, depot, retrait).
     */
    protected abstract ExecutionResult executeTransaction(TransactionRequest request, BigDecimal fees);

    // ==================== HOOKS (optionnels) ====================

    /**
     * ETAPE 5: Enregistre l'audit (hook - implementation par defaut).
     * Les sous-classes peuvent surcharger pour personnaliser.
     */
    protected void recordAudit(String transactionId, TransactionRequest request, ExecutionResult result) {
        // Implementation par defaut - peut etre surchargee
        System.out.println("[AUDIT] " + getOperatorType() + " - Transaction " + transactionId);
    }

    /**
     * ETAPE 6: Envoie les notifications (hook - implementation par defaut).
     * Les sous-classes peuvent surcharger pour personnaliser.
     */
    protected void sendNotifications(TransactionRequest request, ExecutionResult result) {
        // Implementation par defaut - peut etre surchargee
        System.out.println("[NOTIF] Transaction completee pour " + request.getSourceAccount());
    }

    // ==================== METHODES UTILITAIRES ====================

    /**
     * Genere un ID de transaction unique.
     */
    protected String generateTransactionId() {
        return "TXN-" + getOperatorType().substring(0, 3).toUpperCase()
               + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Retourne l'historique des transactions.
     */
    public List<TransactionRecord> getProcessedTransactions() {
        return new ArrayList<>(processedTransactions);
    }

    /**
     * Retourne les statistiques du processeur.
     */
    public ProcessorStats getStats() {
        long successful = processedTransactions.stream()
            .filter(r -> r.getResult().isSuccess())
            .count();
        long failed = processedTransactions.size() - successful;

        BigDecimal totalAmount = processedTransactions.stream()
            .filter(r -> r.getResult().isSuccess())
            .map(r -> r.getResult().getTotalAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = processedTransactions.stream()
            .filter(r -> r.getResult().isSuccess())
            .map(r -> r.getResult().getFees())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProcessorStats(
            getOperatorType(),
            processedTransactions.size(),
            successful,
            failed,
            totalAmount,
            totalFees
        );
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Types de transactions supportees.
     */
    public enum TransactionType {
        DEPOSIT("Depot"),
        WITHDRAWAL("Retrait"),
        TRANSFER("Transfert"),
        PAYMENT("Paiement"),
        BILL_PAYMENT("Paiement facture");

        private final String label;

        TransactionType(String label) {
            this.label = label;
        }

        public String getLabel() { return label; }
    }

    /**
     * Demande de transaction.
     */
    public static class TransactionRequest {
        private final String sourceAccount;
        private final String destinationAccount;
        private final BigDecimal amount;
        private final TransactionType transactionType;
        private final String currency;
        private final String description;

        public TransactionRequest(String source, String dest, BigDecimal amount,
                                 TransactionType type, String currency, String desc) {
            this.sourceAccount = source;
            this.destinationAccount = dest;
            this.amount = amount;
            this.transactionType = type;
            this.currency = currency;
            this.description = desc;
        }

        public String getSourceAccount() { return sourceAccount; }
        public String getDestinationAccount() { return destinationAccount; }
        public BigDecimal getAmount() { return amount; }
        public TransactionType getTransactionType() { return transactionType; }
        public String getCurrency() { return currency; }
        public String getDescription() { return description; }
    }

    /**
     * Resultat de validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String error) {
            this.valid = valid;
            this.errorMessage = error;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String error) {
            return new ValidationResult(false, error);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Resultat de verification des limites.
     */
    public static class LimitCheckResult {
        private final boolean withinLimits;
        private final String message;

        private LimitCheckResult(boolean within, String msg) {
            this.withinLimits = within;
            this.message = msg;
        }

        public static LimitCheckResult ok(String message) {
            return new LimitCheckResult(true, message);
        }

        public static LimitCheckResult exceeded(String message) {
            return new LimitCheckResult(false, message);
        }

        public boolean isWithinLimits() { return withinLimits; }
        public String getMessage() { return message; }
    }

    /**
     * Resultat d'execution.
     */
    public static class ExecutionResult {
        private final boolean success;
        private final String reference;
        private final String errorMessage;

        private ExecutionResult(boolean success, String ref, String error) {
            this.success = success;
            this.reference = ref;
            this.errorMessage = error;
        }

        public static ExecutionResult success(String reference) {
            return new ExecutionResult(true, reference, null);
        }

        public static ExecutionResult failure(String error) {
            return new ExecutionResult(false, null, error);
        }

        public boolean isSuccess() { return success; }
        public String getReference() { return reference; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Resultat final de la transaction.
     */
    public static class TransactionResult {
        private final boolean success;
        private final String transactionId;
        private final String reference;
        private final BigDecimal amount;
        private final BigDecimal fees;
        private final BigDecimal totalAmount;
        private final String errorMessage;
        private final List<String> processingLogs;

        private TransactionResult(boolean success, String txId, String ref,
                                 BigDecimal amount, BigDecimal fees, BigDecimal total,
                                 String error, List<String> logs) {
            this.success = success;
            this.transactionId = txId;
            this.reference = ref;
            this.amount = amount;
            this.fees = fees;
            this.totalAmount = total;
            this.errorMessage = error;
            this.processingLogs = logs;
        }

        public static TransactionResult success(String txId, String ref,
                                                BigDecimal amount, BigDecimal fees,
                                                BigDecimal total, List<String> logs) {
            return new TransactionResult(true, txId, ref, amount, fees, total, null, logs);
        }

        public static TransactionResult failure(String txId, String error, List<String> logs) {
            return new TransactionResult(false, txId, null, null, null, null, error, logs);
        }

        public boolean isSuccess() { return success; }
        public String getTransactionId() { return transactionId; }
        public String getReference() { return reference; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getFees() { return fees; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public String getErrorMessage() { return errorMessage; }
        public List<String> getProcessingLogs() { return processingLogs; }
    }

    /**
     * Enregistrement d'une transaction traitee.
     */
    public static class TransactionRecord {
        private final String transactionId;
        private final TransactionRequest request;
        private final TransactionResult result;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;

        public TransactionRecord(String txId, TransactionRequest req, TransactionResult res,
                                LocalDateTime start, LocalDateTime end) {
            this.transactionId = txId;
            this.request = req;
            this.result = res;
            this.startTime = start;
            this.endTime = end;
        }

        public String getTransactionId() { return transactionId; }
        public TransactionRequest getRequest() { return request; }
        public TransactionResult getResult() { return result; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
    }

    /**
     * Statistiques du processeur.
     */
    public static class ProcessorStats {
        private final String operatorType;
        private final long totalTransactions;
        private final long successfulTransactions;
        private final long failedTransactions;
        private final BigDecimal totalAmountProcessed;
        private final BigDecimal totalFeesCollected;

        public ProcessorStats(String type, long total, long success, long failed,
                             BigDecimal amount, BigDecimal fees) {
            this.operatorType = type;
            this.totalTransactions = total;
            this.successfulTransactions = success;
            this.failedTransactions = failed;
            this.totalAmountProcessed = amount;
            this.totalFeesCollected = fees;
        }

        public String getOperatorType() { return operatorType; }
        public long getTotalTransactions() { return totalTransactions; }
        public long getSuccessfulTransactions() { return successfulTransactions; }
        public long getFailedTransactions() { return failedTransactions; }
        public BigDecimal getTotalAmountProcessed() { return totalAmountProcessed; }
        public BigDecimal getTotalFeesCollected() { return totalFeesCollected; }
        public double getSuccessRate() {
            return totalTransactions > 0
                ? (double) successfulTransactions / totalTransactions * 100 : 0;
        }
    }
}
