package com.banque.adapter.legacy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SERVICE LEGACY - Gateway SMS MTN Cameroun (Adaptee)
 *
 * OBJECTIF 5: Cette classe simule l'API propriétaire de MTN Cameroun
 * avec une architecture orientée objet différente de notre interface standard.
 *
 * L'API MTN utilise:
 * - Classes de requête/réponse spécifiques (MTNRequest, MTNResponse)
 * - Méthodes en anglais avec conventions différentes
 * - Format de numéro spécifique (+237 obligatoire)
 * - Système de callback pour les notifications de livraison
 */
public class MTNCamerounSMSGateway {

    private final String clientId;
    private final String clientSecret;
    private int smsUnits;
    private boolean gatewayOnline;
    private String callbackUrl;

    public MTNCamerounSMSGateway(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.smsUnits = 500; // Simulation
        this.gatewayOnline = true;
    }

    /**
     * Envoie un SMS via le gateway MTN.
     * ATTENTION: Utilise des objets Request/Response spécifiques!
     *
     * @param request Objet de requête MTN
     * @return Objet de réponse MTN
     */
    public MTNResponse dispatchSMS(MTNRequest request) {
        long startTime = System.currentTimeMillis();

        // Simulation latence
        try {
            Thread.sleep(60);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Validation
        if (!validateMTNNumber(request.getRecipient())) {
            return MTNResponse.error("INVALID_RECIPIENT",
                "Le numero doit commencer par +237 suivi de 9 chiffres");
        }

        if (request.getContent() == null || request.getContent().isEmpty()) {
            return MTNResponse.error("EMPTY_CONTENT", "Le contenu du message est vide");
        }

        if (!gatewayOnline) {
            return MTNResponse.error("GATEWAY_OFFLINE", "Le gateway MTN est hors ligne");
        }

        if (smsUnits < 1) {
            return MTNResponse.error("INSUFFICIENT_UNITS", "Unites SMS insuffisantes");
        }

        // Succès
        smsUnits--;
        String transactionId = "MTN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        long processingTime = System.currentTimeMillis() - startTime;

        return MTNResponse.success(transactionId, request.getRecipient(), processingTime);
    }

    /**
     * Envoie des SMS en masse.
     */
    public MTNBatchResponse dispatchBulkSMS(List<MTNRequest> requests) {
        List<MTNResponse> responses = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (MTNRequest request : requests) {
            MTNResponse response = dispatchSMS(request);
            responses.add(response);
            if (response.isSuccessful()) {
                successCount++;
            } else {
                failCount++;
            }
        }

        return new MTNBatchResponse(successCount, failCount, responses);
    }

    /**
     * Récupère le statut d'un message.
     */
    public MTNDeliveryStatus getDeliveryStatus(String transactionId) {
        // Simulation basée sur le hash
        int hash = transactionId.hashCode();
        MTNDeliveryStatus.Status status;

        if (hash % 10 < 6) {
            status = MTNDeliveryStatus.Status.DELIVERED;
        } else if (hash % 10 < 8) {
            status = MTNDeliveryStatus.Status.PENDING;
        } else if (hash % 10 < 9) {
            status = MTNDeliveryStatus.Status.SENT;
        } else {
            status = MTNDeliveryStatus.Status.FAILED;
        }

        return new MTNDeliveryStatus(transactionId, status, LocalDateTime.now());
    }

    /**
     * Consulte les unités SMS disponibles.
     */
    public MTNAccountInfo getAccountInfo() {
        return new MTNAccountInfo(clientId, smsUnits, gatewayOnline, "BUSINESS_PREMIUM");
    }

    /**
     * Configure l'URL de callback pour les notifications.
     */
    public void setDeliveryCallback(String url) {
        this.callbackUrl = url;
    }

    /**
     * Validation du format MTN (+237XXXXXXXXX).
     */
    private boolean validateMTNNumber(String number) {
        if (number == null) return false;
        return number.matches("\\+237[0-9]{9}");
    }

    // Setters pour tests
    public void setSmsUnits(int units) {
        this.smsUnits = units;
    }

    public void setGatewayOnline(boolean online) {
        this.gatewayOnline = online;
    }

    // ==================== CLASSES INTERNES MTN ====================

    /**
     * Requête SMS MTN.
     */
    public static class MTNRequest {
        private final String recipient;
        private final String content;
        private final String senderName;
        private final Priority priority;

        public enum Priority { LOW, NORMAL, HIGH, URGENT }

        public MTNRequest(String recipient, String content, String senderName, Priority priority) {
            this.recipient = recipient;
            this.content = content;
            this.senderName = senderName;
            this.priority = priority;
        }

        public MTNRequest(String recipient, String content, String senderName) {
            this(recipient, content, senderName, Priority.NORMAL);
        }

        public String getRecipient() { return recipient; }
        public String getContent() { return content; }
        public String getSenderName() { return senderName; }
        public Priority getPriority() { return priority; }
    }

    /**
     * Réponse SMS MTN.
     */
    public static class MTNResponse {
        private final boolean successful;
        private final String transactionId;
        private final String recipient;
        private final String errorCode;
        private final String errorMessage;
        private final long processingTimeMs;

        private MTNResponse(boolean successful, String transactionId, String recipient,
                           String errorCode, String errorMessage, long processingTimeMs) {
            this.successful = successful;
            this.transactionId = transactionId;
            this.recipient = recipient;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.processingTimeMs = processingTimeMs;
        }

        public static MTNResponse success(String transactionId, String recipient, long processingTime) {
            return new MTNResponse(true, transactionId, recipient, null, null, processingTime);
        }

        public static MTNResponse error(String code, String message) {
            return new MTNResponse(false, null, null, code, message, 0);
        }

        public boolean isSuccessful() { return successful; }
        public String getTransactionId() { return transactionId; }
        public String getRecipient() { return recipient; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public long getProcessingTimeMs() { return processingTimeMs; }
    }

    /**
     * Réponse d'envoi en masse.
     */
    public static class MTNBatchResponse {
        private final int successCount;
        private final int failureCount;
        private final List<MTNResponse> individualResponses;

        public MTNBatchResponse(int success, int failure, List<MTNResponse> responses) {
            this.successCount = success;
            this.failureCount = failure;
            this.individualResponses = responses;
        }

        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<MTNResponse> getIndividualResponses() { return individualResponses; }
        public int getTotalProcessed() { return successCount + failureCount; }
    }

    /**
     * Statut de livraison MTN.
     */
    public static class MTNDeliveryStatus {
        public enum Status { PENDING, SENT, DELIVERED, FAILED, EXPIRED }

        private final String transactionId;
        private final Status status;
        private final LocalDateTime checkedAt;

        public MTNDeliveryStatus(String transactionId, Status status, LocalDateTime checkedAt) {
            this.transactionId = transactionId;
            this.status = status;
            this.checkedAt = checkedAt;
        }

        public String getTransactionId() { return transactionId; }
        public Status getStatus() { return status; }
        public LocalDateTime getCheckedAt() { return checkedAt; }
    }

    /**
     * Informations du compte MTN.
     */
    public static class MTNAccountInfo {
        private final String clientId;
        private final int availableUnits;
        private final boolean gatewayOnline;
        private final String subscriptionPlan;

        public MTNAccountInfo(String clientId, int units, boolean online, String plan) {
            this.clientId = clientId;
            this.availableUnits = units;
            this.gatewayOnline = online;
            this.subscriptionPlan = plan;
        }

        public String getClientId() { return clientId; }
        public int getAvailableUnits() { return availableUnits; }
        public boolean isGatewayOnline() { return gatewayOnline; }
        public String getSubscriptionPlan() { return subscriptionPlan; }
    }
}
