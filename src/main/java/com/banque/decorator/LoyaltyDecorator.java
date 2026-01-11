package com.banque.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PATTERN DECORATOR - ConcreteDecorator (Programme de Fidelite)
 *
 * OBJECTIF 8: Ajoute un programme de fidelite au compte.
 * Accumule des points sur les transactions et offre des bonus.
 *
 * CARACTERISTIQUES:
 * - Points accumules sur chaque transaction
 * - Niveaux de fidelite (Bronze, Silver, Gold, Platinum)
 * - Bonus mensuels selon le niveau
 * - Reduction des frais
 */
public class LoyaltyDecorator extends AccountDecorator {

    private int loyaltyPoints;
    private LoyaltyTier currentTier;
    private final BigDecimal pointsPerTransaction; // Points par FCFA

    /**
     * Niveaux de fidelite.
     */
    public enum LoyaltyTier {
        BRONZE(0, "Bronze", new BigDecimal("0"), new BigDecimal("0")),
        SILVER(1000, "Silver", new BigDecimal("500"), new BigDecimal("10")),
        GOLD(5000, "Gold", new BigDecimal("1500"), new BigDecimal("20")),
        PLATINUM(15000, "Platinum", new BigDecimal("5000"), new BigDecimal("30"));

        private final int requiredPoints;
        private final String label;
        private final BigDecimal monthlyBonus;
        private final BigDecimal feeDiscount; // Pourcentage de reduction

        LoyaltyTier(int points, String label, BigDecimal bonus, BigDecimal discount) {
            this.requiredPoints = points;
            this.label = label;
            this.monthlyBonus = bonus;
            this.feeDiscount = discount;
        }

        public int getRequiredPoints() { return requiredPoints; }
        public String getLabel() { return label; }
        public BigDecimal getMonthlyBonus() { return monthlyBonus; }
        public BigDecimal getFeeDiscount() { return feeDiscount; }

        public static LoyaltyTier fromPoints(int points) {
            LoyaltyTier result = BRONZE;
            for (LoyaltyTier tier : values()) {
                if (points >= tier.requiredPoints) {
                    result = tier;
                }
            }
            return result;
        }
    }

    /**
     * Cree un decorateur de fidelite.
     */
    public LoyaltyDecorator(Account account) {
        this(account, 0);
    }

    /**
     * Cree un decorateur de fidelite avec points initiaux.
     */
    public LoyaltyDecorator(Account account, int initialPoints) {
        super(account);
        this.loyaltyPoints = initialPoints;
        this.currentTier = LoyaltyTier.fromPoints(initialPoints);
        this.pointsPerTransaction = new BigDecimal("0.01"); // 1 point pour 100 FCFA
    }

    @Override
    public String getDescription() {
        return wrappedAccount.getDescription() + " + Fidelite " +
               currentTier.getLabel() + " (" + loyaltyPoints + " pts)";
    }

    @Override
    protected String getDecoratorName() {
        return "Fidelite(" + currentTier.getLabel() + ")";
    }

    @Override
    public TransactionResult deposit(BigDecimal amount) {
        TransactionResult result = wrappedAccount.deposit(amount);

        if (result.isSuccess()) {
            // Gagner des points
            int pointsEarned = earnPoints(amount);
            System.out.println("[FIDELITE] +" + pointsEarned + " points gagnes " +
                "(total: " + loyaltyPoints + ", niveau: " + currentTier.getLabel() + ")");
        }

        return result;
    }

    @Override
    public TransactionResult withdraw(BigDecimal amount) {
        TransactionResult result = wrappedAccount.withdraw(amount);

        if (result.isSuccess()) {
            // Gagner des points meme sur les retraits
            int pointsEarned = earnPoints(amount.divide(new BigDecimal("2"),
                0, RoundingMode.DOWN)); // Moitie des points
            System.out.println("[FIDELITE] +" + pointsEarned + " points gagnes");
        }

        return result;
    }

    @Override
    public BigDecimal getMonthlyBonus() {
        return wrappedAccount.getMonthlyBonus().add(currentTier.getMonthlyBonus());
    }

    @Override
    public BigDecimal getMonthlyFees() {
        BigDecimal baseFees = wrappedAccount.getMonthlyFees();

        // Appliquer la reduction selon le niveau
        if (currentTier.getFeeDiscount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = baseFees.multiply(currentTier.getFeeDiscount())
                .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            return baseFees.subtract(discount);
        }

        return baseFees;
    }

    /**
     * Gagne des points et met a jour le niveau.
     */
    private int earnPoints(BigDecimal amount) {
        int points = amount.multiply(pointsPerTransaction)
            .setScale(0, RoundingMode.DOWN).intValue();
        loyaltyPoints += points;

        // Verifier si on monte de niveau
        LoyaltyTier newTier = LoyaltyTier.fromPoints(loyaltyPoints);
        if (newTier.ordinal() > currentTier.ordinal()) {
            System.out.println("[FIDELITE] NIVEAU SUPERIEUR! " +
                currentTier.getLabel() + " -> " + newTier.getLabel());
            currentTier = newTier;
        }

        return points;
    }

    /**
     * Utilise des points pour obtenir une reduction.
     *
     * @param points Nombre de points a utiliser
     * @return Montant de la reduction (1 point = 1 FCFA)
     */
    public BigDecimal redeemPoints(int points) {
        if (points > loyaltyPoints) {
            points = loyaltyPoints;
        }
        loyaltyPoints -= points;

        // Verifier si on descend de niveau
        currentTier = LoyaltyTier.fromPoints(loyaltyPoints);

        System.out.println("[FIDELITE] " + points + " points utilises, " +
            loyaltyPoints + " restants");

        return new BigDecimal(points);
    }

    /**
     * Retourne le nombre de points pour atteindre le prochain niveau.
     */
    public int getPointsToNextTier() {
        for (LoyaltyTier tier : LoyaltyTier.values()) {
            if (tier.ordinal() > currentTier.ordinal()) {
                return tier.getRequiredPoints() - loyaltyPoints;
            }
        }
        return 0; // Deja au niveau maximum
    }

    /**
     * Retourne un resume du programme de fidelite.
     */
    public String getLoyaltySummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PROGRAMME FIDELITE ===\n");
        sb.append("Niveau actuel: ").append(currentTier.getLabel()).append("\n");
        sb.append("Points accumules: ").append(loyaltyPoints).append("\n");
        sb.append("Bonus mensuel: ").append(currentTier.getMonthlyBonus()).append(" FCFA\n");
        sb.append("Reduction frais: ").append(currentTier.getFeeDiscount()).append("%\n");

        int toNext = getPointsToNextTier();
        if (toNext > 0) {
            sb.append("Points pour niveau suivant: ").append(toNext).append("\n");
        } else {
            sb.append("*** Niveau maximum atteint! ***\n");
        }

        return sb.toString();
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public LoyaltyTier getCurrentTier() {
        return currentTier;
    }

    /**
     * Ajoute des points bonus (promotion, parrainage, etc.)
     */
    public void addBonusPoints(int points, String reason) {
        loyaltyPoints += points;
        currentTier = LoyaltyTier.fromPoints(loyaltyPoints);
        System.out.println("[FIDELITE] +" + points + " points bonus (" + reason + ")");
    }
}
