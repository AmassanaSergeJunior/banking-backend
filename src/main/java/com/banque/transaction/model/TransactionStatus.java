package com.banque.transaction.model;

/**
 * Statuts possibles d'une transaction.
 */
public enum TransactionStatus {
    CREATED("Créée"),
    PENDING_VERIFICATION("En attente de vérification"),
    VERIFIED("Vérifiée"),
    PROCESSING("En cours de traitement"),
    COMPLETED("Terminée"),
    FAILED("Échouée"),
    CANCELLED("Annulée"),
    REVERSED("Annulée (remboursée)");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
