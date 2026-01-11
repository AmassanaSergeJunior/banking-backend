package com.banque.adapter;

import com.banque.adapter.legacy.MTNCamerounSMSGateway;
import com.banque.adapter.legacy.MTNCamerounSMSGateway.*;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN ADAPTER - Adaptateur MTN Cameroun
 *
 * OBJECTIF 5: Cet adaptateur convertit l'API propriétaire MTN Cameroun
 * vers notre interface standard SMSService.
 *
 * CONVERSIONS EFFECTUEES:
 * - Format numéro: 237XXXXXXXXX -> +237XXXXXXXXX (ajout du +)
 * - Objets: MTNRequest/MTNResponse -> String/SMSResult
 * - Méthodes: sendSMS() -> dispatchSMS()
 * - Statuts: MTNDeliveryStatus.Status -> SMSStatus enum
 * - Crédits: "units" -> "credits"
 */
public class MTNSMSAdapter implements SMSService {

    private static final String PROVIDER_NAME = "MTN Cameroun";

    // Instance du gateway legacy (Adaptee)
    private final MTNCamerounSMSGateway mtnGateway;
    private final String defaultSenderName;

    /**
     * Crée un adaptateur MTN.
     *
     * @param clientId ID client MTN
     * @param clientSecret Secret client MTN
     * @param defaultSenderName Nom d'expéditeur par défaut
     */
    public MTNSMSAdapter(String clientId, String clientSecret, String defaultSenderName) {
        this.mtnGateway = new MTNCamerounSMSGateway(clientId, clientSecret);
        this.defaultSenderName = defaultSenderName;
    }

    /**
     * Constructeur avec gateway existant (pour tests).
     */
    public MTNSMSAdapter(MTNCamerounSMSGateway gateway, String defaultSenderName) {
        this.mtnGateway = gateway;
        this.defaultSenderName = defaultSenderName;
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message) {
        return sendSMS(phoneNumber, message, defaultSenderName);
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message, String senderId) {
        // ADAPTATION: Conversion du format de numéro
        String mtnFormat = convertToMTNFormat(phoneNumber);

        // ADAPTATION: Création de l'objet MTNRequest
        MTNRequest request = new MTNRequest(mtnFormat, message, senderId);

        // ADAPTATION: Appel du gateway legacy
        MTNResponse mtnResponse = mtnGateway.dispatchSMS(request);

        // ADAPTATION: Conversion de la réponse MTN vers SMSResult
        return convertMTNResponseToSMSResult(mtnResponse);
    }

    @Override
    public BulkSMSResult sendBulkSMS(List<String> phoneNumbers, String message) {
        // ADAPTATION: Création de la liste de requêtes MTN
        List<MTNRequest> mtnRequests = new ArrayList<>();
        for (String phone : phoneNumbers) {
            String mtnFormat = convertToMTNFormat(phone);
            mtnRequests.add(new MTNRequest(mtnFormat, message, defaultSenderName));
        }

        // ADAPTATION: Appel de l'envoi en masse MTN
        MTNBatchResponse batchResponse = mtnGateway.dispatchBulkSMS(mtnRequests);

        // ADAPTATION: Conversion des résultats
        List<SMSResult> results = new ArrayList<>();
        for (MTNResponse response : batchResponse.getIndividualResponses()) {
            results.add(convertMTNResponseToSMSResult(response));
        }

        return new BulkSMSResult(
            batchResponse.getSuccessCount(),
            batchResponse.getFailureCount(),
            results,
            batchResponse.getSuccessCount() // 1 unité par SMS
        );
    }

    @Override
    public SMSStatus checkStatus(String messageId) {
        // ADAPTATION: Appel du gateway legacy
        MTNDeliveryStatus mtnStatus = mtnGateway.getDeliveryStatus(messageId);

        // ADAPTATION: Conversion du statut MTN vers SMSStatus
        return convertMTNStatusToSMSStatus(mtnStatus.getStatus());
    }

    @Override
    public int getBalance() {
        // ADAPTATION: Récupération des "units" MTN comme "credits"
        MTNAccountInfo accountInfo = mtnGateway.getAccountInfo();
        return accountInfo.getAvailableUnits();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        // ADAPTATION: Vérification via le gateway
        MTNAccountInfo accountInfo = mtnGateway.getAccountInfo();
        return accountInfo.isGatewayOnline();
    }

    // ==================== METHODES DE CONVERSION ====================

    /**
     * Convertit un numéro vers le format MTN (+237XXXXXXXXX).
     */
    private String convertToMTNFormat(String phoneNumber) {
        if (phoneNumber == null) return null;

        String cleaned = phoneNumber.trim();

        // Si déjà au bon format
        if (cleaned.startsWith("+237")) {
            return cleaned;
        }

        // Si commence par 237 sans +
        if (cleaned.startsWith("237")) {
            return "+" + cleaned;
        }

        // Si c'est juste le numéro local (9 chiffres)
        if (cleaned.length() == 9 && cleaned.matches("[0-9]+")) {
            return "+237" + cleaned;
        }

        // Si commence par un autre +
        if (cleaned.startsWith("+")) {
            return cleaned; // Garder tel quel (numéro international)
        }

        // Par défaut, ajouter +237
        return "+237" + cleaned;
    }

    /**
     * Convertit une réponse MTN en SMSResult.
     */
    private SMSResult convertMTNResponseToSMSResult(MTNResponse mtnResponse) {
        if (mtnResponse.isSuccessful()) {
            return SMSResult.success(
                mtnResponse.getTransactionId(),
                1, // 1 unité par SMS
                mtnResponse.getProcessingTimeMs()
            );
        } else {
            String errorMsg = String.format("[MTN %s] %s",
                mtnResponse.getErrorCode(),
                mtnResponse.getErrorMessage());
            return SMSResult.failure(errorMsg);
        }
    }

    /**
     * Convertit un statut MTN en SMSStatus.
     */
    private SMSStatus convertMTNStatusToSMSStatus(MTNDeliveryStatus.Status mtnStatus) {
        if (mtnStatus == null) return SMSStatus.UNKNOWN;

        switch (mtnStatus) {
            case PENDING:
                return SMSStatus.PENDING;
            case SENT:
                return SMSStatus.SENT;
            case DELIVERED:
                return SMSStatus.DELIVERED;
            case FAILED:
                return SMSStatus.FAILED;
            case EXPIRED:
                return SMSStatus.EXPIRED;
            default:
                return SMSStatus.UNKNOWN;
        }
    }

    /**
     * Configure le callback de livraison.
     */
    public void setDeliveryCallback(String callbackUrl) {
        mtnGateway.setDeliveryCallback(callbackUrl);
    }

    /**
     * Accès au gateway sous-jacent (pour tests avancés).
     */
    public MTNCamerounSMSGateway getUnderlyingGateway() {
        return mtnGateway;
    }
}
