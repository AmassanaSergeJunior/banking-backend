package com.banque.visitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN VISITOR - ConcreteVisitor (Audit)
 *
 * OBJECTIF 9: Visiteur qui genere des logs d'audit pour la conformite.
 * Enregistre toutes les transactions avec horodatage.
 */
public class AuditVisitor implements TransactionVisitor {

    private List<AuditEntry> auditLog;
    private int alertCount;
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("1000000");
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public AuditVisitor() {
        reset();
    }

    @Override
    public void reset() {
        auditLog = new ArrayList<>();
        alertCount = 0;
    }

    @Override
    public void visit(DepotElement depot) {
        AuditLevel level = AuditLevel.INFO;
        String details = "Source: " + depot.getSource();

        if (depot.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            level = AuditLevel.WARNING;
            details += " [DEPOT IMPORTANT]";
            alertCount++;
        }

        addEntry(depot.getId(), "DEPOT", depot.getAccountNumber(),
            depot.getAmount(), level, details);
    }

    @Override
    public void visit(RetraitElement retrait) {
        AuditLevel level = AuditLevel.INFO;
        String details = "Canal: " + retrait.getChannel();

        if (retrait.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            level = AuditLevel.WARNING;
            details += " [RETRAIT IMPORTANT]";
            alertCount++;
        }

        // Alerter sur les retraits ATM de nuit (simulation)
        int hour = retrait.getTimestamp().getHour();
        if (hour >= 23 || hour < 6) {
            level = AuditLevel.ALERT;
            details += " [HEURE INHABITUELLE]";
            alertCount++;
        }

        addEntry(retrait.getId(), "RETRAIT", retrait.getAccountNumber(),
            retrait.getAmount(), level, details);
    }

    @Override
    public void visit(TransfertElement transfert) {
        AuditLevel level = AuditLevel.INFO;
        String details = "Vers: " + transfert.getTargetAccount() +
            ", Frais: " + transfert.getFees() + " FCFA";

        if (transfert.getAmount().compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            level = AuditLevel.WARNING;
            details += " [TRANSFERT IMPORTANT]";
            alertCount++;
        }

        // Alerter sur les transferts avec frais eleves
        if (transfert.getFees().compareTo(new BigDecimal("10000")) > 0) {
            level = AuditLevel.WARNING;
            details += " [FRAIS ELEVES]";
        }

        addEntry(transfert.getId(), "TRANSFERT", transfert.getSourceAccount(),
            transfert.getAmount(), level, details);
    }

    @Override
    public void visit(PaiementElement paiement) {
        AuditLevel level = AuditLevel.INFO;
        String details = "Marchand: " + paiement.getMerchant() +
            ", Categorie: " + paiement.getCategory();

        // Alerter sur certaines categories
        String category = paiement.getCategory().toLowerCase();
        if (category.contains("casino") || category.contains("jeu")) {
            level = AuditLevel.ALERT;
            details += " [CATEGORIE SENSIBLE]";
            alertCount++;
        }

        addEntry(paiement.getId(), "PAIEMENT", paiement.getAccountNumber(),
            paiement.getAmount(), level, details);
    }

    @Override
    public void visit(FraisElement frais) {
        addEntry(frais.getId(), "FRAIS", frais.getAccountNumber(),
            frais.getAmount(), AuditLevel.INFO,
            "Type: " + frais.getFeeType() + ", " + frais.getDescription());
    }

    @Override
    public void visit(RemboursementElement remboursement) {
        AuditLevel level = AuditLevel.INFO;
        String details = "Transaction originale: " + remboursement.getOriginalTransactionId() +
            ", Raison: " + remboursement.getReason();

        // Les remboursements sont toujours a surveiller
        if (remboursement.getAmount().compareTo(new BigDecimal("50000")) > 0) {
            level = AuditLevel.WARNING;
            details += " [REMBOURSEMENT IMPORTANT]";
            alertCount++;
        }

        addEntry(remboursement.getId(), "REMBOURSEMENT", remboursement.getAccountNumber(),
            remboursement.getAmount(), level, details);
    }

    @Override
    public String getVisitorName() {
        return "AuditVisitor";
    }

    private void addEntry(String txId, String type, String account,
                          BigDecimal amount, AuditLevel level, String details) {
        AuditEntry entry = new AuditEntry(txId, type, account, amount, level, details);
        auditLog.add(entry);

        String levelPrefix = level == AuditLevel.ALERT ? "[!!!] " :
                            level == AuditLevel.WARNING ? "[!] " : "";
        System.out.println("[AUDIT] " + levelPrefix + type + " " + txId +
            " - " + amount + " FCFA");
    }

    // ==================== RESULTATS ====================

    public List<AuditEntry> getAuditLog() {
        return new ArrayList<>(auditLog);
    }

    public List<AuditEntry> getAlerts() {
        List<AuditEntry> alerts = new ArrayList<>();
        for (AuditEntry entry : auditLog) {
            if (entry.getLevel() == AuditLevel.WARNING ||
                entry.getLevel() == AuditLevel.ALERT) {
                alerts.add(entry);
            }
        }
        return alerts;
    }

    public int getAlertCount() {
        return alertCount;
    }

    public int getTotalEntries() {
        return auditLog.size();
    }

    public String generateAuditReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("      RAPPORT D'AUDIT\n");
        sb.append("========================================\n\n");

        sb.append("RESUME:\n");
        sb.append("  Transactions auditees: ").append(auditLog.size()).append("\n");
        sb.append("  Alertes generees: ").append(alertCount).append("\n\n");

        sb.append("JOURNAL D'AUDIT:\n");
        sb.append("-".repeat(80)).append("\n");

        for (AuditEntry entry : auditLog) {
            String levelMark = entry.getLevel() == AuditLevel.ALERT ? "[ALERTE]" :
                              entry.getLevel() == AuditLevel.WARNING ? "[ATTENTION]" : "[INFO]";

            sb.append(entry.getTimestamp().format(FORMATTER))
              .append(" | ").append(levelMark)
              .append(" | ").append(entry.getType())
              .append(" | ").append(entry.getTransactionId())
              .append(" | ").append(entry.getAmount()).append(" FCFA")
              .append("\n");
            sb.append("    ").append(entry.getDetails()).append("\n");
        }

        sb.append("-".repeat(80)).append("\n");

        if (alertCount > 0) {
            sb.append("\nALERTES A EXAMINER:\n");
            for (AuditEntry entry : getAlerts()) {
                sb.append("  - ").append(entry.getTransactionId())
                  .append(": ").append(entry.getDetails()).append("\n");
            }
        }

        sb.append("\n========================================\n");
        return sb.toString();
    }

    /**
     * Niveaux d'audit.
     */
    public enum AuditLevel {
        INFO, WARNING, ALERT
    }

    /**
     * Entree d'audit.
     */
    public static class AuditEntry {
        private final String transactionId;
        private final String type;
        private final String account;
        private final BigDecimal amount;
        private final AuditLevel level;
        private final String details;
        private final LocalDateTime timestamp;

        public AuditEntry(String txId, String type, String account,
                         BigDecimal amount, AuditLevel level, String details) {
            this.transactionId = txId;
            this.type = type;
            this.account = account;
            this.amount = amount;
            this.level = level;
            this.details = details;
            this.timestamp = LocalDateTime.now();
        }

        public String getTransactionId() { return transactionId; }
        public String getType() { return type; }
        public String getAccount() { return account; }
        public BigDecimal getAmount() { return amount; }
        public AuditLevel getLevel() { return level; }
        public String getDetails() { return details; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
