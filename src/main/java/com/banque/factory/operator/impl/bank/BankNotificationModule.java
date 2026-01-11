package com.banque.factory.operator.impl.bank;

import com.banque.factory.operator.products.NotificationModule;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * PRODUIT CONCRET: Module de notification pour Banque traditionnelle
 *
 * Caractéristiques:
 * - Messages formels et professionnels
 * - Format bancaire standard
 * - Notifications par SMS et Email
 */
public class BankNotificationModule implements NotificationModule {

    private static final String OPERATOR_NAME = "Banque Traditionnelle";
    private static final String MESSAGE_PREFIX = "[BANQUE]";
    private static final String CHANNEL = "SMS + Email";

    private final NumberFormat currencyFormat;

    public BankNotificationModule() {
        this.currencyFormat = NumberFormat.getInstance(Locale.FRANCE);
    }

    @Override
    public NotificationResult sendTransactionNotification(
            String phoneNumber,
            String transactionType,
            BigDecimal amount,
            BigDecimal balance) {

        String formattedAmount = currencyFormat.format(amount) + " FCFA";
        String formattedBalance = currencyFormat.format(balance) + " FCFA";

        String message = formatMessage(
            "%s Votre compte a été %s de %s. Nouveau solde: %s. Pour toute réclamation, appelez le 8888.",
            MESSAGE_PREFIX,
            getTransactionVerb(transactionType),
            formattedAmount,
            formattedBalance
        );

        // Simulation d'envoi
        return NotificationResult.success(message, CHANNEL, OPERATOR_NAME);
    }

    @Override
    public NotificationResult sendWelcomeNotification(
            String phoneNumber,
            String clientName,
            String accountNumber) {

        String message = formatMessage(
            "%s Cher(e) %s, bienvenue à notre banque. Votre compte N°%s a été ouvert avec succès. " +
            "Téléchargez notre application mobile pour gérer votre compte. Service client: 8888",
            MESSAGE_PREFIX,
            clientName,
            maskAccountNumber(accountNumber)
        );

        return NotificationResult.success(message, CHANNEL, OPERATOR_NAME);
    }

    @Override
    public NotificationResult sendSecurityAlert(
            String phoneNumber,
            String alertType,
            String details) {

        String message = formatMessage(
            "%s ALERTE SECURITE: %s. %s. Si vous n'êtes pas à l'origine de cette action, " +
            "contactez immédiatement le 8888 ou bloquez votre carte via l'application.",
            MESSAGE_PREFIX,
            alertType.toUpperCase(),
            details
        );

        return NotificationResult.success(message, CHANNEL, OPERATOR_NAME);
    }

    @Override
    public String formatMessage(String template, Object... params) {
        return String.format(template, params);
    }

    @Override
    public String getOperatorName() {
        return OPERATOR_NAME;
    }

    @Override
    public String getMessagePrefix() {
        return MESSAGE_PREFIX;
    }

    private String getTransactionVerb(String transactionType) {
        return switch (transactionType.toUpperCase()) {
            case "DEPOSIT" -> "crédité";
            case "WITHDRAWAL" -> "débité";
            case "TRANSFER" -> "débité (transfert)";
            default -> "modifié";
        };
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber.length() <= 4) return accountNumber;
        return accountNumber.substring(0, 2) + "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
