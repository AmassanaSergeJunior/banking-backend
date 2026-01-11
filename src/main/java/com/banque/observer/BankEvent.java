package com.banque.observer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PATTERN OBSERVER - Event (Evenement)
 *
 * OBJECTIF 10: Classe representant un evenement bancaire.
 * Les observateurs sont notifies lorsqu'un evenement se produit.
 *
 * POURQUOI OBSERVER?
 * - Decouplage entre les emetteurs et les recepteurs d'evenements
 * - Notification automatique de plusieurs composants
 * - Extensibilite (ajouter des observateurs sans modifier le code existant)
 * - Ideal pour: notifications, logs, analytics, securite
 */
public class BankEvent {

    private final String eventId;
    private final EventType eventType;
    private final String source;
    private final LocalDateTime timestamp;
    private final Map<String, Object> data;
    private final EventSeverity severity;

    public BankEvent(EventType eventType, String source) {
        this(eventType, source, EventSeverity.INFO);
    }

    public BankEvent(EventType eventType, String source, EventSeverity severity) {
        this.eventId = "EVT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.eventType = eventType;
        this.source = source;
        this.timestamp = LocalDateTime.now();
        this.data = new HashMap<>();
        this.severity = severity;
    }

    // ==================== BUILDER METHODS ====================

    public BankEvent withData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public BankEvent withAmount(BigDecimal amount) {
        return withData("amount", amount);
    }

    public BankEvent withAccount(String accountNumber) {
        return withData("account", accountNumber);
    }

    public BankEvent withMessage(String message) {
        return withData("message", message);
    }

    public BankEvent withUser(String userId) {
        return withData("userId", userId);
    }

    // ==================== GETTERS ====================

    public String getEventId() {
        return eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public String getDataAsString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    public BigDecimal getAmount() {
        Object amount = data.get("amount");
        if (amount instanceof BigDecimal) {
            return (BigDecimal) amount;
        }
        return null;
    }

    public String getAccount() {
        return getDataAsString("account");
    }

    public EventSeverity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return String.format("BankEvent[id=%s, type=%s, source=%s, severity=%s]",
            eventId, eventType, source, severity);
    }

    // ==================== TYPES D'EVENEMENTS ====================

    /**
     * Types d'evenements bancaires.
     */
    public enum EventType {
        // Transactions
        TRANSACTION_CREATED("Transaction creee"),
        TRANSACTION_COMPLETED("Transaction terminee"),
        TRANSACTION_FAILED("Transaction echouee"),
        DEPOSIT_MADE("Depot effectue"),
        WITHDRAWAL_MADE("Retrait effectue"),
        TRANSFER_MADE("Transfert effectue"),

        // Comptes
        ACCOUNT_CREATED("Compte cree"),
        ACCOUNT_UPDATED("Compte modifie"),
        ACCOUNT_CLOSED("Compte ferme"),
        LOW_BALANCE("Solde bas"),
        OVERDRAFT_USED("Decouvert utilise"),

        // Securite
        LOGIN_SUCCESS("Connexion reussie"),
        LOGIN_FAILED("Echec de connexion"),
        PASSWORD_CHANGED("Mot de passe change"),
        SUSPICIOUS_ACTIVITY("Activite suspecte"),
        FRAUD_DETECTED("Fraude detectee"),

        // Systeme
        SYSTEM_ALERT("Alerte systeme"),
        MAINTENANCE_SCHEDULED("Maintenance planifiee"),
        ERROR_OCCURRED("Erreur survenue");

        private final String description;

        EventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Niveaux de severite.
     */
    public enum EventSeverity {
        DEBUG("Debug"),
        INFO("Information"),
        WARNING("Avertissement"),
        ERROR("Erreur"),
        CRITICAL("Critique");

        private final String label;

        EventSeverity(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // ==================== FACTORY METHODS ====================

    public static BankEvent transactionCompleted(String account, BigDecimal amount, String type) {
        return new BankEvent(EventType.TRANSACTION_COMPLETED, "TransactionService")
            .withAccount(account)
            .withAmount(amount)
            .withData("transactionType", type);
    }

    public static BankEvent depositMade(String account, BigDecimal amount) {
        return new BankEvent(EventType.DEPOSIT_MADE, "DepositService")
            .withAccount(account)
            .withAmount(amount);
    }

    public static BankEvent withdrawalMade(String account, BigDecimal amount) {
        return new BankEvent(EventType.WITHDRAWAL_MADE, "WithdrawalService")
            .withAccount(account)
            .withAmount(amount);
    }

    public static BankEvent lowBalance(String account, BigDecimal balance, BigDecimal threshold) {
        return new BankEvent(EventType.LOW_BALANCE, "BalanceMonitor", EventSeverity.WARNING)
            .withAccount(account)
            .withData("balance", balance)
            .withData("threshold", threshold);
    }

    public static BankEvent loginSuccess(String userId, String ipAddress) {
        return new BankEvent(EventType.LOGIN_SUCCESS, "AuthService")
            .withUser(userId)
            .withData("ipAddress", ipAddress);
    }

    public static BankEvent loginFailed(String userId, String reason) {
        return new BankEvent(EventType.LOGIN_FAILED, "AuthService", EventSeverity.WARNING)
            .withUser(userId)
            .withData("reason", reason);
    }

    public static BankEvent suspiciousActivity(String account, String description) {
        return new BankEvent(EventType.SUSPICIOUS_ACTIVITY, "SecurityService", EventSeverity.ERROR)
            .withAccount(account)
            .withMessage(description);
    }

    public static BankEvent fraudDetected(String account, String details) {
        return new BankEvent(EventType.FRAUD_DETECTED, "FraudDetection", EventSeverity.CRITICAL)
            .withAccount(account)
            .withMessage(details);
    }

    public static BankEvent systemError(String message, String stackTrace) {
        return new BankEvent(EventType.ERROR_OCCURRED, "System", EventSeverity.ERROR)
            .withMessage(message)
            .withData("stackTrace", stackTrace);
    }
}
