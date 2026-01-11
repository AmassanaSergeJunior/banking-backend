package com.banque.singleton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PATTERN SINGLETON - Service de Notifications (Client SMS)
 *
 * OBJECTIF 4: Ce singleton garantit qu'il n'existe qu'une seule instance
 * du client SMS pendant toute l'exécution de l'application.
 *
 * POURQUOI SINGLETON?
 * - Une seule connexion au prestataire SMS (économie de ressources)
 * - Configuration centralisée
 * - Compteurs et statistiques globaux
 * - Thread-safe pour accès concurrent
 *
 * IMPLEMENTATION: Double-Checked Locking
 * - Lazy initialization (créé au premier accès)
 * - Thread-safe avec volatile et synchronized
 * - Performance optimisée (pas de synchronisation après création)
 */
public class NotificationServiceSingleton {

    // Instance unique (volatile pour visibilité entre threads)
    private static volatile NotificationServiceSingleton instance;

    // Verrou pour la création thread-safe
    private static final Object lock = new Object();

    // Configuration du service
    private String smsProviderUrl;
    private String apiKey;
    private boolean initialized;
    private final LocalDateTime createdAt;

    // Statistiques thread-safe
    private final AtomicInteger totalSmsSent;
    private final AtomicInteger totalSmsFailed;
    private final AtomicLong totalResponseTime;

    // Historique des notifications (thread-safe)
    private final List<NotificationRecord> notificationHistory;

    // Compteur d'accès (pour démontrer l'unicité)
    private final AtomicInteger accessCount;

    /**
     * Constructeur privé - empêche l'instanciation externe.
     */
    private NotificationServiceSingleton() {
        this.createdAt = LocalDateTime.now();
        this.totalSmsSent = new AtomicInteger(0);
        this.totalSmsFailed = new AtomicInteger(0);
        this.totalResponseTime = new AtomicLong(0);
        this.notificationHistory = Collections.synchronizedList(new ArrayList<>());
        this.accessCount = new AtomicInteger(0);
        this.initialized = false;

        // Simuler un délai d'initialisation (connexion au provider)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Obtient l'instance unique du service (Double-Checked Locking).
     *
     * @return L'instance unique de NotificationServiceSingleton
     */
    public static NotificationServiceSingleton getInstance() {
        // Premier check (sans synchronisation pour performance)
        if (instance == null) {
            synchronized (lock) {
                // Deuxième check (avec synchronisation)
                if (instance == null) {
                    instance = new NotificationServiceSingleton();
                }
            }
        }
        instance.accessCount.incrementAndGet();
        return instance;
    }

    /**
     * Initialise le service avec les paramètres du provider SMS.
     *
     * @param providerUrl URL du prestataire SMS
     * @param apiKey Clé API
     */
    public synchronized void initialize(String providerUrl, String apiKey) {
        if (initialized) {
            throw new IllegalStateException("Le service est déjà initialisé");
        }
        this.smsProviderUrl = providerUrl;
        this.apiKey = apiKey;
        this.initialized = true;
    }

    /**
     * Envoie un SMS (simulation).
     *
     * @param phoneNumber Numéro de téléphone
     * @param message Message à envoyer
     * @return Résultat de l'envoi
     */
    public NotificationResult sendSMS(String phoneNumber, String message) {
        long startTime = System.currentTimeMillis();

        try {
            // Validation
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                totalSmsFailed.incrementAndGet();
                return new NotificationResult(false, "Numéro de téléphone invalide", null);
            }

            if (message == null || message.isEmpty()) {
                totalSmsFailed.incrementAndGet();
                return new NotificationResult(false, "Message vide", null);
            }

            // Simulation de l'envoi (en production: appel API réel)
            Thread.sleep(50); // Simule la latence réseau

            // Générer un ID de notification
            String notificationId = "SMS" + System.currentTimeMillis();

            // Enregistrer dans l'historique
            NotificationRecord record = new NotificationRecord(
                notificationId, phoneNumber, message, LocalDateTime.now(), true
            );
            notificationHistory.add(record);

            // Mettre à jour les statistiques
            totalSmsSent.incrementAndGet();
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTime.addAndGet(responseTime);

            return new NotificationResult(true, "SMS envoyé avec succès", notificationId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            totalSmsFailed.incrementAndGet();
            return new NotificationResult(false, "Envoi interrompu", null);
        }
    }

    /**
     * Envoie un SMS à plusieurs destinataires.
     */
    public List<NotificationResult> sendBulkSMS(List<String> phoneNumbers, String message) {
        List<NotificationResult> results = new ArrayList<>();
        for (String phone : phoneNumbers) {
            results.add(sendSMS(phone, message));
        }
        return results;
    }

    // ==================== GETTERS ====================

    public String getSmsProviderUrl() { return smsProviderUrl; }
    public boolean isInitialized() { return initialized; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public int getTotalSmsSent() { return totalSmsSent.get(); }
    public int getTotalSmsFailed() { return totalSmsFailed.get(); }
    public int getAccessCount() { return accessCount.get(); }

    public double getAverageResponseTime() {
        int total = totalSmsSent.get();
        if (total == 0) return 0;
        return (double) totalResponseTime.get() / total;
    }

    public List<NotificationRecord> getNotificationHistory() {
        return new ArrayList<>(notificationHistory);
    }

    /**
     * Retourne les statistiques du service.
     */
    public ServiceStats getStats() {
        return new ServiceStats(
            createdAt,
            totalSmsSent.get(),
            totalSmsFailed.get(),
            getAverageResponseTime(),
            accessCount.get(),
            notificationHistory.size()
        );
    }

    /**
     * Réinitialise le singleton (pour les tests uniquement).
     * NE PAS UTILISER EN PRODUCTION.
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * Nettoie les ressources avant l'arrêt.
     */
    public synchronized void shutdown() {
        // Fermer les connexions, vider les buffers, etc.
        notificationHistory.clear();
        initialized = false;
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Résultat d'envoi de notification.
     */
    public static class NotificationResult {
        private final boolean success;
        private final String message;
        private final String notificationId;

        public NotificationResult(boolean success, String message, String notificationId) {
            this.success = success;
            this.message = message;
            this.notificationId = notificationId;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getNotificationId() { return notificationId; }
    }

    /**
     * Enregistrement d'une notification.
     */
    public static class NotificationRecord {
        private final String id;
        private final String phoneNumber;
        private final String message;
        private final LocalDateTime sentAt;
        private final boolean success;

        public NotificationRecord(String id, String phone, String msg, LocalDateTime time, boolean success) {
            this.id = id;
            this.phoneNumber = phone;
            this.message = msg;
            this.sentAt = time;
            this.success = success;
        }

        public String getId() { return id; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getMessage() { return message; }
        public LocalDateTime getSentAt() { return sentAt; }
        public boolean isSuccess() { return success; }
    }

    /**
     * Statistiques du service.
     */
    public static class ServiceStats {
        private final LocalDateTime createdAt;
        private final int totalSent;
        private final int totalFailed;
        private final double avgResponseTime;
        private final int accessCount;
        private final int historySize;

        public ServiceStats(LocalDateTime created, int sent, int failed, double avgTime, int access, int history) {
            this.createdAt = created;
            this.totalSent = sent;
            this.totalFailed = failed;
            this.avgResponseTime = avgTime;
            this.accessCount = access;
            this.historySize = history;
        }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public int getTotalSent() { return totalSent; }
        public int getTotalFailed() { return totalFailed; }
        public double getAvgResponseTime() { return avgResponseTime; }
        public int getAccessCount() { return accessCount; }
        public int getHistorySize() { return historySize; }
    }
}
