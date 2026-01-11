package com.banque.singleton;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONTROLEUR REST - Démonstration des Singletons
 *
 * Ce contrôleur expose des endpoints pour:
 * - Voir les statistiques des singletons
 * - Tester les fonctionnalités
 * - Démontrer l'unicité des instances
 */
@RestController
@RequestMapping("/api/singletons")
public class SingletonController {

    private final SingletonManager singletonManager;

    public SingletonController(SingletonManager singletonManager) {
        this.singletonManager = singletonManager;
    }

    // ==================== STATISTIQUES GLOBALES ====================

    /**
     * Retourne les statistiques de tous les singletons.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAllStats() {
        return ResponseEntity.ok(singletonManager.getAllStats());
    }

    /**
     * Vérifie la santé de tous les singletons.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Boolean>> healthCheck() {
        return ResponseEntity.ok(singletonManager.healthCheck());
    }

    // ==================== NOTIFICATION SERVICE ====================

    /**
     * Envoie un SMS via le singleton.
     */
    @PostMapping("/notifications/sms")
    public ResponseEntity<Map<String, Object>> sendSms(
            @RequestParam String phoneNumber,
            @RequestParam String message) {

        NotificationServiceSingleton service = NotificationServiceSingleton.getInstance();
        NotificationServiceSingleton.NotificationResult result = service.sendSMS(phoneNumber, message);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        response.put("notificationId", result.getNotificationId());
        response.put("accessCount", service.getAccessCount());

        return ResponseEntity.ok(response);
    }

    /**
     * Retourne les statistiques du service de notification.
     */
    @GetMapping("/notifications/stats")
    public ResponseEntity<NotificationServiceSingleton.ServiceStats> getNotificationStats() {
        return ResponseEntity.ok(NotificationServiceSingleton.getInstance().getStats());
    }

    /**
     * Retourne l'historique des notifications.
     */
    @GetMapping("/notifications/history")
    public ResponseEntity<List<NotificationServiceSingleton.NotificationRecord>> getNotificationHistory() {
        return ResponseEntity.ok(NotificationServiceSingleton.getInstance().getNotificationHistory());
    }

    // ==================== AUTHENTICATION CONFIG ====================

    /**
     * Retourne la configuration d'authentification.
     */
    @GetMapping("/auth/config")
    public ResponseEntity<AuthenticationConfigSingleton.ConfigSummary> getAuthConfig() {
        return ResponseEntity.ok(AuthenticationConfigSingleton.getInstance().getSummary());
    }

    /**
     * Met à jour un paramètre de configuration.
     */
    @PutMapping("/auth/config")
    public ResponseEntity<Map<String, Object>> updateAuthConfig(
            @RequestParam(required = false) Integer maxFailedAttempts,
            @RequestParam(required = false) Integer sessionTimeoutMinutes,
            @RequestParam(required = false) Boolean multiFactorEnabled) {

        AuthenticationConfigSingleton config = AuthenticationConfigSingleton.getInstance();

        if (maxFailedAttempts != null) {
            config.setMaxFailedAttempts(maxFailedAttempts);
        }
        if (sessionTimeoutMinutes != null) {
            config.setSessionTimeoutMinutes(sessionTimeoutMinutes);
        }
        if (multiFactorEnabled != null) {
            config.setMultiFactorEnabled(multiFactorEnabled);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Configuration mise à jour");
        response.put("modificationCount", config.getModificationCount());

        return ResponseEntity.ok(response);
    }

    // ==================== EVENT MANAGER ====================

    /**
     * Publie un événement système.
     */
    @PostMapping("/events/publish")
    public ResponseEntity<Map<String, Object>> publishEvent(
            @RequestParam String type,
            @RequestParam String source,
            @RequestParam String message) {

        EventManagerSingleton eventManager = EventManagerSingleton.getInstance();

        EventManagerSingleton.EventType eventType;
        try {
            eventType = EventManagerSingleton.EventType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Type d'événement invalide: " + type);
            error.put("validTypes", EventManagerSingleton.EventType.values());
            return ResponseEntity.badRequest().body(error);
        }

        eventManager.publish(eventType, source, message);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Événement publié");
        response.put("totalPublished", eventManager.getTotalEventsPublished());

        return ResponseEntity.ok(response);
    }

    /**
     * Retourne l'historique des événements.
     */
    @GetMapping("/events/history")
    public ResponseEntity<List<EventManagerSingleton.SystemEvent>> getEventHistory(
            @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(EventManagerSingleton.getInstance().getRecentEvents(count));
    }

    /**
     * Retourne les statistiques du gestionnaire d'événements.
     */
    @GetMapping("/events/stats")
    public ResponseEntity<EventManagerSingleton.EventManagerStats> getEventStats() {
        return ResponseEntity.ok(EventManagerSingleton.getInstance().getStats());
    }

    // ==================== DEMONSTRATION UNICITE ====================

    /**
     * Démontre l'unicité des singletons en retournant les compteurs d'accès.
     */
    @GetMapping("/demo/uniqueness")
    public ResponseEntity<Map<String, Object>> demonstrateUniqueness() {
        Map<String, Object> demo = new HashMap<>();

        // Accéder plusieurs fois aux singletons
        for (int i = 0; i < 5; i++) {
            NotificationServiceSingleton.getInstance();
            AuthenticationConfigSingleton.getInstance();
            EventManagerSingleton.getInstance();
        }

        // Collecter les compteurs
        demo.put("notificationServiceAccesses", NotificationServiceSingleton.getInstance().getAccessCount());
        demo.put("authConfigAccesses", AuthenticationConfigSingleton.getInstance().getAccessCount());
        demo.put("eventManagerAccesses", EventManagerSingleton.getInstance().getAccessCount());

        // Vérifier que les instances sont les mêmes
        demo.put("sameNotificationInstance",
            NotificationServiceSingleton.getInstance() == NotificationServiceSingleton.getInstance());
        demo.put("sameAuthConfigInstance",
            AuthenticationConfigSingleton.getInstance() == AuthenticationConfigSingleton.getInstance());
        demo.put("sameEventManagerInstance",
            EventManagerSingleton.getInstance() == EventManagerSingleton.getInstance());

        demo.put("message", "Tous les accès retournent la même instance - Pattern Singleton vérifié!");

        return ResponseEntity.ok(demo);
    }
}
