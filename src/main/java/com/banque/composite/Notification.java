package com.banque.composite;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PATTERN COMPOSITE - Interface Component
 *
 * OBJECTIF 7: Cette interface definit le contrat commun pour toutes les
 * notifications, qu'elles soient simples (Leaf) ou composees (Composite).
 *
 * POURQUOI COMPOSITE?
 * - Traiter uniformement les notifications individuelles et les groupes
 * - Composer des notifications complexes (multi-canal)
 * - Creer des hierarchies de notifications
 * - Simplifier le code client (meme interface pour tous)
 *
 * STRUCTURE:
 * - Notification (Component) : cette interface
 * - SMSNotification, EmailNotification, PushNotification (Leaf)
 * - NotificationGroup (Composite) : contient d'autres Notification
 */
public interface Notification {

    /**
     * Envoie la notification.
     * Pour un Leaf: envoie une notification simple.
     * Pour un Composite: envoie toutes les notifications contenues.
     *
     * @return Resultat de l'envoi
     */
    NotificationResult send();

    /**
     * Retourne le type de notification.
     */
    NotificationType getType();

    /**
     * Retourne le destinataire principal.
     */
    String getRecipient();

    /**
     * Retourne le contenu/message.
     */
    String getContent();

    /**
     * Verifie si la notification est un groupe (Composite).
     */
    default boolean isComposite() {
        return false;
    }

    /**
     * Retourne le nombre de notifications (1 pour Leaf, N pour Composite).
     */
    default int getNotificationCount() {
        return 1;
    }

    // ==================== TYPES ET CLASSES ====================

    /**
     * Types de notifications.
     */
    enum NotificationType {
        SMS("SMS", "Texto"),
        EMAIL("Email", "Courrier electronique"),
        PUSH("Push", "Notification mobile"),
        GROUP("Groupe", "Groupe de notifications"),
        TEMPLATE("Template", "Notification basee sur template");

        private final String code;
        private final String description;

        NotificationType(String code, String desc) {
            this.code = code;
            this.description = desc;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }

    /**
     * Priorite de notification.
     */
    enum Priority {
        LOW(0, "Basse"),
        NORMAL(1, "Normale"),
        HIGH(2, "Haute"),
        URGENT(3, "Urgente");

        private final int level;
        private final String label;

        Priority(int level, String label) {
            this.level = level;
            this.label = label;
        }

        public int getLevel() { return level; }
        public String getLabel() { return label; }
    }

    /**
     * Resultat d'envoi de notification.
     */
    class NotificationResult {
        private final boolean success;
        private final String notificationId;
        private final NotificationType type;
        private final String recipient;
        private final String message;
        private final LocalDateTime sentAt;
        private final List<NotificationResult> childResults; // Pour Composite
        private final int totalSent;
        private final int totalFailed;

        // Constructeur pour Leaf
        public NotificationResult(boolean success, String id, NotificationType type,
                                 String recipient, String message) {
            this.success = success;
            this.notificationId = id;
            this.type = type;
            this.recipient = recipient;
            this.message = message;
            this.sentAt = LocalDateTime.now();
            this.childResults = null;
            this.totalSent = success ? 1 : 0;
            this.totalFailed = success ? 0 : 1;
        }

        // Constructeur pour Composite
        public NotificationResult(String groupId, List<NotificationResult> children) {
            this.notificationId = groupId;
            this.type = NotificationType.GROUP;
            this.recipient = "Groupe";
            this.message = "Groupe de " + children.size() + " notifications";
            this.sentAt = LocalDateTime.now();
            this.childResults = children;

            int sent = 0, failed = 0;
            for (NotificationResult child : children) {
                sent += child.getTotalSent();
                failed += child.getTotalFailed();
            }
            this.totalSent = sent;
            this.totalFailed = failed;
            this.success = failed == 0;
        }

        public static NotificationResult success(String id, NotificationType type,
                                                  String recipient, String message) {
            return new NotificationResult(true, id, type, recipient, message);
        }

        public static NotificationResult failure(String id, NotificationType type,
                                                  String recipient, String errorMessage) {
            return new NotificationResult(false, id, type, recipient, errorMessage);
        }

        public static NotificationResult group(String groupId, List<NotificationResult> children) {
            return new NotificationResult(groupId, children);
        }

        public boolean isSuccess() { return success; }
        public String getNotificationId() { return notificationId; }
        public NotificationType getType() { return type; }
        public String getRecipient() { return recipient; }
        public String getMessage() { return message; }
        public LocalDateTime getSentAt() { return sentAt; }
        public List<NotificationResult> getChildResults() { return childResults; }
        public int getTotalSent() { return totalSent; }
        public int getTotalFailed() { return totalFailed; }
        public boolean isGroup() { return childResults != null; }

        public double getSuccessRate() {
            int total = totalSent + totalFailed;
            return total > 0 ? (double) totalSent / total * 100 : 0;
        }

        @Override
        public String toString() {
            if (isGroup()) {
                return String.format("NotificationGroup[id=%s, sent=%d, failed=%d, rate=%.1f%%]",
                    notificationId, totalSent, totalFailed, getSuccessRate());
            }
            return String.format("Notification[%s, id=%s, to=%s, success=%s]",
                type.getCode(), notificationId, recipient, success);
        }
    }
}
