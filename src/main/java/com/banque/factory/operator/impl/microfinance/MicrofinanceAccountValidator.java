package com.banque.factory.operator.impl.microfinance;

import com.banque.factory.operator.products.AccountValidator;

import java.math.BigDecimal;

/**
 * PRODUIT CONCRET: Validateur de compte pour Microfinance
 *
 * Règles adaptées aux populations à faibles revenus:
 * - Dépôt minimum très bas (5000 FCFA)
 * - Peut exiger un garant
 * - Plafonds intermédiaires
 * - Processus d'ouverture simplifié mais avec accompagnement
 */
public class MicrofinanceAccountValidator implements AccountValidator {

    private static final String OPERATOR_NAME = "Microfinance";
    private static final BigDecimal MINIMUM_DEPOSIT = new BigDecimal("5000");  // 5000 FCFA
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("1000000");  // 1M FCFA

    @Override
    public ValidationResult validateAccountCreation(String accountNumber, String clientId, BigDecimal initialDeposit) {
        // Format de compte microfinance: MF + 8 chiffres
        if (accountNumber == null || !accountNumber.matches("MF\\d{8}")) {
            return ValidationResult.failure(
                "Format de compte microfinance invalide. Format attendu: MF00000000",
                OPERATOR_NAME
            );
        }

        // Vérification du client ID
        if (clientId == null || clientId.length() < 3) {
            return ValidationResult.failure(
                "Identifiant client invalide",
                OPERATOR_NAME
            );
        }

        // Vérification du dépôt minimum (plus accessible que la banque)
        if (initialDeposit == null || initialDeposit.compareTo(MINIMUM_DEPOSIT) < 0) {
            return ValidationResult.failure(
                String.format("Dépôt initial insuffisant. Minimum requis: %s FCFA. " +
                    "Vous pouvez épargner progressivement pour atteindre ce montant.", MINIMUM_DEPOSIT),
                OPERATOR_NAME
            );
        }

        return ValidationResult.success(
            "Compte microfinance ouvert! Un conseiller vous contactera pour votre suivi personnalisé.",
            OPERATOR_NAME
        );
    }

    @Override
    public ValidationResult validateTransaction(String accountNumber, BigDecimal amount, String transactionType) {
        // Vérification du montant
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.failure("Montant invalide", OPERATOR_NAME);
        }

        // Plafond microfinance
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            return ValidationResult.failure(
                String.format("Montant supérieur au plafond (%s FCFA). " +
                    "Contactez votre conseiller pour les transactions importantes.", MAX_TRANSACTION_AMOUNT),
                OPERATOR_NAME
            );
        }

        // Validation spéciale pour les retraits importants
        if ("WITHDRAWAL".equalsIgnoreCase(transactionType) &&
            amount.compareTo(new BigDecimal("200000")) > 0) {
            return ValidationResult.success(
                String.format("Transaction de %s FCFA validée. Note: Prévenez votre agence " +
                    "24h à l'avance pour les retraits > 200 000 FCFA.", amount),
                OPERATOR_NAME
            );
        }

        return ValidationResult.success(
            String.format("Transaction de %s FCFA validée", amount),
            OPERATOR_NAME
        );
    }

    @Override
    public String getOperatorName() {
        return OPERATOR_NAME;
    }

    @Override
    public BigDecimal getMinimumDeposit() {
        return MINIMUM_DEPOSIT;
    }
}
