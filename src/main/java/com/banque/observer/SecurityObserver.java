package com.banque.observer;

import java.math.BigDecimal;
import java.util.*;

/**
 * PATTERN OBSERVER - ConcreteObserver (Securite)
 *
 * OBJECTIF 10: Observateur qui surveille les evenements de securite.
 */
public class SecurityObserver implements EventObserver {

    private final Map<String, List<BankEvent>> failedLoginAttempts;
    private final List<SecurityAlert> alerts;
    private final BigDecimal suspiciousAmountThreshold;
    private final int maxFailedLogins;

    public SecurityObserver() {
        this.failedLoginAttempts = new HashMap<>();
        this.alerts = new ArrayList<>();
        this.suspiciousAmountThreshold = new BigDecimal("2000000");
        this.maxFailedLogins = 3;
    }

    @Override
    public void onEvent(BankEvent event) {
        switch (event.getEventType()) {
            case LOGIN_FAILED:
                handleLoginFailed(event);
                break;
            case WITHDRAWAL_MADE:
            case TRANSFER_MADE:
                checkSuspiciousAmount(event);
                break;
            case SUSPICIOUS_ACTIVITY:
            case FRAUD_DETECTED:
                handleSecurityEvent(event);
                break;
            default:
                // Ignorer les autres evenements
                break;
        }
    }

    @Override
    public String getObserverName() {
        return "SecurityObserver";
    }

    @Override
    public boolean isInterestedIn(BankEvent.EventType eventType) {
        switch (eventType) {
            case LOGIN_FAILED:
            case LOGIN_SUCCESS:
            case WITHDRAWAL_MADE:
            case TRANSFER_MADE:
            case SUSPICIOUS_ACTIVITY:
            case FRAUD_DETECTED:
                return true;
            default:
                return false;
        }
    }

    private void handleLoginFailed(BankEvent event) {
        String userId = event.getDataAsString("userId");
        if (userId == null) return;

        failedLoginAttempts.computeIfAbsent(userId, k -> new ArrayList<>()).add(event);

        List<BankEvent> attempts = failedLoginAttempts.get(userId);

        if (attempts.size() >= maxFailedLogins) {
            // Creer une alerte
            SecurityAlert alert = new SecurityAlert(
                AlertType.ACCOUNT_LOCKED,
                "Compte bloque apres " + attempts.size() + " tentatives echouees",
                userId,
                event.getEventId()
            );
            alerts.add(alert);

            System.out.println("  [SECURITE] ALERTE: Compte " + userId +
                " bloque - Trop de tentatives echouees!");

            // Reinitialiser le compteur
            failedLoginAttempts.remove(userId);
        } else {
            System.out.println("  [SECURITE] Tentative echouee " + attempts.size() +
                "/" + maxFailedLogins + " pour " + userId);
        }
    }

    private void checkSuspiciousAmount(BankEvent event) {
        BigDecimal amount = event.getAmount();
        if (amount == null) return;

        if (amount.compareTo(suspiciousAmountThreshold) > 0) {
            SecurityAlert alert = new SecurityAlert(
                AlertType.HIGH_VALUE_TRANSACTION,
                "Transaction de " + amount + " FCFA detectee",
                event.getAccount(),
                event.getEventId()
            );
            alerts.add(alert);

            System.out.println("  [SECURITE] ALERTE: Transaction elevee detectee - " +
                amount + " FCFA sur " + event.getAccount());
        }
    }

    private void handleSecurityEvent(BankEvent event) {
        AlertType alertType = event.getEventType() == BankEvent.EventType.FRAUD_DETECTED ?
            AlertType.FRAUD : AlertType.SUSPICIOUS;

        SecurityAlert alert = new SecurityAlert(
            alertType,
            event.getDataAsString("message"),
            event.getAccount(),
            event.getEventId()
        );
        alerts.add(alert);

        System.out.println("  [SECURITE] ALERTE CRITIQUE: " + event.getEventType() +
            " - " + event.getAccount());
    }

    public List<SecurityAlert> getAlerts() {
        return new ArrayList<>(alerts);
    }

    public int getAlertCount() {
        return alerts.size();
    }

    public void clearAlerts() {
        alerts.clear();
    }

    public void resetFailedAttempts(String userId) {
        failedLoginAttempts.remove(userId);
    }

    // ==================== CLASSES INTERNES ====================

    public enum AlertType {
        ACCOUNT_LOCKED("Compte bloque"),
        HIGH_VALUE_TRANSACTION("Transaction elevee"),
        SUSPICIOUS("Activite suspecte"),
        FRAUD("Fraude");

        private final String description;

        AlertType(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    public static class SecurityAlert {
        private final AlertType type;
        private final String message;
        private final String account;
        private final String eventId;
        private final long timestamp;

        public SecurityAlert(AlertType type, String message, String account, String eventId) {
            this.type = type;
            this.message = message;
            this.account = account;
            this.eventId = eventId;
            this.timestamp = System.currentTimeMillis();
        }

        public AlertType getType() { return type; }
        public String getMessage() { return message; }
        public String getAccount() { return account; }
        public String getEventId() { return eventId; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("Alert[%s, %s, %s]", type, account, message);
        }
    }
}
