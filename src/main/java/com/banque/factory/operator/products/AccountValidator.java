package com.banque.factory.operator.products;

import java.math.BigDecimal;

/**
 * PRODUIT ABSTRAIT 1: Validateur de compte
 *
 * OBJECTIF 2: Chaque opérateur a ses propres règles de validation de compte.
 * Cette interface définit le contrat commun pour tous les validateurs.
 *
 * Exemples de différences entre opérateurs:
 * - Banque: exige pièce d'identité, justificatif de domicile, revenus minimum
 * - Mobile Money: exige juste un numéro de téléphone valide
 * - Microfinance: peut exiger un garant ou une épargne préalable
 */
public interface AccountValidator {

    /**
     * Valide si un compte peut être ouvert avec les informations fournies.
     *
     * @param accountNumber Numéro de compte proposé
     * @param clientId Identifiant du client
     * @param initialDeposit Dépôt initial
     * @return Résultat de la validation
     */
    ValidationResult validateAccountCreation(String accountNumber, String clientId, BigDecimal initialDeposit);

    /**
     * Valide si une transaction est autorisée sur ce compte.
     *
     * @param accountNumber Numéro de compte
     * @param amount Montant de la transaction
     * @param transactionType Type de transaction (DEPOSIT, WITHDRAWAL, TRANSFER)
     * @return Résultat de la validation
     */
    ValidationResult validateTransaction(String accountNumber, BigDecimal amount, String transactionType);

    /**
     * Retourne le nom de l'opérateur associé à ce validateur.
     */
    String getOperatorName();

    /**
     * Retourne le dépôt minimum requis pour ouvrir un compte.
     */
    BigDecimal getMinimumDeposit();

    /**
     * Classe interne pour le résultat de validation.
     */
    class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String validatorName;

        public ValidationResult(boolean valid, String message, String validatorName) {
            this.valid = valid;
            this.message = message;
            this.validatorName = validatorName;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getValidatorName() { return validatorName; }

        public static ValidationResult success(String message, String validatorName) {
            return new ValidationResult(true, message, validatorName);
        }

        public static ValidationResult failure(String message, String validatorName) {
            return new ValidationResult(false, message, validatorName);
        }
    }
}
