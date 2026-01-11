package com.banque.adapter.legacy;

import java.util.UUID;

/**
 * SERVICE LEGACY - Client REST Twilio (Adaptee)
 *
 * OBJECTIF 5: Cette classe simule l'API Twilio (service international)
 * avec son style REST et ses conventions propres.
 *
 * L'API Twilio utilise:
 * - Format E.164 pour les numéros (+XXXXXXXXXXX)
 * - Réponses sous forme d'objets Message/MessageInstance
 * - Gestion des comptes via SID/Auth Token
 * - Prix en dollars avec tarification internationale
 */
public class TwilioRestClient {

    private final String accountSid;
    private final String authToken;
    private double accountBalance; // En dollars
    private boolean apiEnabled;
    private final double pricePerSMS = 0.0075; // Prix Twilio simulé

    public TwilioRestClient(String accountSid, String authToken) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.accountBalance = 50.0; // 50$ de crédit initial
        this.apiEnabled = true;
    }

    /**
     * Crée et envoie un message via Twilio.
     * ATTENTION: Format et structure Twilio spécifiques!
     *
     * @param to Numéro destinataire (format E.164: +XXXXXXXXXXX)
     * @param from Numéro expéditeur ou Sender ID
     * @param body Corps du message
     * @return MessageInstance Twilio
     */
    public MessageInstance createMessage(String to, String from, String body) {
        long startTime = System.currentTimeMillis();

        // Simulation latence réseau internationale
        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Validation format E.164
        if (!isValidE164(to)) {
            throw new TwilioRestException(21211,
                "The 'To' number " + to + " is not a valid phone number.");
        }

        if (body == null || body.isEmpty()) {
            throw new TwilioRestException(21602, "Message body is required.");
        }

        if (!apiEnabled) {
            throw new TwilioRestException(20003, "Account is suspended.");
        }

        if (accountBalance < pricePerSMS) {
            throw new TwilioRestException(20008, "Insufficient funds in account.");
        }

        // Envoi réussi
        accountBalance -= pricePerSMS;
        String messageSid = "SM" + UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        long duration = System.currentTimeMillis() - startTime;

        return new MessageInstance(
            messageSid,
            accountSid,
            to,
            from,
            body,
            MessageInstance.Status.QUEUED,
            pricePerSMS,
            "USD",
            duration
        );
    }

    /**
     * Récupère le statut d'un message.
     */
    public MessageInstance fetchMessage(String messageSid) {
        // Simulation de récupération
        int hash = messageSid.hashCode();
        MessageInstance.Status status;

        if (hash % 10 < 5) {
            status = MessageInstance.Status.DELIVERED;
        } else if (hash % 10 < 7) {
            status = MessageInstance.Status.SENT;
        } else if (hash % 10 < 9) {
            status = MessageInstance.Status.QUEUED;
        } else {
            status = MessageInstance.Status.FAILED;
        }

        return new MessageInstance(
            messageSid,
            accountSid,
            "+237699000000", // Simulé
            "BANQUE",
            "Message content",
            status,
            pricePerSMS,
            "USD",
            0
        );
    }

    /**
     * Récupère les informations du compte.
     */
    public AccountInstance fetchAccount() {
        return new AccountInstance(
            accountSid,
            "Banque Multi-Operateur",
            AccountInstance.Type.FULL,
            AccountInstance.Status.ACTIVE,
            accountBalance,
            "USD"
        );
    }

    /**
     * Vérifie si le numéro est au format E.164.
     */
    private boolean isValidE164(String number) {
        if (number == null) return false;
        // Format E.164: + suivi de 1 à 15 chiffres
        return number.matches("\\+[1-9][0-9]{1,14}");
    }

    // Setters pour tests
    public void setAccountBalance(double balance) {
        this.accountBalance = balance;
    }

    public void setApiEnabled(boolean enabled) {
        this.apiEnabled = enabled;
    }

    public double getAccountBalance() {
        return accountBalance;
    }

    // ==================== CLASSES TWILIO ====================

    /**
     * Instance de message Twilio.
     */
    public static class MessageInstance {
        public enum Status {
            QUEUED, SENDING, SENT, DELIVERED, UNDELIVERED, FAILED
        }

        private final String sid;
        private final String accountSid;
        private final String to;
        private final String from;
        private final String body;
        private final Status status;
        private final double price;
        private final String priceUnit;
        private final long apiResponseMs;

        public MessageInstance(String sid, String accountSid, String to, String from,
                              String body, Status status, double price, String priceUnit,
                              long apiResponseMs) {
            this.sid = sid;
            this.accountSid = accountSid;
            this.to = to;
            this.from = from;
            this.body = body;
            this.status = status;
            this.price = price;
            this.priceUnit = priceUnit;
            this.apiResponseMs = apiResponseMs;
        }

        public String getSid() { return sid; }
        public String getAccountSid() { return accountSid; }
        public String getTo() { return to; }
        public String getFrom() { return from; }
        public String getBody() { return body; }
        public Status getStatus() { return status; }
        public double getPrice() { return price; }
        public String getPriceUnit() { return priceUnit; }
        public long getApiResponseMs() { return apiResponseMs; }

        @Override
        public String toString() {
            return String.format("MessageInstance[sid=%s, to=%s, status=%s, price=%.4f %s]",
                               sid, to, status, price, priceUnit);
        }
    }

    /**
     * Instance de compte Twilio.
     */
    public static class AccountInstance {
        public enum Type { TRIAL, FULL }
        public enum Status { ACTIVE, SUSPENDED, CLOSED }

        private final String sid;
        private final String friendlyName;
        private final Type type;
        private final Status status;
        private final double balance;
        private final String currency;

        public AccountInstance(String sid, String name, Type type, Status status,
                              double balance, String currency) {
            this.sid = sid;
            this.friendlyName = name;
            this.type = type;
            this.status = status;
            this.balance = balance;
            this.currency = currency;
        }

        public String getSid() { return sid; }
        public String getFriendlyName() { return friendlyName; }
        public Type getType() { return type; }
        public Status getStatus() { return status; }
        public double getBalance() { return balance; }
        public String getCurrency() { return currency; }
    }

    /**
     * Exception Twilio REST.
     */
    public static class TwilioRestException extends RuntimeException {
        private final int errorCode;

        public TwilioRestException(int code, String message) {
            super(message);
            this.errorCode = code;
        }

        public int getErrorCode() { return errorCode; }
    }
}
