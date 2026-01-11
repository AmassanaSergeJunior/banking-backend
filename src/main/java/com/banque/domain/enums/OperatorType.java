package com.banque.domain.enums;

/**
 * Énumération des différents types d'opérateurs financiers.
 *
 * OBJECTIF 2: Chaque type d'opérateur a sa propre famille d'objets cohérents
 * (validateurs, calculateurs de taux, modules de notification, adaptateurs).
 */
public enum OperatorType {

    BANK("Banque traditionnelle", "Établissement bancaire classique"),
    MOBILE_MONEY("Mobile Money", "Opérateur de paiement mobile (Orange Money, MTN Money, etc.)"),
    MICROFINANCE("Microfinance", "Institution de microfinance");

    private final String displayName;
    private final String description;

    OperatorType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
