package com.banque.factory.operator.impl.microfinance;

import com.banque.factory.operator.products.RateCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PRODUIT CONCRET: Calculateur de taux pour Microfinance
 *
 * Caractéristiques:
 * - Frais modérés pour rester accessible
 * - Taux d'intérêt épargne incitatif pour les petits épargnants
 * - Pas de frais pour les petites transactions
 * - Tarification sociale
 */
public class MicrofinanceRateCalculator implements RateCalculator {

    private static final String OPERATOR_NAME = "Microfinance";

    // Seuil en dessous duquel les transactions sont gratuites
    private static final BigDecimal FREE_THRESHOLD = new BigDecimal("10000");

    // Taux pour les transactions
    private static final BigDecimal TRANSACTION_RATE = new BigDecimal("0.008");  // 0.8%
    private static final BigDecimal INTER_OPERATOR_RATE = new BigDecimal("0.012");  // 1.2%
    private static final BigDecimal WITHDRAWAL_RATE = new BigDecimal("0.01");  // 1%

    // Taux d'intérêt épargne (incitatifs pour les petits épargnants)
    private static final BigDecimal SAVINGS_RATE_SMALL = new BigDecimal("0.04");  // 4%
    private static final BigDecimal SAVINGS_RATE_MEDIUM = new BigDecimal("0.035");  // 3.5%
    private static final BigDecimal SAVINGS_RATE_LARGE = new BigDecimal("0.03");  // 3%

    // Seuils
    private static final BigDecimal SMALL_THRESHOLD = new BigDecimal("100000");
    private static final BigDecimal MEDIUM_THRESHOLD = new BigDecimal("500000");

    @Override
    public BigDecimal calculateTransactionFee(BigDecimal amount, String transactionType) {
        // Transactions gratuites en dessous du seuil (mission sociale)
        if (amount.compareTo(FREE_THRESHOLD) <= 0) {
            return BigDecimal.ZERO;
        }

        // Frais proportionnels
        return amount.multiply(TRANSACTION_RATE).setScale(0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculateInterOperatorFee(BigDecimal amount, String destinationOperator) {
        // Frais inter-opérateurs réduits par rapport à la banque
        BigDecimal fee = amount.multiply(INTER_OPERATOR_RATE);

        // Minimum 200 FCFA pour couvrir les coûts
        BigDecimal minFee = new BigDecimal("200");
        return fee.max(minFee).setScale(0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculateSavingsInterestRate(BigDecimal accountBalance) {
        // Taux DEGRESSIF: plus incitatif pour les petits épargnants
        // (inverse de la banque - mission sociale)
        if (accountBalance.compareTo(SMALL_THRESHOLD) <= 0) {
            return SAVINGS_RATE_SMALL;  // 4% pour les petits soldes
        } else if (accountBalance.compareTo(MEDIUM_THRESHOLD) <= 0) {
            return SAVINGS_RATE_MEDIUM;  // 3.5% pour les soldes moyens
        }
        return SAVINGS_RATE_LARGE;  // 3% pour les gros soldes
    }

    @Override
    public BigDecimal calculateWithdrawalCommission(BigDecimal amount) {
        // Petits retraits gratuits
        if (amount.compareTo(new BigDecimal("20000")) <= 0) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(WITHDRAWAL_RATE).setScale(0, RoundingMode.CEILING);
    }

    @Override
    public String getOperatorName() {
        return OPERATOR_NAME;
    }

    @Override
    public BigDecimal getBaseRate() {
        return TRANSACTION_RATE;
    }
}
