package com.banque.observer;

/**
 * PATTERN OBSERVER - Observer Interface
 *
 * OBJECTIF 10: Interface que tous les observateurs doivent implementer.
 * Definit la methode de notification.
 */
public interface EventObserver {

    /**
     * Methode appelee lorsqu'un evenement se produit.
     *
     * @param event L'evenement notifie
     */
    void onEvent(BankEvent event);

    /**
     * Retourne le nom de l'observateur.
     */
    String getObserverName();

    /**
     * Verifier si l'observateur est interesse par un type d'evenement.
     */
    default boolean isInterestedIn(BankEvent.EventType eventType) {
        return true; // Par defaut, interesse par tous les evenements
    }

    /**
     * Verifie si l'observateur est actif.
     */
    default boolean isActive() {
        return true;
    }
}
