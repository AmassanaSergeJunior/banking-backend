package com.banque.decorator;

import java.math.BigDecimal;

/**
 * PATTERN DECORATOR - ConcreteDecorator (Notifications)
 *
 * OBJECTIF 8: Ajoute des notifications automatiques au compte.
 * Envoie des SMS/Email sur chaque transaction.
 *
 * CARACTERISTIQUES:
 * - Notification sur chaque depot
 * - Notification sur chaque retrait
 * - Notification sur chaque transfert
 * - Alertes de solde bas
 * - Configurable (SMS, Email, ou les deux)
 */
public class NotificationDecorator extends AccountDecorator {

    private final String phoneNumber;
    private final String email;
    private final boolean smsEnabled;
    private final boolean emailEnabled;
    private final BigDecimal lowBalanceThreshold;
    private int notificationsSent;

    /**
     * Cree un decorateur de notifications SMS.
     */
    public NotificationDecorator(Account account, String phoneNumber) {
        this(account, phoneNumber, null, true, false);
    }

    /**
     * Cree un decorateur de notifications Email.
     */
    public static NotificationDecorator withEmail(Account account, String email) {
        return new NotificationDecorator(account, null, email, false, true);
    }

    /**
     * Cree un decorateur de notifications SMS + Email.
     */
    public static NotificationDecorator withBoth(Account account, String phone, String email) {
        return new NotificationDecorator(account, phone, email, true, true);
    }

    /**
     * Constructeur complet.
     */
    public NotificationDecorator(Account account, String phoneNumber, String email,
                                  boolean smsEnabled, boolean emailEnabled) {
        super(account);
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.smsEnabled = smsEnabled;
        this.emailEnabled = emailEnabled;
        this.lowBalanceThreshold = new BigDecimal("10000"); // 10,000 FCFA
        this.notificationsSent = 0;
    }

    @Override
    public String getDescription() {
        String channels = "";
        if (smsEnabled && emailEnabled) channels = "SMS+Email";
        else if (smsEnabled) channels = "SMS";
        else if (emailEnabled) channels = "Email";

        return wrappedAccount.getDescription() + " + Alertes " + channels;
    }

    @Override
    protected String getDecoratorName() {
        return "Alertes";
    }

    @Override
    public TransactionResult deposit(BigDecimal amount) {
        TransactionResult result = wrappedAccount.deposit(amount);

        if (result.isSuccess()) {
            String message = String.format(
                "DEPOT: +%.0f FCFA sur votre compte %s. Nouveau solde: %.0f FCFA. Ref: %s",
                amount, maskAccountNumber(getAccountNumber()),
                result.getNewBalance(), result.getTransactionId()
            );
            sendNotification("Depot sur votre compte", message);
        }

        return result;
    }

    @Override
    public TransactionResult withdraw(BigDecimal amount) {
        TransactionResult result = wrappedAccount.withdraw(amount);

        if (result.isSuccess()) {
            String message = String.format(
                "RETRAIT: -%.0f FCFA sur votre compte %s. Nouveau solde: %.0f FCFA. Ref: %s",
                amount, maskAccountNumber(getAccountNumber()),
                result.getNewBalance(), result.getTransactionId()
            );
            sendNotification("Retrait sur votre compte", message);

            // Verifier le seuil de solde bas
            checkLowBalance(result.getNewBalance());
        } else {
            // Alerte sur tentative echouee
            String message = String.format(
                "ALERTE: Tentative de retrait de %.0f FCFA echouee sur %s. Raison: %s",
                amount, maskAccountNumber(getAccountNumber()), result.getMessage()
            );
            sendNotification("Tentative de retrait echouee", message);
        }

        return result;
    }

    @Override
    public TransactionResult transfer(Account target, BigDecimal amount) {
        TransactionResult result = wrappedAccount.transfer(target, amount);

        if (result.isSuccess()) {
            String message = String.format(
                "TRANSFERT: %.0f FCFA envoye vers %s. Nouveau solde: %.0f FCFA. Ref: %s",
                amount, maskAccountNumber(target.getAccountNumber()),
                result.getNewBalance(), result.getTransactionId()
            );
            sendNotification("Transfert effectue", message);

            // Verifier le seuil de solde bas
            checkLowBalance(result.getNewBalance());
        }

        return result;
    }

    /**
     * Envoie une notification.
     */
    private void sendNotification(String subject, String message) {
        if (smsEnabled && phoneNumber != null) {
            sendSMS(message);
        }
        if (emailEnabled && email != null) {
            sendEmail(subject, message);
        }
        notificationsSent++;
    }

    /**
     * Simule l'envoi d'un SMS.
     */
    private void sendSMS(String message) {
        System.out.println("[SMS -> " + maskPhoneNumber(phoneNumber) + "] " +
            truncate(message, 50));
    }

    /**
     * Simule l'envoi d'un Email.
     */
    private void sendEmail(String subject, String message) {
        System.out.println("[EMAIL -> " + maskEmail(email) + "] " + subject);
    }

    /**
     * Verifie si le solde est bas et envoie une alerte.
     */
    private void checkLowBalance(BigDecimal balance) {
        if (balance != null && balance.compareTo(lowBalanceThreshold) < 0) {
            String message = String.format(
                "ATTENTION: Votre solde est faible (%.0f FCFA). " +
                "Pensez a approvisionner votre compte %s.",
                balance, maskAccountNumber(getAccountNumber())
            );
            sendNotification("Alerte solde bas", message);
        }
    }

    /**
     * Envoie une alerte personnalisee.
     */
    public void sendCustomAlert(String subject, String message) {
        sendNotification(subject, message);
    }

    /**
     * Masque le numero de compte.
     */
    private String maskAccountNumber(String account) {
        if (account == null || account.length() < 6) return "****";
        return account.substring(0, 4) + "****" + account.substring(account.length() - 2);
    }

    /**
     * Masque le numero de telephone.
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) return "****";
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 3);
    }

    /**
     * Masque l'adresse email.
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        String[] parts = email.split("@");
        String local = parts[0];
        if (local.length() <= 2) return local + "***@" + parts[1];
        return local.substring(0, 2) + "***@" + parts[1];
    }

    /**
     * Tronque un message.
     */
    private String truncate(String message, int maxLength) {
        if (message == null) return "";
        if (message.length() <= maxLength) return message;
        return message.substring(0, maxLength - 3) + "...";
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public int getNotificationsSent() {
        return notificationsSent;
    }

    /**
     * Retourne les parametres de notification.
     */
    public String getNotificationSettings() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PARAMETRES NOTIFICATIONS ===\n");
        if (smsEnabled) {
            sb.append("SMS: Actif -> ").append(maskPhoneNumber(phoneNumber)).append("\n");
        }
        if (emailEnabled) {
            sb.append("Email: Actif -> ").append(maskEmail(email)).append("\n");
        }
        sb.append("Seuil alerte solde bas: ").append(lowBalanceThreshold).append(" FCFA\n");
        sb.append("Notifications envoyees: ").append(notificationsSent).append("\n");
        return sb.toString();
    }
}
