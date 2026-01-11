package com.banque.factory.operator.products;

import java.math.BigDecimal;

/**
 * PRODUIT ABSTRAIT 3: Module de notification
 *
 * OBJECTIF 2: Chaque opérateur a son propre format et canal de notification.
 * Cette interface définit le contrat commun pour tous les modules.
 *
 * Exemples de différences entre opérateurs:
 * - Banque: notifications formelles par email et SMS, relevés mensuels
 * - Mobile Money: notifications SMS instantanées avec codes USSD
 * - Microfinance: notifications simplifiées, parfois vocales
 */
public interface NotificationModule {

    /**
     * Envoie une notification de transaction.
     *
     * @param phoneNumber Numéro de téléphone du destinataire
     * @param transactionType Type de transaction
     * @param amount Montant
     * @param balance Nouveau solde
     * @return Résultat de l'envoi
     */
    NotificationResult sendTransactionNotification(
        String phoneNumber,
        String transactionType,
        BigDecimal amount,
        BigDecimal balance
    );

    /**
     * Envoie une notification de bienvenue lors de l'ouverture de compte.
     *
     * @param phoneNumber Numéro de téléphone
     * @param clientName Nom du client
     * @param accountNumber Numéro de compte
     * @return Résultat de l'envoi
     */
    NotificationResult sendWelcomeNotification(
        String phoneNumber,
        String clientName,
        String accountNumber
    );

    /**
     * Envoie une alerte de sécurité.
     *
     * @param phoneNumber Numéro de téléphone
     * @param alertType Type d'alerte
     * @param details Détails de l'alerte
     * @return Résultat de l'envoi
     */
    NotificationResult sendSecurityAlert(
        String phoneNumber,
        String alertType,
        String details
    );

    /**
     * Formate un message selon le style de l'opérateur.
     *
     * @param template Template du message
     * @param params Paramètres à substituer
     * @return Message formaté
     */
    String formatMessage(String template, Object... params);

    /**
     * Retourne le nom de l'opérateur.
     */
    String getOperatorName();

    /**
     * Retourne le préfixe utilisé dans les messages.
     */
    String getMessagePrefix();

    /**
     * Classe pour le résultat de notification.
     */
    class NotificationResult {
        private final boolean sent;
        private final String message;
        private final String channel;
        private final String operatorName;

        public NotificationResult(boolean sent, String message, String channel, String operatorName) {
            this.sent = sent;
            this.message = message;
            this.channel = channel;
            this.operatorName = operatorName;
        }

        public boolean isSent() { return sent; }
        public String getMessage() { return message; }
        public String getChannel() { return channel; }
        public String getOperatorName() { return operatorName; }

        public static NotificationResult success(String message, String channel, String operatorName) {
            return new NotificationResult(true, message, channel, operatorName);
        }

        public static NotificationResult failure(String message, String channel, String operatorName) {
            return new NotificationResult(false, message, channel, operatorName);
        }
    }
}
