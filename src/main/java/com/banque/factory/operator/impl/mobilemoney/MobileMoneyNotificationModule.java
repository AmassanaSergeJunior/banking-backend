package com.banque.factory.operator.impl.mobilemoney;

import com.banque.factory.operator.products.NotificationModule;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * PRODUIT CONCRET: Module de notification pour Mobile Money
 *
 * Caractéristiques:
 * - Messages courts et directs (style SMS)
 * - Codes USSD inclus
 * - Notifications instantanées
 * - Ton plus décontracté
 */
public class MobileMoneyNotificationModule implements NotificationModule {

    private static final String OPERATOR_NAME = "Mobile Money";
    private static final String MESSAGE_PREFIX = "MoMo:";
    private static final String CHANNEL = "SMS";

    private final NumberFormat currencyFormat;

    public MobileMoneyNotificationModule() {
        this.currencyFormat = NumberFormat.getInstance(Locale.FRANCE);
    }

    @Override
    public NotificationResult sendTransactionNotification(
            String phoneNumber,
            String transactionType,
            BigDecimal amount,
            BigDecimal balance) {

        String formattedAmount = currencyFormat.format(amount);
        String formattedBalance = currencyFormat.format(balance);

        String emoji = getTransactionEmoji(transactionType);
        String verb = getTransactionVerb(transactionType);

        String message = formatMessage(
            "%s %s %s %s FCFA. Solde: %s FCFA. Tapez *126# pour voir l'historique.",
            MESSAGE_PREFIX,
            emoji,
            verb,
            formattedAmount,
            formattedBalance
        );

        return NotificationResult.success(message, CHANNEL, OPERATOR_NAME);
    }

    @Override
    public NotificationResult sendWelcomeNotification(
            String phoneNumber,
            String clientName,
            String accountNumber) {

        String message = formatMessage(
            "%s Bienvenue %s! Votre Mobile Money est actif. " +
            "Envoyez de l'argent avec *126*1#, retirez avec *126*2#. " +
            "Code PIN: Ne le partagez jamais!",
            MESSAGE_PREFIX,
            clientName.split(" ")[0]  // Prénom seulement
        );

        return NotificationResult.success(message, CHANNEL, OPERATOR_NAME);
    }

    @Override
    public NotificationResult sendSecurityAlert(
            String phoneNumber,
            String alertType,
            String details) {

        String message = formatMessage(
            "%s ATTENTION! %s. %s. Si ce n'est pas vous, appelez VITE le 123 (gratuit)!",
            MESSAGE_PREFIX,
            alertType,
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
            case "DEPOSIT" -> "Recu";
            case "WITHDRAWAL" -> "Retrait";
            case "TRANSFER" -> "Envoi";
            default -> "Transaction";
        };
    }

    private String getTransactionEmoji(String transactionType) {
        return switch (transactionType.toUpperCase()) {
            case "DEPOSIT" -> "+";
            case "WITHDRAWAL" -> "-";
            case "TRANSFER" -> ">";
            default -> "*";
        };
    }
}
