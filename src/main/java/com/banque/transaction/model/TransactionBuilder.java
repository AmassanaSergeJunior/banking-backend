package com.banque.transaction.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN BUILDER - Constructeur de transactions avec API fluide
 *
 * OBJECTIF 3: Ce builder permet de construire des transactions complexes
 * pas à pas, avec des étapes optionnelles et paramétrables.
 *
 * POURQUOI BUILDER?
 * - Permet de construire des objets complexes étape par étape
 * - Évite les constructeurs avec de nombreux paramètres
 * - Permet de produire différentes variantes (transaction courte vs complète)
 * - API fluide pour une utilisation intuitive
 *
 * EXEMPLE D'UTILISATION:
 * <pre>
 * Transaction transaction = new TransactionBuilder()
 *     .type(TransactionType.TRANSFER_INTER_OPERATOR)
 *     .from("ACC001")
 *     .to("ACC002")
 *     .amount(50000)
 *     .withVerification()
 *     .withCommission(Commission.interOperatorFee())
 *     .withLogging()
 *     .withNotification()
 *     .build();
 * </pre>
 */
public class TransactionBuilder {

    // Champs requis
    TransactionType type;
    String sourceAccount;
    String destinationAccount;
    BigDecimal amount;

    // Champs optionnels avec valeurs par défaut
    String currency = "XAF";
    String reference;
    String description;

    // Conversion de devise
    String targetCurrency;
    BigDecimal exchangeRate;

    // Commissions
    List<Commission> commissions = new ArrayList<>();

    // Étapes optionnelles (toutes désactivées par défaut)
    boolean verificationEnabled = false;
    boolean currencyConversionEnabled = false;
    boolean loggingEnabled = false;
    boolean notificationEnabled = false;
    boolean fraudCheckEnabled = false;

    // Informations d'opérateur
    String sourceOperator;
    String destinationOperator;

    /**
     * Constructeur par défaut.
     */
    public TransactionBuilder() {
    }

    // ==================== CONFIGURATION DE BASE ====================

    /**
     * Définit le type de transaction.
     */
    public TransactionBuilder type(TransactionType type) {
        this.type = type;
        return this;
    }

    /**
     * Définit le compte source.
     */
    public TransactionBuilder from(String sourceAccount) {
        this.sourceAccount = sourceAccount;
        return this;
    }

    /**
     * Définit le compte source avec son opérateur.
     */
    public TransactionBuilder from(String sourceAccount, String operator) {
        this.sourceAccount = sourceAccount;
        this.sourceOperator = operator;
        return this;
    }

    /**
     * Définit le compte destination.
     */
    public TransactionBuilder to(String destinationAccount) {
        this.destinationAccount = destinationAccount;
        return this;
    }

    /**
     * Définit le compte destination avec son opérateur.
     */
    public TransactionBuilder to(String destinationAccount, String operator) {
        this.destinationAccount = destinationAccount;
        this.destinationOperator = operator;
        return this;
    }

    /**
     * Définit le montant de la transaction.
     */
    public TransactionBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Définit le montant de la transaction (version double).
     */
    public TransactionBuilder amount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
        return this;
    }

    /**
     * Définit la devise source.
     */
    public TransactionBuilder currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Définit une référence personnalisée.
     */
    public TransactionBuilder reference(String reference) {
        this.reference = reference;
        return this;
    }

    /**
     * Définit une description.
     */
    public TransactionBuilder description(String description) {
        this.description = description;
        return this;
    }

    // ==================== ÉTAPES OPTIONNELLES ====================

    /**
     * Active la vérification du compte et du solde.
     */
    public TransactionBuilder withVerification() {
        this.verificationEnabled = true;
        return this;
    }

    /**
     * Active la vérification anti-fraude.
     */
    public TransactionBuilder withFraudCheck() {
        this.fraudCheckEnabled = true;
        return this;
    }

    /**
     * Active la conversion de devise.
     *
     * @param targetCurrency Devise cible
     * @param exchangeRate Taux de change
     */
    public TransactionBuilder withCurrencyConversion(String targetCurrency, BigDecimal exchangeRate) {
        this.currencyConversionEnabled = true;
        this.targetCurrency = targetCurrency;
        this.exchangeRate = exchangeRate;
        return this;
    }

    /**
     * Active la conversion de devise (version double).
     */
    public TransactionBuilder withCurrencyConversion(String targetCurrency, double exchangeRate) {
        return withCurrencyConversion(targetCurrency, BigDecimal.valueOf(exchangeRate));
    }

    /**
     * Ajoute une commission à la transaction.
     */
    public TransactionBuilder withCommission(Commission commission) {
        this.commissions.add(commission);
        return this;
    }

    /**
     * Ajoute une commission en pourcentage.
     */
    public TransactionBuilder withCommission(String name, double percentage) {
        this.commissions.add(Commission.builder(name).percentage(percentage).build());
        return this;
    }

    /**
     * Ajoute une commission fixe.
     */
    public TransactionBuilder withFixedCommission(String name, double amount) {
        this.commissions.add(Commission.builder(name).fixedAmount(amount).build());
        return this;
    }

    /**
     * Ajoute plusieurs commissions.
     */
    public TransactionBuilder withCommissions(Commission... commissions) {
        for (Commission c : commissions) {
            this.commissions.add(c);
        }
        return this;
    }

    /**
     * Active la journalisation détaillée.
     */
    public TransactionBuilder withLogging() {
        this.loggingEnabled = true;
        return this;
    }

    /**
     * Active les notifications (SMS/Email).
     */
    public TransactionBuilder withNotification() {
        this.notificationEnabled = true;
        return this;
    }

    /**
     * Active toutes les étapes de sécurité (vérification + anti-fraude).
     */
    public TransactionBuilder withFullSecurity() {
        this.verificationEnabled = true;
        this.fraudCheckEnabled = true;
        return this;
    }

    /**
     * Active toutes les fonctionnalités (vérification, logging, notification).
     */
    public TransactionBuilder withAllFeatures() {
        this.verificationEnabled = true;
        this.fraudCheckEnabled = true;
        this.loggingEnabled = true;
        this.notificationEnabled = true;
        return this;
    }

    // ==================== CONSTRUCTION ====================

    /**
     * Construit la transaction.
     *
     * @return La transaction configurée
     * @throws IllegalStateException si les champs requis sont manquants
     */
    public Transaction build() {
        validate();
        return new Transaction(this);
    }

    /**
     * Construit et exécute la transaction.
     *
     * @return Le résultat de l'exécution
     */
    public Transaction.TransactionResult buildAndExecute() {
        Transaction transaction = build();
        return transaction.execute();
    }

    /**
     * Valide que tous les champs requis sont présents.
     */
    private void validate() {
        List<String> errors = new ArrayList<>();

        if (type == null) {
            errors.add("Le type de transaction est requis");
        }
        if (sourceAccount == null || sourceAccount.isEmpty()) {
            errors.add("Le compte source est requis");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Le montant doit être positif");
        }

        // Vérifications spécifiques selon le type
        if (type != null) {
            switch (type) {
                case TRANSFER_INTERNAL:
                case TRANSFER_INTER_OPERATOR:
                case TRANSFER_INTERNATIONAL:
                    if (destinationAccount == null || destinationAccount.isEmpty()) {
                        errors.add("Le compte destination est requis pour les transferts");
                    }
                    break;
                default:
                    break;
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Erreurs de validation: " + String.join(", ", errors));
        }
    }

    /**
     * Retourne un résumé de la configuration actuelle (pour debug).
     */
    public String getConfigurationSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration du Builder:\n");
        sb.append("  Type: ").append(type).append("\n");
        sb.append("  Source: ").append(sourceAccount).append("\n");
        sb.append("  Destination: ").append(destinationAccount).append("\n");
        sb.append("  Montant: ").append(amount).append(" ").append(currency).append("\n");
        sb.append("  Étapes: ");
        List<String> steps = new ArrayList<>();
        if (verificationEnabled) steps.add("Vérification");
        if (fraudCheckEnabled) steps.add("Anti-fraude");
        if (currencyConversionEnabled) steps.add("Conversion(" + targetCurrency + ")");
        if (!commissions.isEmpty()) steps.add("Commissions(" + commissions.size() + ")");
        if (loggingEnabled) steps.add("Logging");
        if (notificationEnabled) steps.add("Notification");
        sb.append(steps.isEmpty() ? "Aucune" : String.join(", ", steps));
        return sb.toString();
    }
}
