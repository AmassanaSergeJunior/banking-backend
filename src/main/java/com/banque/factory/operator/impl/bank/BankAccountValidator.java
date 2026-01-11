package com.banque.factory.operator.impl.bank;

import com.banque.factory.operator.products.AccountValidator;

import java.math.BigDecimal;

/**
 * PRODUIT CONCRET: Validateur de compte pour Banque traditionnelle
 *
 * Règles strictes de validation:
 * - Dépôt minimum élevé (50 000 FCFA)
 * - Vérification stricte du format de compte
 * - Limites de transaction élevées
 */
public class BankAccountValidator implements AccountValidator {

    private static final String OPERATOR_NAME = "Banque Traditionnelle";
    private static final BigDecimal MINIMUM_DEPOSIT = new BigDecimal("50000");
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("10000000");

    @Override
    public ValidationResult validateAccountCreation(String accountNumber, String clientId, BigDecimal initialDeposit) {
        // Vérification du format du numéro de compte (format bancaire: 2 lettres + 10 chiffres)
        if (accountNumber == null || !accountNumber.matches("[A-Z]{2}\\d{10}")) {
            return ValidationResult.failure(
                "Format de compte bancaire invalide. Format attendu: XX0000000000",
                OPERATOR_NAME
            );
        }

        // Vérification du client ID
        if (clientId == null || clientId.length() < 5) {
            return ValidationResult.failure(
                "Identifiant client invalide (minimum 5 caractères)",
                OPERATOR_NAME
            );
        }

        // Vérification du dépôt minimum
        if (initialDeposit == null || initialDeposit.compareTo(MINIMUM_DEPOSIT) < 0) {
            return ValidationResult.failure(
                String.format("Dépôt initial insuffisant. Minimum requis: %s FCFA", MINIMUM_DEPOSIT),
                OPERATOR_NAME
            );
        }

        return ValidationResult.success(
            "Compte bancaire validé. Dépôt initial: " + initialDeposit + " FCFA",
            OPERATOR_NAME
        );
    }

    @Override
    public ValidationResult validateTransaction(String accountNumber, BigDecimal amount, String transactionType) {
        // Vérification du montant
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.failure("Montant invalide", OPERATOR_NAME);
        }

        // Vérification du plafond
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            return ValidationResult.failure(
                String.format("Montant supérieur au plafond autorisé (%s FCFA)", MAX_TRANSACTION_AMOUNT),
                OPERATOR_NAME
            );
        }

        return ValidationResult.success(
            String.format("Transaction %s de %s FCFA validée", transactionType, amount),
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
