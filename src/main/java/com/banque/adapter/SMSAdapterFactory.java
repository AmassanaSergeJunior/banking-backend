package com.banque.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * FACTORY pour les Adaptateurs SMS
 *
 * OBJECTIF 5: Cette factory centralise la création des adaptateurs SMS
 * et permet de sélectionner dynamiquement le provider approprié.
 */
public class SMSAdapterFactory {

    /**
     * Types de providers SMS supportés.
     */
    public enum SMSProvider {
        ORANGE_CAMEROUN("Orange Cameroun", "Orange CM"),
        MTN_CAMEROUN("MTN Cameroun", "MTN CM"),
        TWILIO("Twilio International", "Twilio");

        private final String fullName;
        private final String shortName;

        SMSProvider(String fullName, String shortName) {
            this.fullName = fullName;
            this.shortName = shortName;
        }

        public String getFullName() { return fullName; }
        public String getShortName() { return shortName; }
    }

    // Configuration par défaut des providers
    private static final Map<SMSProvider, ProviderConfig> DEFAULT_CONFIGS = new HashMap<>();

    static {
        // Configuration Orange Cameroun
        DEFAULT_CONFIGS.put(SMSProvider.ORANGE_CAMEROUN, new ProviderConfig(
            "orange-api-key-demo",
            "merchant-001",
            "BANQUE_XYZ"
        ));

        // Configuration MTN Cameroun
        DEFAULT_CONFIGS.put(SMSProvider.MTN_CAMEROUN, new ProviderConfig(
            "mtn-client-id-demo",
            "mtn-client-secret-demo",
            "BANQUE_XYZ"
        ));

        // Configuration Twilio
        DEFAULT_CONFIGS.put(SMSProvider.TWILIO, new ProviderConfig(
            "AC-twilio-account-sid-demo",
            "twilio-auth-token-demo",
            "+15551234567"
        ));
    }

    /**
     * Crée un adaptateur SMS pour le provider spécifié avec la config par défaut.
     *
     * @param provider Le provider SMS souhaité
     * @return L'adaptateur SMS correspondant
     */
    public static SMSService createAdapter(SMSProvider provider) {
        ProviderConfig config = DEFAULT_CONFIGS.get(provider);
        if (config == null) {
            throw new IllegalArgumentException("Provider non configuré: " + provider);
        }
        return createAdapter(provider, config);
    }

    /**
     * Crée un adaptateur SMS avec une configuration personnalisée.
     *
     * @param provider Le provider SMS souhaité
     * @param config La configuration du provider
     * @return L'adaptateur SMS correspondant
     */
    public static SMSService createAdapter(SMSProvider provider, ProviderConfig config) {
        switch (provider) {
            case ORANGE_CAMEROUN:
                return new OrangeSMSAdapter(
                    config.getApiKeyOrClientId(),
                    config.getSecretOrMerchantId(),
                    config.getSenderIdOrFromNumber()
                );

            case MTN_CAMEROUN:
                return new MTNSMSAdapter(
                    config.getApiKeyOrClientId(),
                    config.getSecretOrMerchantId(),
                    config.getSenderIdOrFromNumber()
                );

            case TWILIO:
                return new TwilioSMSAdapter(
                    config.getApiKeyOrClientId(),
                    config.getSecretOrMerchantId(),
                    config.getSenderIdOrFromNumber()
                );

            default:
                throw new IllegalArgumentException("Provider non supporté: " + provider);
        }
    }

    /**
     * Crée tous les adaptateurs disponibles.
     *
     * @return Map des adaptateurs par provider
     */
    public static Map<SMSProvider, SMSService> createAllAdapters() {
        Map<SMSProvider, SMSService> adapters = new HashMap<>();
        for (SMSProvider provider : SMSProvider.values()) {
            adapters.put(provider, createAdapter(provider));
        }
        return adapters;
    }

    /**
     * Retourne les providers disponibles.
     */
    public static Set<SMSProvider> getAvailableProviders() {
        return DEFAULT_CONFIGS.keySet();
    }

    /**
     * Sélectionne automatiquement le meilleur provider basé sur le numéro.
     */
    public static SMSProvider selectProviderByNumber(String phoneNumber) {
        if (phoneNumber == null) return SMSProvider.TWILIO;

        String cleaned = phoneNumber.replace("+", "").replace(" ", "");

        // Numéros camerounais
        if (cleaned.startsWith("237")) {
            String localNumber = cleaned.substring(3);

            // Orange: commence par 69, 655-659
            if (localNumber.startsWith("69") ||
                (localNumber.startsWith("65") && localNumber.charAt(2) >= '5')) {
                return SMSProvider.ORANGE_CAMEROUN;
            }

            // MTN: commence par 67, 68, 650-654
            if (localNumber.startsWith("67") || localNumber.startsWith("68") ||
                (localNumber.startsWith("65") && localNumber.charAt(2) < '5')) {
                return SMSProvider.MTN_CAMEROUN;
            }
        }

        // Par défaut: Twilio pour les numéros internationaux
        return SMSProvider.TWILIO;
    }

    /**
     * Configuration d'un provider SMS.
     */
    public static class ProviderConfig {
        private final String apiKeyOrClientId;
        private final String secretOrMerchantId;
        private final String senderIdOrFromNumber;

        public ProviderConfig(String apiKey, String secret, String senderId) {
            this.apiKeyOrClientId = apiKey;
            this.secretOrMerchantId = secret;
            this.senderIdOrFromNumber = senderId;
        }

        public String getApiKeyOrClientId() { return apiKeyOrClientId; }
        public String getSecretOrMerchantId() { return secretOrMerchantId; }
        public String getSenderIdOrFromNumber() { return senderIdOrFromNumber; }
    }
}
