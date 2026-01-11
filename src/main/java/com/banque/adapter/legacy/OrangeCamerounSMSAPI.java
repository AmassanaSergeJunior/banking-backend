package com.banque.adapter.legacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * SERVICE LEGACY - API SMS Orange Cameroun (Adaptee)
 *
 * OBJECTIF 5: Cette classe simule l'API propriétaire d'Orange Cameroun
 * avec ses méthodes et formats spécifiques qui diffèrent de notre interface standard.
 *
 * L'API Orange utilise:
 * - Méthodes en français (envoyerMessage, verifierLivraison)
 * - Format de numéro spécifique (sans le +)
 * - Codes de retour numériques au lieu de booléens
 * - Structure de réponse différente (Map)
 */
public class OrangeCamerounSMSAPI {

    private final String apiKey;
    private final String merchantId;
    private int creditsDisponibles;
    private boolean serviceActif;

    // Codes de retour Orange
    public static final int CODE_SUCCES = 200;
    public static final int CODE_ERREUR_NUMERO = 401;
    public static final int CODE_ERREUR_CREDITS = 402;
    public static final int CODE_ERREUR_SERVICE = 500;

    public OrangeCamerounSMSAPI(String apiKey, String merchantId) {
        this.apiKey = apiKey;
        this.merchantId = merchantId;
        this.creditsDisponibles = 1000; // Simulation
        this.serviceActif = true;
    }

    /**
     * Envoie un message via l'API Orange.
     * ATTENTION: Format incompatible avec notre interface standard!
     *
     * @param numeroDestinataire Numéro au format 237XXXXXXXXX (sans +)
     * @param contenu Contenu du message
     * @param expediteur Nom de l'expéditeur
     * @return Map contenant le résultat
     */
    public Map<String, Object> envoyerMessage(String numeroDestinataire,
                                               String contenu,
                                               String expediteur) {
        Map<String, Object> resultat = new HashMap<>();
        long debut = System.currentTimeMillis();

        // Simulation du traitement
        try {
            Thread.sleep(50); // Latence réseau simulée
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Validation du numéro (format Orange: 237XXXXXXXXX)
        if (!validerNumeroOrange(numeroDestinataire)) {
            resultat.put("codeRetour", CODE_ERREUR_NUMERO);
            resultat.put("messageErreur", "Format numero invalide. Utilisez 237XXXXXXXXX");
            resultat.put("succes", false);
            return resultat;
        }

        // Vérification des crédits
        if (creditsDisponibles < 1) {
            resultat.put("codeRetour", CODE_ERREUR_CREDITS);
            resultat.put("messageErreur", "Credits insuffisants");
            resultat.put("succes", false);
            return resultat;
        }

        // Vérification du service
        if (!serviceActif) {
            resultat.put("codeRetour", CODE_ERREUR_SERVICE);
            resultat.put("messageErreur", "Service temporairement indisponible");
            resultat.put("succes", false);
            return resultat;
        }

        // Envoi réussi (simulation)
        String identifiantMessage = "ORA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        creditsDisponibles--;

        resultat.put("codeRetour", CODE_SUCCES);
        resultat.put("succes", true);
        resultat.put("identifiantMessage", identifiantMessage);
        resultat.put("numeroDestinataire", numeroDestinataire);
        resultat.put("expediteur", expediteur);
        resultat.put("creditsUtilises", 1);
        resultat.put("creditsRestants", creditsDisponibles);
        resultat.put("tempsTraitement", System.currentTimeMillis() - debut);
        resultat.put("horodatage", System.currentTimeMillis());

        return resultat;
    }

    /**
     * Vérifie le statut de livraison d'un message.
     *
     * @param identifiantMessage ID du message
     * @return Map avec le statut
     */
    public Map<String, Object> verifierLivraison(String identifiantMessage) {
        Map<String, Object> resultat = new HashMap<>();

        // Simulation: statuts aléatoires basés sur l'ID
        int hash = identifiantMessage.hashCode();
        String statut;
        if (hash % 10 < 7) {
            statut = "LIVRE";
        } else if (hash % 10 < 9) {
            statut = "EN_ATTENTE";
        } else {
            statut = "ECHEC";
        }

        resultat.put("identifiantMessage", identifiantMessage);
        resultat.put("statut", statut);
        resultat.put("dateVerification", System.currentTimeMillis());

        return resultat;
    }

    /**
     * Consulte le solde de crédits.
     *
     * @return Map avec les informations de solde
     */
    public Map<String, Object> consulterSolde() {
        Map<String, Object> resultat = new HashMap<>();
        resultat.put("merchantId", merchantId);
        resultat.put("creditsDisponibles", creditsDisponibles);
        resultat.put("forfait", "ENTREPRISE_PRO");
        resultat.put("dateExpiration", "2025-12-31");
        return resultat;
    }

    /**
     * Vérifie si le service Orange est opérationnel.
     */
    public Map<String, Object> verifierEtatService() {
        Map<String, Object> resultat = new HashMap<>();
        resultat.put("serviceActif", serviceActif);
        resultat.put("nomService", "Orange Cameroun SMS Gateway");
        resultat.put("version", "2.1.0");
        resultat.put("region", "CEMAC");
        return resultat;
    }

    /**
     * Validation du format de numéro Orange (237XXXXXXXXX).
     */
    private boolean validerNumeroOrange(String numero) {
        if (numero == null) return false;
        // Format attendu: 237 suivi de 9 chiffres
        return numero.matches("237[0-9]{9}");
    }

    // Setters pour tests
    public void setCreditsDisponibles(int credits) {
        this.creditsDisponibles = credits;
    }

    public void setServiceActif(boolean actif) {
        this.serviceActif = actif;
    }

    public String getMerchantId() {
        return merchantId;
    }
}
