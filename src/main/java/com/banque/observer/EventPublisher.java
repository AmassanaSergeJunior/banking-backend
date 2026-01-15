package com.banque.observer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PATTERN OBSERVER - Subject (Publisher)
 *
 * OBJECTIF 10: Classe qui gere les observateurs et publie les evenements.
 * Maintient une liste d'observateurs et les notifie lors d'evenements.
 */
public class EventPublisher {

    private final List<EventObserver> observers;
    private final List<BankEvent> eventHistory;
    private final int maxHistorySize;
    private boolean asyncMode;

    public EventPublisher() {
        this(1000);
    }

    public EventPublisher(int maxHistorySize) {
        this.observers = new CopyOnWriteArrayList<>();
        this.eventHistory = Collections.synchronizedList(new ArrayList<>());
        this.maxHistorySize = maxHistorySize;
        this.asyncMode = false;
    }

    // ==================== GESTION DES OBSERVATEURS ====================

    /**
     * Ajouter un observateur.
     */
    public void subscribe(EventObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            System.out.println("[PUBLISHER] Observateur ajoute: " + observer.getObserverName());
        }
    }

    /**
     * Retire un observateur.
     */
    public void unsubscribe(EventObserver observer) {
        if (observers.remove(observer)) {
            System.out.println("[PUBLISHER] Observateur retire: " + observer.getObserverName());
        }
    }

    /**
     * Retire tous les observateurs.
     */
    public void unsubscribeAll() {
        observers.clear();
        System.out.println("[PUBLISHER] Tous les observateurs retires");
    }

    /**
     * Retourne la liste des observateurs.
     */
    public List<EventObserver> getObservers() {
        return new ArrayList<>(observers);
    }

    /**
     * Retourne le nombre d'observateurs.
     */
    public int getObserverCount() {
        return observers.size();
    }

    // ==================== PUBLICATION D'EVENEMENTS ====================

    /**
     * Publie un evenement a tous les observateurs.
     */
    public void publish(BankEvent event) {
        if (event == null) return;

        // Enregistrer dans l'historique
        addToHistory(event);

        System.out.println("\n[PUBLISHER] Publication: " + event.getEventType() +
            " (" + event.getSeverity() + ")");

        // Notifier les observateurs
        for (EventObserver observer : observers) {
            if (observer.isActive() && observer.isInterestedIn(event.getEventType())) {
                try {
                    observer.onEvent(event);
                } catch (Exception e) {
                    System.err.println("[PUBLISHER] Erreur dans observateur " +
                        observer.getObserverName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Publie plusieurs evenements.
     */
    public void publishAll(List<BankEvent> events) {
        for (BankEvent event : events) {
            publish(event);
        }
    }

    /**
     * Publie un evenement de maniere asynchrone (simulation).
     */
    public void publishAsync(BankEvent event) {
        // Dans une vraie implementation, utiliser un ExecutorService
        new Thread(() -> publish(event)).start();
    }

    // ==================== HISTORIQUE ====================

    private void addToHistory(BankEvent event) {
        eventHistory.add(event);
        // Limiter la taille de l'historique
        while (eventHistory.size() > maxHistorySize) {
            eventHistory.remove(0);
        }
    }

    /**
     * Retourne l'historique des evenements.
     */
    public List<BankEvent> getEventHistory() {
        return new ArrayList<>(eventHistory);
    }

    /**
     * Retourne les derniers evenements.
     */
    public List<BankEvent> getRecentEvents(int count) {
        int size = eventHistory.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(eventHistory.subList(fromIndex, size));
    }

    /**
     * Retourne les evenements par type.
     */
    public List<BankEvent> getEventsByType(BankEvent.EventType type) {
        List<BankEvent> filtered = new ArrayList<>();
        for (BankEvent event : eventHistory) {
            if (event.getEventType() == type) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    /**
     * Efface l'historique.
     */
    public void clearHistory() {
        eventHistory.clear();
    }

    // ==================== CONFIGURATION ====================

    public void setAsyncMode(boolean asyncMode) {
        this.asyncMode = asyncMode;
    }

    public boolean isAsyncMode() {
        return asyncMode;
    }
}
