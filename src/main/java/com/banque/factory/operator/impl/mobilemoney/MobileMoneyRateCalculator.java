package com.banque.factory.operator.impl.mobilemoney;

import com.banque.factory.operator.products.RateCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PRODUIT CONCRET: Calculateur de taux pour Mobile Money
 *
 * Caractéristiques:
 * - Frais par palier de montant (pas de frais fixes)
 * - Frais de retrait plus élevés
 * - Pas d'intérêts sur l'épargne (ce n'est pas une banque)
 * - Commissions simplifiées
 */
public class MobileMoneyRateCalculator implements RateCalculator {

    private static final String OPERATOR_NAME = "Mobile Money";

    // Paliers de frais (style Orange Money / MTN Money)
    private static final BigDecimal[][] FEE_TIERS = {
        {new BigDecimal("5000"), new BigDecimal("50")},      // 0-5000: 50 FCFA
        {new BigDecimal("10000"), new BigDecimal("100")},    // 5001-10000: 100 FCFA
        {new BigDecimal("25000"), new BigDecimal("200")},    // 10001-25000: 200 FCFA
        {new BigDecimal("50000"), new BigDecimal("350")},    // 25001-50000: 350 FCFA
        {new BigDecimal("100000"), new BigDecimal("500")},   // 50001-100000: 500 FCFA
        {new BigDecimal("250000"), new BigDecimal("1000")},  // 100001-250000: 1000 FCFA
        {new BigDecimal("500000"), new BigDecimal("1500")},  // 250001-500000: 1500 FCFA
    };

    private static final BigDecimal WITHDRAWAL_RATE = new BigDecimal("0.02");  // 2%
    private static final BigDecimal INTER_OPERATOR_RATE = new BigDecimal("0.02");  // 2%

    @Override
    public BigDecimal calculateTransactionFee(BigDecimal amount, String transactionType) {
        // Frais par palier
        for (BigDecimal[] tier : FEE_TIERS) {
            if (amount.compareTo(tier[0]) <= 0) {
                return tier[1];
            }
        }
        // Au-delà du dernier palier: 2% du montant
        return amount.multiply(new BigDecimal("0.02")).setScale(0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculateInterOperatorFee(BigDecimal amount, String destinationOperator) {
        // Frais fixes + pourcentage pour transferts vers autres opérateurs
        BigDecimal baseFee = calculateTransactionFee(amount, "TRANSFER");
        BigDecimal interOpFee = amount.multiply(INTER_OPERATOR_RATE);

        return baseFee.add(interOpFee).setScale(0, RoundingMode.CEILING);
    }

    @Override
    public BigDecimal calculateSavingsInterestRate(BigDecimal accountBalance) {
        // Mobile Money ne propose pas d'intérêts (ce n'est pas un compte épargne)
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateWithdrawalCommission(BigDecimal amount) {
        // Retrait = frais de transaction + 2%
        BigDecimal transactionFee = calculateTransactionFee(amount, "WITHDRAWAL");
        BigDecimal withdrawalFee = amount.multiply(WITHDRAWAL_RATE);

        return transactionFee.add(withdrawalFee).setScale(0, RoundingMode.CEILING);
    }

    @Override
    public String getOperatorName() {
        return OPERATOR_NAME;
    }

    @Override
    public BigDecimal getBaseRate() {
        return new BigDecimal("0.02"); // 2% en moyenne
    }
}
