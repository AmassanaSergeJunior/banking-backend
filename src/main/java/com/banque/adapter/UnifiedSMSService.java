package com.banque.adapter;

import com.banque.adapter.SMSAdapterFactory.SMSProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SERVICE UNIFIE SMS
 *
 * OBJECTIF 5: Ce service utilise les adaptateurs pour fournir une interface
 * unifiée d'envoi de SMS, quel que soit le provider sous-jacent.
 *
 * Fonctionnalités:
 * - Sélection automatique du provider selon le numéro
 * - Fallback vers un autre provider en cas d'échec
 * - Statistiques d'envoi par provider
 * - Envoi intelligent basé sur la disponibilité
 */
@Service
public class UnifiedSMSService {

    private final Map<SMSProvider, SMSService> adapters;
    private final Map<SMSProvider, ProviderStats> statistics;
    private SMSProvider defaultProvider;

    public UnifiedSMSService() {
        this.adapters = SMSAdapterFactory.createAllAdapters();
        this.statistics = new HashMap<>();
        this.defaultProvider = SMSProvider.ORANGE_CAMEROUN;

        // Initialiser les statistiques
        for (SMSProvider provider : SMSProvider.values()) {
            statistics.put(provider, new ProviderStats());
        }
    }

    /**
     * Envoie un SMS en sélectionnant automatiquement le provider.
     */
    public SMSService.SMSResult sendSMS(String phoneNumber, String message) {
        SMSProvider provider = SMSAdapterFactory.selectProviderByNumber(phoneNumber);
        return sendSMSViaProvider(provider, phoneNumber, message);
    }

    /**
     * Envoie un SMS via un provider spécifique.
     */
    public SMSService.SMSResult sendSMSViaProvider(SMSProvider provider,
                                                    String phoneNumber,
                                                    String message) {
        SMSService adapter = adapters.get(provider);
        if (adapter == null) {
            return SMSService.SMSResult.failure("Provider non disponible: " + provider);
        }

        long startTime = System.currentTimeMillis();
        SMSService.SMSResult result = adapter.sendSMS(phoneNumber, message);
        long duration = System.currentTimeMillis() - startTime;

        // Mettre à jour les statistiques
        updateStats(provider, result, duration);

        return result;
    }

    /**
     * Envoie un SMS avec fallback automatique en cas d'échec.
     */
    public SMSService.SMSResult sendSMSWithFallback(String phoneNumber, String message) {
        // Essayer le provider optimal
        SMSProvider primaryProvider = SMSAdapterFactory.selectProviderByNumber(phoneNumber);
        SMSService.SMSResult result = sendSMSViaProvider(primaryProvider, phoneNumber, message);

        if (result.isSuccess()) {
            return result;
        }

        // Fallback vers les autres providers
        for (SMSProvider fallbackProvider : SMSProvider.values()) {
            if (fallbackProvider != primaryProvider) {
                SMSService adapter = adapters.get(fallbackProvider);
                if (adapter.isAvailable()) {
                    result = sendSMSViaProvider(fallbackProvider, phoneNumber, message);
                    if (result.isSuccess()) {
                        return result;
                    }
                }
            }
        }

        return SMSService.SMSResult.failure("Tous les providers ont echoue");
    }

    /**
     * Envoie des SMS en masse avec sélection intelligente.
     */
    public Map<String, SMSService.SMSResult> sendBulkSMSSmart(List<String> phoneNumbers,
                                                              String message) {
        Map<String, SMSService.SMSResult> results = new HashMap<>();

        for (String phone : phoneNumbers) {
            SMSService.SMSResult result = sendSMS(phone, message);
            results.put(phone, result);
        }

        return results;
    }

    /**
     * Vérifie le statut d'un message.
     */
    public SMSService.SMSStatus checkStatus(SMSProvider provider, String messageId) {
        SMSService adapter = adapters.get(provider);
        if (adapter == null) {
            return SMSService.SMSStatus.UNKNOWN;
        }
        return adapter.checkStatus(messageId);
    }

    /**
     * Retourne le solde d'un provider.
     */
    public int getBalance(SMSProvider provider) {
        SMSService adapter = adapters.get(provider);
        return adapter != null ? adapter.getBalance() : 0;
    }

    /**
     * Retourne les soldes de tous les providers.
     */
    public Map<SMSProvider, Integer> getAllBalances() {
        Map<SMSProvider, Integer> balances = new HashMap<>();
        for (Map.Entry<SMSProvider, SMSService> entry : adapters.entrySet()) {
            balances.put(entry.getKey(), entry.getValue().getBalance());
        }
        return balances;
    }

    /**
     * Vérifie la disponibilité des providers.
     */
    public Map<SMSProvider, Boolean> getProvidersAvailability() {
        Map<SMSProvider, Boolean> availability = new HashMap<>();
        for (Map.Entry<SMSProvider, SMSService> entry : adapters.entrySet()) {
            availability.put(entry.getKey(), entry.getValue().isAvailable());
        }
        return availability;
    }

    /**
     * Retourne les statistiques de tous les providers.
     */
    public Map<SMSProvider, ProviderStats> getStatistics() {
        return new HashMap<>(statistics);
    }

    /**
     * Retourne le provider par défaut.
     */
    public SMSProvider getDefaultProvider() {
        return defaultProvider;
    }

    /**
     * Définit le provider par défaut.
     */
    public void setDefaultProvider(SMSProvider provider) {
        this.defaultProvider = provider;
    }

    /**
     * Retourne un résumé global.
     */
    public ServiceSummary getSummary() {
        int totalSent = 0;
        int totalFailed = 0;
        int totalCredits = 0;

        for (ProviderStats stats : statistics.values()) {
            totalSent += stats.getTotalSent();
            totalFailed += stats.getTotalFailed();
            totalCredits += stats.getTotalCreditsUsed();
        }

        Map<SMSProvider, Integer> balances = getAllBalances();
        int totalBalance = balances.values().stream().mapToInt(Integer::intValue).sum();

        return new ServiceSummary(
            adapters.size(),
            totalSent,
            totalFailed,
            totalCredits,
            totalBalance
        );
    }

    // ==================== METHODES PRIVEES ====================

    private void updateStats(SMSProvider provider, SMSService.SMSResult result, long duration) {
        ProviderStats stats = statistics.get(provider);
        if (stats != null) {
            if (result.isSuccess()) {
                stats.recordSuccess(result.getCreditsUsed(), duration);
            } else {
                stats.recordFailure();
            }
        }
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Statistiques par provider.
     */
    public static class ProviderStats {
        private int totalSent;
        private int totalFailed;
        private int totalCreditsUsed;
        private long totalResponseTime;

        public void recordSuccess(int credits, long responseTime) {
            totalSent++;
            totalCreditsUsed += credits;
            totalResponseTime += responseTime;
        }

        public void recordFailure() {
            totalFailed++;
        }

        public int getTotalSent() { return totalSent; }
        public int getTotalFailed() { return totalFailed; }
        public int getTotalCreditsUsed() { return totalCreditsUsed; }
        public double getAverageResponseTime() {
            return totalSent > 0 ? (double) totalResponseTime / totalSent : 0;
        }
        public double getSuccessRate() {
            int total = totalSent + totalFailed;
            return total > 0 ? (double) totalSent / total * 100 : 0;
        }
    }

    /**
     * Résumé global du service.
     */
    public static class ServiceSummary {
        private final int providersCount;
        private final int totalSent;
        private final int totalFailed;
        private final int totalCreditsUsed;
        private final int totalBalanceAvailable;

        public ServiceSummary(int providers, int sent, int failed, int credits, int balance) {
            this.providersCount = providers;
            this.totalSent = sent;
            this.totalFailed = failed;
            this.totalCreditsUsed = credits;
            this.totalBalanceAvailable = balance;
        }

        public int getProvidersCount() { return providersCount; }
        public int getTotalSent() { return totalSent; }
        public int getTotalFailed() { return totalFailed; }
        public int getTotalCreditsUsed() { return totalCreditsUsed; }
        public int getTotalBalanceAvailable() { return totalBalanceAvailable; }
        public double getGlobalSuccessRate() {
            int total = totalSent + totalFailed;
            return total > 0 ? (double) totalSent / total * 100 : 0;
        }
    }
}
