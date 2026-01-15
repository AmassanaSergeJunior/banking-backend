package com.banque.observer;

import java.util.*;

/**
 * PATTERN OBSERVER - ConcreteObserver (Notifications)
 *
 * OBJECTIF 10: Observateur qui envoie des notifications SMS/Email.
 */
public class NotificationObserver implements EventObserver {

    private final Map<String, ContactInfo> contacts;
    private final List<SentNotification> sentNotifications;
    private final Set<BankEvent.EventType> notifiableEvents;

    public NotificationObserver() {
        this.contacts = new HashMap<>();
        this.sentNotifications = new ArrayList<>();
        this.notifiableEvents = new HashSet<>();

        // Evenements qui declenchent des notifications par defauts
        notifiableEvents.add(BankEvent.EventType.DEPOSIT_MADE);
        notifiableEvents.add(BankEvent.EventType.WITHDRAWAL_MADE);
        notifiableEvents.add(BankEvent.EventType.TRANSFER_MADE);
        notifiableEvents.add(BankEvent.EventType.LOW_BALANCE);
        notifiableEvents.add(BankEvent.EventType.SUSPICIOUS_ACTIVITY);
        notifiableEvents.add(BankEvent.EventType.FRAUD_DETECTED);
        notifiableEvents.add(BankEvent.EventType.LOGIN_FAILED);
    }

    @Override
    public void onEvent(BankEvent event) {
        String account = event.getAccount();
        if (account == null) return;

        ContactInfo contact = contacts.get(account);
        if (contact == null) return;

        String message = createMessage(event);

        // Envoyer SMS
        if (contact.getPhone() != null) {
            sendSMS(contact.getPhone(), message, event);
        }

        // Envoyer Email pour les evenements critiques
        if (contact.getEmail() != null &&
            event.getSeverity().ordinal() >= BankEvent.EventSeverity.WARNING.ordinal()) {
            sendEmail(contact.getEmail(), event.getEventType().getDescription(), message, event);
        }
    }

    @Override
    public String getObserverName() {
        return "NotificationObserver";
    }

    @Override
    public boolean isInterestedIn(BankEvent.EventType eventType) {
        return notifiableEvents.contains(eventType);
    }

    private String createMessage(BankEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("BANQUE: ").append(event.getEventType().getDescription());

        if (event.getAmount() != null) {
            sb.append(" - ").append(event.getAmount()).append(" FCFA");
        }

        String msg = event.getDataAsString("message");
        if (msg != null) {
            sb.append(". ").append(msg);
        }

        return sb.toString();
    }

    private void sendSMS(String phone, String message, BankEvent event) {
        System.out.println("  [SMS -> " + maskPhone(phone) + "] " + truncate(message, 40));

        sentNotifications.add(new SentNotification(
            "SMS", phone, message, event.getEventId()));
    }

    private void sendEmail(String email, String subject, String body, BankEvent event) {
        System.out.println("  [EMAIL -> " + maskEmail(email) + "] " + subject);

        sentNotifications.add(new SentNotification(
            "EMAIL", email, body, event.getEventId()));
    }

    // ==================== GESTION DES CONTACTS ====================

    public void registerContact(String accountNumber, String phone, String email) {
        contacts.put(accountNumber, new ContactInfo(phone, email));
    }

    public void removeContact(String accountNumber) {
        contacts.remove(accountNumber);
    }

    public void addNotifiableEvent(BankEvent.EventType eventType) {
        notifiableEvents.add(eventType);
    }

    public void removeNotifiableEvent(BankEvent.EventType eventType) {
        notifiableEvents.remove(eventType);
    }

    public List<SentNotification> getSentNotifications() {
        return new ArrayList<>(sentNotifications);
    }

    public int getNotificationCount() {
        return sentNotifications.size();
    }

    // ==================== UTILITAIRES ====================

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "****";
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 3);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        String[] parts = email.split("@");
        return parts[0].substring(0, 2) + "***@" + parts[1];
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    // ==================== CLASSES INTERNES ====================

    public static class ContactInfo {
        private final String phone;
        private final String email;

        public ContactInfo(String phone, String email) {
            this.phone = phone;
            this.email = email;
        }

        public String getPhone() { return phone; }
        public String getEmail() { return email; }
    }

    public static class SentNotification {
        private final String type;
        private final String recipient;
        private final String message;
        private final String eventId;

        public SentNotification(String type, String recipient, String message, String eventId) {
            this.type = type;
            this.recipient = recipient;
            this.message = message;
            this.eventId = eventId;
        }

        public String getType() { return type; }
        public String getRecipient() { return recipient; }
        public String getMessage() { return message; }
        public String getEventId() { return eventId; }
    }
}
