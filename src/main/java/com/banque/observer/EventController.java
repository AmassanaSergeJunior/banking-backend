package com.banque.observer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * PATTERN OBSERVER - Controller REST
 *
 * OBJECTIF 10: Expose les fonctionnalites du pattern Observer via API REST.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/observers")
    public ResponseEntity<Map<String, Object>> getObservers() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", eventService.getPublisher().getObserverCount());

        List<String> names = new ArrayList<>();
        for (EventObserver obs : eventService.getPublisher().getObservers()) {
            names.add(obs.getObserverName());
        }
        result.put("observers", names);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @RequestParam(defaultValue = "20") int limit) {

        List<Map<String, Object>> events = new ArrayList<>();
        for (BankEvent event : eventService.getPublisher().getRecentEvents(limit)) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", event.getEventId());
            e.put("type", event.getEventType());
            e.put("severity", event.getSeverity());
            e.put("source", event.getSource());
            e.put("timestamp", event.getTimestamp().toString());
            events.add(e);
        }

        return ResponseEntity.ok(events);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        StatisticsObserver stats = eventService.getStatisticsObserver();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEvents", stats.getTotalEvents());
        result.put("transactionCount", stats.getTransactionCount());
        result.put("totalAmount", stats.getTotalTransactionAmount());
        result.put("averageAmount", stats.getAverageTransactionAmount());
        result.put("byType", stats.getEventCounts());
        result.put("bySeverity", stats.getSeverityCounts());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<SecurityObserver.SecurityAlert>> getSecurityAlerts() {
        return ResponseEntity.ok(eventService.getSecurityObserver().getAlerts());
    }

    @GetMapping("/demo")
    public ResponseEntity<DemoResponse> demo() {
        System.out.println("\n========================================");
        System.out.println("  DEMONSTRATION PATTERN OBSERVER");
        System.out.println("  Systeme d'Evenements Bancaires");
        System.out.println("========================================\n");

        // Enregistrer des contacts
        eventService.registerContact("CM001", "+237690123456", "client@mail.com");
        eventService.registerContact("CM002", "+237677889900", "autre@mail.com");

        System.out.println(">>> Observateurs actifs: " +
            eventService.getPublisher().getObserverCount());
        for (EventObserver obs : eventService.getPublisher().getObservers()) {
            System.out.println("    - " + obs.getObserverName());
        }

        // Scenario 1: Depot
        System.out.println("\n>>> SCENARIO 1: Depot sur compte");
        eventService.publishDeposit("CM001", new BigDecimal("500000"));

        // Scenario 2: Retrait avec solde bas
        System.out.println("\n>>> SCENARIO 2: Retrait + Alerte solde bas");
        eventService.publishWithdrawal("CM001", new BigDecimal("450000"));
        eventService.publishLowBalance("CM001", new BigDecimal("50000"), new BigDecimal("100000"));

        // Scenario 3: Tentatives de connexion echouees
        System.out.println("\n>>> SCENARIO 3: Tentatives de connexion echouees");
        eventService.publishLoginFailed("user123", "Mot de passe incorrect");
        eventService.publishLoginFailed("user123", "Mot de passe incorrect");
        eventService.publishLoginFailed("user123", "Mot de passe incorrect");

        // Scenario 4: Transaction suspecte
        System.out.println("\n>>> SCENARIO 4: Transaction suspecte");
        eventService.publishWithdrawal("CM002", new BigDecimal("5000000"));

        // Scenario 5: Fraude detectee
        System.out.println("\n>>> SCENARIO 5: Fraude detectee");
        eventService.publishFraudDetected("CM002", "Utilisation carte dans pays different");

        // Resume
        System.out.println("\n========================================");
        System.out.println("  RESUME DE LA DEMONSTRATION");
        System.out.println("========================================");

        StatisticsObserver stats = eventService.getStatisticsObserver();
        System.out.println(stats.generateSummary());

        System.out.println("Alertes securite: " + eventService.getSecurityObserver().getAlertCount());
        System.out.println("Notifications envoyees: " + eventService.getNotificationObserver().getNotificationCount());
        System.out.println("Logs enregistres: " + eventService.getLoggingObserver().getLogCount());

        DemoResponse response = new DemoResponse();
        response.setSuccess(true);
        response.setMessage("Demonstration complete - Pattern Observer");
        response.setTotalEvents(stats.getTotalEvents());
        response.setAlertCount(eventService.getSecurityObserver().getAlertCount());
        response.setNotificationCount(eventService.getNotificationObserver().getNotificationCount());

        return ResponseEntity.ok(response);
    }

    public static class DemoResponse {
        private boolean success;
        private String message;
        private int totalEvents;
        private int alertCount;
        private int notificationCount;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getTotalEvents() { return totalEvents; }
        public void setTotalEvents(int totalEvents) { this.totalEvents = totalEvents; }
        public int getAlertCount() { return alertCount; }
        public void setAlertCount(int alertCount) { this.alertCount = alertCount; }
        public int getNotificationCount() { return notificationCount; }
        public void setNotificationCount(int notificationCount) { this.notificationCount = notificationCount; }
    }
}
