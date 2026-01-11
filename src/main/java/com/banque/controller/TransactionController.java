package com.banque.controller;

import com.banque.service.TransactionService;
import com.banque.transaction.model.Commission;
import com.banque.transaction.model.Transaction;
import com.banque.transaction.model.TransactionType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST pour les transactions.
 *
 * OBJECTIF 3: Ce controller démontre l'utilisation du pattern Builder
 * pour créer différentes variantes de transactions.
 *
 * Endpoints:
 * - POST /api/transactions/quick - Transaction courte
 * - POST /api/transactions/full - Transaction complète
 * - POST /api/transactions/inter-operator - Transfert inter-opérateurs
 * - POST /api/transactions/international - Transfert international
 * - POST /api/transactions/custom - Transaction personnalisée
 * - GET /api/transactions - Historique des transactions
 * - GET /api/transactions/{reference} - Détail d'une transaction
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Crée une transaction courte/rapide.
     *
     * Variante COURTE du Builder:
     * - Pas de vérification supplémentaire
     * - Commission minimale
     * - Notification simple
     */
    @PostMapping("/quick")
    public ResponseEntity<Map<String, Object>> executeQuickTransfer(
            @RequestParam String sourceAccount,
            @RequestParam String destinationAccount,
            @RequestParam BigDecimal amount) {

        Transaction.TransactionResult result = transactionService.executeQuickTransfer(
            sourceAccount, destinationAccount, amount
        );

        return buildResponse(result, "Transaction Courte");
    }

    /**
     * Crée une transaction complète avec toutes les étapes.
     *
     * Variante COMPLETE du Builder:
     * - Vérification du compte
     * - Anti-fraude
     * - Commissions multiples
     * - Logging détaillé
     * - Notifications complètes
     */
    @PostMapping("/full")
    public ResponseEntity<Map<String, Object>> executeFullTransfer(
            @RequestParam String sourceAccount,
            @RequestParam String destinationAccount,
            @RequestParam BigDecimal amount) {

        Transaction.TransactionResult result = transactionService.executeFullTransfer(
            sourceAccount, destinationAccount, amount
        );

        return buildResponse(result, "Transaction Complète");
    }

    /**
     * Crée un transfert inter-opérateurs.
     */
    @PostMapping("/inter-operator")
    public ResponseEntity<Map<String, Object>> executeInterOperatorTransfer(
            @RequestParam String sourceAccount,
            @RequestParam String sourceOperator,
            @RequestParam String destinationAccount,
            @RequestParam String destinationOperator,
            @RequestParam BigDecimal amount) {

        Transaction.TransactionResult result = transactionService.executeInterOperatorTransfer(
            sourceAccount, sourceOperator,
            destinationAccount, destinationOperator,
            amount
        );

        return buildResponse(result, "Transfert Inter-Opérateurs");
    }

    /**
     * Crée un transfert international avec conversion de devise.
     */
    @PostMapping("/international")
    public ResponseEntity<Map<String, Object>> executeInternationalTransfer(
            @RequestParam String sourceAccount,
            @RequestParam String destinationAccount,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "XAF") String sourceCurrency,
            @RequestParam String targetCurrency,
            @RequestParam BigDecimal exchangeRate) {

        Transaction.TransactionResult result = transactionService.executeInternationalTransfer(
            sourceAccount, destinationAccount,
            amount, sourceCurrency, targetCurrency, exchangeRate
        );

        return buildResponse(result, "Transfert International");
    }

    /**
     * Crée une transaction personnalisée en choisissant les étapes.
     */
    @PostMapping("/custom")
    public ResponseEntity<Map<String, Object>> executeCustomTransaction(
            @RequestParam TransactionType type,
            @RequestParam String sourceAccount,
            @RequestParam(required = false) String destinationAccount,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "false") boolean withVerification,
            @RequestParam(defaultValue = "false") boolean withFraudCheck,
            @RequestParam(defaultValue = "false") boolean withLogging,
            @RequestParam(defaultValue = "false") boolean withNotification,
            @RequestParam(defaultValue = "0") double commissionPercentage) {

        List<Commission> commissions = null;
        if (commissionPercentage > 0) {
            commissions = List.of(
                Commission.builder("Commission personnalisée")
                    .percentage(commissionPercentage)
                    .build()
            );
        }

        Transaction.TransactionResult result = transactionService.executeCustomTransaction(
            type, sourceAccount, destinationAccount, amount,
            withVerification, withFraudCheck, withLogging, withNotification,
            commissions
        );

        return buildResponse(result, "Transaction Personnalisée");
    }

    /**
     * Retourne l'historique des transactions.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTransactionHistory() {
        List<Map<String, Object>> history = transactionService.getTransactionHistory()
            .stream()
            .map(this::transactionToMap)
            .collect(Collectors.toList());

        return ResponseEntity.ok(history);
    }

    /**
     * Retourne le détail d'une transaction.
     */
    @GetMapping("/{reference}")
    public ResponseEntity<Map<String, Object>> getTransaction(@PathVariable String reference) {
        Transaction transaction = transactionService.findByReference(reference);

        if (transaction == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Transaction non trouvée");
            error.put("reference", reference);
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = transactionToMap(transaction);
        response.put("logs", transaction.getLogs());

        return ResponseEntity.ok(response);
    }

    /**
     * Compare les deux variantes (courte vs complète) pour un même montant.
     */
    @GetMapping("/compare-variants")
    public ResponseEntity<Map<String, Object>> compareVariants(
            @RequestParam(defaultValue = "100000") BigDecimal amount) {

        // Simuler les deux variantes (sans exécuter)
        Transaction quickTx = transactionService.getDirector()
            .buildQuickTransaction("ACC001", "ACC002", amount);

        Transaction fullTx = transactionService.getDirector()
            .buildFullTransaction("ACC001", "ACC002", amount);

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("amount", amount);

        Map<String, Object> quick = new HashMap<>();
        quick.put("variant", "Transaction Courte");
        quick.put("totalCommissions", quickTx.getTotalCommissions());
        quick.put("finalAmount", quickTx.getFinalAmount());
        quick.put("steps", getStepsList(quickTx));
        comparison.put("quick", quick);

        Map<String, Object> full = new HashMap<>();
        full.put("variant", "Transaction Complète");
        full.put("totalCommissions", fullTx.getTotalCommissions());
        full.put("finalAmount", fullTx.getFinalAmount());
        full.put("steps", getStepsList(fullTx));
        comparison.put("full", full);

        comparison.put("commissionDifference",
            fullTx.getTotalCommissions().subtract(quickTx.getTotalCommissions()));

        return ResponseEntity.ok(comparison);
    }

    // ==================== METHODES UTILITAIRES ====================

    private ResponseEntity<Map<String, Object>> buildResponse(
            Transaction.TransactionResult result, String variant) {

        Map<String, Object> response = new HashMap<>();
        response.put("variant", variant);
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());

        Transaction tx = result.getTransaction();
        response.put("reference", tx.getReference());
        response.put("type", tx.getType().getDescription());
        response.put("amount", tx.getAmount());
        response.put("currency", tx.getCurrency());
        response.put("totalCommissions", tx.getTotalCommissions());
        response.put("finalAmount", tx.getFinalAmount());
        response.put("status", tx.getStatus().getDescription());
        response.put("steps", getStepsList(tx));
        response.put("executionLogs", result.getExecutionLogs());

        if (tx.isCurrencyConversionEnabled()) {
            response.put("convertedAmount", tx.getConvertedAmount());
            response.put("targetCurrency", tx.getTargetCurrency());
            response.put("exchangeRate", tx.getExchangeRate());
        }

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> transactionToMap(Transaction tx) {
        Map<String, Object> map = new HashMap<>();
        map.put("reference", tx.getReference());
        map.put("type", tx.getType().getDescription());
        map.put("sourceAccount", tx.getSourceAccount());
        map.put("destinationAccount", tx.getDestinationAccount());
        map.put("amount", tx.getAmount());
        map.put("currency", tx.getCurrency());
        map.put("totalCommissions", tx.getTotalCommissions());
        map.put("finalAmount", tx.getFinalAmount());
        map.put("status", tx.getStatus().getDescription());
        map.put("createdAt", tx.getCreatedAt());
        map.put("steps", getStepsList(tx));
        return map;
    }

    private List<String> getStepsList(Transaction tx) {
        List<String> steps = new java.util.ArrayList<>();
        if (tx.isVerificationEnabled()) steps.add("Vérification");
        if (tx.isFraudCheckEnabled()) steps.add("Anti-fraude");
        if (tx.isCurrencyConversionEnabled()) steps.add("Conversion");
        if (!tx.getCommissions().isEmpty()) {
            steps.add("Commissions(" + tx.getCommissions().size() + ")");
        }
        if (tx.isLoggingEnabled()) steps.add("Logging");
        if (tx.isNotificationEnabled()) steps.add("Notification");
        return steps;
    }
}
