package com.banque.transaction.model;

/**
 * Types de transactions supportés par le système.
 */
public enum TransactionType {
    DEPOSIT("Dépôt"),
    WITHDRAWAL("Retrait"),
    TRANSFER_INTERNAL("Transfert interne"),
    TRANSFER_INTER_OPERATOR("Transfert inter-opérateurs"),
    TRANSFER_INTERNATIONAL("Transfert international"),
    PAYMENT("Paiement"),
    BILL_PAYMENT("Paiement de facture");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
