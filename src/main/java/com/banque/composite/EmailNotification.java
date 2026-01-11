package com.banque.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PATTERN COMPOSITE - Leaf (Notification Email)
 *
 * OBJECTIF 7: Notification Email simple (feuille de l'arbre).
 * Ne peut pas contenir d'autres notifications.
 */
public class EmailNotification implements Notification {

    private final String emailAddress;
    private final String subject;
    private final String body;
    private final Priority priority;
    private final List<String> ccList;
    private final List<String> attachments;
    private final boolean isHtml;

    public EmailNotification(String emailAddress, String subject, String body) {
        this(emailAddress, subject, body, Priority.NORMAL, false);
    }

    public EmailNotification(String emailAddress, String subject, String body,
                            Priority priority, boolean isHtml) {
        this.emailAddress = emailAddress;
        this.subject = subject;
        this.body = body;
        this.priority = priority;
        this.isHtml = isHtml;
        this.ccList = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    @Override
    public NotificationResult send() {
        String notificationId = "EMAIL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Validation
        if (!isValidEmail(emailAddress)) {
            return NotificationResult.failure(notificationId, NotificationType.EMAIL,
                emailAddress, "Adresse email invalide");
        }

        if (subject == null || subject.isEmpty()) {
            return NotificationResult.failure(notificationId, NotificationType.EMAIL,
                emailAddress, "Sujet de l'email vide");
        }

        if (body == null || body.isEmpty()) {
            return NotificationResult.failure(notificationId, NotificationType.EMAIL,
                emailAddress, "Corps de l'email vide");
        }

        // Simulation de l'envoi Email
        try {
            Thread.sleep(50); // Latence SMTP simulee
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Log de l'envoi
        System.out.println("[EMAIL] Envoi a " + maskEmail(emailAddress) +
            " [" + priority.getLabel() + "]");
        System.out.println("  Sujet: " + truncate(subject, 40));
        if (!ccList.isEmpty()) {
            System.out.println("  CC: " + ccList.size() + " destinataire(s)");
        }
        if (!attachments.isEmpty()) {
            System.out.println("  Pieces jointes: " + attachments.size());
        }

        return NotificationResult.success(notificationId, NotificationType.EMAIL,
            emailAddress, "Email envoye avec succes");
    }

    @Override
    public NotificationType getType() {
        return NotificationType.EMAIL;
    }

    @Override
    public String getRecipient() {
        return emailAddress;
    }

    @Override
    public String getContent() {
        return body;
    }

    public String getSubject() {
        return subject;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isHtml() {
        return isHtml;
    }

    // ==================== BUILDER METHODS ====================

    public EmailNotification addCc(String email) {
        if (isValidEmail(email)) {
            ccList.add(email);
        }
        return this;
    }

    public EmailNotification addAttachment(String filename) {
        attachments.add(filename);
        return this;
    }

    public List<String> getCcList() {
        return new ArrayList<>(ccList);
    }

    public List<String> getAttachments() {
        return new ArrayList<>(attachments);
    }

    // ==================== METHODES UTILITAIRES ====================

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        String[] parts = email.split("@");
        String local = parts[0];
        if (local.length() <= 2) return local + "***@" + parts[1];
        return local.substring(0, 2) + "***@" + parts[1];
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    @Override
    public String toString() {
        return String.format("EmailNotification[to=%s, subject=%s, priority=%s]",
            maskEmail(emailAddress), truncate(subject, 20), priority.getLabel());
    }
}
