package com.banque.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * PATTERN COMPOSITE - Composite (Groupe de Notifications)
 *
 * OBJECTIF 7: Groupe de notifications qui peut contenir d'autres
 * notifications (Leaf ou Composite). Permet de traiter uniformement
 * les notifications individuelles et les groupes.
 *
 * CARACTERISTIQUES:
 * - Peut contenir des SMS, Email, Push ou d'autres groupes
 * - Envoie toutes les notifications contenues lors de send()
 * - Agregation des resultats
 * - Supporte l'imbrication (groupes dans des groupes)
 */
public class NotificationGroup implements Notification {

    private final String groupId;
    private final String groupName;
    private final List<Notification> notifications;
    private final boolean stopOnFirstFailure;

    public NotificationGroup(String groupName) {
        this(groupName, false);
    }

    public NotificationGroup(String groupName, boolean stopOnFirstFailure) {
        this.groupId = "GRP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.groupName = groupName;
        this.notifications = new ArrayList<>();
        this.stopOnFirstFailure = stopOnFirstFailure;
    }

    /**
     * Ajoute une notification au groupe.
     */
    public NotificationGroup add(Notification notification) {
        if (notification != null && notification != this) {
            notifications.add(notification);
        }
        return this;
    }

    /**
     * Ajoute plusieurs notifications au groupe.
     */
    public NotificationGroup addAll(Notification... notifications) {
        for (Notification n : notifications) {
            add(n);
        }
        return this;
    }

    /**
     * Retire une notification du groupe.
     */
    public NotificationGroup remove(Notification notification) {
        notifications.remove(notification);
        return this;
    }

    /**
     * Vide le groupe.
     */
    public void clear() {
        notifications.clear();
    }

    @Override
    public NotificationResult send() {
        System.out.println("\n[GROUPE] Envoi du groupe '" + groupName + "' (" +
            getNotificationCount() + " notification(s))");
        System.out.println("----------------------------------------");

        List<NotificationResult> results = new ArrayList<>();

        for (Notification notification : notifications) {
            NotificationResult result = notification.send();
            results.add(result);

            // Arreter si echec et option activee
            if (stopOnFirstFailure && !result.isSuccess()) {
                System.out.println("[GROUPE] Arret sur premier echec");
                break;
            }
        }

        System.out.println("----------------------------------------");
        int sent = results.stream().mapToInt(NotificationResult::getTotalSent).sum();
        int failed = results.stream().mapToInt(NotificationResult::getTotalFailed).sum();
        System.out.println("[GROUPE] Termine: " + sent + " envoye(s), " + failed + " echec(s)\n");

        return NotificationResult.group(groupId, results);
    }

    @Override
    public NotificationType getType() {
        return NotificationType.GROUP;
    }

    @Override
    public String getRecipient() {
        return groupName + " (" + notifications.size() + " destinataires)";
    }

    @Override
    public String getContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("Groupe '").append(groupName).append("' contenant:\n");
        for (Notification n : notifications) {
            sb.append("  - ").append(n.getType().getCode())
              .append(" -> ").append(n.getRecipient()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public int getNotificationCount() {
        int count = 0;
        for (Notification n : notifications) {
            count += n.getNotificationCount();
        }
        return count;
    }

    /**
     * Retourne le nombre d'enfants directs (sans recursion).
     */
    public int getDirectChildCount() {
        return notifications.size();
    }

    /**
     * Retourne les notifications contenues (copie).
     */
    public List<Notification> getNotifications() {
        return new ArrayList<>(notifications);
    }

    /**
     * Retourne le nom du groupe.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Retourne l'ID du groupe.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Compte les notifications par type.
     */
    public java.util.Map<NotificationType, Integer> countByType() {
        java.util.Map<NotificationType, Integer> counts = new java.util.HashMap<>();
        countByTypeRecursive(this, counts);
        return counts;
    }

    private void countByTypeRecursive(Notification n, java.util.Map<NotificationType, Integer> counts) {
        if (n.isComposite()) {
            NotificationGroup group = (NotificationGroup) n;
            for (Notification child : group.notifications) {
                countByTypeRecursive(child, counts);
            }
        } else {
            counts.merge(n.getType(), 1, Integer::sum);
        }
    }

    @Override
    public String toString() {
        return String.format("NotificationGroup[name=%s, id=%s, children=%d, total=%d]",
            groupName, groupId, notifications.size(), getNotificationCount());
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Cree un groupe multi-canal pour un seul destinataire.
     */
    public static NotificationGroup multiChannel(String name, String phone, String email,
                                                  String deviceToken, String message) {
        NotificationGroup group = new NotificationGroup(name);

        if (phone != null && !phone.isEmpty()) {
            group.add(new SMSNotification(phone, message));
        }
        if (email != null && !email.isEmpty()) {
            group.add(new EmailNotification(email, "Notification", message));
        }
        if (deviceToken != null && !deviceToken.isEmpty()) {
            group.add(new PushNotification(deviceToken, "Notification", message));
        }

        return group;
    }

    /**
     * Cree un groupe de diffusion SMS.
     */
    public static NotificationGroup smsBroadcast(String name, List<String> phoneNumbers, String message) {
        NotificationGroup group = new NotificationGroup(name);
        for (String phone : phoneNumbers) {
            group.add(new SMSNotification(phone, message));
        }
        return group;
    }

    /**
     * Cree un groupe de diffusion Email.
     */
    public static NotificationGroup emailBroadcast(String name, List<String> emails,
                                                    String subject, String body) {
        NotificationGroup group = new NotificationGroup(name);
        for (String email : emails) {
            group.add(new EmailNotification(email, subject, body));
        }
        return group;
    }
}
