package com.banque.singleton;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * PATTERN SINGLETON - Gestionnaire d'Événements Système
 *
 * OBJECTIF 4: Ce singleton centralise la gestion des événements système,
 * permettant une communication découplée entre les composants.
 *
 * POURQUOI SINGLETON?
 * - Point central pour tous les événements du système
 * - Découplage entre émetteurs et récepteurs
 * - File d'attente unique pour le traitement asynchrone
 * - Thread-safe pour émission/réception concurrentes
 *
 * IMPLEMENTATION: Enum Singleton (recommandé par Joshua Bloch)
 * - Thread-safe par défaut
 * - Résistant à la sérialisation et réflexion
 * - Simple et élégant
 */
public enum EventManagerSingleton {

    INSTANCE;

    // Types d'événements
    public enum EventType {
        TRANSACTION_CREATED,
        TRANSACTION_COMPLETED,
        TRANSACTION_FAILED,
        USER_LOGIN,
        USER_LOGOUT,
        USER_LOGIN_FAILED,
        ACCOUNT_CREATED,
        ACCOUNT_BLOCKED,
        SECURITY_ALERT,
        SYSTEM_ERROR,
        NOTIFICATION_SENT
    }

    // Listeners par type d'événement
    private final ConcurrentHashMap<EventType, List<Consumer<SystemEvent>>> listeners;

    // File d'attente pour traitement asynchrone
    private final BlockingQueue<SystemEvent> eventQueue;

    // Historique des événements
    private final List<SystemEvent> eventHistory;

    // Executor pour traitement asynchrone
    private ExecutorService executor;

    // Statistiques
    private final AtomicLong totalEventsPublished;
    private final AtomicLong totalEventsProcessed;
    private final AtomicInteger accessCount;
    private final LocalDateTime createdAt;
    private volatile boolean running;

    /**
     * Constructeur de l'enum (appelé une seule fois par la JVM).
     */
    EventManagerSingleton() {
        this.listeners = new ConcurrentHashMap<>();
        this.eventQueue = new LinkedBlockingQueue<>();
        this.eventHistory = Collections.synchronizedList(new ArrayList<>());
        this.totalEventsPublished = new AtomicLong(0);
        this.totalEventsProcessed = new AtomicLong(0);
        this.accessCount = new AtomicInteger(0);
        this.createdAt = LocalDateTime.now();
        this.running = false;

        // Initialiser les listes de listeners pour chaque type
        for (EventType type : EventType.values()) {
            listeners.put(type, Collections.synchronizedList(new ArrayList<>()));
        }
    }

    /**
     * Obtient l'instance (pour compatibilité avec le pattern classique).
     */
    public static EventManagerSingleton getInstance() {
        INSTANCE.accessCount.incrementAndGet();
        return INSTANCE;
    }

    /**
     * Démarre le traitement asynchrone des événements.
     */
    public synchronized void start() {
        if (running) return;

        executor = Executors.newFixedThreadPool(2);
        running = true;

        // Thread de traitement des événements
        executor.submit(() -> {
            while (running || !eventQueue.isEmpty()) {
                try {
                    SystemEvent event = eventQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        processEvent(event);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * Arrête le gestionnaire proprement.
     */
    public synchronized void shutdown() {
        running = false;
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Enregistre un listener pour un type d'événement.
     *
     * @param type Type d'événement à écouter
     * @param listener Fonction à appeler lors de l'événement
     */
    public void subscribe(EventType type, Consumer<SystemEvent> listener) {
        listeners.get(type).add(listener);
    }

    /**
     * Retire un listener.
     */
    public void unsubscribe(EventType type, Consumer<SystemEvent> listener) {
        listeners.get(type).remove(listener);
    }

    /**
     * Publie un événement (synchrone - notifie immédiatement les listeners).
     */
    public void publish(SystemEvent event) {
        totalEventsPublished.incrementAndGet();
        eventHistory.add(event);

        // Notifier les listeners de manière synchrone
        List<Consumer<SystemEvent>> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            for (Consumer<SystemEvent> listener : typeListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    // Log l'erreur mais continue avec les autres listeners
                    System.err.println("Erreur lors du traitement de l'événement: " + e.getMessage());
                }
            }
        }
        totalEventsProcessed.incrementAndGet();
    }

    /**
     * Publie un événement de manière asynchrone (via la file d'attente).
     */
    public void publishAsync(SystemEvent event) {
        totalEventsPublished.incrementAndGet();
        eventQueue.offer(event);
    }

    /**
     * Publie un événement simple.
     */
    public void publish(EventType type, String source, String message) {
        publish(new SystemEvent(type, source, message, null));
    }

    /**
     * Publie un événement avec données.
     */
    public void publish(EventType type, String source, String message, Map<String, Object> data) {
        publish(new SystemEvent(type, source, message, data));
    }

    /**
     * Traite un événement de la file d'attente.
     */
    private void processEvent(SystemEvent event) {
        eventHistory.add(event);

        List<Consumer<SystemEvent>> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            for (Consumer<SystemEvent> listener : typeListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    System.err.println("Erreur lors du traitement async: " + e.getMessage());
                }
            }
        }
        totalEventsProcessed.incrementAndGet();
    }

    // ==================== GETTERS ====================

    public LocalDateTime getCreatedAt() { return createdAt; }
    public long getTotalEventsPublished() { return totalEventsPublished.get(); }
    public long getTotalEventsProcessed() { return totalEventsProcessed.get(); }
    public int getAccessCount() { return accessCount.get(); }
    public int getPendingEvents() { return eventQueue.size(); }
    public boolean isRunning() { return running; }

    public List<SystemEvent> getEventHistory() {
        return new ArrayList<>(eventHistory);
    }

    public List<SystemEvent> getEventHistory(EventType type) {
        return eventHistory.stream()
            .filter(e -> e.getType() == type)
            .toList();
    }

    public List<SystemEvent> getRecentEvents(int count) {
        int size = eventHistory.size();
        int start = Math.max(0, size - count);
        return new ArrayList<>(eventHistory.subList(start, size));
    }

    /**
     * Retourne les statistiques du gestionnaire.
     */
    public EventManagerStats getStats() {
        return new EventManagerStats(
            createdAt,
            totalEventsPublished.get(),
            totalEventsProcessed.get(),
            eventQueue.size(),
            eventHistory.size(),
            accessCount.get(),
            running,
            countListeners()
        );
    }

    private int countListeners() {
        return listeners.values().stream()
            .mapToInt(List::size)
            .sum();
    }

    /**
     * Vide l'historique des événements.
     */
    public void clearHistory() {
        eventHistory.clear();
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Représente un événement système.
     */
    public static class SystemEvent {
        private final String eventId;
        private final EventType type;
        private final String source;
        private final String message;
        private final Map<String, Object> data;
        private final LocalDateTime timestamp;

        public SystemEvent(EventType type, String source, String message, Map<String, Object> data) {
            this.eventId = "EVT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);
            this.type = type;
            this.source = source;
            this.message = message;
            this.data = data != null ? new HashMap<>(data) : new HashMap<>();
            this.timestamp = LocalDateTime.now();
        }

        public String getEventId() { return eventId; }
        public EventType getType() { return type; }
        public String getSource() { return source; }
        public String getMessage() { return message; }
        public Map<String, Object> getData() { return Collections.unmodifiableMap(data); }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s: %s", timestamp, type, source, message);
        }
    }

    /**
     * Statistiques du gestionnaire d'événements.
     */
    public static class EventManagerStats {
        private final LocalDateTime createdAt;
        private final long totalPublished;
        private final long totalProcessed;
        private final int pendingCount;
        private final int historySize;
        private final int accessCount;
        private final boolean running;
        private final int listenerCount;

        public EventManagerStats(LocalDateTime created, long pub, long proc, int pending,
                                int history, int access, boolean run, int listeners) {
            this.createdAt = created;
            this.totalPublished = pub;
            this.totalProcessed = proc;
            this.pendingCount = pending;
            this.historySize = history;
            this.accessCount = access;
            this.running = run;
            this.listenerCount = listeners;
        }

        // Getters
        public LocalDateTime getCreatedAt() { return createdAt; }
        public long getTotalPublished() { return totalPublished; }
        public long getTotalProcessed() { return totalProcessed; }
        public int getPendingCount() { return pendingCount; }
        public int getHistorySize() { return historySize; }
        public int getAccessCount() { return accessCount; }
        public boolean isRunning() { return running; }
        public int getListenerCount() { return listenerCount; }
    }
}
