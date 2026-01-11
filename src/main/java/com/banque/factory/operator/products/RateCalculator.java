package com.banque.factory.operator.products;

import java.math.BigDecimal;

/**
 * PRODUIT ABSTRAIT 2: Calculateur de taux et commissions
 *
 * OBJECTIF 2: Chaque opérateur a ses propres taux et commissions.
 * Cette interface définit le contrat commun pour tous les calculateurs.
 *
 * Exemples de différences entre opérateurs:
 * - Banque: frais fixes + pourcentage, taux préférentiels pour gros montants
 * - Mobile Money: pourcentage fixe par palier de montant
 * - Microfinance: taux plus élevés mais plafonds plus bas
 */
public interface RateCalculator {

    /**
     * Calcule les frais de transaction.
     *
     * @param amount Montant de la transaction
     * @param transactionType Type de transaction
     * @return Montant des frais
     */
    BigDecimal calculateTransactionFee(BigDecimal amount, String transactionType);

    /**
     * Calcule les frais de transfert inter-opérateurs.
     *
     * @param amount Montant du transfert
     * @param destinationOperator Opérateur de destination
     * @return Montant des frais
     */
    BigDecimal calculateInterOperatorFee(BigDecimal amount, String destinationOperator);

    /**
     * Calcule le taux d'intérêt annuel pour un compte épargne.
     *
     * @param accountBalance Solde du compte
     * @return Taux d'intérêt en pourcentage
     */
    BigDecimal calculateSavingsInterestRate(BigDecimal accountBalance);

    /**
     * Calcule la commission sur un retrait.
     *
     * @param amount Montant du retrait
     * @return Montant de la commission
     */
    BigDecimal calculateWithdrawalCommission(BigDecimal amount);

    /**
     * Retourne le nom de l'opérateur associé.
     */
    String getOperatorName();

    /**
     * Retourne le taux de base de l'opérateur.
     */
    BigDecimal getBaseRate();

    /**
     * Classe pour encapsuler le détail des frais calculés.
     */
    class FeeDetail {
        private final BigDecimal baseFee;
        private final BigDecimal percentageFee;
        private final BigDecimal totalFee;
        private final String description;

        public FeeDetail(BigDecimal baseFee, BigDecimal percentageFee, String description) {
            this.baseFee = baseFee;
            this.percentageFee = percentageFee;
            this.totalFee = baseFee.add(percentageFee);
            this.description = description;
        }

        public BigDecimal getBaseFee() { return baseFee; }
        public BigDecimal getPercentageFee() { return percentageFee; }
        public BigDecimal getTotalFee() { return totalFee; }
        public String getDescription() { return description; }
    }
}
