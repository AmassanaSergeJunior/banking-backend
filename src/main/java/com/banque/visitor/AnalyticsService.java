package com.banque.visitor;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

/**
 * PATTERN VISITOR - Service d'Analytics
 *
 * OBJECTIF 9: Service qui utilise les visiteurs pour analyser
 * les transactions et generer des rapports.
 */
@Service
public class AnalyticsService {

    private final List<TransactionElement> transactions;

    public AnalyticsService() {
        this.transactions = new ArrayList<>();
    }

    // ==================== GESTION DES TRANSACTIONS ====================

    public void addTransaction(TransactionElement transaction) {
        transactions.add(transaction);
    }

    public void addAllTransactions(List<TransactionElement> txList) {
        transactions.addAll(txList);
    }

    public List<TransactionElement> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public void clearTransactions() {
        transactions.clear();
    }

    // ==================== FACTORY METHODS ====================

    public DepotElement createDeposit(String account, BigDecimal amount, String source) {
        DepotElement depot = new DepotElement(account, amount, source);
        transactions.add(depot);
        return depot;
    }

    public RetraitElement createWithdrawal(String account, BigDecimal amount, String channel) {
        RetraitElement retrait = new RetraitElement(account, amount, channel);
        transactions.add(retrait);
        return retrait;
    }

    public TransfertElement createTransfer(String from, String to, BigDecimal amount, BigDecimal fees) {
        TransfertElement transfert = new TransfertElement(from, to, amount, fees);
        transactions.add(transfert);
        return transfert;
    }

    public PaiementElement createPayment(String account, BigDecimal amount,
                                          String merchant, String category) {
        PaiementElement paiement = new PaiementElement(account, amount, merchant, category);
        transactions.add(paiement);
        return paiement;
    }

    public FraisElement createFee(String account, BigDecimal amount,
                                   String feeType, String description) {
        FraisElement frais = new FraisElement(account, amount, feeType, description);
        transactions.add(frais);
        return frais;
    }

    public RemboursementElement createRefund(String account, BigDecimal amount,
                                              String originalTxId, String reason) {
        RemboursementElement remboursement = new RemboursementElement(
            account, amount, originalTxId, reason);
        transactions.add(remboursement);
        return remboursement;
    }

    // ==================== ANALYTICS ====================

    /**
     * Execute un visiteur sur toutes les transactions.
     */
    public void analyzeWithVisitor(TransactionVisitor visitor) {
        visitor.reset();
        for (TransactionElement tx : transactions) {
            tx.accept(visitor);
        }
    }

    /**
     * Genere des statistiques.
     */
    public StatisticsVisitor generateStatistics() {
        StatisticsVisitor visitor = new StatisticsVisitor();
        analyzeWithVisitor(visitor);
        return visitor;
    }

    /**
     * Genere un rapport fiscal.
     */
    public TaxVisitor generateTaxReport() {
        TaxVisitor visitor = new TaxVisitor();
        analyzeWithVisitor(visitor);
        return visitor;
    }

    /**
     * Genere un rapport d'audit.
     */
    public AuditVisitor generateAuditReport() {
        AuditVisitor visitor = new AuditVisitor();
        analyzeWithVisitor(visitor);
        return visitor;
    }

    /**
     * Execute plusieurs visiteurs en une seule passe.
     */
    public Map<String, TransactionVisitor> analyzeAll() {
        StatisticsVisitor stats = new StatisticsVisitor();
        TaxVisitor tax = new TaxVisitor();
        AuditVisitor audit = new AuditVisitor();

        for (TransactionElement tx : transactions) {
            tx.accept(stats);
            tx.accept(tax);
            tx.accept(audit);
        }

        Map<String, TransactionVisitor> results = new LinkedHashMap<>();
        results.put("statistics", stats);
        results.put("tax", tax);
        results.put("audit", audit);
        return results;
    }

    /**
     * Genere un rapport complet.
     */
    public String generateFullReport() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("################################################################\n");
        sb.append("#                 RAPPORT COMPLET D'ANALYSE                   #\n");
        sb.append("#                    Pattern Visitor                          #\n");
        sb.append("################################################################\n");

        StatisticsVisitor stats = generateStatistics();
        sb.append(stats.generateReport());

        TaxVisitor tax = generateTaxReport();
        sb.append(tax.generateTaxReport());

        AuditVisitor audit = generateAuditReport();
        sb.append(audit.generateAuditReport());

        sb.append("################################################################\n");
        sb.append("#                    FIN DU RAPPORT                           #\n");
        sb.append("################################################################\n\n");

        return sb.toString();
    }
}
