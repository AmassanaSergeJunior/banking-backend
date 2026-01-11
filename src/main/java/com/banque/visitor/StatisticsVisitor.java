package com.banque.visitor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN VISITOR - ConcreteVisitor (Statistiques)
 *
 * OBJECTIF 9: Visiteur qui calcule des statistiques sur les transactions.
 * Compte les transactions par type, calcule les totaux et moyennes.
 */
public class StatisticsVisitor implements TransactionVisitor {

    private int totalTransactions;
    private BigDecimal totalCredits; // Depots + Remboursements
    private BigDecimal totalDebits;  // Retraits + Paiements + Frais
    private BigDecimal totalTransfers;
    private BigDecimal totalFees;
    private Map<TransactionElement.ElementType, Integer> countByType;
    private Map<TransactionElement.ElementType, BigDecimal> amountByType;
    private Map<String, Integer> countByChannel;
    private Map<String, BigDecimal> amountByCategory;

    public StatisticsVisitor() {
        reset();
    }

    @Override
    public void reset() {
        totalTransactions = 0;
        totalCredits = BigDecimal.ZERO;
        totalDebits = BigDecimal.ZERO;
        totalTransfers = BigDecimal.ZERO;
        totalFees = BigDecimal.ZERO;
        countByType = new HashMap<>();
        amountByType = new HashMap<>();
        countByChannel = new HashMap<>();
        amountByCategory = new HashMap<>();

        for (TransactionElement.ElementType type : TransactionElement.ElementType.values()) {
            countByType.put(type, 0);
            amountByType.put(type, BigDecimal.ZERO);
        }
    }

    @Override
    public void visit(DepotElement depot) {
        totalTransactions++;
        totalCredits = totalCredits.add(depot.getAmount());
        incrementCount(depot.getElementType());
        addAmount(depot.getElementType(), depot.getAmount());

        System.out.println("[STATS] Depot analyse: " + depot.getAmount() + " FCFA");
    }

    @Override
    public void visit(RetraitElement retrait) {
        totalTransactions++;
        totalDebits = totalDebits.add(retrait.getAmount());
        incrementCount(retrait.getElementType());
        addAmount(retrait.getElementType(), retrait.getAmount());

        // Stats par canal
        String channel = retrait.getChannel();
        countByChannel.merge(channel, 1, Integer::sum);

        System.out.println("[STATS] Retrait analyse: " + retrait.getAmount() + " FCFA via " + channel);
    }

    @Override
    public void visit(TransfertElement transfert) {
        totalTransactions++;
        totalTransfers = totalTransfers.add(transfert.getAmount());
        totalFees = totalFees.add(transfert.getFees());
        incrementCount(transfert.getElementType());
        addAmount(transfert.getElementType(), transfert.getAmount());

        System.out.println("[STATS] Transfert analyse: " + transfert.getAmount() + " FCFA");
    }

    @Override
    public void visit(PaiementElement paiement) {
        totalTransactions++;
        totalDebits = totalDebits.add(paiement.getAmount());
        incrementCount(paiement.getElementType());
        addAmount(paiement.getElementType(), paiement.getAmount());

        // Stats par categorie
        String category = paiement.getCategory();
        amountByCategory.merge(category, paiement.getAmount(), BigDecimal::add);

        System.out.println("[STATS] Paiement analyse: " + paiement.getAmount() + " FCFA (" + category + ")");
    }

    @Override
    public void visit(FraisElement frais) {
        totalTransactions++;
        totalDebits = totalDebits.add(frais.getAmount());
        totalFees = totalFees.add(frais.getAmount());
        incrementCount(frais.getElementType());
        addAmount(frais.getElementType(), frais.getAmount());

        System.out.println("[STATS] Frais analyse: " + frais.getAmount() + " FCFA");
    }

    @Override
    public void visit(RemboursementElement remboursement) {
        totalTransactions++;
        totalCredits = totalCredits.add(remboursement.getAmount());
        incrementCount(remboursement.getElementType());
        addAmount(remboursement.getElementType(), remboursement.getAmount());

        System.out.println("[STATS] Remboursement analyse: " + remboursement.getAmount() + " FCFA");
    }

    @Override
    public String getVisitorName() {
        return "StatisticsVisitor";
    }

    private void incrementCount(TransactionElement.ElementType type) {
        countByType.merge(type, 1, Integer::sum);
    }

    private void addAmount(TransactionElement.ElementType type, BigDecimal amount) {
        amountByType.merge(type, amount, BigDecimal::add);
    }

    // ==================== RESULTATS ====================

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public BigDecimal getTotalDebits() {
        return totalDebits;
    }

    public BigDecimal getTotalTransfers() {
        return totalTransfers;
    }

    public BigDecimal getTotalFees() {
        return totalFees;
    }

    public BigDecimal getNetFlow() {
        return totalCredits.subtract(totalDebits);
    }

    public BigDecimal getAverageTransaction() {
        if (totalTransactions == 0) return BigDecimal.ZERO;
        BigDecimal total = totalCredits.add(totalDebits).add(totalTransfers);
        return total.divide(new BigDecimal(totalTransactions), 0, RoundingMode.HALF_UP);
    }

    public Map<TransactionElement.ElementType, Integer> getCountByType() {
        return new HashMap<>(countByType);
    }

    public Map<TransactionElement.ElementType, BigDecimal> getAmountByType() {
        return new HashMap<>(amountByType);
    }

    public Map<String, Integer> getCountByChannel() {
        return new HashMap<>(countByChannel);
    }

    public Map<String, BigDecimal> getAmountByCategory() {
        return new HashMap<>(amountByCategory);
    }

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("      RAPPORT STATISTIQUES\n");
        sb.append("========================================\n\n");

        sb.append("RESUME GENERAL:\n");
        sb.append("  Transactions totales: ").append(totalTransactions).append("\n");
        sb.append("  Credits totaux: ").append(totalCredits).append(" FCFA\n");
        sb.append("  Debits totaux: ").append(totalDebits).append(" FCFA\n");
        sb.append("  Transferts totaux: ").append(totalTransfers).append(" FCFA\n");
        sb.append("  Frais totaux: ").append(totalFees).append(" FCFA\n");
        sb.append("  Flux net: ").append(getNetFlow()).append(" FCFA\n");
        sb.append("  Transaction moyenne: ").append(getAverageTransaction()).append(" FCFA\n");

        sb.append("\nPAR TYPE:\n");
        for (TransactionElement.ElementType type : TransactionElement.ElementType.values()) {
            int count = countByType.get(type);
            BigDecimal amount = amountByType.get(type);
            if (count > 0) {
                sb.append("  ").append(type.getLabel()).append(": ")
                  .append(count).append(" (").append(amount).append(" FCFA)\n");
            }
        }

        if (!countByChannel.isEmpty()) {
            sb.append("\nPAR CANAL:\n");
            for (Map.Entry<String, Integer> entry : countByChannel.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(": ")
                  .append(entry.getValue()).append(" transaction(s)\n");
            }
        }

        if (!amountByCategory.isEmpty()) {
            sb.append("\nPAR CATEGORIE:\n");
            for (Map.Entry<String, BigDecimal> entry : amountByCategory.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(": ")
                  .append(entry.getValue()).append(" FCFA\n");
            }
        }

        sb.append("\n========================================\n");
        return sb.toString();
    }
}
