package com.banque.visitor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * PATTERN VISITOR - Controller REST
 *
 * OBJECTIF 9: Expose les fonctionnalites d'analytics via API REST.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        StatisticsVisitor stats = analyticsService.generateStatistics();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTransactions", stats.getTotalTransactions());
        result.put("totalCredits", stats.getTotalCredits());
        result.put("totalDebits", stats.getTotalDebits());
        result.put("totalTransfers", stats.getTotalTransfers());
        result.put("totalFees", stats.getTotalFees());
        result.put("netFlow", stats.getNetFlow());
        result.put("averageTransaction", stats.getAverageTransaction());
        result.put("countByType", stats.getCountByType());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/tax")
    public ResponseEntity<Map<String, Object>> getTaxReport() {
        TaxVisitor tax = analyticsService.generateTaxReport();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taxableIncome", tax.getTaxableIncome());
        result.put("taxableExpenses", tax.getTaxableExpenses());
        result.put("netTaxableAmount", tax.getNetTaxableAmount());
        result.put("totalVAT", tax.getTotalVAT());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/audit")
    public ResponseEntity<Map<String, Object>> getAuditReport() {
        AuditVisitor audit = analyticsService.generateAuditReport();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEntries", audit.getTotalEntries());
        result.put("alertCount", audit.getAlertCount());
        result.put("alerts", audit.getAlerts());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/demo")
    public ResponseEntity<DemoResponse> demo() {
        System.out.println("\n========================================");
        System.out.println("  DEMONSTRATION PATTERN VISITOR");
        System.out.println("  Analytics et Rapports");
        System.out.println("========================================\n");

        analyticsService.clearTransactions();

        // Creer des transactions de test
        System.out.println(">>> Creation de transactions de test...\n");

        analyticsService.createDeposit("CM001", new BigDecimal("500000"), "Salaire");
        analyticsService.createDeposit("CM001", new BigDecimal("100000"), "Virement");
        analyticsService.createWithdrawal("CM001", new BigDecimal("50000"), "ATM");
        analyticsService.createWithdrawal("CM001", new BigDecimal("30000"), "Guichet");
        analyticsService.createTransfer("CM001", "CM002", new BigDecimal("200000"), new BigDecimal("1000"));
        analyticsService.createPayment("CM001", new BigDecimal("25000"), "Supermarche", "Alimentation");
        analyticsService.createPayment("CM001", new BigDecimal("15000"), "Station", "Transport");
        analyticsService.createFee("CM001", new BigDecimal("2500"), "Mensuel", "Frais de tenue");
        analyticsService.createRefund("CM001", new BigDecimal("5000"), "PAY-001", "Erreur");

        System.out.println("\n>>> Application des visiteurs...\n");

        // Generer les rapports
        String fullReport = analyticsService.generateFullReport();
        System.out.println(fullReport);

        DemoResponse response = new DemoResponse();
        response.setSuccess(true);
        response.setMessage("Demonstration complete - Pattern Visitor");
        response.setTransactionCount(analyticsService.getTransactions().size());

        StatisticsVisitor stats = analyticsService.generateStatistics();
        response.setTotalCredits(stats.getTotalCredits());
        response.setTotalDebits(stats.getTotalDebits());
        response.setNetFlow(stats.getNetFlow());

        return ResponseEntity.ok(response);
    }

    public static class DemoResponse {
        private boolean success;
        private String message;
        private int transactionCount;
        private BigDecimal totalCredits;
        private BigDecimal totalDebits;
        private BigDecimal netFlow;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getTransactionCount() { return transactionCount; }
        public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
        public BigDecimal getTotalCredits() { return totalCredits; }
        public void setTotalCredits(BigDecimal totalCredits) { this.totalCredits = totalCredits; }
        public BigDecimal getTotalDebits() { return totalDebits; }
        public void setTotalDebits(BigDecimal totalDebits) { this.totalDebits = totalDebits; }
        public BigDecimal getNetFlow() { return netFlow; }
        public void setNetFlow(BigDecimal netFlow) { this.netFlow = netFlow; }
    }
}
