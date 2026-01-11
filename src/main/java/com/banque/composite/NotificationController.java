package com.banque.composite;

import com.banque.composite.Notification.NotificationResult;
import com.banque.composite.Notification.NotificationType;
import com.banque.composite.Notification.Priority;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * PATTERN COMPOSITE - Controller REST pour les Notifications
 *
 * OBJECTIF 7: Expose les fonctionnalites du pattern Composite via API REST.
 *
 * ENDPOINTS:
 * - POST /api/notifications/sms - Envoie un SMS
 * - POST /api/notifications/email - Envoie un Email
 * - POST /api/notifications/push - Envoie un Push
 * - POST /api/notifications/group - Envoie un groupe
 * - POST /api/notifications/multi-channel - Envoie multi-canal
 * - POST /api/notifications/broadcast - Diffusion
 * - POST /api/notifications/template/{id} - Via template
 * - GET /api/notifications/templates - Liste des templates
 * - GET /api/notifications/stats - Statistiques
 * - GET /api/notifications/history - Historique
 * - GET /api/notifications/demo - Demonstration
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ==================== ENVOI SIMPLE ====================

    /**
     * Envoie un SMS.
     */
    @PostMapping("/sms")
    public ResponseEntity<NotificationResponse> sendSMS(@RequestBody SMSRequest request) {
        NotificationResult result = notificationService.sendSMS(
            request.getPhoneNumber(),
            request.getMessage()
        );
        return ResponseEntity.ok(NotificationResponse.from(result));
    }

    /**
     * Envoie un Email.
     */
    @PostMapping("/email")
    public ResponseEntity<NotificationResponse> sendEmail(@RequestBody EmailRequest request) {
        NotificationResult result = notificationService.sendEmail(
            request.getEmail(),
            request.getSubject(),
            request.getBody()
        );
        return ResponseEntity.ok(NotificationResponse.from(result));
    }

    /**
     * Envoie une notification Push.
     */
    @PostMapping("/push")
    public ResponseEntity<NotificationResponse> sendPush(@RequestBody PushRequest request) {
        NotificationResult result = notificationService.sendPush(
            request.getDeviceToken(),
            request.getTitle(),
            request.getBody()
        );
        return ResponseEntity.ok(NotificationResponse.from(result));
    }

    // ==================== ENVOI GROUPE ====================

    /**
     * Envoie un groupe multi-canal.
     */
    @PostMapping("/multi-channel")
    public ResponseEntity<NotificationResponse> sendMultiChannel(@RequestBody MultiChannelRequest request) {
        NotificationResult result = notificationService.sendMultiChannel(
            request.getName(),
            request.getPhone(),
            request.getEmail(),
            request.getDeviceToken(),
            request.getMessage()
        );
        return ResponseEntity.ok(NotificationResponse.from(result));
    }

    /**
     * Envoie une diffusion SMS.
     */
    @PostMapping("/broadcast/sms")
    public ResponseEntity<NotificationResponse> sendSMSBroadcast(@RequestBody BroadcastRequest request) {
        NotificationResult result = notificationService.sendSMSBroadcast(
            request.getName(),
            request.getRecipients(),
            request.getMessage()
        );
        return ResponseEntity.ok(NotificationResponse.from(result));
    }

    /**
     * Envoie une diffusion Email.
     */
    @PostMapping("/broadcast/email")
    public ResponseEntity<NotificationResponse> sendEmailBroadcast(@RequestBody EmailBroadcastRequest request) {
        NotificationResult result = notificationService.sendEmailBroadcast(
            request.getName(),
            request.getRecipients(),
            request.getSubject(),
            request.getBody()
        );
        return ResponseEntity.ok(NotificationResponse.from(result));
    }

    // ==================== ENVOI VIA TEMPLATE ====================

    /**
     * Envoie via template.
     */
    @PostMapping("/template/{templateId}")
    public ResponseEntity<NotificationResponse> sendFromTemplate(
            @PathVariable String templateId,
            @RequestBody TemplateRequest request) {

        NotificationResult result;

        // Determiner le type d'envoi
        if (request.isMultiChannel()) {
            result = notificationService.sendMultiChannelFromTemplate(
                templateId,
                request.getPhone(),
                request.getEmail(),
                request.getDeviceToken(),
                request.getVariables()
            );
        } else if (request.getPhone() != null) {
            result = notificationService.sendSMSFromTemplate(
                templateId,
                request.getPhone(),
                request.getVariables()
            );
        } else if (request.getEmail() != null) {
            result = notificationService.sendEmailFromTemplate(
                templateId,
                request.getEmail(),
                request.getVariables()
            );
        } else if (request.getDeviceToken() != null) {
            result = notificationService.sendPushFromTemplate(
                templateId,
                request.getDeviceToken(),
                request.getVariables()
            );
        } else {
            return ResponseEntity.badRequest()
                .body(NotificationResponse.error("Aucun destinataire specifie"));
        }

        return ResponseEntity.ok(NotificationResponse.from(result));
    }

    // ==================== TEMPLATES ====================

    /**
     * Liste tous les templates disponibles.
     */
    @GetMapping("/templates")
    public ResponseEntity<List<TemplateInfo>> listTemplates() {
        List<TemplateInfo> templates = new ArrayList<>();
        for (NotificationTemplate t : notificationService.getAllTemplates()) {
            templates.add(new TemplateInfo(t.getTemplateId(), t.getTemplateName()));
        }
        return ResponseEntity.ok(templates);
    }

    // ==================== STATISTIQUES ====================

    /**
     * Retourne les statistiques d'envoi.
     */
    @GetMapping("/stats")
    public ResponseEntity<NotificationService.NotificationStats> getStats() {
        return ResponseEntity.ok(notificationService.getStats());
    }

    /**
     * Retourne l'historique des notifications.
     */
    @GetMapping("/history")
    public ResponseEntity<List<NotificationResponse>> getHistory(
            @RequestParam(defaultValue = "50") int limit) {
        List<NotificationResponse> responses = new ArrayList<>();
        for (NotificationResult r : notificationService.getHistory(limit)) {
            responses.add(NotificationResponse.from(r));
        }
        return ResponseEntity.ok(responses);
    }

    /**
     * Remet les statistiques a zero.
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetStats() {
        notificationService.resetStats();
        return ResponseEntity.ok(Map.of("message", "Statistiques remises a zero"));
    }

    // ==================== DEMONSTRATION ====================

    /**
     * Demonstration du pattern Composite.
     */
    @GetMapping("/demo")
    public ResponseEntity<DemoResponse> demo() {
        System.out.println("\n========================================");
        System.out.println("  DEMONSTRATION PATTERN COMPOSITE");
        System.out.println("  Notifications Multi-Canal");
        System.out.println("========================================\n");

        DemoResponse response = new DemoResponse();

        // 1. Notifications simples (Leaf)
        System.out.println(">>> ETAPE 1: Notifications Simples (Leaf)");
        System.out.println("------------------------------------------");

        SMSNotification sms = new SMSNotification("+237690123456", "Votre code OTP: 123456");
        NotificationResult smsResult = sms.send();
        response.addStep("SMS Simple", smsResult);

        EmailNotification email = new EmailNotification(
            "client@example.com",
            "Confirmation de transaction",
            "Votre transaction de 50000 FCFA a ete effectuee."
        );
        NotificationResult emailResult = email.send();
        response.addStep("Email Simple", emailResult);

        PushNotification push = new PushNotification(
            "device-token-abc123",
            "Alerte Securite",
            "Nouvelle connexion detectee"
        );
        NotificationResult pushResult = push.send();
        response.addStep("Push Simple", pushResult);

        // 2. Groupe de notifications (Composite)
        System.out.println("\n>>> ETAPE 2: Groupe de Notifications (Composite)");
        System.out.println("------------------------------------------");

        NotificationGroup alertGroup = new NotificationGroup("Alertes Securite")
            .add(new SMSNotification("+237670111222", "ALERTE: Connexion inhabituelle"))
            .add(new EmailNotification("security@client.com", "Alerte Securite",
                "Une connexion depuis un nouvel appareil a ete detectee."))
            .add(new PushNotification("token-xyz", "ALERTE", "Verifiez votre compte"));

        NotificationResult groupResult = alertGroup.send();
        response.addStep("Groupe Alertes", groupResult);

        // 3. Groupes imbriques (Composite dans Composite)
        System.out.println("\n>>> ETAPE 3: Groupes Imbriques (Composite dans Composite)");
        System.out.println("------------------------------------------");

        // Groupe pour Jean
        NotificationGroup jeanGroup = new NotificationGroup("Notifications Jean")
            .add(new SMSNotification("+237655111222", "Bonjour Jean"))
            .add(new EmailNotification("jean@mail.com", "Message", "Bienvenue Jean"));

        // Groupe pour Marie
        NotificationGroup marieGroup = new NotificationGroup("Notifications Marie")
            .add(new SMSNotification("+237699333444", "Bonjour Marie"))
            .add(new EmailNotification("marie@mail.com", "Message", "Bienvenue Marie"));

        // Groupe parent
        NotificationGroup allClients = new NotificationGroup("Tous les Clients")
            .add(jeanGroup)
            .add(marieGroup)
            .add(new PushNotification("broadcast-token", "Annonce", "Promo speciale!"));

        System.out.println("Structure du groupe hierarchique:");
        System.out.println("  Tous les Clients");
        System.out.println("    |-- Notifications Jean (2)");
        System.out.println("    |-- Notifications Marie (2)");
        System.out.println("    |-- Push Broadcast (1)");
        System.out.println("  Total: " + allClients.getNotificationCount() + " notifications\n");

        NotificationResult nestedResult = allClients.send();
        response.addStep("Groupes Imbriques", nestedResult);

        // 4. Utilisation de templates
        System.out.println("\n>>> ETAPE 4: Notifications via Templates");
        System.out.println("------------------------------------------");

        Map<String, String> transVars = new HashMap<>();
        transVars.put("type", "Virement");
        transVars.put("montant", "100000");
        transVars.put("reference", "TRX-2024-001");
        transVars.put("solde", "450000");
        transVars.put("nom", "Kamga Paul");
        transVars.put("date", "08/01/2026 14:30");

        NotificationResult templateResult = notificationService.sendMultiChannelFromTemplate(
            "TRANS_CONFIRM",
            "+237677889900",
            "paul.kamga@email.com",
            "device-paul-123",
            transVars
        );
        response.addStep("Template Transaction", templateResult);

        // 5. Diffusion (Broadcast)
        System.out.println("\n>>> ETAPE 5: Diffusion SMS (Broadcast)");
        System.out.println("------------------------------------------");

        List<String> phones = Arrays.asList(
            "+237690001111",
            "+237670002222",
            "+237655003333"
        );
        NotificationResult broadcastResult = notificationService.sendSMSBroadcast(
            "Promo Nouvelle Annee",
            phones,
            "BANQUE: Bonne annee 2026! Profitez de 0% de frais jusqu'au 31 janvier!"
        );
        response.addStep("Broadcast SMS", broadcastResult);

        // Resume
        System.out.println("\n========================================");
        System.out.println("  RESUME DE LA DEMONSTRATION");
        System.out.println("========================================");

        NotificationService.NotificationStats stats = notificationService.getStats();
        System.out.println("Total envoyes: " + stats.getTotalSent());
        System.out.println("Total echecs: " + stats.getTotalFailed());
        System.out.println("Taux de succes: " + String.format("%.1f%%", stats.getSuccessRate()));
        System.out.println("Par type: " + stats.getSentByType());

        response.setStats(stats);
        response.setSuccess(true);
        response.setMessage("Demonstration complete - Pattern Composite");

        return ResponseEntity.ok(response);
    }

    // ==================== DTOs ====================

    public static class SMSRequest {
        private String phoneNumber;
        private String message;

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class EmailRequest {
        private String email;
        private String subject;
        private String body;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }

    public static class PushRequest {
        private String deviceToken;
        private String title;
        private String body;

        public String getDeviceToken() { return deviceToken; }
        public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }

    public static class MultiChannelRequest {
        private String name;
        private String phone;
        private String email;
        private String deviceToken;
        private String message;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDeviceToken() { return deviceToken; }
        public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class BroadcastRequest {
        private String name;
        private List<String> recipients;
        private String message;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class EmailBroadcastRequest {
        private String name;
        private List<String> recipients;
        private String subject;
        private String body;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }

    public static class TemplateRequest {
        private String phone;
        private String email;
        private String deviceToken;
        private Map<String, String> variables;
        private boolean multiChannel;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDeviceToken() { return deviceToken; }
        public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
        public Map<String, String> getVariables() { return variables != null ? variables : new HashMap<>(); }
        public void setVariables(Map<String, String> variables) { this.variables = variables; }
        public boolean isMultiChannel() { return multiChannel; }
        public void setMultiChannel(boolean multiChannel) { this.multiChannel = multiChannel; }
    }

    public static class TemplateInfo {
        private String id;
        private String name;

        public TemplateInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
    }

    public static class NotificationResponse {
        private boolean success;
        private String notificationId;
        private String type;
        private String recipient;
        private String message;
        private int totalSent;
        private int totalFailed;
        private double successRate;

        public static NotificationResponse from(NotificationResult result) {
            NotificationResponse response = new NotificationResponse();
            response.success = result.isSuccess();
            response.notificationId = result.getNotificationId();
            response.type = result.getType().getCode();
            response.recipient = result.getRecipient();
            response.message = result.getMessage();
            response.totalSent = result.getTotalSent();
            response.totalFailed = result.getTotalFailed();
            response.successRate = result.getSuccessRate();
            return response;
        }

        public static NotificationResponse error(String message) {
            NotificationResponse response = new NotificationResponse();
            response.success = false;
            response.message = message;
            return response;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getNotificationId() { return notificationId; }
        public String getType() { return type; }
        public String getRecipient() { return recipient; }
        public String getMessage() { return message; }
        public int getTotalSent() { return totalSent; }
        public int getTotalFailed() { return totalFailed; }
        public double getSuccessRate() { return successRate; }
    }

    public static class DemoResponse {
        private boolean success;
        private String message;
        private List<DemoStep> steps = new ArrayList<>();
        private NotificationService.NotificationStats stats;

        public void addStep(String name, NotificationResult result) {
            steps.add(new DemoStep(name, result));
        }

        public void setSuccess(boolean success) { this.success = success; }
        public void setMessage(String message) { this.message = message; }
        public void setStats(NotificationService.NotificationStats stats) { this.stats = stats; }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<DemoStep> getSteps() { return steps; }
        public NotificationService.NotificationStats getStats() { return stats; }
    }

    public static class DemoStep {
        private String name;
        private boolean success;
        private int sent;
        private int failed;

        public DemoStep(String name, NotificationResult result) {
            this.name = name;
            this.success = result.isSuccess();
            this.sent = result.getTotalSent();
            this.failed = result.getTotalFailed();
        }

        public String getName() { return name; }
        public boolean isSuccess() { return success; }
        public int getSent() { return sent; }
        public int getFailed() { return failed; }
    }
}
