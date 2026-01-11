package com.banque.factory.operator.impl.microfinance;

import com.banque.factory.operator.products.NotificationModule;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * PRODUIT CONCRET: Module de notification pour Microfinance
 *
 * Caractéristiques:
 * - Messages simples et accessibles
 * - Ton chaleureux et personnalisé
 * - Rappels éducatifs sur l'épargne
 * - Support multilingue possible
 */
public class MicrofinanceNotificationModule implements NotificationModule {

    private static final String OPERATOR_NAME = "Microfinance";
    private static final String MESSAGE_PREFIX = "[Votre Caisse]";
    private static final String CHANNEL = "SMS";

    private final NumberFormat currencyFormat;

    public MicrofinanceNotificationModule() {
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

        String message;
        if ("DEPOSIT".equalsIgnoreCase(transactionType)) {
            message = formatMessage(
                "%s Bravo! Depot de %s FCFA recu. Solde: %s FCFA. " +
                "Continuez a epargner pour atteindre vos objectifs!",
                MESSAGE_PREFIX,
                formattedAmount,
                formattedBalance
            );
        } else {
            message = formatMessage(
                "%s Retrait de %s FCFA effectue. Solde: %s FCFA. " +
                "Votre conseiller reste a votre ecoute.",
                MESSAGE_PREFIX,
                formattedAmount,
                formattedBalance
            );
        }

        return NotificationResult.success(message, CHANNEL, OPERATOR_NAME);
    }

    @Override
    public NotificationResult sendWelcomeNotification(
            String phoneNumber,
            String clientName,
            String accountNumber) {

        String message = formatMessage(
            "%s Bienvenue dans notre famille %s! " +
            "Votre compte N°%s est ouvert. " +
            "Passez nous voir a l'agence, votre conseiller %s vous attend!",
            MESSAGE_PREFIX,
            clientName,
            accountNumber,
            getAssignedAdvisor()
        );

        return NotificationResult.success(message, CHANNEL, OPERATOR_NAME);
    }

    @Override
    public NotificationResult sendSecurityAlert(
            String phoneNumber,
            String alertType,
            String details) {

        String message = formatMessage(
            "%s ATTENTION: %s. %s. " +
            "En cas de doute, venez a l'agence avec votre piece d'identite.",
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

    private String getAssignedAdvisor() {
        // En production: récupérer le conseiller assigné depuis la base
        String[] advisors = {"M. Kamga", "Mme Nguemo", "M. Ondoua"};
        return advisors[(int) (Math.random() * advisors.length)];
    }
}
