package com.banque.factory.operator.products;

import java.math.BigDecimal;
import java.util.Map;

/**
 * PRODUIT ABSTRAIT 4: Adaptateur vers systèmes externes
 *
 * OBJECTIF 2: Chaque opérateur communique avec des systèmes externes différents.
 * Cette interface définit le contrat commun pour tous les adaptateurs.
 *
 * Exemples de différences entre opérateurs:
 * - Banque: connexion au système SWIFT, réseau interbancaire, BEAC
 * - Mobile Money: API de l'opérateur télécom, systèmes de recharge
 * - Microfinance: systèmes locaux, coopératives partenaires
 */
public interface ExternalSystemAdapter {

    /**
     * Vérifie la connectivité avec le système externe.
     *
     * @return true si le système est accessible
     */
    boolean checkConnectivity();

    /**
     * Effectue un transfert vers un système externe.
     *
     * @param destinationAccount Compte de destination
     * @param amount Montant à transférer
     * @param reference Référence de la transaction
     * @return Résultat du transfert
     */
    TransferResult executeExternalTransfer(
        String destinationAccount,
        BigDecimal amount,
        String reference
    );

    /**
     * Récupère le solde depuis le système externe (pour réconciliation).
     *
     * @param accountNumber Numéro de compte
     * @return Solde récupéré
     */
    BigDecimal fetchExternalBalance(String accountNumber);

    /**
     * Synchronise les données avec le système externe.
     *
     * @param data Données à synchroniser
     * @return Résultat de la synchronisation
     */
    SyncResult synchronize(Map<String, Object> data);

    /**
     * Retourne le nom du système externe.
     */
    String getExternalSystemName();

    /**
     * Retourne le nom de l'opérateur.
     */
    String getOperatorName();

    /**
     * Retourne le protocole utilisé (REST, SOAP, SWIFT, etc.).
     */
    String getProtocol();

    /**
     * Classe pour le résultat de transfert externe.
     */
    class TransferResult {
        private final boolean success;
        private final String externalReference;
        private final String message;
        private final String systemName;

        public TransferResult(boolean success, String externalReference, String message, String systemName) {
            this.success = success;
            this.externalReference = externalReference;
            this.message = message;
            this.systemName = systemName;
        }

        public boolean isSuccess() { return success; }
        public String getExternalReference() { return externalReference; }
        public String getMessage() { return message; }
        public String getSystemName() { return systemName; }

        public static TransferResult success(String externalReference, String systemName) {
            return new TransferResult(true, externalReference, "Transfert réussi", systemName);
        }

        public static TransferResult failure(String message, String systemName) {
            return new TransferResult(false, null, message, systemName);
        }
    }

    /**
     * Classe pour le résultat de synchronisation.
     */
    class SyncResult {
        private final boolean success;
        private final int recordsSynced;
        private final String message;

        public SyncResult(boolean success, int recordsSynced, String message) {
            this.success = success;
            this.recordsSynced = recordsSynced;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public int getRecordsSynced() { return recordsSynced; }
        public String getMessage() { return message; }
    }
}
