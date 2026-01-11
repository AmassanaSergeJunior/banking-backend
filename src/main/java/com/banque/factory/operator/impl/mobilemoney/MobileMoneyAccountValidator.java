package com.banque.factory.operator.impl.mobilemoney;

import com.banque.factory.operator.products.AccountValidator;

import java.math.BigDecimal;

/**
 * PRODUIT CONCRET: Validateur de compte pour Mobile Money
 *
 * Règles simplifiées de validation:
 * - Pas de dépôt minimum
 * - Numéro de téléphone comme identifiant
 * - Plafonds de transaction plus bas
 * - Ouverture de compte instantanée
 */
public class MobileMoneyAccountValidator implements AccountValidator {

    private static final String OPERATOR_NAME = "Mobile Money";
    private static final BigDecimal MINIMUM_DEPOSIT = BigDecimal.ZERO;  // Pas de minimum
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("500000");  // 500k FCFA max

    @Override
    public ValidationResult validateAccountCreation(String accountNumber, String clientId, BigDecimal initialDeposit) {
        // Le numéro de compte EST le numéro de téléphone
        // Format attendu: 6XXXXXXXX (9 chiffres commençant par 6)
        if (accountNumber == null || !accountNumber.matches("6\\d{8}")) {
            return ValidationResult.failure(
                "Numéro de téléphone Mobile Money invalide. Format: 6XXXXXXXX",
                OPERATOR_NAME
            );
        }

        // Vérification du client ID (peut être le même que le numéro)
        if (clientId == null || clientId.isEmpty()) {
            return ValidationResult.failure(
                "Identifiant client requis",
                OPERATOR_NAME
            );
        }

        // Pas de dépôt minimum requis
        return ValidationResult.success(
            "Compte Mobile Money activé instantanément sur le numéro " + maskPhone(accountNumber),
            OPERATOR_NAME
        );
    }

    @Override
    public ValidationResult validateTransaction(String accountNumber, BigDecimal amount, String transactionType) {
        // Vérification du montant
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ValidationResult.failure("Montant invalide", OPERATOR_NAME);
        }

        // Plafond Mobile Money plus restrictif
        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            return ValidationResult.failure(
                String.format("Montant supérieur au plafond Mobile Money (%s FCFA). " +
                    "Utilisez une banque pour les gros montants.", MAX_TRANSACTION_AMOUNT),
                OPERATOR_NAME
            );
        }

        return ValidationResult.success(
            String.format("Transaction Mobile Money de %s FCFA validée", amount),
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

    private String maskPhone(String phone) {
        return phone.substring(0, 3) + "***" + phone.substring(6);
    }
}
