package com.banque.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PATTERN DECORATOR - ConcreteDecorator (Assurance)
 *
 * OBJECTIF 8: Ajoute une assurance au compte.
 * Protege le titulaire en cas de fraude, deces, ou perte de revenus.
 *
 * CARACTERISTIQUES:
 * - Frais d'assurance mensuel
 * - Couverture maximale configurable
 * - Differents types de couverture
 */
public class InsuranceDecorator extends AccountDecorator {

    private final InsuranceType insuranceType;
    private final BigDecimal monthlyPremium;
    private final BigDecimal coverageLimit;

    /**
     * Types d'assurance disponibles.
     */
    public enum InsuranceType {
        BASIC("Basique", new BigDecimal("500"), new BigDecimal("500000"),
              "Protection contre la fraude"),
        STANDARD("Standard", new BigDecimal("1500"), new BigDecimal("2000000"),
                 "Fraude + Deces + Invalidite"),
        PREMIUM("Premium", new BigDecimal("3000"), new BigDecimal("5000000"),
                "Couverture complete + Perte de revenus");

        private final String label;
        private final BigDecimal defaultPremium;
        private final BigDecimal defaultCoverage;
        private final String description;

        InsuranceType(String label, BigDecimal premium, BigDecimal coverage, String desc) {
            this.label = label;
            this.defaultPremium = premium;
            this.defaultCoverage = coverage;
            this.description = desc;
        }

        public String getLabel() { return label; }
        public BigDecimal getDefaultPremium() { return defaultPremium; }
        public BigDecimal getDefaultCoverage() { return defaultCoverage; }
        public String getDescription() { return description; }
    }

    /**
     * Cree un decorateur d'assurance basique.
     */
    public InsuranceDecorator(Account account) {
        this(account, InsuranceType.BASIC);
    }

    /**
     * Cree un decorateur d'assurance avec type specifie.
     */
    public InsuranceDecorator(Account account, InsuranceType insuranceType) {
        this(account, insuranceType, insuranceType.getDefaultPremium(),
             insuranceType.getDefaultCoverage());
    }

    /**
     * Cree un decorateur d'assurance personnalise.
     */
    public InsuranceDecorator(Account account, InsuranceType insuranceType,
                              BigDecimal monthlyPremium, BigDecimal coverageLimit) {
        super(account);
        this.insuranceType = insuranceType;
        this.monthlyPremium = monthlyPremium;
        this.coverageLimit = coverageLimit;
    }

    @Override
    public String getDescription() {
        return wrappedAccount.getDescription() + " + Assurance " +
               insuranceType.getLabel();
    }

    @Override
    protected String getDecoratorName() {
        return "Assurance(" + insuranceType.getLabel() + ")";
    }

    @Override
    public BigDecimal getMonthlyFees() {
        return wrappedAccount.getMonthlyFees().add(monthlyPremium);
    }

    /**
     * Retourne les details de la couverture.
     */
    public String getCoverageDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ASSURANCE ").append(insuranceType.getLabel().toUpperCase()).append(" ===\n");
        sb.append("Prime mensuelle: ").append(monthlyPremium).append(" FCFA\n");
        sb.append("Couverture maximale: ").append(coverageLimit).append(" FCFA\n");
        sb.append("Description: ").append(insuranceType.getDescription()).append("\n");

        switch (insuranceType) {
            case BASIC:
                sb.append("\nGaranties:\n");
                sb.append("  - Protection contre la fraude bancaire\n");
                sb.append("  - Remboursement des transactions non autorisees\n");
                break;
            case STANDARD:
                sb.append("\nGaranties:\n");
                sb.append("  - Protection contre la fraude bancaire\n");
                sb.append("  - Remboursement des transactions non autorisees\n");
                sb.append("  - Capital deces (100% de la couverture)\n");
                sb.append("  - Invalidite permanente totale\n");
                break;
            case PREMIUM:
                sb.append("\nGaranties:\n");
                sb.append("  - Protection contre la fraude bancaire\n");
                sb.append("  - Remboursement des transactions non autorisees\n");
                sb.append("  - Capital deces (100% de la couverture)\n");
                sb.append("  - Invalidite permanente totale\n");
                sb.append("  - Perte d'emploi (6 mois d'indemnites)\n");
                sb.append("  - Assistance juridique\n");
                sb.append("  - Rapatriement medical\n");
                break;
        }

        return sb.toString();
    }

    /**
     * Simule une reclamation d'assurance.
     *
     * @param claimType Type de reclamation
     * @param amount Montant reclame
     * @return Resultat de la reclamation
     */
    public ClaimResult fileClaim(String claimType, BigDecimal amount) {
        System.out.println("[ASSURANCE] Reclamation deposee: " + claimType +
            " - Montant: " + amount + " FCFA");

        // Verifier le montant
        if (amount.compareTo(coverageLimit) > 0) {
            BigDecimal approved = coverageLimit;
            System.out.println("[ASSURANCE] Montant ajuste au plafond: " + approved + " FCFA");
            return new ClaimResult(true, approved,
                "Reclamation approuvee (plafond applique)");
        }

        return new ClaimResult(true, amount, "Reclamation approuvee");
    }

    /**
     * Verifie si un type de sinistre est couvert.
     */
    public boolean isCovered(String claimType) {
        String type = claimType.toLowerCase();

        // Basique: seulement fraude
        if (type.contains("fraude") || type.contains("fraud")) {
            return true;
        }

        // Standard: + deces et invalidite
        if (insuranceType == InsuranceType.STANDARD || insuranceType == InsuranceType.PREMIUM) {
            if (type.contains("deces") || type.contains("death") ||
                type.contains("invalidite") || type.contains("disability")) {
                return true;
            }
        }

        // Premium: + perte d'emploi
        if (insuranceType == InsuranceType.PREMIUM) {
            if (type.contains("emploi") || type.contains("job") ||
                type.contains("chomage") || type.contains("revenu")) {
                return true;
            }
        }

        return false;
    }

    public InsuranceType getInsuranceType() {
        return insuranceType;
    }

    public BigDecimal getMonthlyPremium() {
        return monthlyPremium;
    }

    public BigDecimal getCoverageLimit() {
        return coverageLimit;
    }

    /**
     * Resultat d'une reclamation d'assurance.
     */
    public static class ClaimResult {
        private final boolean approved;
        private final BigDecimal amount;
        private final String message;

        public ClaimResult(boolean approved, BigDecimal amount, String message) {
            this.approved = approved;
            this.amount = amount;
            this.message = message;
        }

        public boolean isApproved() { return approved; }
        public BigDecimal getAmount() { return amount; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return String.format("Claim[approved=%s, amount=%.0f, msg=%s]",
                approved, amount, message);
        }
    }
}
