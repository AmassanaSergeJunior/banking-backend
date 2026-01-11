package com.banque.singleton;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GESTIONNAIRE DE SINGLETONS
 *
 * OBJECTIF 4: Cette classe gère le cycle de vie des singletons:
 * - Initialisation au démarrage
 * - Nettoyage propre à l'arrêt
 * - Statistiques centralisées
 *
 * Elle s'intègre avec Spring Boot via @PostConstruct et @PreDestroy.
 */
@Component
public class SingletonManager {

    private final LocalDateTime startTime;
    private boolean initialized;

    public SingletonManager() {
        this.startTime = LocalDateTime.now();
        this.initialized = false;
    }

    /**
     * Initialise tous les singletons au démarrage de l'application.
     */
    @PostConstruct
    public void initialize() {
        System.out.println("=== SingletonManager: Initialisation des singletons ===");

        // 1. Initialiser le service de notification
        NotificationServiceSingleton notifService = NotificationServiceSingleton.getInstance();
        notifService.initialize("https://sms-provider.example.com/api", "demo-api-key");
        System.out.println("  [OK] NotificationServiceSingleton initialisé");

        // 2. Configurer l'authentification
        AuthenticationConfigSingleton authConfig = AuthenticationConfigSingleton.getInstance();
        // Les valeurs par défaut sont déjà définies dans le constructeur
        System.out.println("  [OK] AuthenticationConfigSingleton initialisé");

        // 3. Démarrer le gestionnaire d'événements
        EventManagerSingleton eventManager = EventManagerSingleton.getInstance();
        eventManager.start();
        System.out.println("  [OK] EventManagerSingleton démarré");

        // Enregistrer le shutdown hook pour nettoyage (en plus de @PreDestroy)
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));

        initialized = true;
        System.out.println("=== Tous les singletons sont initialisés ===\n");

        // Publier un événement de démarrage
        eventManager.publish(
            EventManagerSingleton.EventType.SYSTEM_ERROR,
            "SingletonManager",
            "Système démarré avec succès",
            Map.of("startTime", startTime.toString())
        );
    }

    /**
     * Nettoie les ressources à l'arrêt de l'application.
     */
    @PreDestroy
    public void cleanup() {
        System.out.println("\n=== SingletonManager: Nettoyage des singletons ===");

        // 1. Arrêter le gestionnaire d'événements
        try {
            EventManagerSingleton.getInstance().shutdown();
            System.out.println("  [OK] EventManagerSingleton arrêté");
        } catch (Exception e) {
            System.err.println("  [ERR] Erreur arrêt EventManager: " + e.getMessage());
        }

        // 2. Nettoyer le service de notification
        try {
            NotificationServiceSingleton.getInstance().shutdown();
            System.out.println("  [OK] NotificationServiceSingleton nettoyé");
        } catch (Exception e) {
            System.err.println("  [ERR] Erreur nettoyage NotificationService: " + e.getMessage());
        }

        // 3. Réinitialiser la configuration (optionnel)
        try {
            AuthenticationConfigSingleton.getInstance().resetToDefaults();
            System.out.println("  [OK] AuthenticationConfigSingleton réinitialisé");
        } catch (Exception e) {
            System.err.println("  [ERR] Erreur reset AuthConfig: " + e.getMessage());
        }

        System.out.println("=== Nettoyage terminé ===\n");
    }

    /**
     * Retourne les statistiques de tous les singletons.
     */
    public Map<String, Object> getAllStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("managerStartTime", startTime);
        stats.put("initialized", initialized);

        // Stats NotificationService
        NotificationServiceSingleton.ServiceStats notifStats =
            NotificationServiceSingleton.getInstance().getStats();
        Map<String, Object> notifMap = new HashMap<>();
        notifMap.put("createdAt", notifStats.getCreatedAt());
        notifMap.put("totalSent", notifStats.getTotalSent());
        notifMap.put("totalFailed", notifStats.getTotalFailed());
        notifMap.put("avgResponseTime", notifStats.getAvgResponseTime());
        notifMap.put("accessCount", notifStats.getAccessCount());
        stats.put("notificationService", notifMap);

        // Stats AuthConfig
        AuthenticationConfigSingleton.ConfigSummary authStats =
            AuthenticationConfigSingleton.getInstance().getSummary();
        Map<String, Object> authMap = new HashMap<>();
        authMap.put("defaultMethod", authStats.getDefaultMethod());
        authMap.put("maxAttempts", authStats.getMaxAttempts());
        authMap.put("sessionTimeout", authStats.getSessionTimeout());
        authMap.put("accessCount", authStats.getAccessCount());
        authMap.put("modificationCount", authStats.getModificationCount());
        stats.put("authConfig", authMap);

        // Stats EventManager
        EventManagerSingleton.EventManagerStats eventStats =
            EventManagerSingleton.getInstance().getStats();
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("createdAt", eventStats.getCreatedAt());
        eventMap.put("totalPublished", eventStats.getTotalPublished());
        eventMap.put("totalProcessed", eventStats.getTotalProcessed());
        eventMap.put("pendingCount", eventStats.getPendingCount());
        eventMap.put("running", eventStats.isRunning());
        eventMap.put("accessCount", eventStats.getAccessCount());
        stats.put("eventManager", eventMap);

        return stats;
    }

    /**
     * Vérifie la santé de tous les singletons.
     */
    public Map<String, Boolean> healthCheck() {
        Map<String, Boolean> health = new HashMap<>();

        // Vérifier NotificationService
        try {
            NotificationServiceSingleton ns = NotificationServiceSingleton.getInstance();
            health.put("notificationService", ns.isInitialized());
        } catch (Exception e) {
            health.put("notificationService", false);
        }

        // Vérifier AuthConfig
        try {
            AuthenticationConfigSingleton.getInstance();
            health.put("authConfig", true);
        } catch (Exception e) {
            health.put("authConfig", false);
        }

        // Vérifier EventManager
        try {
            EventManagerSingleton em = EventManagerSingleton.getInstance();
            health.put("eventManager", em.isRunning());
        } catch (Exception e) {
            health.put("eventManager", false);
        }

        health.put("overall", !health.containsValue(false));
        return health;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
}
