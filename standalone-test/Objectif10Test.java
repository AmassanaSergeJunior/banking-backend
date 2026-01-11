import java.math.BigDecimal;
import java.util.*;
import java.lang.reflect.*;

/**
 * ================================================================
 *         TESTS OBJECTIF 10 - PATTERN OBSERVER
 *           Systeme d'Evenements Bancaires
 * ================================================================
 */
public class Objectif10Test {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static List<String> failedTestNames = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("\n================================================================");
        System.out.println("         TESTS OBJECTIF 10 - PATTERN OBSERVER");
        System.out.println("           Systeme d'Evenements Bancaires");
        System.out.println("================================================================\n");

        testBankEvent();
        testEventObserverInterface();
        testEventPublisher();
        testLoggingObserver();
        testNotificationObserver();
        testSecurityObserver();
        testStatisticsObserver();
        testMultipleObservers();
        testEventFiltering();

        printSummary();
    }

    static void testBankEvent() {
        printTestHeader("Test 1: BankEvent");
        try {
            Class<?> eventClass = Class.forName("com.banque.observer.BankEvent");
            Class<?> eventTypeClass = Class.forName("com.banque.observer.BankEvent$EventType");

            // Factory method
            Method depositMade = eventClass.getMethod("depositMade", String.class, BigDecimal.class);
            Object event = depositMade.invoke(null, "CM001", new BigDecimal("50000"));
            assertNotNull("Evenement cree", event);

            Method getEventId = eventClass.getMethod("getEventId");
            String id = (String) getEventId.invoke(event);
            assertTrue("EventId commence par EVT-", id.startsWith("EVT-"));

            Method getEventType = eventClass.getMethod("getEventType");
            Object type = getEventType.invoke(event);
            assertEquals("Type est DEPOSIT_MADE", "DEPOSIT_MADE", type.toString());

            Method getAmount = eventClass.getMethod("getAmount");
            BigDecimal amount = (BigDecimal) getAmount.invoke(event);
            assertEquals("Montant correct", new BigDecimal("50000"), amount);

            System.out.println("    [OK] BankEvent fonctionne correctement\n");
        } catch (Exception e) {
            failTest("BankEvent", e);
        }
    }

    static void testEventObserverInterface() {
        printTestHeader("Test 2: Interface EventObserver");
        try {
            Class<?> observerClass = Class.forName("com.banque.observer.EventObserver");
            assertTrue("EventObserver est une interface", observerClass.isInterface());

            Class<?> eventClass = Class.forName("com.banque.observer.BankEvent");
            Method onEvent = observerClass.getMethod("onEvent", eventClass);
            assertNotNull("Methode onEvent(BankEvent) existe", onEvent);

            Method getName = observerClass.getMethod("getObserverName");
            assertNotNull("Methode getObserverName() existe", getName);

            System.out.println("    [OK] Interface Observer bien definie\n");
        } catch (Exception e) {
            failTest("EventObserver", e);
        }
    }

    static void testEventPublisher() {
        printTestHeader("Test 3: EventPublisher (Subject)");
        try {
            Class<?> publisherClass = Class.forName("com.banque.observer.EventPublisher");
            Class<?> observerClass = Class.forName("com.banque.observer.EventObserver");

            Object publisher = publisherClass.getDeclaredConstructor().newInstance();

            // Verifier subscribe
            Method subscribe = publisherClass.getMethod("subscribe", observerClass);
            assertNotNull("Methode subscribe() existe", subscribe);

            // Verifier unsubscribe
            Method unsubscribe = publisherClass.getMethod("unsubscribe", observerClass);
            assertNotNull("Methode unsubscribe() existe", unsubscribe);

            // Verifier publish
            Class<?> eventClass = Class.forName("com.banque.observer.BankEvent");
            Method publish = publisherClass.getMethod("publish", eventClass);
            assertNotNull("Methode publish() existe", publish);

            // Verifier getObserverCount
            Method getCount = publisherClass.getMethod("getObserverCount");
            assertEquals("Initialement 0 observateurs", 0, ((Integer) getCount.invoke(publisher)).intValue());

            System.out.println("    [OK] EventPublisher gere les observateurs\n");
        } catch (Exception e) {
            failTest("EventPublisher", e);
        }
    }

    static void testLoggingObserver() {
        printTestHeader("Test 4: LoggingObserver");
        try {
            Class<?> loggingClass = Class.forName("com.banque.observer.LoggingObserver");
            Class<?> observerInterface = Class.forName("com.banque.observer.EventObserver");

            assertTrue("LoggingObserver implemente EventObserver",
                observerInterface.isAssignableFrom(loggingClass));

            Object logger = loggingClass.getDeclaredConstructor().newInstance();

            // Creer et traiter un evenement
            Object event = createDepositEvent("CM001", new BigDecimal("100000"));

            Method onEvent = loggingClass.getMethod("onEvent",
                Class.forName("com.banque.observer.BankEvent"));
            onEvent.invoke(logger, event);

            // Verifier le log
            Method getLogCount = loggingClass.getMethod("getLogCount");
            assertEquals("1 log enregistre", 1, ((Integer) getLogCount.invoke(logger)).intValue());

            Method getName = loggingClass.getMethod("getObserverName");
            assertEquals("Nom correct", "LoggingObserver", getName.invoke(logger));

            System.out.println("    [OK] LoggingObserver enregistre les evenements\n");
        } catch (Exception e) {
            failTest("LoggingObserver", e);
        }
    }

    static void testNotificationObserver() {
        printTestHeader("Test 5: NotificationObserver");
        try {
            Class<?> notifClass = Class.forName("com.banque.observer.NotificationObserver");
            Class<?> observerInterface = Class.forName("com.banque.observer.EventObserver");

            assertTrue("NotificationObserver implemente EventObserver",
                observerInterface.isAssignableFrom(notifClass));

            Object notifier = notifClass.getDeclaredConstructor().newInstance();

            // Enregistrer un contact
            Method registerContact = notifClass.getMethod("registerContact",
                String.class, String.class, String.class);
            registerContact.invoke(notifier, "CM001", "+237690123456", "test@mail.com");

            // Envoyer un evenement
            Object event = createDepositEvent("CM001", new BigDecimal("50000"));
            Method onEvent = notifClass.getMethod("onEvent",
                Class.forName("com.banque.observer.BankEvent"));
            onEvent.invoke(notifier, event);

            // Verifier les notifications
            Method getCount = notifClass.getMethod("getNotificationCount");
            int count = (Integer) getCount.invoke(notifier);
            assertTrue("Au moins 1 notification envoyee", count >= 1);

            System.out.println("    [OK] NotificationObserver envoie des notifications\n");
        } catch (Exception e) {
            failTest("NotificationObserver", e);
        }
    }

    static void testSecurityObserver() {
        printTestHeader("Test 6: SecurityObserver");
        try {
            Class<?> securityClass = Class.forName("com.banque.observer.SecurityObserver");
            Class<?> observerInterface = Class.forName("com.banque.observer.EventObserver");

            assertTrue("SecurityObserver implemente EventObserver",
                observerInterface.isAssignableFrom(securityClass));

            Object security = securityClass.getDeclaredConstructor().newInstance();

            // Simuler des tentatives de connexion echouees
            Class<?> eventClass = Class.forName("com.banque.observer.BankEvent");
            Method loginFailed = eventClass.getMethod("loginFailed", String.class, String.class);

            Method onEvent = securityClass.getMethod("onEvent", eventClass);

            // 3 tentatives echouees
            for (int i = 0; i < 3; i++) {
                Object event = loginFailed.invoke(null, "user123", "Mot de passe incorrect");
                onEvent.invoke(security, event);
            }

            // Verifier les alertes
            Method getAlertCount = securityClass.getMethod("getAlertCount");
            int alerts = (Integer) getAlertCount.invoke(security);
            assertTrue("Alerte generee apres 3 echecs", alerts >= 1);

            System.out.println("    [OK] SecurityObserver detecte les problemes de securite\n");
        } catch (Exception e) {
            failTest("SecurityObserver", e);
        }
    }

    static void testStatisticsObserver() {
        printTestHeader("Test 7: StatisticsObserver");
        try {
            Class<?> statsClass = Class.forName("com.banque.observer.StatisticsObserver");
            Class<?> observerInterface = Class.forName("com.banque.observer.EventObserver");

            assertTrue("StatisticsObserver implemente EventObserver",
                observerInterface.isAssignableFrom(statsClass));

            Object stats = statsClass.getDeclaredConstructor().newInstance();

            // Envoyer plusieurs evenements
            Class<?> eventClass = Class.forName("com.banque.observer.BankEvent");
            Method onEvent = statsClass.getMethod("onEvent", eventClass);

            onEvent.invoke(stats, createDepositEvent("CM001", new BigDecimal("100000")));
            onEvent.invoke(stats, createDepositEvent("CM002", new BigDecimal("50000")));
            onEvent.invoke(stats, createWithdrawalEvent("CM001", new BigDecimal("30000")));

            // Verifier les stats
            Method getTotalEvents = statsClass.getMethod("getTotalEvents");
            assertEquals("3 evenements traites", 3, ((Integer) getTotalEvents.invoke(stats)).intValue());

            Method getTransactionCount = statsClass.getMethod("getTransactionCount");
            assertEquals("3 transactions", 3, ((Integer) getTransactionCount.invoke(stats)).intValue());

            Method getTotalAmount = statsClass.getMethod("getTotalTransactionAmount");
            BigDecimal total = (BigDecimal) getTotalAmount.invoke(stats);
            assertEquals("Total = 180000", new BigDecimal("180000"), total);

            System.out.println("    [OK] StatisticsObserver collecte les statistiques\n");
        } catch (Exception e) {
            failTest("StatisticsObserver", e);
        }
    }

    static void testMultipleObservers() {
        printTestHeader("Test 8: Plusieurs Observateurs");
        try {
            Class<?> publisherClass = Class.forName("com.banque.observer.EventPublisher");
            Class<?> observerInterface = Class.forName("com.banque.observer.EventObserver");
            Class<?> eventClass = Class.forName("com.banque.observer.BankEvent");

            Object publisher = publisherClass.getDeclaredConstructor().newInstance();

            // Creer les observateurs
            Object logger = Class.forName("com.banque.observer.LoggingObserver")
                .getDeclaredConstructor().newInstance();
            Object stats = Class.forName("com.banque.observer.StatisticsObserver")
                .getDeclaredConstructor().newInstance();

            // S'abonner
            Method subscribe = publisherClass.getMethod("subscribe", observerInterface);
            subscribe.invoke(publisher, logger);
            subscribe.invoke(publisher, stats);

            Method getCount = publisherClass.getMethod("getObserverCount");
            assertEquals("2 observateurs", 2, ((Integer) getCount.invoke(publisher)).intValue());

            // Publier un evenement
            Method publish = publisherClass.getMethod("publish", eventClass);
            Object event = createDepositEvent("CM001", new BigDecimal("75000"));
            publish.invoke(publisher, event);

            // Verifier que les deux ont recu
            Method getLogCount = logger.getClass().getMethod("getLogCount");
            assertEquals("Logger a recu", 1, ((Integer) getLogCount.invoke(logger)).intValue());

            Method getTotalEvents = stats.getClass().getMethod("getTotalEvents");
            assertEquals("Stats a recu", 1, ((Integer) getTotalEvents.invoke(stats)).intValue());

            System.out.println("    [OK] Plusieurs observateurs notifies simultanement\n");
        } catch (Exception e) {
            failTest("Multiple Observers", e);
        }
    }

    static void testEventFiltering() {
        printTestHeader("Test 9: Filtrage d'Evenements");
        try {
            Class<?> observerInterface = Class.forName("com.banque.observer.EventObserver");
            Class<?> securityClass = Class.forName("com.banque.observer.SecurityObserver");
            Class<?> eventTypeClass = Class.forName("com.banque.observer.BankEvent$EventType");

            Object security = securityClass.getDeclaredConstructor().newInstance();

            // Verifier isInterestedIn
            Method isInterestedIn = observerInterface.getMethod("isInterestedIn", eventTypeClass);

            Object loginFailed = getEnumConstant(eventTypeClass, "LOGIN_FAILED");
            Object depositMade = getEnumConstant(eventTypeClass, "DEPOSIT_MADE");

            boolean interestedInLogin = (Boolean) isInterestedIn.invoke(security, loginFailed);
            assertTrue("SecurityObserver interesse par LOGIN_FAILED", interestedInLogin);

            // SecurityObserver n'est pas interesse par DEPOSIT_MADE
            boolean interestedInDeposit = (Boolean) isInterestedIn.invoke(security, depositMade);
            assertFalse("SecurityObserver pas interesse par DEPOSIT_MADE", interestedInDeposit);

            System.out.println("    [OK] Filtrage d'evenements fonctionne\n");
        } catch (Exception e) {
            failTest("Event Filtering", e);
        }
    }

    // ==================== HELPERS ====================

    static Object createDepositEvent(String account, BigDecimal amount) throws Exception {
        Class<?> eventClass = Class.forName("com.banque.observer.BankEvent");
        Method factory = eventClass.getMethod("depositMade", String.class, BigDecimal.class);
        return factory.invoke(null, account, amount);
    }

    static Object createWithdrawalEvent(String account, BigDecimal amount) throws Exception {
        Class<?> eventClass = Class.forName("com.banque.observer.BankEvent");
        Method factory = eventClass.getMethod("withdrawalMade", String.class, BigDecimal.class);
        return factory.invoke(null, account, amount);
    }

    static Object getEnumConstant(Class<?> enumClass, String name) {
        for (Object constant : enumClass.getEnumConstants()) {
            if (constant.toString().equals(name)) {
                return constant;
            }
        }
        return null;
    }

    static void printTestHeader(String name) {
        System.out.println(">> " + name);
        System.out.println("   " + "-".repeat(50));
    }

    static void assertTrue(String message, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("    [PASS] " + message);
        } else {
            failedTests++;
            failedTestNames.add(message);
            System.out.println("    [FAIL] " + message);
        }
    }

    static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    static void assertEquals(String message, Object expected, Object actual) {
        boolean equal = (expected == null && actual == null) ||
                        (expected != null && expected.equals(actual));
        totalTests++;
        if (equal) {
            passedTests++;
            System.out.println("    [PASS] " + message);
        } else {
            failedTests++;
            failedTestNames.add(message);
            System.out.println("    [FAIL] " + message + " (attendu: " + expected + ", obtenu: " + actual + ")");
        }
    }

    static void assertEquals(String message, int expected, int actual) {
        assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
    }

    static void assertNotNull(String message, Object obj) {
        assertTrue(message, obj != null);
    }

    static void failTest(String name, Exception e) {
        totalTests++;
        failedTests++;
        failedTestNames.add(name + ": " + e.getMessage());
        System.out.println("    [FAIL] " + name + ": " + e.getMessage());
        e.printStackTrace();
    }

    static void printSummary() {
        System.out.println("\n================================================================");
        System.out.println("                    RESUME DES TESTS");
        System.out.println("================================================================\n");
        System.out.println("  Total: " + totalTests + " | Reussis: " + passedTests +
            " | Echoues: " + failedTests);
        System.out.printf("  Taux: %.1f%%\n", (totalTests > 0 ? passedTests * 100.0 / totalTests : 0));

        if (failedTests == 0) {
            System.out.println("\n  *** PATTERN OBSERVER IMPLEMENTE AVEC SUCCES! ***");
        } else {
            System.out.println("\n  Echecs: " + failedTestNames);
        }
        System.out.println("\n================================================================\n");
    }
}
