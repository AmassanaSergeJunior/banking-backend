package com.banque.composite;

import java.util.UUID;

/**
 * PATTERN COMPOSITE - Leaf (Notification SMS)
 *
 * OBJECTIF 7: Notification SMS simple (feuille de l'arbre).
 * Ne peut pas contenir d'autres notifications.
 */
public class SMSNotification implements Notification {

    private final String phoneNumber;
    private final String message;
    private final Priority priority;
    private final String senderId;

    public SMSNotification(String phoneNumber, String message) {
        this(phoneNumber, message, Priority.NORMAL, "BANQUE");
    }

    public SMSNotification(String phoneNumber, String message, Priority priority) {
        this(phoneNumber, message, priority, "BANQUE");
    }

    public SMSNotification(String phoneNumber, String message, Priority priority, String senderId) {
        this.phoneNumber = normalizePhoneNumber(phoneNumber);
        this.message = message;
        this.priority = priority;
        this.senderId = senderId;
    }

    @Override
    public NotificationResult send() {
        String notificationId = "SMS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Validation
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return NotificationResult.failure(notificationId, NotificationType.SMS,
                phoneNumber, "Numero de telephone invalide");
        }

        if (message == null || message.isEmpty()) {
            return NotificationResult.failure(notificationId, NotificationType.SMS,
                phoneNumber, "Message vide");
        }

        // Simulation de l'envoi SMS
        try {
            Thread.sleep(30); // Latence reseau simulee
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Log de l'envoi
        System.out.println("[SMS] Envoi a " + maskPhoneNumber(phoneNumber) +
            " [" + priority.getLabel() + "]: " + truncateMessage(message, 30));

        return NotificationResult.success(notificationId, NotificationType.SMS,
            phoneNumber, "SMS envoye avec succes");
    }

    @Override
    public NotificationType getType() {
        return NotificationType.SMS;
    }

    @Override
    public String getRecipient() {
        return phoneNumber;
    }

    @Override
    public String getContent() {
        return message;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getSenderId() {
        return senderId;
    }

    // ==================== METHODES UTILITAIRES ====================

    private String normalizePhoneNumber(String phone) {
        if (phone == null) return null;
        String cleaned = phone.replaceAll("[^0-9+]", "");
        if (!cleaned.startsWith("+") && cleaned.startsWith("237")) {
            cleaned = "+" + cleaned;
        }
        if (cleaned.length() == 9 && cleaned.matches("[6][0-9]{8}")) {
            cleaned = "+237" + cleaned;
        }
        return cleaned;
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) return "****";
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 3);
    }

    private String truncateMessage(String msg, int maxLength) {
        if (msg == null) return "";
        if (msg.length() <= maxLength) return msg;
        return msg.substring(0, maxLength - 3) + "...";
    }

    @Override
    public String toString() {
        return String.format("SMSNotification[to=%s, priority=%s, message=%s]",
            maskPhoneNumber(phoneNumber), priority.getLabel(), truncateMessage(message, 20));
    }
}
