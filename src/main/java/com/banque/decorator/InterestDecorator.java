package com.banque.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PATTERN DECORATOR - ConcreteDecorator (Interets)
 *
 * OBJECTIF 8: Ajoute le calcul des interets au compte.
 * Applicable principalement aux comptes epargne.
 *
 * CARACTERISTIQUES:
 * - Taux d'interet configurable
 * - Calcul mensuel des interets
 * - Bonus ajoute au solde
 */
public class InterestDecorator extends AccountDecorator {

    private final BigDecimal annualInterestRate; // Taux annuel en pourcentage
    private final BigDecimal minimumBalanceForInterest;

    /**
     * Cree un decorateur d'interets avec taux par defaut (3.5% par an).
     */
    public InterestDecorator(Account account) {
        this(account, new BigDecimal("3.5"));
    }

    /**
     * Cree un decorateur d'interets avec taux personnalise.
     *
     * @param account Compte a decorer
     * @param annualInterestRate Taux d'interet annuel en pourcentage (ex: 3.5 pour 3.5%)
     */
    public InterestDecorator(Account account, BigDecimal annualInterestRate) {
        this(account, annualInterestRate, new BigDecimal("10000"));
    }

    /**
     * Cree un decorateur d'interets complet.
     *
     * @param account Compte a decorer
     * @param annualInterestRate Taux annuel en pourcentage
     * @param minimumBalance Solde minimum pour recevoir des interets
     */
    public InterestDecorator(Account account, BigDecimal annualInterestRate,
                             BigDecimal minimumBalance) {
        super(account);
        this.annualInterestRate = annualInterestRate;
        this.minimumBalanceForInterest = minimumBalance;
    }

    @Override
    public String getDescription() {
        return wrappedAccount.getDescription() + " + Interets " +
               annualInterestRate + "%/an";
    }

    @Override
    protected String getDecoratorName() {
        return "Interets(" + annualInterestRate + "%)";
    }

    @Override
    public BigDecimal getMonthlyBonus() {
        // Calculer les interets mensuels
        BigDecimal baseBonus = wrappedAccount.getMonthlyBonus();
        BigDecimal interest = calculateMonthlyInterest();
        return baseBonus.add(interest);
    }

    /**
     * Calcule les interets mensuels.
     */
    public BigDecimal calculateMonthlyInterest() {
        BigDecimal balance = getBalance();

        // Verifier le solde minimum
        if (balance.compareTo(minimumBalanceForInterest) < 0) {
            return BigDecimal.ZERO;
        }

        // Interet mensuel = Solde * (Taux annuel / 12 / 100)
        BigDecimal monthlyRate = annualInterestRate
            .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP)
            .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        return balance.multiply(monthlyRate).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Applique les interets au compte (a appeler mensuellement).
     */
    public TransactionResult applyMonthlyInterest() {
        BigDecimal interest = calculateMonthlyInterest();

        if (interest.compareTo(BigDecimal.ZERO) <= 0) {
            return TransactionResult.failure("INT-0",
                "Solde insuffisant pour les interets (min: " + minimumBalanceForInterest + " FCFA)");
        }

        System.out.println("[INTERETS] Application de " + interest + " FCFA sur " +
            getAccountNumber() + " (taux: " + annualInterestRate + "%/an)");

        return wrappedAccount.deposit(interest);
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public BigDecimal getMinimumBalanceForInterest() {
        return minimumBalanceForInterest;
    }

    /**
     * Calcule la projection des interets sur une periode.
     *
     * @param months Nombre de mois
     * @return Interets totaux projetes
     */
    public BigDecimal projectInterest(int months) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal projectedBalance = getBalance();

        for (int i = 0; i < months; i++) {
            if (projectedBalance.compareTo(minimumBalanceForInterest) >= 0) {
                BigDecimal monthlyRate = annualInterestRate
                    .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP)
                    .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                BigDecimal interest = projectedBalance.multiply(monthlyRate)
                    .setScale(0, RoundingMode.HALF_UP);
                total = total.add(interest);
                projectedBalance = projectedBalance.add(interest);
            }
        }

        return total;
    }
}
