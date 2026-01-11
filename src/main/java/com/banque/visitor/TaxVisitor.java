package com.banque.visitor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN VISITOR - ConcreteVisitor (Fiscalite)
 *
 * OBJECTIF 9: Visiteur qui calcule les informations fiscales.
 * Identifie les transactions taxables et calcule les impots.
 */
public class TaxVisitor implements TransactionVisitor {

    private BigDecimal taxableIncome;
    private BigDecimal taxableExpenses;
    private BigDecimal totalWithholdingTax;
    private BigDecimal totalVAT;
    private List<TaxableTransaction> taxableTransactions;

    // Taux fiscaux camerounais
    private static final BigDecimal WITHHOLDING_TAX_RATE = new BigDecimal("5.5"); // 5.5%
    private static final BigDecimal VAT_RATE = new BigDecimal("19.25"); // 19.25%
    private static final BigDecimal INTEREST_TAX_RATE = new BigDecimal("16.5"); // 16.5%

    public TaxVisitor() {
        reset();
    }

    @Override
    public void reset() {
        taxableIncome = BigDecimal.ZERO;
        taxableExpenses = BigDecimal.ZERO;
        totalWithholdingTax = BigDecimal.ZERO;
        totalVAT = BigDecimal.ZERO;
        taxableTransactions = new ArrayList<>();
    }

    @Override
    public void visit(DepotElement depot) {
        // Les depots ne sont generalement pas taxables
        // Sauf s'ils representent des revenus
        if (depot.getSource() != null && depot.getSource().toLowerCase().contains("salaire")) {
            taxableIncome = taxableIncome.add(depot.getAmount());
            taxableTransactions.add(new TaxableTransaction(
                depot.getId(), "Revenu", depot.getAmount(), BigDecimal.ZERO));
        }
        System.out.println("[FISC] Depot analyse: " + depot.getSource());
    }

    @Override
    public void visit(RetraitElement retrait) {
        // Les retraits ne sont pas taxables
        System.out.println("[FISC] Retrait - non taxable");
    }

    @Override
    public void visit(TransfertElement transfert) {
        // Les frais de transfert peuvent inclure la TVA
        if (transfert.getFees().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal vat = calculateVAT(transfert.getFees());
            totalVAT = totalVAT.add(vat);

            taxableTransactions.add(new TaxableTransaction(
                transfert.getId(), "TVA sur frais transfert", transfert.getFees(), vat));
        }
        System.out.println("[FISC] Transfert - frais: " + transfert.getFees() + " FCFA");
    }

    @Override
    public void visit(PaiementElement paiement) {
        // Les paiements peuvent etre des charges deductibles
        if (isDeductibleExpense(paiement.getCategory())) {
            taxableExpenses = taxableExpenses.add(paiement.getAmount());
            taxableTransactions.add(new TaxableTransaction(
                paiement.getId(), "Charge deductible", paiement.getAmount(), BigDecimal.ZERO));
        }

        // Calcul de la TVA sur certains paiements
        if (isVATApplicable(paiement.getCategory())) {
            BigDecimal vat = calculateVAT(paiement.getAmount());
            totalVAT = totalVAT.add(vat);
        }

        System.out.println("[FISC] Paiement analyse: " + paiement.getCategory());
    }

    @Override
    public void visit(FraisElement frais) {
        // Les frais bancaires incluent la TVA
        BigDecimal vat = calculateVAT(frais.getAmount());
        totalVAT = totalVAT.add(vat);

        // Les frais bancaires sont des charges deductibles
        taxableExpenses = taxableExpenses.add(frais.getAmount());

        taxableTransactions.add(new TaxableTransaction(
            frais.getId(), "Frais bancaires (TVA incluse)", frais.getAmount(), vat));

        System.out.println("[FISC] Frais bancaires: " + frais.getAmount() + " FCFA, TVA: " + vat);
    }

    @Override
    public void visit(RemboursementElement remboursement) {
        // Les remboursements peuvent reduire le revenu imposable
        if (remboursement.getReason() != null &&
            remboursement.getReason().toLowerCase().contains("trop-percu")) {
            taxableIncome = taxableIncome.subtract(remboursement.getAmount());
        }
        System.out.println("[FISC] Remboursement: " + remboursement.getReason());
    }

    @Override
    public String getVisitorName() {
        return "TaxVisitor";
    }

    // ==================== CALCULS FISCAUX ====================

    private BigDecimal calculateVAT(BigDecimal amount) {
        return amount.multiply(VAT_RATE)
            .divide(new BigDecimal("119.25"), 0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateWithholdingTax(BigDecimal amount) {
        return amount.multiply(WITHHOLDING_TAX_RATE)
            .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
    }

    private boolean isDeductibleExpense(String category) {
        if (category == null) return false;
        String cat = category.toLowerCase();
        return cat.contains("professionnel") || cat.contains("business") ||
               cat.contains("transport") || cat.contains("materiel");
    }

    private boolean isVATApplicable(String category) {
        if (category == null) return false;
        String cat = category.toLowerCase();
        return !cat.contains("exonere") && !cat.contains("sante") && !cat.contains("education");
    }

    // ==================== RESULTATS ====================

    public BigDecimal getTaxableIncome() {
        return taxableIncome;
    }

    public BigDecimal getTaxableExpenses() {
        return taxableExpenses;
    }

    public BigDecimal getNetTaxableAmount() {
        return taxableIncome.subtract(taxableExpenses);
    }

    public BigDecimal getTotalVAT() {
        return totalVAT;
    }

    public BigDecimal getTotalWithholdingTax() {
        return totalWithholdingTax;
    }

    public List<TaxableTransaction> getTaxableTransactions() {
        return new ArrayList<>(taxableTransactions);
    }

    public String generateTaxReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("      RAPPORT FISCAL\n");
        sb.append("========================================\n\n");

        sb.append("RESUME FISCAL:\n");
        sb.append("  Revenus imposables: ").append(taxableIncome).append(" FCFA\n");
        sb.append("  Charges deductibles: ").append(taxableExpenses).append(" FCFA\n");
        sb.append("  Base imposable nette: ").append(getNetTaxableAmount()).append(" FCFA\n");
        sb.append("  TVA collectee: ").append(totalVAT).append(" FCFA\n");

        if (!taxableTransactions.isEmpty()) {
            sb.append("\nTRANSACTIONS FISCALES:\n");
            for (TaxableTransaction tx : taxableTransactions) {
                sb.append("  - ").append(tx.type).append(": ")
                  .append(tx.amount).append(" FCFA");
                if (tx.tax.compareTo(BigDecimal.ZERO) > 0) {
                    sb.append(" (taxe: ").append(tx.tax).append(")");
                }
                sb.append("\n");
            }
        }

        sb.append("\n========================================\n");
        return sb.toString();
    }

    /**
     * Transaction fiscale.
     */
    public static class TaxableTransaction {
        private final String transactionId;
        private final String type;
        private final BigDecimal amount;
        private final BigDecimal tax;

        public TaxableTransaction(String id, String type, BigDecimal amount, BigDecimal tax) {
            this.transactionId = id;
            this.type = type;
            this.amount = amount;
            this.tax = tax;
        }

        public String getTransactionId() { return transactionId; }
        public String getType() { return type; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getTax() { return tax; }
    }
}
