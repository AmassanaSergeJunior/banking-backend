package com.banque.composite;

import com.banque.composite.Notification.NotificationResult;
import com.banque.composite.Notification.NotificationType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PATTERN COMPOSITE - Service de Notifications
 *
 * OBJECTIF 7: Service centralise pour gerer toutes les notifications.
 * Utilise le pattern Composite pour traiter uniformement les notifications
 * simples et les groupes.
 *
 * FONCTIONNALITES:
 * - Envoi de notifications simples (SMS, Email, Push)
 * - Envoi de groupes de notifications
 * - Templates pre-definis
 * - Statistiques d'envoi
 * - Historique des notifications
 */
@Service
public class NotificationService {

    private final Map<String, NotificationTemplate> templates;
    private final List<NotificationResult> history;
    private final AtomicInteger totalSent;
    private final AtomicInteger totalFailed;
    private final Map<NotificationType, AtomicInteger> sentByType;

    public NotificationService() {
        this.templates = new ConcurrentHashMap<>();
        this.history = Collections.synchronizedList(new ArrayList<>());
        this.totalSent = new AtomicInteger(0);
        this.totalFailed = new AtomicInteger(0);
        this.sentByType = new ConcurrentHashMap<>();

        // Initialiser les compteurs par type
        for (NotificationType type : NotificationType.values()) {
            sentByType.put(type, new AtomicInteger(0));
        }

        // Charger les templates pre-definis
        loadDefaultTemplates();
    }

    private void loadDefaultTemplates() {
        registerTemplate(NotificationTemplate.transactionConfirmation());
        registerTemplate(NotificationTemplate.securityAlert());
        registerTemplate(NotificationTemplate.paymentReminder());
        registerTemplate(NotificationTemplate.otpCode());
        registerTemplate(NotificationTemplate.welcomeNewCustomer());
    }

    // ==================== ENVOI SIMPLE ====================

    /**
     * Envoie un SMS simple.
     */
    public NotificationResult sendSMS(String phoneNumber, String message) {
        SMSNotification sms = new SMSNotification(phoneNumber, message);
        return sendAndRecord(sms);
    }

    /**
     * Envoie un SMS avec priorite.
     */
    public NotificationResult sendSMS(String phoneNumber, String message,
                                       Notification.Priority priority) {
        SMSNotification sms = new SMSNotification(phoneNumber, message, priority);
        return sendAndRecord(sms);
    }

    /**
     * Envoie un Email simple.
     */
    public NotificationResult sendEmail(String email, String subject, String body) {
        EmailNotification emailNotif = new EmailNotification(email, subject, body);
        return sendAndRecord(emailNotif);
    }

    /**
     * Envoie un Email avec priorite et format HTML.
     */
    public NotificationResult sendEmail(String email, String subject, String body,
                                         Notification.Priority priority, boolean isHtml) {
        EmailNotification emailNotif = new EmailNotification(email, subject, body, priority, isHtml);
        return sendAndRecord(emailNotif);
    }

    /**
     * Envoie une notification Push simple.
     */
    public NotificationResult sendPush(String deviceToken, String title, String body) {
        PushNotification push = new PushNotification(deviceToken, title, body);
        return sendAndRecord(push);
    }

    // ==================== ENVOI GROUPE ====================

    /**
     * Envoie un groupe de notifications.
     */
    public NotificationResult sendGroup(NotificationGroup group) {
        return sendAndRecord(group);
    }

    /**
     * Cree et envoie un groupe multi-canal.
     */
    public NotificationResult sendMultiChannel(String name, String phone, String email,
                                                String deviceToken, String message) {
        NotificationGroup group = NotificationGroup.multiChannel(name, phone, email, deviceToken, message);
        return sendAndRecord(group);
    }

    /**
     * Envoie une diffusion SMS.
     */
    public NotificationResult sendSMSBroadcast(String name, List<String> phoneNumbers, String message) {
        NotificationGroup group = NotificationGroup.smsBroadcast(name, phoneNumbers, message);
        return sendAndRecord(group);
    }

    /**
     * Envoie une diffusion Email.
     */
    public NotificationResult sendEmailBroadcast(String name, List<String> emails,
                                                  String subject, String body) {
        NotificationGroup group = NotificationGroup.emailBroadcast(name, emails, subject, body);
        return sendAndRecord(group);
    }

    // ==================== ENVOI VIA TEMPLATE ====================

    /**
     * Envoie une notification SMS via template.
     */
    public NotificationResult sendSMSFromTemplate(String templateId, String phoneNumber,
                                                   Map<String, String> variables) {
        NotificationTemplate template = templates.get(templateId);
        if (template == null) {
            return NotificationResult.failure("TPL-ERR", NotificationType.SMS,
                phoneNumber, "Template non trouve: " + templateId);
        }
        SMSNotification sms = template.createSMS(phoneNumber, variables);
        return sendAndRecord(sms);
    }

    /**
     * Envoie une notification Email via template.
     */
    public NotificationResult sendEmailFromTemplate(String templateId, String email,
                                                     Map<String, String> variables) {
        NotificationTemplate template = templates.get(templateId);
        if (template == null) {
            return NotificationResult.failure("TPL-ERR", NotificationType.EMAIL,
                email, "Template non trouve: " + templateId);
        }
        EmailNotification emailNotif = template.createEmail(email, variables);
        return sendAndRecord(emailNotif);
    }

    /**
     * Envoie une notification Push via template.
     */
    public NotificationResult sendPushFromTemplate(String templateId, String deviceToken,
                                                    Map<String, String> variables) {
        NotificationTemplate template = templates.get(templateId);
        if (template == null) {
            return NotificationResult.failure("TPL-ERR", NotificationType.PUSH,
                deviceToken, "Template non trouve: " + templateId);
        }
        PushNotification push = template.createPush(deviceToken, variables);
        return sendAndRecord(push);
    }

    /**
     * Envoie un groupe multi-canal via template.
     */
    public NotificationResult sendMultiChannelFromTemplate(String templateId,
                                                            String phone, String email,
                                                            String deviceToken,
                                                            Map<String, String> variables) {
        NotificationTemplate template = templates.get(templateId);
        if (template == null) {
            return NotificationResult.failure("TPL-ERR", NotificationType.GROUP,
                "multi", "Template non trouve: " + templateId);
        }
        NotificationGroup group = template.createMultiChannel(phone, email, deviceToken, variables);
        return sendAndRecord(group);
    }

    // ==================== GESTION DES TEMPLATES ====================

    /**
     * Enregistre un nouveau template.
     */
    public void registerTemplate(NotificationTemplate template) {
        templates.put(template.getTemplateId(), template);
    }

    /**
     * Retourne un template par son ID.
     */
    public NotificationTemplate getTemplate(String templateId) {
        return templates.get(templateId);
    }

    /**
     * Liste tous les templates disponibles.
     */
    public List<NotificationTemplate> getAllTemplates() {
        return new ArrayList<>(templates.values());
    }

    // ==================== STATISTIQUES ====================

    /**
     * Retourne les statistiques d'envoi.
     */
    public NotificationStats getStats() {
        return new NotificationStats(
            totalSent.get(),
            totalFailed.get(),
            getCountsByType(),
            history.size()
        );
    }

    /**
     * Retourne l'historique des notifications.
     */
    public List<NotificationResult> getHistory() {
        return new ArrayList<>(history);
    }

    /**
     * Retourne l'historique limite.
     */
    public List<NotificationResult> getHistory(int limit) {
        int size = history.size();
        int fromIndex = Math.max(0, size - limit);
        return new ArrayList<>(history.subList(fromIndex, size));
    }

    /**
     * Efface l'historique.
     */
    public void clearHistory() {
        history.clear();
    }

    /**
     * Remet les statistiques a zero.
     */
    public void resetStats() {
        totalSent.set(0);
        totalFailed.set(0);
        for (AtomicInteger counter : sentByType.values()) {
            counter.set(0);
        }
        history.clear();
    }

    // ==================== METHODES UTILITAIRES ====================

    private NotificationResult sendAndRecord(Notification notification) {
        NotificationResult result = notification.send();
        recordResult(result);
        return result;
    }

    private void recordResult(NotificationResult result) {
        history.add(result);
        totalSent.addAndGet(result.getTotalSent());
        totalFailed.addAndGet(result.getTotalFailed());

        // Mettre a jour les compteurs par type
        if (result.isGroup() && result.getChildResults() != null) {
            for (NotificationResult child : result.getChildResults()) {
                updateTypeCounter(child);
            }
        } else {
            updateTypeCounter(result);
        }
    }

    private void updateTypeCounter(NotificationResult result) {
        NotificationType type = result.getType();
        if (type != NotificationType.GROUP) {
            sentByType.get(type).incrementAndGet();
        }
    }

    private Map<String, Integer> getCountsByType() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (NotificationType type : NotificationType.values()) {
            if (type != NotificationType.GROUP) {
                counts.put(type.getCode(), sentByType.get(type).get());
            }
        }
        return counts;
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Statistiques de notifications.
     */
    public static class NotificationStats {
        private final int totalSent;
        private final int totalFailed;
        private final Map<String, Integer> sentByType;
        private final int historySize;

        public NotificationStats(int totalSent, int totalFailed,
                                 Map<String, Integer> sentByType, int historySize) {
            this.totalSent = totalSent;
            this.totalFailed = totalFailed;
            this.sentByType = sentByType;
            this.historySize = historySize;
        }

        public int getTotalSent() { return totalSent; }
        public int getTotalFailed() { return totalFailed; }
        public int getTotal() { return totalSent + totalFailed; }
        public Map<String, Integer> getSentByType() { return sentByType; }
        public int getHistorySize() { return historySize; }

        public double getSuccessRate() {
            int total = getTotal();
            return total > 0 ? (double) totalSent / total * 100 : 0;
        }

        @Override
        public String toString() {
            return String.format("Stats[sent=%d, failed=%d, rate=%.1f%%, history=%d]",
                totalSent, totalFailed, getSuccessRate(), historySize);
        }
    }
}
