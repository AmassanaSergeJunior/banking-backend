import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Consumer;

/**
 * TESTS OBJECTIF 4: Pattern Singleton
 *
 * Ce fichier contient des tests standalone pour verifier:
 * 1. L'unicite des instances (meme reference)
 * 2. Le thread-safety (acces concurrents)
 * 3. Les trois implementations differentes
 * 4. Les fonctionnalites de chaque singleton
 *
 * Compilation: javac Objectif4Test.java
 * Execution: java Objectif4Test
 */
public class Objectif4Test {

    private static int testsReussis = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================");
        System.out.println("        TESTS OBJECTIF 4 - PATTERN SINGLETON                   ");
        System.out.println("        Ressources Globales Thread-Safe                        ");
        System.out.println("================================================================\n");

        // Tests NotificationServiceSingleton (Double-Checked Locking)
        System.out.println("================================================================");
        System.out.println("  TESTS: NotificationServiceSingleton (Double-Checked Locking)");
        System.out.println("================================================================\n");

        testNotificationSingletonUnicity();
        testNotificationSingletonFunctionality();
        testNotificationSingletonConcurrency();

        // Tests AuthenticationConfigSingleton (Holder Pattern)
        System.out.println("\n================================================================");
        System.out.println("  TESTS: AuthenticationConfigSingleton (Holder Pattern)");
        System.out.println("================================================================\n");

        testAuthConfigSingletonUnicity();
        testAuthConfigSingletonFunctionality();
        testAuthConfigSingletonConcurrency();

        // Tests EventManagerSingleton (Enum Singleton)
        System.out.println("\n================================================================");
        System.out.println("  TESTS: EventManagerSingleton (Enum Singleton)");
        System.out.println("================================================================\n");

        testEventManagerSingletonUnicity();
        testEventManagerSingletonFunctionality();
        testEventManagerSingletonConcurrency();

        // Test de stress combine
        System.out.println("\n================================================================");
        System.out.println("  TEST DE STRESS: Tous les Singletons Ensemble");
        System.out.println("================================================================\n");

        testCombinedStress();

        // Resume
        System.out.println("\n================================================================");
        System.out.println("                    RESUME DES TESTS                           ");
        System.out.println("================================================================");
        System.out.printf("  Tests reussis: %d/%d%n", testsReussis, testsTotal);
        if (testsReussis == testsTotal) {
            System.out.println("  Statut: [OK] TOUS LES TESTS PASSES");
        } else {
            System.out.println("  Statut: [ECHEC] CERTAINS TESTS ONT ECHOUE");
        }
        System.out.println("================================================================");

        // Cleanup
        EventManagerSingleton.INSTANCE.shutdown();
    }

    // ==================== TESTS NOTIFICATION SERVICE ====================

    private static void testNotificationSingletonUnicity() {
        System.out.println("TEST 1: Verification de l'unicite de l'instance");
        testsTotal++;

        NotificationServiceSingleton instance1 = NotificationServiceSingleton.getInstance();
        NotificationServiceSingleton instance2 = NotificationServiceSingleton.getInstance();
        NotificationServiceSingleton instance3 = NotificationServiceSingleton.getInstance();

        boolean success = (instance1 == instance2) && (instance2 == instance3);

        if (success) {
            System.out.println("  [OK] Les trois appels retournent la MEME instance");
            System.out.println("  [OK] Pattern Singleton correctement implemente");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Instances differentes detectees!");
        }
        System.out.println();
    }

    private static void testNotificationSingletonFunctionality() {
        System.out.println("TEST 2: Fonctionnalites du service de notification");
        testsTotal++;

        NotificationServiceSingleton service = NotificationServiceSingleton.getInstance();
        service.initialize("https://test-provider.com", "test-key");

        // Test envoi SMS
        NotificationServiceSingleton.NotificationResult result =
            service.sendSMS("+237699000000", "Test message");

        boolean success = result.isSuccess()
            && service.getTotalSmsSent() > 0
            && service.isInitialized();

        if (success) {
            System.out.println("  [OK] Service initialise correctement");
            System.out.println("  [OK] SMS envoye avec succes: " + result.getNotificationId());
            System.out.println("  [OK] Statistiques: " + service.getTotalSmsSent() + " SMS envoyes");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Fonctionnalite SMS non fonctionnelle");
        }
        System.out.println();
    }

    private static void testNotificationSingletonConcurrency() throws Exception {
        System.out.println("TEST 3: Acces concurrent (100 threads)");
        testsTotal++;

        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<NotificationServiceSingleton> instances =
            Collections.synchronizedSet(new HashSet<>());
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    NotificationServiceSingleton instance = NotificationServiceSingleton.getInstance();
                    instances.add(instance);

                    // Envoyer un SMS
                    NotificationServiceSingleton.NotificationResult result =
                        instance.sendSMS("+23769900" + String.format("%04d", index), "Concurrent test " + index);

                    if (result.isSuccess()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        boolean success = instances.size() == 1 && successCount.get() == threadCount;

        if (success) {
            System.out.println("  [OK] " + threadCount + " threads ont accede au singleton");
            System.out.println("  [OK] UNE SEULE instance creee (taille set: " + instances.size() + ")");
            System.out.println("  [OK] " + successCount.get() + "/" + threadCount + " SMS envoyes avec succes");
            System.out.println("  [OK] Compteur d'acces: " + NotificationServiceSingleton.getInstance().getAccessCount());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + instances.size() + " instances ou " + successCount.get() + " succes");
        }
        System.out.println();
    }

    // ==================== TESTS AUTHENTICATION CONFIG ====================

    private static void testAuthConfigSingletonUnicity() {
        System.out.println("TEST 4: Verification de l'unicite de l'instance");
        testsTotal++;

        AuthenticationConfigSingleton config1 = AuthenticationConfigSingleton.getInstance();
        AuthenticationConfigSingleton config2 = AuthenticationConfigSingleton.getInstance();
        AuthenticationConfigSingleton config3 = AuthenticationConfigSingleton.getInstance();

        boolean success = (config1 == config2) && (config2 == config3);

        if (success) {
            System.out.println("  [OK] Les trois appels retournent la MEME instance");
            System.out.println("  [OK] Holder Pattern correctement implemente");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Instances differentes detectees!");
        }
        System.out.println();
    }

    private static void testAuthConfigSingletonFunctionality() {
        System.out.println("TEST 5: Fonctionnalites de la configuration");
        testsTotal++;

        AuthenticationConfigSingleton config = AuthenticationConfigSingleton.getInstance();

        // Test valeurs par defaut
        int defaultMax = config.getMaxFailedAttempts();

        // Modifier une valeur
        config.setMaxFailedAttempts(10);
        int newMax = config.getMaxFailedAttempts();

        // Verifier la modification depuis une autre "instance"
        int verifyMax = AuthenticationConfigSingleton.getInstance().getMaxFailedAttempts();

        boolean success = defaultMax == 5 && newMax == 10 && verifyMax == 10;

        if (success) {
            System.out.println("  [OK] Valeur par defaut: " + defaultMax);
            System.out.println("  [OK] Nouvelle valeur: " + newMax);
            System.out.println("  [OK] Modification visible depuis autre acces: " + verifyMax);
            System.out.println("  [OK] Modifications comptees: " + config.getModificationCount());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Probleme de configuration");
        }

        // Remettre la valeur par defaut
        config.setMaxFailedAttempts(5);
        System.out.println();
    }

    private static void testAuthConfigSingletonConcurrency() throws Exception {
        System.out.println("TEST 6: Modifications concurrentes (50 threads)");
        testsTotal++;

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Set<AuthenticationConfigSingleton> instances =
            Collections.synchronizedSet(new HashSet<>());
        AtomicInteger errors = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int value = i + 1;
            executor.submit(() -> {
                try {
                    AuthenticationConfigSingleton config = AuthenticationConfigSingleton.getInstance();
                    instances.add(config);

                    // Modifier la configuration
                    config.setSessionTimeoutMinutes(value);

                    // Lire pour verifier coherence
                    int read = config.getSessionTimeoutMinutes();
                    if (read < 1 || read > threadCount) {
                        errors.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        boolean success = instances.size() == 1 && errors.get() == 0;

        if (success) {
            System.out.println("  [OK] " + threadCount + " threads ont modifie la config");
            System.out.println("  [OK] UNE SEULE instance (taille set: " + instances.size() + ")");
            System.out.println("  [OK] Aucune erreur de coherence");
            System.out.println("  [OK] Compteur d'acces: " + AuthenticationConfigSingleton.getInstance().getAccessCount());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + instances.size() + " instances ou " + errors.get() + " erreurs");
        }
        System.out.println();
    }

    // ==================== TESTS EVENT MANAGER ====================

    private static void testEventManagerSingletonUnicity() {
        System.out.println("TEST 7: Verification de l'unicite de l'instance");
        testsTotal++;

        EventManagerSingleton em1 = EventManagerSingleton.getInstance();
        EventManagerSingleton em2 = EventManagerSingleton.getInstance();
        EventManagerSingleton em3 = EventManagerSingleton.INSTANCE;

        boolean success = (em1 == em2) && (em2 == em3) && (em1 == EventManagerSingleton.INSTANCE);

        if (success) {
            System.out.println("  [OK] Les trois appels retournent la MEME instance");
            System.out.println("  [OK] Enum Singleton correctement implemente");
            System.out.println("  [OK] Acces via INSTANCE et getInstance() identiques");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Instances differentes detectees!");
        }
        System.out.println();
    }

    private static void testEventManagerSingletonFunctionality() {
        System.out.println("TEST 8: Fonctionnalites du gestionnaire d'evenements");
        testsTotal++;

        EventManagerSingleton em = EventManagerSingleton.getInstance();
        em.start();

        AtomicInteger eventReceived = new AtomicInteger(0);

        // Enregistrer un listener
        Consumer<EventManagerSingleton.SystemEvent> listener = event -> {
            eventReceived.incrementAndGet();
        };
        em.subscribe(EventManagerSingleton.EventType.TRANSACTION_CREATED, listener);

        // Publier un evenement
        em.publish(
            EventManagerSingleton.EventType.TRANSACTION_CREATED,
            "Test",
            "Transaction de test",
            Map.of("amount", 1000)
        );

        // Attendre un peu pour le traitement
        try { Thread.sleep(200); } catch (InterruptedException e) {}

        boolean success = eventReceived.get() == 1
            && em.getTotalEventsPublished() > 0
            && em.isRunning();

        if (success) {
            System.out.println("  [OK] EventManager demarre");
            System.out.println("  [OK] Listener enregistre et notifie");
            System.out.println("  [OK] Evenements publies: " + em.getTotalEventsPublished());
            System.out.println("  [OK] Evenements recus: " + eventReceived.get());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + eventReceived.get() + " evenements recus");
        }

        em.unsubscribe(EventManagerSingleton.EventType.TRANSACTION_CREATED, listener);
        System.out.println();
    }

    private static void testEventManagerSingletonConcurrency() throws Exception {
        System.out.println("TEST 9: Publications concurrentes (200 evenements)");
        testsTotal++;

        int threadCount = 200;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(threadCount);

        EventManagerSingleton em = EventManagerSingleton.getInstance();
        long startPublished = em.getTotalEventsPublished();

        AtomicInteger received = new AtomicInteger(0);
        Consumer<EventManagerSingleton.SystemEvent> listener = event -> {
            received.incrementAndGet();
        };
        em.subscribe(EventManagerSingleton.EventType.USER_LOGIN, listener);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    EventManagerSingleton.getInstance().publish(
                        EventManagerSingleton.EventType.USER_LOGIN,
                        "Thread-" + index,
                        "Login concurrent #" + index
                    );
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Attendre le traitement
        Thread.sleep(500);

        long endPublished = em.getTotalEventsPublished();
        long published = endPublished - startPublished;

        boolean success = published == threadCount && received.get() == threadCount;

        if (success) {
            System.out.println("  [OK] " + threadCount + " evenements publies en parallele");
            System.out.println("  [OK] " + received.get() + " evenements recus par le listener");
            System.out.println("  [OK] Aucune perte d'evenement");
            System.out.println("  [OK] Total publie: " + em.getTotalEventsPublished());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + published + " publies, " + received.get() + " recus");
        }

        em.unsubscribe(EventManagerSingleton.EventType.USER_LOGIN, listener);
        System.out.println();
    }

    // ==================== TEST DE STRESS COMBINE ====================

    private static void testCombinedStress() throws Exception {
        System.out.println("TEST 10: Stress test combine (300 operations mixtes)");
        testsTotal++;

        int threadCount = 300;
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger(0);

        Set<NotificationServiceSingleton> notifInstances = Collections.synchronizedSet(new HashSet<>());
        Set<AuthenticationConfigSingleton> authInstances = Collections.synchronizedSet(new HashSet<>());
        Set<EventManagerSingleton> eventInstances = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    // Operation differente selon l'index
                    switch (index % 3) {
                        case 0:
                            NotificationServiceSingleton ns = NotificationServiceSingleton.getInstance();
                            notifInstances.add(ns);
                            ns.sendSMS("+23769900" + String.format("%04d", index), "Stress " + index);
                            break;
                        case 1:
                            AuthenticationConfigSingleton ac = AuthenticationConfigSingleton.getInstance();
                            authInstances.add(ac);
                            ac.getMaxFailedAttempts();
                            break;
                        case 2:
                            EventManagerSingleton em = EventManagerSingleton.getInstance();
                            eventInstances.add(em);
                            em.publish(EventManagerSingleton.EventType.SYSTEM_ERROR,
                                      "Stress", "Test #" + index);
                            break;
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        boolean success = notifInstances.size() == 1
            && authInstances.size() == 1
            && eventInstances.size() == 1
            && errors.get() == 0;

        if (success) {
            System.out.println("  [OK] " + threadCount + " operations mixtes executees");
            System.out.println("  [OK] NotificationService: 1 instance unique");
            System.out.println("  [OK] AuthConfig: 1 instance unique");
            System.out.println("  [OK] EventManager: 1 instance unique");
            System.out.println("  [OK] Aucune erreur detectee");
            System.out.println("\n  Statistiques finales:");
            System.out.println("    - NotificationService acces: " +
                NotificationServiceSingleton.getInstance().getAccessCount());
            System.out.println("    - AuthConfig acces: " +
                AuthenticationConfigSingleton.getInstance().getAccessCount());
            System.out.println("    - EventManager acces: " +
                EventManagerSingleton.getInstance().getAccessCount());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Probleme d'unicite ou erreurs");
            System.out.println("    NotifInstances: " + notifInstances.size());
            System.out.println("    AuthInstances: " + authInstances.size());
            System.out.println("    EventInstances: " + eventInstances.size());
            System.out.println("    Erreurs: " + errors.get());
        }
        System.out.println();
    }
}

// ============================================================================
// CLASSES SINGLETON SIMPLIFIEES POUR TEST STANDALONE
// (En production, utiliser les vraies classes du package com.banque.singleton)
// ============================================================================

/**
 * Singleton avec Double-Checked Locking
 */
class NotificationServiceSingleton {
    private static volatile NotificationServiceSingleton instance;
    private static final Object lock = new Object();

    private String smsProviderUrl;
    private String apiKey;
    private boolean initialized;
    private final LocalDateTime createdAt;
    private final AtomicInteger totalSmsSent;
    private final AtomicInteger totalSmsFailed;
    private final AtomicLong totalResponseTime;
    private final List<NotificationRecord> notificationHistory;
    private final AtomicInteger accessCount;

    private NotificationServiceSingleton() {
        this.createdAt = LocalDateTime.now();
        this.totalSmsSent = new AtomicInteger(0);
        this.totalSmsFailed = new AtomicInteger(0);
        this.totalResponseTime = new AtomicLong(0);
        this.notificationHistory = Collections.synchronizedList(new ArrayList<>());
        this.accessCount = new AtomicInteger(0);
        this.initialized = false;
    }

    public static NotificationServiceSingleton getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new NotificationServiceSingleton();
                }
            }
        }
        instance.accessCount.incrementAndGet();
        return instance;
    }

    public synchronized void initialize(String providerUrl, String apiKey) {
        if (initialized) return; // Deja initialise
        this.smsProviderUrl = providerUrl;
        this.apiKey = apiKey;
        this.initialized = true;
    }

    public NotificationResult sendSMS(String phoneNumber, String message) {
        long startTime = System.currentTimeMillis();

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            totalSmsFailed.incrementAndGet();
            return new NotificationResult(false, "Numero invalide", null);
        }

        try {
            Thread.sleep(10); // Simule latence
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String notificationId = "SMS" + System.currentTimeMillis() + Thread.currentThread().getId();
        notificationHistory.add(new NotificationRecord(notificationId, phoneNumber, message,
                                                       LocalDateTime.now(), true));
        totalSmsSent.incrementAndGet();
        totalResponseTime.addAndGet(System.currentTimeMillis() - startTime);

        return new NotificationResult(true, "SMS envoye", notificationId);
    }

    public String getSmsProviderUrl() { return smsProviderUrl; }
    public boolean isInitialized() { return initialized; }
    public int getTotalSmsSent() { return totalSmsSent.get(); }
    public int getTotalSmsFailed() { return totalSmsFailed.get(); }
    public int getAccessCount() { return accessCount.get(); }

    public static class NotificationResult {
        private final boolean success;
        private final String message;
        private final String notificationId;

        public NotificationResult(boolean success, String message, String notificationId) {
            this.success = success;
            this.message = message;
            this.notificationId = notificationId;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getNotificationId() { return notificationId; }
    }

    public static class NotificationRecord {
        private final String id;
        private final String phoneNumber;
        private final String message;
        private final LocalDateTime sentAt;
        private final boolean success;

        public NotificationRecord(String id, String phone, String msg, LocalDateTime time, boolean success) {
            this.id = id;
            this.phoneNumber = phone;
            this.message = msg;
            this.sentAt = time;
            this.success = success;
        }
    }
}

/**
 * Singleton avec Holder Pattern
 */
class AuthenticationConfigSingleton {

    private static class Holder {
        private static final AuthenticationConfigSingleton INSTANCE = new AuthenticationConfigSingleton();
    }

    private volatile int maxFailedAttempts;
    private volatile int sessionTimeoutMinutes;
    private final AtomicInteger accessCount;
    private final AtomicInteger modificationCount;

    private AuthenticationConfigSingleton() {
        this.maxFailedAttempts = 5;
        this.sessionTimeoutMinutes = 30;
        this.accessCount = new AtomicInteger(0);
        this.modificationCount = new AtomicInteger(0);
    }

    public static AuthenticationConfigSingleton getInstance() {
        AuthenticationConfigSingleton instance = Holder.INSTANCE;
        instance.accessCount.incrementAndGet();
        return instance;
    }

    public int getMaxFailedAttempts() { return maxFailedAttempts; }
    public int getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public int getAccessCount() { return accessCount.get(); }
    public int getModificationCount() { return modificationCount.get(); }

    public synchronized void setMaxFailedAttempts(int max) {
        this.maxFailedAttempts = max;
        modificationCount.incrementAndGet();
    }

    public synchronized void setSessionTimeoutMinutes(int minutes) {
        this.sessionTimeoutMinutes = minutes;
        modificationCount.incrementAndGet();
    }
}

/**
 * Singleton avec Enum (recommande par Joshua Bloch)
 */
enum EventManagerSingleton {
    INSTANCE;

    public enum EventType {
        TRANSACTION_CREATED, TRANSACTION_COMPLETED, TRANSACTION_FAILED,
        USER_LOGIN, USER_LOGOUT, USER_LOGIN_FAILED,
        ACCOUNT_CREATED, ACCOUNT_BLOCKED,
        SECURITY_ALERT, SYSTEM_ERROR, NOTIFICATION_SENT
    }

    private final ConcurrentHashMap<EventType, List<Consumer<SystemEvent>>> listeners;
    private final BlockingQueue<SystemEvent> eventQueue;
    private final List<SystemEvent> eventHistory;
    private final AtomicLong totalEventsPublished;
    private final AtomicLong totalEventsProcessed;
    private final AtomicInteger accessCount;
    private final LocalDateTime createdAt;
    private volatile boolean running;
    private ExecutorService executor;

    EventManagerSingleton() {
        this.listeners = new ConcurrentHashMap<>();
        this.eventQueue = new LinkedBlockingQueue<>();
        this.eventHistory = Collections.synchronizedList(new ArrayList<>());
        this.totalEventsPublished = new AtomicLong(0);
        this.totalEventsProcessed = new AtomicLong(0);
        this.accessCount = new AtomicInteger(0);
        this.createdAt = LocalDateTime.now();
        this.running = false;

        for (EventType type : EventType.values()) {
            listeners.put(type, Collections.synchronizedList(new ArrayList<>()));
        }
    }

    public static EventManagerSingleton getInstance() {
        INSTANCE.accessCount.incrementAndGet();
        return INSTANCE;
    }

    public synchronized void start() {
        if (running) return;
        executor = Executors.newFixedThreadPool(2);
        running = true;

        executor.submit(() -> {
            while (running || !eventQueue.isEmpty()) {
                try {
                    SystemEvent event = eventQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (event != null) processEvent(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

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
            }
        }
    }

    public void subscribe(EventType type, Consumer<SystemEvent> listener) {
        listeners.get(type).add(listener);
    }

    public void unsubscribe(EventType type, Consumer<SystemEvent> listener) {
        listeners.get(type).remove(listener);
    }

    public void publish(SystemEvent event) {
        totalEventsPublished.incrementAndGet();
        eventHistory.add(event);

        List<Consumer<SystemEvent>> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            for (Consumer<SystemEvent> listener : typeListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    // Continue
                }
            }
        }
        totalEventsProcessed.incrementAndGet();
    }

    public void publish(EventType type, String source, String message) {
        publish(new SystemEvent(type, source, message, null));
    }

    public void publish(EventType type, String source, String message, Map<String, Object> data) {
        publish(new SystemEvent(type, source, message, data));
    }

    private void processEvent(SystemEvent event) {
        eventHistory.add(event);
        List<Consumer<SystemEvent>> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            for (Consumer<SystemEvent> listener : typeListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    // Continue
                }
            }
        }
        totalEventsProcessed.incrementAndGet();
    }

    public long getTotalEventsPublished() { return totalEventsPublished.get(); }
    public long getTotalEventsProcessed() { return totalEventsProcessed.get(); }
    public int getAccessCount() { return accessCount.get(); }
    public boolean isRunning() { return running; }

    public static class SystemEvent {
        private final String eventId;
        private final EventType type;
        private final String source;
        private final String message;
        private final Map<String, Object> data;
        private final LocalDateTime timestamp;

        public SystemEvent(EventType type, String source, String message, Map<String, Object> data) {
            this.eventId = "EVT" + System.currentTimeMillis();
            this.type = type;
            this.source = source;
            this.message = message;
            this.data = data != null ? new HashMap<>(data) : new HashMap<>();
            this.timestamp = LocalDateTime.now();
        }

        public EventType getType() { return type; }
        public String getSource() { return source; }
        public String getMessage() { return message; }
    }
}
