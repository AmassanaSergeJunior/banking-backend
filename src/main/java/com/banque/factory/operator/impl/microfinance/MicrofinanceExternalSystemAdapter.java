package com.banque.factory.operator.impl.microfinance;

import com.banque.factory.operator.products.ExternalSystemAdapter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * PRODUIT CONCRET: Adaptateur système externe pour Microfinance
 *
 * Caractéristiques:
 * - Connexion au réseau des caisses partenaires
 * - Synchronisation batch (pas de temps réel)
 * - Protocole simplifié
 * - Interopérabilité avec coopératives locales
 */
public class MicrofinanceExternalSystemAdapter implements ExternalSystemAdapter {

    private static final String OPERATOR_NAME = "Microfinance";
    private static final String EXTERNAL_SYSTEM = "Réseau Caisses Partenaires";
    private static final String PROTOCOL = "REST/XML";

    @Override
    public boolean checkConnectivity() {
        // Simulation de vérification de connectivité
        // En production: vérifier la connexion au serveur central du réseau
        return true;
    }

    @Override
    public TransferResult executeExternalTransfer(
            String destinationAccount,
            BigDecimal amount,
            String reference) {

        // Vérification de la connectivité
        if (!checkConnectivity()) {
            return TransferResult.failure(
                "Réseau des caisses partenaires temporairement indisponible. " +
                "Réessayez dans quelques minutes.",
                EXTERNAL_SYSTEM
            );
        }

        // Validation du compte destination (format microfinance)
        if (destinationAccount == null ||
            (!destinationAccount.matches("MF\\d{8}") && !destinationAccount.matches("\\d{9}"))) {
            return TransferResult.failure(
                "Numéro de compte destination invalide",
                EXTERNAL_SYSTEM
            );
        }

        // Simulation du transfert
        String txRef = "MFI" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        return TransferResult.success(txRef, EXTERNAL_SYSTEM);
    }

    @Override
    public BigDecimal fetchExternalBalance(String accountNumber) {
        // Simulation - En production: requête au système central
        return new BigDecimal("75000"); // Solde fictif modeste
    }

    @Override
    public SyncResult synchronize(Map<String, Object> data) {
        // Microfinance fait une synchronisation batch quotidienne
        int recordCount = data != null ? data.size() : 0;

        // En production: synchronisation avec le serveur central
        return new SyncResult(
            true,
            recordCount,
            String.format("Synchronisation quotidienne effectuée. %d transactions synchronisées. " +
                "Prochaine synchronisation: demain 6h00.", recordCount)
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
