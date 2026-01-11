package com.banque.composite;

import java.util.HashMap;
import java.util.Map;

/**
 * PATTERN COMPOSITE - Template de Notification
 *
 * OBJECTIF 7: Permet de creer des notifications basees sur des templates
 * reutilisables avec des variables de substitution.
 *
 * AVANTAGES:
 * - Reutilisation des messages
 * - Coherence des communications
 * - Facilite la maintenance des messages
 * - Support multi-canal (SMS, Email, Push)
 */
public class NotificationTemplate {

    private final String templateId;
    private final String templateName;
    private final String smsTemplate;
    private final String emailSubjectTemplate;
    private final String emailBodyTemplate;
    private final String pushTitleTemplate;
    private final String pushBodyTemplate;

    private NotificationTemplate(Builder builder) {
        this.templateId = builder.templateId;
        this.templateName = builder.templateName;
        this.smsTemplate = builder.smsTemplate;
        this.emailSubjectTemplate = builder.emailSubjectTemplate;
        this.emailBodyTemplate = builder.emailBodyTemplate;
        this.pushTitleTemplate = builder.pushTitleTemplate;
        this.pushBodyTemplate = builder.pushBodyTemplate;
    }

    /**
     * Cree une notification SMS a partir du template.
     */
    public SMSNotification createSMS(String phoneNumber, Map<String, String> variables) {
        String message = substituteVariables(smsTemplate, variables);
        return new SMSNotification(phoneNumber, message);
    }

    /**
     * Cree une notification Email a partir du template.
     */
    public EmailNotification createEmail(String email, Map<String, String> variables) {
        String subject = substituteVariables(emailSubjectTemplate, variables);
        String body = substituteVariables(emailBodyTemplate, variables);
        return new EmailNotification(email, subject, body);
    }

    /**
     * Cree une notification Push a partir du template.
     */
    public PushNotification createPush(String deviceToken, Map<String, String> variables) {
        String title = substituteVariables(pushTitleTemplate, variables);
        String body = substituteVariables(pushBodyTemplate, variables);
        return new PushNotification(deviceToken, title, body);
    }

    /**
     * Cree un groupe multi-canal a partir du template.
     */
    public NotificationGroup createMultiChannel(String phone, String email,
                                                 String deviceToken, Map<String, String> variables) {
        NotificationGroup group = new NotificationGroup(templateName + " - Multi-canal");

        if (phone != null && !phone.isEmpty() && smsTemplate != null) {
            group.add(createSMS(phone, variables));
        }
        if (email != null && !email.isEmpty() && emailSubjectTemplate != null) {
            group.add(createEmail(email, variables));
        }
        if (deviceToken != null && !deviceToken.isEmpty() && pushTitleTemplate != null) {
            group.add(createPush(deviceToken, variables));
        }

        return group;
    }

    /**
     * Substitue les variables dans le template.
     * Format: ${variableName}
     */
    private String substituteVariables(String template, Map<String, String> variables) {
        if (template == null) return "";

        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    @Override
    public String toString() {
        return String.format("NotificationTemplate[id=%s, name=%s]", templateId, templateName);
    }

    // ==================== BUILDER ====================

    public static Builder builder(String templateId) {
        return new Builder(templateId);
    }

    public static class Builder {
        private final String templateId;
        private String templateName;
        private String smsTemplate;
        private String emailSubjectTemplate;
        private String emailBodyTemplate;
        private String pushTitleTemplate;
        private String pushBodyTemplate;

        public Builder(String templateId) {
            this.templateId = templateId;
            this.templateName = templateId;
        }

        public Builder name(String name) {
            this.templateName = name;
            return this;
        }

        public Builder sms(String template) {
            this.smsTemplate = template;
            return this;
        }

        public Builder email(String subject, String body) {
            this.emailSubjectTemplate = subject;
            this.emailBodyTemplate = body;
            return this;
        }

        public Builder push(String title, String body) {
            this.pushTitleTemplate = title;
            this.pushBodyTemplate = body;
            return this;
        }

        public NotificationTemplate build() {
            return new NotificationTemplate(this);
        }
    }

    // ==================== TEMPLATES PRE-DEFINIS ====================

    /**
     * Template pour les confirmations de transaction.
     */
    public static NotificationTemplate transactionConfirmation() {
        return builder("TRANS_CONFIRM")
            .name("Confirmation de Transaction")
            .sms("BANQUE: Transaction ${type} de ${montant} FCFA effectuee. " +
                 "Ref: ${reference}. Solde: ${solde} FCFA.")
            .email("Confirmation de votre transaction ${type}",
                   "Cher(e) ${nom},\n\n" +
                   "Votre transaction a ete effectuee avec succes.\n\n" +
                   "Details:\n" +
                   "- Type: ${type}\n" +
                   "- Montant: ${montant} FCFA\n" +
                   "- Reference: ${reference}\n" +
                   "- Date: ${date}\n" +
                   "- Nouveau solde: ${solde} FCFA\n\n" +
                   "Merci de votre confiance.\n" +
                   "L'equipe de la Banque")
            .push("Transaction ${type}",
                  "${montant} FCFA - Ref: ${reference}")
            .build();
    }

    /**
     * Template pour les alertes de securite.
     */
    public static NotificationTemplate securityAlert() {
        return builder("SEC_ALERT")
            .name("Alerte de Securite")
            .sms("ALERTE SECURITE: ${message}. Si ce n'est pas vous, " +
                 "appelez le 8200 immediatement.")
            .email("ALERTE: Activite suspecte sur votre compte",
                   "Cher(e) ${nom},\n\n" +
                   "ATTENTION: Une activite inhabituelle a ete detectee:\n\n" +
                   "${message}\n\n" +
                   "Date/Heure: ${date}\n" +
                   "Appareil: ${appareil}\n" +
                   "Localisation: ${localisation}\n\n" +
                   "Si vous n'etes pas a l'origine de cette action, " +
                   "veuillez nous contacter immediatement au 8200.\n\n" +
                   "Votre securite est notre priorite.\n" +
                   "Service Securite - Banque")
            .push("ALERTE SECURITE",
                  "${message}")
            .build();
    }

    /**
     * Template pour les rappels de paiement.
     */
    public static NotificationTemplate paymentReminder() {
        return builder("PAY_REMIND")
            .name("Rappel de Paiement")
            .sms("RAPPEL: Votre echeance de ${montant} FCFA arrive a terme " +
                 "le ${date}. Ref: ${reference}.")
            .email("Rappel: Echeance de paiement",
                   "Cher(e) ${nom},\n\n" +
                   "Nous vous rappelons que votre echeance arrive bientot.\n\n" +
                   "Details:\n" +
                   "- Montant: ${montant} FCFA\n" +
                   "- Date d'echeance: ${date}\n" +
                   "- Reference: ${reference}\n\n" +
                   "Pensez a approvisionner votre compte.\n\n" +
                   "Cordialement,\n" +
                   "L'equipe de la Banque")
            .push("Rappel de paiement",
                  "${montant} FCFA - Echeance: ${date}")
            .build();
    }

    /**
     * Template pour le code OTP.
     */
    public static NotificationTemplate otpCode() {
        return builder("OTP_CODE")
            .name("Code OTP")
            .sms("BANQUE: Votre code de verification est ${code}. " +
                 "Valide ${duree} minutes. Ne partagez jamais ce code.")
            .email("Votre code de verification",
                   "Cher(e) ${nom},\n\n" +
                   "Votre code de verification est:\n\n" +
                   "    ${code}\n\n" +
                   "Ce code est valide pendant ${duree} minutes.\n" +
                   "Operation: ${operation}\n\n" +
                   "IMPORTANT: Ne partagez jamais ce code avec qui que ce soit.\n" +
                   "La banque ne vous demandera jamais ce code.\n\n" +
                   "Service Securite - Banque")
            .push("Code de verification",
                  "Votre code: ${code}")
            .build();
    }

    /**
     * Template pour le bienvenue nouveau client.
     */
    public static NotificationTemplate welcomeNewCustomer() {
        return builder("WELCOME")
            .name("Bienvenue")
            .sms("Bienvenue chez BANQUE ${nom}! Votre compte ${compte} est actif. " +
                 "Telechargez notre app: banque.cm/app")
            .email("Bienvenue chez nous!",
                   "Cher(e) ${nom},\n\n" +
                   "Bienvenue dans la famille de notre banque!\n\n" +
                   "Votre compte est maintenant actif:\n" +
                   "- Numero de compte: ${compte}\n" +
                   "- Type: ${type}\n" +
                   "- Agence: ${agence}\n\n" +
                   "Prochaines etapes:\n" +
                   "1. Telechargez notre application mobile\n" +
                   "2. Activez la double authentification\n" +
                   "3. Parametrez vos alertes\n\n" +
                   "Nous sommes ravis de vous compter parmi nous!\n\n" +
                   "Cordialement,\n" +
                   "L'equipe de la Banque")
            .push("Bienvenue ${nom}!",
                  "Votre compte ${compte} est pret")
            .build();
    }
}
