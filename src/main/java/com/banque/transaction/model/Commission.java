package com.banque.transaction.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Représente une commission applicable à une transaction.
 *
 * Une commission peut être:
 * - Un montant fixe
 * - Un pourcentage du montant
 * - Une combinaison des deux (fixe + pourcentage)
 */
public class Commission {

    private final String name;
    private final String description;
    private final BigDecimal fixedAmount;
    private final BigDecimal percentage;
    private final BigDecimal minimumAmount;
    private final BigDecimal maximumAmount;

    private Commission(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.fixedAmount = builder.fixedAmount;
        this.percentage = builder.percentage;
        this.minimumAmount = builder.minimumAmount;
        this.maximumAmount = builder.maximumAmount;
    }

    /**
     * Calcule le montant de la commission pour un montant de transaction donné.
     */
    public BigDecimal calculate(BigDecimal transactionAmount) {
        BigDecimal commission = BigDecimal.ZERO;

        // Ajouter le montant fixe
        if (fixedAmount != null) {
            commission = commission.add(fixedAmount);
        }

        // Ajouter le pourcentage
        if (percentage != null && transactionAmount != null) {
            BigDecimal percentageAmount = transactionAmount
                .multiply(percentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            commission = commission.add(percentageAmount);
        }

        // Appliquer le minimum
        if (minimumAmount != null && commission.compareTo(minimumAmount) < 0) {
            commission = minimumAmount;
        }

        // Appliquer le maximum
        if (maximumAmount != null && commission.compareTo(maximumAmount) > 0) {
            commission = maximumAmount;
        }

        return commission.setScale(0, RoundingMode.CEILING);
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getFixedAmount() { return fixedAmount; }
    public BigDecimal getPercentage() { return percentage; }
    public BigDecimal getMinimumAmount() { return minimumAmount; }
    public BigDecimal getMaximumAmount() { return maximumAmount; }

    /**
     * Retourne une description de la formule de commission.
     */
    public String getFormula() {
        StringBuilder formula = new StringBuilder();
        if (fixedAmount != null && fixedAmount.compareTo(BigDecimal.ZERO) > 0) {
            formula.append(fixedAmount).append(" FCFA");
        }
        if (percentage != null && percentage.compareTo(BigDecimal.ZERO) > 0) {
            if (formula.length() > 0) formula.append(" + ");
            formula.append(percentage).append("%");
        }
        if (minimumAmount != null) {
            formula.append(" (min: ").append(minimumAmount).append(")");
        }
        if (maximumAmount != null) {
            formula.append(" (max: ").append(maximumAmount).append(")");
        }
        return formula.toString();
    }

    // ==================== BUILDER ====================

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String description;
        private BigDecimal fixedAmount;
        private BigDecimal percentage;
        private BigDecimal minimumAmount;
        private BigDecimal maximumAmount;

        public Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder fixedAmount(BigDecimal amount) {
            this.fixedAmount = amount;
            return this;
        }

        public Builder fixedAmount(double amount) {
            this.fixedAmount = BigDecimal.valueOf(amount);
            return this;
        }

        public Builder percentage(BigDecimal percentage) {
            this.percentage = percentage;
            return this;
        }

        public Builder percentage(double percentage) {
            this.percentage = BigDecimal.valueOf(percentage);
            return this;
        }

        public Builder minimum(BigDecimal minimum) {
            this.minimumAmount = minimum;
            return this;
        }

        public Builder minimum(double minimum) {
            this.minimumAmount = BigDecimal.valueOf(minimum);
            return this;
        }

        public Builder maximum(BigDecimal maximum) {
            this.maximumAmount = maximum;
            return this;
        }

        public Builder maximum(double maximum) {
            this.maximumAmount = BigDecimal.valueOf(maximum);
            return this;
        }

        public Commission build() {
            return new Commission(this);
        }
    }

    // ==================== COMMISSIONS PREDEFINIES ====================

    /**
     * Commission standard de transfert (1%)
     */
    public static Commission transferFee() {
        return builder("Frais de transfert")
            .description("Commission standard sur les transferts")
            .percentage(1.0)
            .minimum(100)
            .build();
    }

    /**
     * Commission inter-opérateurs (1.5% + 500 fixe)
     */
    public static Commission interOperatorFee() {
        return builder("Frais inter-opérateurs")
            .description("Commission pour transferts entre opérateurs différents")
            .fixedAmount(500)
            .percentage(1.5)
            .minimum(1000)
            .build();
    }

    /**
     * Commission internationale (2% + 2000 fixe)
     */
    public static Commission internationalFee() {
        return builder("Frais internationaux")
            .description("Commission pour transferts internationaux")
            .fixedAmount(2000)
            .percentage(2.0)
            .minimum(3000)
            .maximum(50000)
            .build();
    }

    /**
     * Frais de service (montant fixe)
     */
    public static Commission serviceFee(double amount) {
        return builder("Frais de service")
            .description("Frais de service fixe")
            .fixedAmount(amount)
            .build();
    }

    /**
     * TVA (19.25% sur les commissions)
     */
    public static Commission vat() {
        return builder("TVA")
            .description("Taxe sur la valeur ajoutée")
            .percentage(19.25)
            .build();
    }
}
