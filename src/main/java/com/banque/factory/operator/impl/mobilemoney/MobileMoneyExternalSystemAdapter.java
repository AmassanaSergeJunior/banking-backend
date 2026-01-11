package com.banque.factory.operator.impl.mobilemoney;

import com.banque.factory.operator.products.ExternalSystemAdapter;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * PRODUIT CONCRET: Adaptateur système externe pour Mobile Money
 *
 * Caractéristiques:
 * - Connexion à l'API de l'opérateur télécom
 * - Protocole REST moderne
 * - Transactions en temps réel
 * - Intégration avec le système de recharge
 */
public class MobileMoneyExternalSystemAdapter implements ExternalSystemAdapter {

    private static final String OPERATOR_NAME = "Mobile Money";
    private static final String EXTERNAL_SYSTEM = "Plateforme Telecom API";
    private static final String PROTOCOL = "REST/JSON";

    @Override
    public boolean checkConnectivity() {
        // Simulation de vérification de connectivité à l'API telecom
        // En production: health check de l'API partenaire
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
                "Service Mobile Money temporairement indisponible",
                EXTERNAL_SYSTEM
            );
        }

        // Validation du numéro de destination (format téléphone)
        if (destinationAccount == null || !destinationAccount.matches("\\d{9}")) {
            return TransferResult.failure(
                "Numéro de téléphone invalide",
                EXTERNAL_SYSTEM
            );
        }

        // Simulation du transfert via API telecom
        String txRef = "MOMO" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // En production: appel REST à l'API du partenaire
        return TransferResult.success(txRef, EXTERNAL_SYSTEM);
    }

    @Override
    public BigDecimal fetchExternalBalance(String accountNumber) {
        // Simulation de récupération du solde via API
        // En production: GET /api/accounts/{phone}/balance
        return new BigDecimal("150000"); // Solde fictif
    }

    @Override
    public SyncResult synchronize(Map<String, Object> data) {
        // Mobile Money fonctionne en temps réel, pas de synchronisation batch
        return new SyncResult(
            true,
            0,
            "Mobile Money fonctionne en temps réel - synchronisation non requise"
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
