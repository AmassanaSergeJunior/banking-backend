package com.banque.factory.operator.impl.bank;

import com.banque.factory.operator.products.RateCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PRODUIT CONCRET: Calculateur de taux pour Banque traditionnelle
 *
 * Caractéristiques:
 * - Frais fixes + pourcentage
 * - Taux préférentiels pour gros montants
 * - Commission de retrait faible
 * - Taux d'intérêt épargne attractif
 */
public class BankRateCalculator implements RateCalculator {

    private static final String OPERATOR_NAME = "Banque Traditionnelle";

    // Frais de base
    private static final BigDecimal BASE_FEE = new BigDecimal("500");  // 500 FCFA
    private static final BigDecimal PERCENTAGE_RATE = new BigDecimal("0.01");  // 1%
    private static final BigDecimal INTER_OPERATOR_RATE = new BigDecimal("0.015");  // 1.5%
    private static final BigDecimal WITHDRAWAL_RATE = new BigDecimal("0.005");  // 0.5%

    // Taux d'intérêt épargne
    private static final BigDecimal SAVINGS_RATE_LOW = new BigDecimal("0.025");  // 2.5%
    private static final BigDecimal SAVINGS_RATE_MEDIUM = new BigDecimal("0.035");  // 3.5%
    private static final BigDecimal SAVINGS_RATE_HIGH = new BigDecimal("0.045");  // 4.5%

    // Seuils pour taux préférentiels
    private static final BigDecimal MEDIUM_THRESHOLD = new BigDecimal("500000");
    private static final BigDecimal HIGH_THRESHOLD = new BigDecimal("2000000");

    @Override
    public BigDecimal calculateTransactionFee(BigDecimal amount, String transactionType) {
        // Frais = BASE_FEE + (amount * PERCENTAGE_RATE)
        BigDecimal percentageFee = amount.multiply(PERCENTAGE_RATE);

        // Réduction pour gros montants
        if (amount.compareTo(HIGH_THRESHOLD) >= 0) {
            percentageFee = percentageFee.multiply(new BigDecimal("0.5")); // 50% de réduction
        } else if (amount.compareTo(MEDIUM_THRESHOLD) >= 0) {
            percentageFee = percentageFee.multiply(new BigDecimal("0.75")); // 25% de réduction
        }

        return BASE_FEE.add(percentageFee).setScale(0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculateInterOperatorFee(BigDecimal amount, String destinationOperator) {
        // Frais inter-opérateurs plus élevés
        BigDecimal fee = amount.multiply(INTER_OPERATOR_RATE);

        // Frais supplémentaires si destination Mobile Money
        if ("MOBILE_MONEY".equalsIgnoreCase(destinationOperator)) {
            fee = fee.add(new BigDecimal("1000")); // 1000 FCFA supplémentaires
        }

        return fee.setScale(0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculateSavingsInterestRate(BigDecimal accountBalance) {
        // Taux progressif selon le solde
        if (accountBalance.compareTo(HIGH_THRESHOLD) >= 0) {
            return SAVINGS_RATE_HIGH;
        } else if (accountBalance.compareTo(MEDIUM_THRESHOLD) >= 0) {
            return SAVINGS_RATE_MEDIUM;
        }
        return SAVINGS_RATE_LOW;
    }

    @Override
    public BigDecimal calculateWithdrawalCommission(BigDecimal amount) {
        return amount.multiply(WITHDRAWAL_RATE).setScale(0, RoundingMode.CEILING);
    }

    @Override
    public String getOperatorName() {
        return OPERATOR_NAME;
    }

    @Override
    public BigDecimal getBaseRate() {
        return PERCENTAGE_RATE;
    }
}
