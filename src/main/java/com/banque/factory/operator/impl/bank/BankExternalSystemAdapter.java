package com.banque.factory.operator.impl.bank;

import com.banque.factory.operator.products.ExternalSystemAdapter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * PRODUIT CONCRET: Adaptateur système externe pour Banque traditionnelle
 *
 * Caractéristiques:
 * - Connexion au réseau SWIFT pour transferts internationaux
 * - Connexion au réseau interbancaire national (BEAC/CEMAC)
 * - Protocole sécurisé avec cryptage
 */
public class BankExternalSystemAdapter implements ExternalSystemAdapter {

    private static final String OPERATOR_NAME = "Banque Traditionnelle";
    private static final String EXTERNAL_SYSTEM = "Réseau Interbancaire BEAC/SWIFT";
    private static final String PROTOCOL = "SWIFT/ISO20022";

    @Override
    public boolean checkConnectivity() {
        // Simulation de vérification de connectivité
        // En production: ping du serveur SWIFT ou du réseau interbancaire
        return true;
    }

    @Override
    public TransferResult executeExternalTransfer(
            String destinationAccount,
            BigDecimal amount,
            String reference) {

        // Vérification préalable de la connectivité
        if (!checkConnectivity()) {
            return TransferResult.failure(
                "Impossible de joindre le réseau interbancaire",
                EXTERNAL_SYSTEM
            );
        }

        // Validation du compte destination (format IBAN simplifié)
        if (destinationAccount == null || destinationAccount.length() < 10) {
            return TransferResult.failure(
                "Format de compte destination invalide",
                EXTERNAL_SYSTEM
            );
        }

        // Simulation du transfert SWIFT
        String swiftReference = "SWIFT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // En production: appel API SWIFT réel
        return TransferResult.success(swiftReference, EXTERNAL_SYSTEM);
    }

    @Override
    public BigDecimal fetchExternalBalance(String accountNumber) {
        // Simulation de récupération du solde depuis le système central
        // En production: requête au core banking system
        return new BigDecimal("1000000"); // Solde fictif
    }

    @Override
    public SyncResult synchronize(Map<String, Object> data) {
        // Simulation de synchronisation avec le système central
        int recordCount = data != null ? data.size() : 0;

        // En production: envoi des données au système central
        return new SyncResult(
            true,
            recordCount,
            String.format("Synchronisation réussie avec %s. %d enregistrements traités.",
                EXTERNAL_SYSTEM, recordCount)
        );
    }

    @Override
    public String getExternalSystemName() {
        return EXTERNAL_SYSTEM;
    }

    @Override
    public String getOperatorName() {
        return OPERATOR_NAME;
    }

    @Override
    public String getProtocol() {
        return PROTOCOL;
    }
}
