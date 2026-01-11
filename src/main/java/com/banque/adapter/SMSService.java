package com.banque.adapter;

/**
 * PATTERN ADAPTER - Interface Cible (Target)
 *
 * OBJECTIF 5: Cette interface definit le contrat que notre systeme attend
 * pour l'envoi de SMS. Tous les adaptateurs doivent implementer cette interface.
 *
 * POURQUOI ADAPTER?
 * - Integrer des services SMS externes avec des APIs differentes
 * - Uniformiser l'acces aux differents prestataires (Orange, MTN, Twilio)
 * - Permettre de changer de prestataire sans modifier le code client
 * - Isoler les specificites de chaque API externe
 */
public interface SMSService {

    /**
     * Envoie un SMS simple.
     *
     * @param phoneNumber Numero de telephone (format international)
     * @param message Contenu du message
     * @return Resultat de l'envoi
     */
    SMSResult sendSMS(String phoneNumber, String message);

    /**
     * Envoie un SMS avec un expediteur personnalise.
     *
     * @param phoneNumber Numero de telephone
     * @param message Contenu du message
     * @param senderId Identifiant de l'expediteur (ex: "BANQUE_XYZ")
     * @return Resultat de l'envoi
     */
    SMSResult sendSMS(String phoneNumber, String message, String senderId);

    /**
     * Envoie un SMS en masse.
     *
     * @param phoneNumbers Liste des numeros
     * @param message Message commun
     * @return Resultat global de l'envoi
     */
    BulkSMSResult sendBulkSMS(java.util.List<String> phoneNumbers, String message);

    /**
     * Verifie le statut d'un SMS envoye.
     *
     * @param messageId Identifiant du message
     * @return Statut du message
     */
    SMSStatus checkStatus(String messageId);

    /**
     * Retourne le solde/credits disponibles.
     *
     * @return Nombre de credits SMS restants
     */
    int getBalance();

    /**
     * Retourne le nom du prestataire.
     */
    String getProviderName();

    /**
     * Verifie si le service est disponible.
     */
    boolean isAvailable();

    // ==================== CLASSES DE RESULTATS ====================

    /**
     * Resultat d'envoi d'un SMS.
     */
    class SMSResult {
        private final boolean success;
        private final String messageId;
        private final String errorMessage;
        private final int creditsUsed;
        private final long responseTimeMs;

        public SMSResult(boolean success, String messageId, String errorMessage,
                        int creditsUsed, long responseTimeMs) {
            this.success = success;
            this.messageId = messageId;
            this.errorMessage = errorMessage;
            this.creditsUsed = creditsUsed;
            this.responseTimeMs = responseTimeMs;
        }

        public static SMSResult success(String messageId, int credits, long responseTime) {
            return new SMSResult(true, messageId, null, credits, responseTime);
        }

        public static SMSResult failure(String errorMessage) {
            return new SMSResult(false, null, errorMessage, 0, 0);
        }

        public boolean isSuccess() { return success; }
        public String getMessageId() { return messageId; }
        public String getErrorMessage() { return errorMessage; }
        public int getCreditsUsed() { return creditsUsed; }
        public long getResponseTimeMs() { return responseTimeMs; }

        @Override
        public String toString() {
            return success
                ? String.format("SMS envoye [ID: %s, Credits: %d, Temps: %dms]",
                               messageId, creditsUsed, responseTimeMs)
                : String.format("Echec SMS: %s", errorMessage);
        }
    }

    /**
     * Resultat d'envoi en masse.
     */
    class BulkSMSResult {
        private final int totalSent;
        private final int totalFailed;
        private final java.util.List<SMSResult> results;
        private final int totalCreditsUsed;

        public BulkSMSResult(int sent, int failed, java.util.List<SMSResult> results, int credits) {
            this.totalSent = sent;
            this.totalFailed = failed;
            this.results = results;
            this.totalCreditsUsed = credits;
        }

        public int getTotalSent() { return totalSent; }
        public int getTotalFailed() { return totalFailed; }
        public java.util.List<SMSResult> getResults() { return results; }
        public int getTotalCreditsUsed() { return totalCreditsUsed; }
        public double getSuccessRate() {
            int total = totalSent + totalFailed;
            return total > 0 ? (double) totalSent / total * 100 : 0;
        }
    }

    /**
     * Statut d'un message SMS.
     */
    enum SMSStatus {
        PENDING("En attente"),
        SENT("Envoye"),
        DELIVERED("Livre"),
        FAILED("Echec"),
        EXPIRED("Expire"),
        UNKNOWN("Inconnu");

        private final String description;

        SMSStatus(String desc) {
            this.description = desc;
        }

        public String getDescription() { return description; }
    }
}
