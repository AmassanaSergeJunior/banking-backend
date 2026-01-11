package com.banque.template;

import com.banque.template.TransactionProcessor.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONTROLEUR REST - Processeurs de Transactions
 *
 * OBJECTIF 6: Ce controller expose les fonctionnalites de traitement
 * de transactions via REST API, demontrant le pattern Template Method.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionProcessorController {

    private final TransactionProcessorService processorService;

    public TransactionProcessorController(TransactionProcessorService processorService) {
        this.processorService = processorService;
    }

    // ==================== OPERATIONS ====================

    /**
     * Effectue un depot.
     */
    @PostMapping("/{operatorType}/deposit")
    public ResponseEntity<Map<String, Object>> deposit(
            @PathVariable String operatorType,
            @RequestParam String account,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "Depot") String description) {

        try {
            TransactionResult result = processorService.deposit(
                operatorType, account, amount, description
            );
            return ResponseEntity.ok(formatResult(result, operatorType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Effectue un retrait.
     */
    @PostMapping("/{operatorType}/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @PathVariable String operatorType,
            @RequestParam String account,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "Retrait") String description) {

        try {
            TransactionResult result = processorService.withdraw(
                operatorType, account, amount, description
            );
            return ResponseEntity.ok(formatResult(result, operatorType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Effectue un transfert.
     */
    @PostMapping("/{operatorType}/transfer")
    public ResponseEntity<Map<String, Object>> transfer(
            @PathVariable String operatorType,
            @RequestParam String sourceAccount,
            @RequestParam String destAccount,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "Transfert") String description) {

        try {
            TransactionResult result = processorService.transfer(
                operatorType, sourceAccount, destAccount, amount, description
            );
            return ResponseEntity.ok(formatResult(result, operatorType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Transaction generique.
     */
    @PostMapping("/{operatorType}/process")
    public ResponseEntity<Map<String, Object>> processTransaction(
            @PathVariable String operatorType,
            @RequestBody TransactionRequestDTO request) {

        try {
            TransactionRequest txRequest = new TransactionRequest(
                request.getSourceAccount(),
                request.getDestinationAccount(),
                request.getAmount(),
                TransactionType.valueOf(request.getType().toUpperCase()),
                request.getCurrency() != null ? request.getCurrency() : "XAF",
                request.getDescription()
            );

            TransactionResult result = processorService.processTransaction(operatorType, txRequest);
            return ResponseEntity.ok(formatResult(result, operatorType));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ==================== STATISTIQUES ====================

    /**
     * Retourne les statistiques de tous les processeurs.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAllStats() {
        Map<String, ProcessorStats> stats = processorService.getAllStats();

        Map<String, Object> response = new HashMap<>();
        for (Map.Entry<String, ProcessorStats> entry : stats.entrySet()) {
            ProcessorStats s = entry.getValue();
            response.put(entry.getKey(), Map.of(
                "operatorType", s.getOperatorType(),
                "totalTransactions", s.getTotalTransactions(),
                "successful", s.getSuccessfulTransactions(),
                "failed", s.getFailedTransactions(),
                "successRate", String.format("%.2f%%", s.getSuccessRate()),
                "totalAmount", s.getTotalAmountProcessed(),
                "totalFees", s.getTotalFeesCollected()
            ));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Retourne les statistiques d'un processeur.
     */
    @GetMapping("/{operatorType}/stats")
    public ResponseEntity<Map<String, Object>> getProcessorStats(@PathVariable String operatorType) {
        TransactionProcessor processor = processorService.getProcessor(operatorType);
        if (processor == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Operateur inconnu: " + operatorType));
        }

        ProcessorStats stats = processor.getStats();
        Map<String, Object> response = new HashMap<>();
        response.put("operatorType", stats.getOperatorType());
        response.put("totalTransactions", stats.getTotalTransactions());
        response.put("successful", stats.getSuccessfulTransactions());
        response.put("failed", stats.getFailedTransactions());
        response.put("successRate", String.format("%.2f%%", stats.getSuccessRate()));
        response.put("totalAmount", stats.getTotalAmountProcessed());
        response.put("totalFees", stats.getTotalFeesCollected());

        return ResponseEntity.ok(response);
    }

    /**
     * Retourne l'historique des transactions d'un processeur.
     */
    @GetMapping("/{operatorType}/history")
    public ResponseEntity<List<TransactionRecord>> getHistory(@PathVariable String operatorType) {
        TransactionProcessor processor = processorService.getProcessor(operatorType);
        if (processor == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(processor.getProcessedTransactions());
    }

    /**
     * Retourne les types d'operateurs disponibles.
     */
    @GetMapping("/operators")
    public ResponseEntity<Map<String, Object>> getOperators() {
        String[] operators = processorService.getAvailableOperatorTypes();

        Map<String, Object> response = new HashMap<>();
        response.put("operators", operators);

        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("BANK", "Banque traditionnelle - Frais fixe + %, limites elevees");
        descriptions.put("MOBILE_MONEY", "Mobile Money - Frais par paliers, traitement instantane");
        descriptions.put("MICROFINANCE", "Microfinance - Frais tres bas, inclusion financiere");
        response.put("descriptions", descriptions);

        return ResponseEntity.ok(response);
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * DTO pour les requetes de transaction.
     */
    public static class TransactionRequestDTO {
        private String sourceAccount;
        private String destinationAccount;
        private BigDecimal amount;
        private String type;
        private String currency;
        private String description;

        public String getSourceAccount() { return sourceAccount; }
        public void setSourceAccount(String sourceAccount) { this.sourceAccount = sourceAccount; }
        public String getDestinationAccount() { return destinationAccount; }
        public void setDestinationAccount(String dest) { this.destinationAccount = dest; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // ==================== METHODES UTILITAIRES ====================

    private Map<String, Object> formatResult(TransactionResult result, String operatorType) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("transactionId", result.getTransactionId());
        response.put("operatorType", operatorType);

        if (result.isSuccess()) {
            response.put("reference", result.getReference());
            response.put("amount", result.getAmount());
            response.put("fees", result.getFees());
            response.put("totalAmount", result.getTotalAmount());
        } else {
            response.put("error", result.getErrorMessage());
        }

        response.put("processingLogs", result.getProcessingLogs());

        return response;
    }
}
