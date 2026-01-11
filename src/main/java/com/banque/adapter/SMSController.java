package com.banque.adapter;

import com.banque.adapter.SMSAdapterFactory.SMSProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONTROLEUR REST - Service SMS Unifie
 *
 * OBJECTIF 5: Ce controller expose les fonctionnalites SMS via REST API,
 * demontrant l'utilisation du pattern Adapter.
 */
@RestController
@RequestMapping("/api/sms")
public class SMSController {

    private final UnifiedSMSService smsService;

    public SMSController(UnifiedSMSService smsService) {
        this.smsService = smsService;
    }

    // ==================== ENVOI SMS ====================

    /**
     * Envoie un SMS avec selection automatique du provider.
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendSMS(
            @RequestParam String phoneNumber,
            @RequestParam String message) {

        SMSService.SMSResult result = smsService.sendSMS(phoneNumber, message);
        return ResponseEntity.ok(formatResult(result, null));
    }

    /**
     * Envoie un SMS via un provider specifique.
     */
    @PostMapping("/send/{provider}")
    public ResponseEntity<Map<String, Object>> sendSMSViaProvider(
            @PathVariable String provider,
            @RequestParam String phoneNumber,
            @RequestParam String message) {

        try {
            SMSProvider smsProvider = SMSProvider.valueOf(provider.toUpperCase());
            SMSService.SMSResult result = smsService.sendSMSViaProvider(smsProvider, phoneNumber, message);
            return ResponseEntity.ok(formatResult(result, smsProvider));
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Provider inconnu: " + provider);
            error.put("availableProviders", SMSProvider.values());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Envoie un SMS avec fallback automatique.
     */
    @PostMapping("/send-fallback")
    public ResponseEntity<Map<String, Object>> sendSMSWithFallback(
            @RequestParam String phoneNumber,
            @RequestParam String message) {

        SMSService.SMSResult result = smsService.sendSMSWithFallback(phoneNumber, message);
        return ResponseEntity.ok(formatResult(result, null));
    }

    /**
     * Envoie des SMS en masse.
     */
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> sendBulkSMS(
            @RequestBody BulkSMSRequest request) {

        Map<String, SMSService.SMSResult> results =
            smsService.sendBulkSMSSmart(request.getPhoneNumbers(), request.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("totalRecipients", request.getPhoneNumbers().size());

        int sent = 0, failed = 0;
        Map<String, Object> details = new HashMap<>();
        for (Map.Entry<String, SMSService.SMSResult> entry : results.entrySet()) {
            SMSService.SMSResult r = entry.getValue();
            details.put(entry.getKey(), Map.of(
                "success", r.isSuccess(),
                "messageId", r.getMessageId() != null ? r.getMessageId() : "N/A",
                "error", r.getErrorMessage() != null ? r.getErrorMessage() : ""
            ));
            if (r.isSuccess()) sent++; else failed++;
        }

        response.put("sent", sent);
        response.put("failed", failed);
        response.put("successRate", request.getPhoneNumbers().size() > 0
            ? (double) sent / request.getPhoneNumbers().size() * 100 : 0);
        response.put("details", details);

        return ResponseEntity.ok(response);
    }

    // ==================== STATUTS ET INFOS ====================

    /**
     * Verifie le statut d'un message.
     */
    @GetMapping("/status/{provider}/{messageId}")
    public ResponseEntity<Map<String, Object>> checkStatus(
            @PathVariable String provider,
            @PathVariable String messageId) {

        try {
            SMSProvider smsProvider = SMSProvider.valueOf(provider.toUpperCase());
            SMSService.SMSStatus status = smsService.checkStatus(smsProvider, messageId);

            Map<String, Object> response = new HashMap<>();
            response.put("messageId", messageId);
            response.put("provider", smsProvider.getFullName());
            response.put("status", status.name());
            response.put("description", status.getDescription());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Provider inconnu: " + provider));
        }
    }

    /**
     * Retourne les soldes de tous les providers.
     */
    @GetMapping("/balances")
    public ResponseEntity<Map<String, Object>> getBalances() {
        Map<SMSProvider, Integer> balances = smsService.getAllBalances();

        Map<String, Object> response = new HashMap<>();
        for (Map.Entry<SMSProvider, Integer> entry : balances.entrySet()) {
            response.put(entry.getKey().getShortName(), Map.of(
                "credits", entry.getValue(),
                "fullName", entry.getKey().getFullName()
            ));
        }

        int total = balances.values().stream().mapToInt(Integer::intValue).sum();
        response.put("totalCredits", total);

        return ResponseEntity.ok(response);
    }

    /**
     * Retourne la disponibilite des providers.
     */
    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> getAvailability() {
        Map<SMSProvider, Boolean> availability = smsService.getProvidersAvailability();

        Map<String, Object> response = new HashMap<>();
        for (Map.Entry<SMSProvider, Boolean> entry : availability.entrySet()) {
            response.put(entry.getKey().getShortName(), Map.of(
                "available", entry.getValue(),
                "fullName", entry.getKey().getFullName()
            ));
        }

        long availableCount = availability.values().stream().filter(v -> v).count();
        response.put("availableCount", availableCount);
        response.put("totalProviders", availability.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Retourne les statistiques par provider.
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<SMSProvider, UnifiedSMSService.ProviderStats> stats = smsService.getStatistics();

        Map<String, Object> response = new HashMap<>();
        for (Map.Entry<SMSProvider, UnifiedSMSService.ProviderStats> entry : stats.entrySet()) {
            UnifiedSMSService.ProviderStats s = entry.getValue();
            response.put(entry.getKey().getShortName(), Map.of(
                "sent", s.getTotalSent(),
                "failed", s.getTotalFailed(),
                "creditsUsed", s.getTotalCreditsUsed(),
                "avgResponseTime", String.format("%.2f ms", s.getAverageResponseTime()),
                "successRate", String.format("%.2f%%", s.getSuccessRate())
            ));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Retourne un resume global du service.
     */
    @GetMapping("/summary")
    public ResponseEntity<UnifiedSMSService.ServiceSummary> getSummary() {
        return ResponseEntity.ok(smsService.getSummary());
    }

    /**
     * Liste les providers disponibles.
     */
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getProviders() {
        Map<String, Object> response = new HashMap<>();

        for (SMSProvider provider : SMSProvider.values()) {
            response.put(provider.name(), Map.of(
                "fullName", provider.getFullName(),
                "shortName", provider.getShortName()
            ));
        }

        response.put("default", smsService.getDefaultProvider().name());

        return ResponseEntity.ok(response);
    }

    /**
     * Determine le provider optimal pour un numero.
     */
    @GetMapping("/detect-provider")
    public ResponseEntity<Map<String, Object>> detectProvider(@RequestParam String phoneNumber) {
        SMSProvider provider = SMSAdapterFactory.selectProviderByNumber(phoneNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("phoneNumber", phoneNumber);
        response.put("recommendedProvider", provider.name());
        response.put("providerName", provider.getFullName());

        return ResponseEntity.ok(response);
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Requete d'envoi en masse.
     */
    public static class BulkSMSRequest {
        private List<String> phoneNumbers;
        private String message;

        public List<String> getPhoneNumbers() { return phoneNumbers; }
        public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // ==================== METHODES UTILITAIRES ====================

    private Map<String, Object> formatResult(SMSService.SMSResult result, SMSProvider provider) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());

        if (result.isSuccess()) {
            response.put("messageId", result.getMessageId());
            response.put("creditsUsed", result.getCreditsUsed());
            response.put("responseTimeMs", result.getResponseTimeMs());
        } else {
            response.put("error", result.getErrorMessage());
        }

        if (provider != null) {
            response.put("provider", provider.getFullName());
        }

        return response;
    }
}
