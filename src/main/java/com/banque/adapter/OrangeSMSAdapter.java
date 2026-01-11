package com.banque.adapter;

import com.banque.adapter.legacy.OrangeCamerounSMSAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PATTERN ADAPTER - Adaptateur Orange Cameroun
 *
 * OBJECTIF 5: Cet adaptateur convertit l'API propriétaire Orange Cameroun
 * vers notre interface standard SMSService.
 *
 * CONVERSIONS EFFECTUEES:
 * - Format numéro: +237XXXXXXXXX -> 237XXXXXXXXX (suppression du +)
 * - Méthodes: sendSMS() -> envoyerMessage()
 * - Résultats: Map<String, Object> -> SMSResult
 * - Statuts: String (LIVRE, EN_ATTENTE, ECHEC) -> SMSStatus enum
 */
public class OrangeSMSAdapter implements SMSService {

    private static final String PROVIDER_NAME = "Orange Cameroun";

    // Instance de l'API legacy (Adaptee)
    private final OrangeCamerounSMSAPI orangeApi;
    private final String defaultSenderId;

    /**
     * Crée un adaptateur Orange.
     *
     * @param apiKey Clé API Orange
     * @param merchantId ID du marchand
     * @param defaultSenderId Expéditeur par défaut
     */
    public OrangeSMSAdapter(String apiKey, String merchantId, String defaultSenderId) {
        this.orangeApi = new OrangeCamerounSMSAPI(apiKey, merchantId);
        this.defaultSenderId = defaultSenderId;
    }

    /**
     * Constructeur avec API existante (pour tests).
     */
    public OrangeSMSAdapter(OrangeCamerounSMSAPI orangeApi, String defaultSenderId) {
        this.orangeApi = orangeApi;
        this.defaultSenderId = defaultSenderId;
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message) {
        return sendSMS(phoneNumber, message, defaultSenderId);
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message, String senderId) {
        // ADAPTATION: Conversion du format de numéro
        String orangeFormat = convertToOrangeFormat(phoneNumber);

        // ADAPTATION: Appel de l'API legacy
        Map<String, Object> orangeResult = orangeApi.envoyerMessage(orangeFormat, message, senderId);

        // ADAPTATION: Conversion du résultat Orange vers SMSResult
        return convertOrangeResultToSMSResult(orangeResult);
    }

    @Override
    public BulkSMSResult sendBulkSMS(List<String> phoneNumbers, String message) {
        List<SMSResult> results = new ArrayList<>();
        int sent = 0;
        int failed = 0;
        int totalCredits = 0;

        for (String phone : phoneNumbers) {
            SMSResult result = sendSMS(phone, message);
            results.add(result);

            if (result.isSuccess()) {
                sent++;
                totalCredits += result.getCreditsUsed();
            } else {
                failed++;
            }
        }

        return new BulkSMSResult(sent, failed, results, totalCredits);
    }

    @Override
    public SMSStatus checkStatus(String messageId) {
        // ADAPTATION: Appel de l'API legacy
        Map<String, Object> statusResult = orangeApi.verifierLivraison(messageId);

        // ADAPTATION: Conversion du statut Orange vers SMSStatus
        return convertOrangeStatusToSMSStatus((String) statusResult.get("statut"));
    }

    @Override
    public int getBalance() {
        // ADAPTATION: Extraction des crédits depuis le format Orange
        Map<String, Object> soldeInfo = orangeApi.consulterSolde();
        return (int) soldeInfo.get("creditsDisponibles");
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        // ADAPTATION: Vérification via l'API legacy
        Map<String, Object> etatService = orangeApi.verifierEtatService();
        return (boolean) etatService.get("serviceActif");
    }

    // ==================== METHODES DE CONVERSION ====================

    /**
     * Convertit un numéro international vers le format Orange (sans +).
     * +237699000000 -> 237699000000
     */
    private String convertToOrangeFormat(String phoneNumber) {
        if (phoneNumber == null) return null;

        // Supprimer le + si présent
        String cleaned = phoneNumber.trim();
        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1);
        }

        // Ajouter le préfixe 237 si absent
        if (!cleaned.startsWith("237") && cleaned.length() == 9) {
            cleaned = "237" + cleaned;
        }

        return cleaned;
    }

    /**
     * Convertit un résultat Orange en SMSResult.
     */
    private SMSResult convertOrangeResultToSMSResult(Map<String, Object> orangeResult) {
        boolean success = (boolean) orangeResult.getOrDefault("succes", false);

        if (success) {
            String messageId = (String) orangeResult.get("identifiantMessage");
            int credits = (int) orangeResult.getOrDefault("creditsUtilises", 1);
            long responseTime = (long) orangeResult.getOrDefault("tempsTraitement", 0L);

            return SMSResult.success(messageId, credits, responseTime);
        } else {
            String errorMessage = (String) orangeResult.getOrDefault("messageErreur", "Erreur inconnue");
            return SMSResult.failure("[Orange] " + errorMessage);
        }
    }

    /**
     * Convertit un statut Orange en SMSStatus.
     */
    private SMSStatus convertOrangeStatusToSMSStatus(String orangeStatus) {
        if (orangeStatus == null) return SMSStatus.UNKNOWN;

        switch (orangeStatus) {
            case "LIVRE":
                return SMSStatus.DELIVERED;
            case "EN_ATTENTE":
                return SMSStatus.PENDING;
            case "ENVOYE":
                return SMSStatus.SENT;
            case "ECHEC":
                return SMSStatus.FAILED;
            case "EXPIRE":
                return SMSStatus.EXPIRED;
            default:
                return SMSStatus.UNKNOWN;
        }
    }

    /**
     * Accès à l'API sous-jacente (pour tests avancés).
     */
    public OrangeCamerounSMSAPI getUnderlyingApi() {
        return orangeApi;
    }
}
