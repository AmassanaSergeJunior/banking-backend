package com.banque.adapter;

import com.banque.adapter.legacy.TwilioRestClient;
import com.banque.adapter.legacy.TwilioRestClient.*;

import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN ADAPTER - Adaptateur Twilio (International)
 *
 * OBJECTIF 5: Cet adaptateur convertit l'API REST Twilio
 * vers notre interface standard SMSService.
 *
 * CONVERSIONS EFFECTUEES:
 * - Format numéro: Validation E.164 (+XXXXXXXXXXX)
 * - Objets: MessageInstance -> SMSResult
 * - Prix: dollars -> crédits (conversion)
 * - Exceptions: TwilioRestException -> SMSResult.failure()
 * - Statuts: MessageInstance.Status -> SMSStatus enum
 */
public class TwilioSMSAdapter implements SMSService {

    private static final String PROVIDER_NAME = "Twilio International";
    private static final double CREDIT_TO_DOLLAR_RATE = 0.0075; // 1 crédit = 0.0075$

    // Instance du client REST legacy (Adaptee)
    private final TwilioRestClient twilioClient;
    private final String defaultFromNumber;

    /**
     * Crée un adaptateur Twilio.
     *
     * @param accountSid SID du compte Twilio
     * @param authToken Token d'authentification
     * @param defaultFromNumber Numéro/ID expéditeur par défaut
     */
    public TwilioSMSAdapter(String accountSid, String authToken, String defaultFromNumber) {
        this.twilioClient = new TwilioRestClient(accountSid, authToken);
        this.defaultFromNumber = defaultFromNumber;
    }

    /**
     * Constructeur avec client existant (pour tests).
     */
    public TwilioSMSAdapter(TwilioRestClient client, String defaultFromNumber) {
        this.twilioClient = client;
        this.defaultFromNumber = defaultFromNumber;
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message) {
        return sendSMS(phoneNumber, message, defaultFromNumber);
    }

    @Override
    public SMSResult sendSMS(String phoneNumber, String message, String senderId) {
        // ADAPTATION: Validation et conversion du format E.164
        String e164Format = convertToE164Format(phoneNumber);

        try {
            // ADAPTATION: Appel de l'API Twilio
            MessageInstance twilioMessage = twilioClient.createMessage(e164Format, senderId, message);

            // ADAPTATION: Conversion MessageInstance -> SMSResult
            return convertTwilioMessageToSMSResult(twilioMessage);

        } catch (TwilioRestException e) {
            // ADAPTATION: Exception Twilio -> SMSResult.failure()
            return SMSResult.failure(String.format("[Twilio %d] %s", e.getErrorCode(), e.getMessage()));
        }
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
        try {
            // ADAPTATION: Récupération du message Twilio
            MessageInstance twilioMessage = twilioClient.fetchMessage(messageId);

            // ADAPTATION: Conversion du statut Twilio vers SMSStatus
            return convertTwilioStatusToSMSStatus(twilioMessage.getStatus());

        } catch (TwilioRestException e) {
            return SMSStatus.UNKNOWN;
        }
    }

    @Override
    public int getBalance() {
        // ADAPTATION: Conversion dollars -> crédits
        AccountInstance account = twilioClient.fetchAccount();
        double balanceDollars = account.getBalance();
        return (int) (balanceDollars / CREDIT_TO_DOLLAR_RATE);
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        try {
            AccountInstance account = twilioClient.fetchAccount();
            return account.getStatus() == AccountInstance.Status.ACTIVE;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== METHODES DE CONVERSION ====================

    /**
     * Convertit un numéro vers le format E.164 (+XXXXXXXXXXX).
     */
    private String convertToE164Format(String phoneNumber) {
        if (phoneNumber == null) return null;

        String cleaned = phoneNumber.trim();

        // Si déjà au format E.164
        if (cleaned.matches("\\+[1-9][0-9]{1,14}")) {
            return cleaned;
        }

        // Si commence par 237 sans +
        if (cleaned.startsWith("237") && cleaned.length() == 12) {
            return "+" + cleaned;
        }

        // Si c'est un numéro camerounais local (9 chiffres)
        if (cleaned.length() == 9 && cleaned.matches("[0-9]+")) {
            return "+237" + cleaned;
        }

        // Si pas de + mais ressemble à un numéro international
        if (cleaned.matches("[0-9]{10,15}")) {
            return "+" + cleaned;
        }

        // Retourner tel quel (l'API Twilio validera)
        return cleaned;
    }

    /**
     * Convertit un MessageInstance Twilio en SMSResult.
     */
    private SMSResult convertTwilioMessageToSMSResult(MessageInstance twilioMessage) {
        // Conversion prix dollars -> crédits
        int credits = (int) Math.ceil(twilioMessage.getPrice() / CREDIT_TO_DOLLAR_RATE);

        return SMSResult.success(
            twilioMessage.getSid(),
            credits,
            twilioMessage.getApiResponseMs()
        );
    }

    /**
     * Convertit un statut Twilio en SMSStatus.
     */
    private SMSStatus convertTwilioStatusToSMSStatus(MessageInstance.Status twilioStatus) {
        if (twilioStatus == null) return SMSStatus.UNKNOWN;

        switch (twilioStatus) {
            case QUEUED:
            case SENDING:
                return SMSStatus.PENDING;
            case SENT:
                return SMSStatus.SENT;
            case DELIVERED:
                return SMSStatus.DELIVERED;
            case UNDELIVERED:
            case FAILED:
                return SMSStatus.FAILED;
            default:
                return SMSStatus.UNKNOWN;
        }
    }

    /**
     * Retourne le solde en dollars (pour info).
     */
    public double getBalanceInDollars() {
        AccountInstance account = twilioClient.fetchAccount();
        return account.getBalance();
    }

    /**
     * Accès au client sous-jacent (pour tests avancés).
     */
    public TwilioRestClient getUnderlyingClient() {
        return twilioClient;
    }
}
