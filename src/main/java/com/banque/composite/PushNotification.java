package com.banque.composite;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PATTERN COMPOSITE - Leaf (Notification Push)
 *
 * OBJECTIF 7: Notification Push mobile simple (feuille de l'arbre).
 * Ne peut pas contenir d'autres notifications.
 */
public class PushNotification implements Notification {

    private final String deviceToken;
    private final String title;
    private final String body;
    private final Priority priority;
    private final String icon;
    private final String clickAction;
    private final Map<String, String> data;

    public PushNotification(String deviceToken, String title, String body) {
        this(deviceToken, title, body, Priority.NORMAL);
    }

    public PushNotification(String deviceToken, String title, String body, Priority priority) {
        this.deviceToken = deviceToken;
        this.title = title;
        this.body = body;
        this.priority = priority;
        this.icon = "ic_notification";
        this.clickAction = null;
        this.data = new HashMap<>();
    }

    @Override
    public NotificationResult send() {
        String notificationId = "PUSH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Validation
        if (deviceToken == null || deviceToken.isEmpty()) {
            return NotificationResult.failure(notificationId, NotificationType.PUSH,
                deviceToken, "Token d'appareil invalide");
        }

        if (title == null || title.isEmpty()) {
            return NotificationResult.failure(notificationId, NotificationType.PUSH,
                deviceToken, "Titre de notification vide");
        }

        // Simulation de l'envoi Push (Firebase/APNs)
        try {
            Thread.sleep(25); // Latence Push simulee
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Log de l'envoi
        System.out.println("[PUSH] Envoi a appareil " + maskToken(deviceToken) +
            " [" + priority.getLabel() + "]");
        System.out.println("  Titre: " + title);
        System.out.println("  Message: " + truncate(body, 40));
        if (!data.isEmpty()) {
            System.out.println("  Data: " + data.size() + " champ(s)");
        }

        return NotificationResult.success(notificationId, NotificationType.PUSH,
            deviceToken, "Notification push envoyee");
    }

    @Override
    public NotificationType getType() {
        return NotificationType.PUSH;
    }

    @Override
    public String getRecipient() {
        return deviceToken;
    }

    @Override
    public String getContent() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getIcon() {
        return icon;
    }

    // ==================== BUILDER METHODS ====================

    public PushNotification withData(String key, String value) {
        data.put(key, value);
        return this;
    }

    public PushNotification withClickAction(String action) {
        return new PushNotification(deviceToken, title, body, priority) {
            @Override
            public String toString() {
                return PushNotification.this.toString() + " -> " + action;
            }
        };
    }

    public Map<String, String> getData() {
        return new HashMap<>(data);
    }

    // ==================== METHODES UTILITAIRES ====================

    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "****";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    @Override
    public String toString() {
        return String.format("PushNotification[device=%s, title=%s, priority=%s]",
            maskToken(deviceToken), title, priority.getLabel());
    }
}
