import java.util.*;
import java.util.concurrent.*;
import java.lang.reflect.*;

/**
 * ================================================================
 *         TESTS OBJECTIF 7 - PATTERN COMPOSITE
 *              Notifications Multi-Canal
 * ================================================================
 *
 * Ce fichier teste l'implementation du pattern Composite pour
 * le systeme de notifications de la banque.
 *
 * Compilation et execution:
 *   cd standalone-test
 *   javac -encoding UTF-8 Objectif7Test.java
 *   java Objectif7Test
 */
public class Objectif7Test {

    // Compteurs de tests
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static List<String> failedTestNames = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("\n================================================================");
        System.out.println("         TESTS OBJECTIF 7 - PATTERN COMPOSITE");
        System.out.println("              Notifications Multi-Canal");
        System.out.println("================================================================\n");

        // Execution des tests
        testInterfaceNotification();
        testSMSNotification();
        testEmailNotification();
        testPushNotification();
        testNotificationGroup();
        testNestedGroups();
        testNotificationCount();
        testGroupStopOnFailure();
        testCountByType();
        testNotificationResult();
        testNotificationTemplate();
        testMultiChannelFactory();

        // Resume final
        printSummary();
    }

    // ==================== TEST 1: Interface Notification ====================
    static void testInterfaceNotification() {
        printTestHeader("Test 1: Interface Notification (Component)");

        try {
            // Verifier que l'interface existe avec les bonnes methodes
            Class<?> notifClass = Class.forName("com.banque.composite.Notification");

            assertTrue("Notification est une interface", notifClass.isInterface());

            // Verifier les methodes requises
            Method sendMethod = notifClass.getMethod("send");
            assertNotNull("Methode send() existe", sendMethod);

            Method getTypeMethod = notifClass.getMethod("getType");
            assertNotNull("Methode getType() existe", getTypeMethod);

            Method getRecipientMethod = notifClass.getMethod("getRecipient");
            assertNotNull("Methode getRecipient() existe", getRecipientMethod);

            Method getContentMethod = notifClass.getMethod("getContent");
            assertNotNull("Methode getContent() existe", getContentMethod);

            // Methodes default
            Method isCompositeMethod = notifClass.getMethod("isComposite");
            assertNotNull("Methode isComposite() existe", isCompositeMethod);
            assertTrue("isComposite() est une methode default", isCompositeMethod.isDefault());

            Method getCountMethod = notifClass.getMethod("getNotificationCount");
            assertNotNull("Methode getNotificationCount() existe", getCountMethod);
            assertTrue("getNotificationCount() est une methode default", getCountMethod.isDefault());

            System.out.println("    [OK] Interface Notification bien definie avec methodes Component\n");
        } catch (Exception e) {
            failTest("Interface Notification", e);
        }
    }

    // ==================== TEST 2: SMS Notification (Leaf) ====================
    static void testSMSNotification() {
        printTestHeader("Test 2: SMSNotification (Leaf)");

        try {
            Class<?> smsClass = Class.forName("com.banque.composite.SMSNotification");
            Class<?> notifInterface = Class.forName("com.banque.composite.Notification");

            // Verifier implementation
            assertTrue("SMSNotification implemente Notification",
                notifInterface.isAssignableFrom(smsClass));

            // Creer une instance
            Constructor<?> ctor = smsClass.getConstructor(String.class, String.class);
            Object sms = ctor.newInstance("+237690123456", "Test message SMS");

            // Verifier les methodes
            Method getRecipient = smsClass.getMethod("getRecipient");
            String recipient = (String) getRecipient.invoke(sms);
            assertTrue("Recipient contient le numero", recipient.contains("690123456"));

            Method getContent = smsClass.getMethod("getContent");
            String content = (String) getContent.invoke(sms);
            assertEquals("Content est le message", "Test message SMS", content);

            Method isComposite = smsClass.getMethod("isComposite");
            boolean composite = (Boolean) isComposite.invoke(sms);
            assertFalse("SMS n'est pas composite (Leaf)", composite);

            Method getCount = smsClass.getMethod("getNotificationCount");
            int count = (Integer) getCount.invoke(sms);
            assertEquals("Count est 1 pour Leaf", 1, count);

            // Test envoi
            Method send = smsClass.getMethod("send");
            Object result = send.invoke(sms);
            assertNotNull("send() retourne un resultat", result);

            Method isSuccess = result.getClass().getMethod("isSuccess");
            assertTrue("Envoi SMS reussi", (Boolean) isSuccess.invoke(result));

            System.out.println("    [OK] SMSNotification fonctionne comme Leaf\n");
        } catch (Exception e) {
            failTest("SMSNotification", e);
        }
    }

    // ==================== TEST 3: Email Notification (Leaf) ====================
    static void testEmailNotification() {
        printTestHeader("Test 3: EmailNotification (Leaf)");

        try {
            Class<?> emailClass = Class.forName("com.banque.composite.EmailNotification");
            Class<?> notifInterface = Class.forName("com.banque.composite.Notification");

            assertTrue("EmailNotification implemente Notification",
                notifInterface.isAssignableFrom(emailClass));

            Constructor<?> ctor = emailClass.getConstructor(String.class, String.class, String.class);
            Object email = ctor.newInstance("test@example.com", "Sujet Test", "Corps du message");

            Method isComposite = emailClass.getMethod("isComposite");
            assertFalse("Email n'est pas composite (Leaf)", (Boolean) isComposite.invoke(email));

            Method getSubject = emailClass.getMethod("getSubject");
            assertEquals("Sujet correct", "Sujet Test", (String) getSubject.invoke(email));

            // Test validation email invalide
            Object invalidEmail = ctor.newInstance("invalid-email", "Sujet", "Corps");
            Method send = emailClass.getMethod("send");
            Object result = send.invoke(invalidEmail);

            Method isSuccess = result.getClass().getMethod("isSuccess");
            assertFalse("Email invalide echoue", (Boolean) isSuccess.invoke(result));

            System.out.println("    [OK] EmailNotification fonctionne comme Leaf avec validation\n");
        } catch (Exception e) {
            failTest("EmailNotification", e);
        }
    }

    // ==================== TEST 4: Push Notification (Leaf) ====================
    static void testPushNotification() {
        printTestHeader("Test 4: PushNotification (Leaf)");

        try {
            Class<?> pushClass = Class.forName("com.banque.composite.PushNotification");
            Class<?> notifInterface = Class.forName("com.banque.composite.Notification");

            assertTrue("PushNotification implemente Notification",
                notifInterface.isAssignableFrom(pushClass));

            Constructor<?> ctor = pushClass.getConstructor(String.class, String.class, String.class);
            Object push = ctor.newInstance("device-token-abc", "Titre Push", "Corps du push");

            Method isComposite = pushClass.getMethod("isComposite");
            assertFalse("Push n'est pas composite (Leaf)", (Boolean) isComposite.invoke(push));

            Method getTitle = pushClass.getMethod("getTitle");
            assertEquals("Titre correct", "Titre Push", (String) getTitle.invoke(push));

            Method getCount = pushClass.getMethod("getNotificationCount");
            assertEquals("Count est 1", 1, (Integer) getCount.invoke(push));

            System.out.println("    [OK] PushNotification fonctionne comme Leaf\n");
        } catch (Exception e) {
            failTest("PushNotification", e);
        }
    }

    // ==================== TEST 5: NotificationGroup (Composite) ====================
    static void testNotificationGroup() {
        printTestHeader("Test 5: NotificationGroup (Composite)");

        try {
            Class<?> groupClass = Class.forName("com.banque.composite.NotificationGroup");
            Class<?> notifInterface = Class.forName("com.banque.composite.Notification");
            Class<?> smsClass = Class.forName("com.banque.composite.SMSNotification");
            Class<?> emailClass = Class.forName("com.banque.composite.EmailNotification");

            assertTrue("NotificationGroup implemente Notification",
                notifInterface.isAssignableFrom(groupClass));

            // Creer un groupe
            Constructor<?> groupCtor = groupClass.getConstructor(String.class);
            Object group = groupCtor.newInstance("Groupe Test");

            // Verifier isComposite
            Method isComposite = groupClass.getMethod("isComposite");
            assertTrue("Group est composite", (Boolean) isComposite.invoke(group));

            // Ajouter des notifications
            Method addMethod = groupClass.getMethod("add", notifInterface);

            Constructor<?> smsCtor = smsClass.getConstructor(String.class, String.class);
            Object sms1 = smsCtor.newInstance("+237690111111", "Message 1");
            Object sms2 = smsCtor.newInstance("+237690222222", "Message 2");

            Constructor<?> emailCtor = emailClass.getConstructor(String.class, String.class, String.class);
            Object email = emailCtor.newInstance("test@mail.com", "Sujet", "Corps");

            addMethod.invoke(group, sms1);
            addMethod.invoke(group, sms2);
            addMethod.invoke(group, email);

            // Verifier le count
            Method getCount = groupClass.getMethod("getNotificationCount");
            assertEquals("Group contient 3 notifications", 3, (Integer) getCount.invoke(group));

            Method getDirectCount = groupClass.getMethod("getDirectChildCount");
            assertEquals("3 enfants directs", 3, (Integer) getDirectCount.invoke(group));

            // Envoyer le groupe
            Method send = groupClass.getMethod("send");
            Object result = send.invoke(group);

            Method getTotalSent = result.getClass().getMethod("getTotalSent");
            assertEquals("3 notifications envoyees", 3, (Integer) getTotalSent.invoke(result));

            System.out.println("    [OK] NotificationGroup fonctionne comme Composite\n");
        } catch (Exception e) {
            failTest("NotificationGroup", e);
        }
    }

    // ==================== TEST 6: Groupes Imbriques ====================
    static void testNestedGroups() {
        printTestHeader("Test 6: Groupes Imbriques (Composite dans Composite)");

        try {
            Class<?> groupClass = Class.forName("com.banque.composite.NotificationGroup");
            Class<?> notifInterface = Class.forName("com.banque.composite.Notification");
            Class<?> smsClass = Class.forName("com.banque.composite.SMSNotification");

            Constructor<?> groupCtor = groupClass.getConstructor(String.class);
            Constructor<?> smsCtor = smsClass.getConstructor(String.class, String.class);
            Method addMethod = groupClass.getMethod("add", notifInterface);

            // Groupe enfant 1
            Object child1 = groupCtor.newInstance("Enfant 1");
            addMethod.invoke(child1, smsCtor.newInstance("+237690001111", "Msg 1.1"));
            addMethod.invoke(child1, smsCtor.newInstance("+237690001112", "Msg 1.2"));

            // Groupe enfant 2
            Object child2 = groupCtor.newInstance("Enfant 2");
            addMethod.invoke(child2, smsCtor.newInstance("+237690002221", "Msg 2.1"));
            addMethod.invoke(child2, smsCtor.newInstance("+237690002222", "Msg 2.2"));
            addMethod.invoke(child2, smsCtor.newInstance("+237690002223", "Msg 2.3"));

            // Groupe parent
            Object parent = groupCtor.newInstance("Parent");
            addMethod.invoke(parent, child1);
            addMethod.invoke(parent, child2);
            addMethod.invoke(parent, smsCtor.newInstance("+237690000000", "Msg parent"));

            // Verifier le comptage recursif
            Method getCount = groupClass.getMethod("getNotificationCount");
            int totalCount = (Integer) getCount.invoke(parent);
            assertEquals("Total recursif = 6 (2+3+1)", 6, totalCount);

            Method getDirectCount = groupClass.getMethod("getDirectChildCount");
            int directCount = (Integer) getDirectCount.invoke(parent);
            assertEquals("Direct = 3 (2 groupes + 1 sms)", 3, directCount);

            // Envoyer
            Method send = groupClass.getMethod("send");
            Object result = send.invoke(parent);

            Method getTotalSent = result.getClass().getMethod("getTotalSent");
            assertEquals("6 notifications envoyees au total", 6, (Integer) getTotalSent.invoke(result));

            System.out.println("    [OK] Groupes imbriques fonctionnent correctement\n");
        } catch (Exception e) {
            failTest("Groupes imbriques", e);
        }
    }

    // ==================== TEST 7: Notification Count ====================
    static void testNotificationCount() {
        printTestHeader("Test 7: getNotificationCount()");

        try {
            Class<?> groupClass = Class.forName("com.banque.composite.NotificationGroup");
            Class<?> notifInterface = Class.forName("com.banque.composite.Notification");
            Class<?> smsClass = Class.forName("com.banque.composite.SMSNotification");
            Class<?> emailClass = Class.forName("com.banque.composite.EmailNotification");
            Class<?> pushClass = Class.forName("com.banque.composite.PushNotification");

            Constructor<?> groupCtor = groupClass.getConstructor(String.class);
            Method addMethod = groupClass.getMethod("add", notifInterface);
            Method getCount = groupClass.getMethod("getNotificationCount");

            // Test groupe vide
            Object emptyGroup = groupCtor.newInstance("Vide");
            assertEquals("Groupe vide = 0", 0, (Integer) getCount.invoke(emptyGroup));

            // Test avec differents types
            Object mixedGroup = groupCtor.newInstance("Mixed");
            Constructor<?> smsCtor = smsClass.getConstructor(String.class, String.class);
            Constructor<?> emailCtor = emailClass.getConstructor(String.class, String.class, String.class);
            Constructor<?> pushCtor = pushClass.getConstructor(String.class, String.class, String.class);

            addMethod.invoke(mixedGroup, smsCtor.newInstance("+237690111111", "SMS"));
            assertEquals("Apres 1 SMS = 1", 1, (Integer) getCount.invoke(mixedGroup));

            addMethod.invoke(mixedGroup, emailCtor.newInstance("a@b.com", "S", "B"));
            assertEquals("Apres 1 Email = 2", 2, (Integer) getCount.invoke(mixedGroup));

            addMethod.invoke(mixedGroup, pushCtor.newInstance("token", "T", "B"));
            assertEquals("Apres 1 Push = 3", 3, (Integer) getCount.invoke(mixedGroup));

            System.out.println("    [OK] getNotificationCount() compte correctement\n");
        } catch (Exception e) {
            failTest("getNotificationCount", e);
        }
    }

    // ==================== TEST 8: Stop on First Failure ====================
    static void testGroupStopOnFailure() {
        printTestHeader("Test 8: stopOnFirstFailure");

        try {
            Class<?> groupClass = Class.forName("com.banque.composite.NotificationGroup");
            Class<?> notifInterface = Class.forName("com.banque.composite.Notification");
            Class<?> emailClass = Class.forName("com.banque.composite.EmailNotification");

            // Constructeur avec stopOnFirstFailure
            Constructor<?> groupCtor = groupClass.getConstructor(String.class, boolean.class);
            Method addMethod = groupClass.getMethod("add", notifInterface);
            Constructor<?> emailCtor = emailClass.getConstructor(String.class, String.class, String.class);

            // Groupe avec arret sur echec
            Object stopGroup = groupCtor.newInstance("Stop Group", true);

            // Email valide
            addMethod.invoke(stopGroup, emailCtor.newInstance("valid@mail.com", "Sujet", "Corps"));
            // Email invalide (va echouer)
            addMethod.invoke(stopGroup, emailCtor.newInstance("invalid", "Sujet", "Corps"));
            // Autre email (ne devrait pas etre envoye)
            addMethod.invoke(stopGroup, emailCtor.newInstance("another@mail.com", "Sujet", "Corps"));

            Method send = groupClass.getMethod("send");
            Object result = send.invoke(stopGroup);

            Method getTotalSent = result.getClass().getMethod("getTotalSent");
            Method getTotalFailed = result.getClass().getMethod("getTotalFailed");

            int sent = (Integer) getTotalSent.invoke(result);
            int failed = (Integer) getTotalFailed.invoke(result);

            // Avec stopOnFirstFailure=true, devrait s'arreter apres le 2e (echec)
            assertTrue("Envois < 3 avec stopOnFirstFailure", sent + failed < 3);
            assertTrue("Au moins 1 echec", failed >= 1);

            System.out.println("    [OK] stopOnFirstFailure arrete apres premier echec\n");
        } catch (Exception e) {
            failTest("stopOnFirstFailure", e);
        }
    }

    // ==================== TEST 9: Count by Type ====================
    static void testCountByType() {
        printTestHeader("Test 9: countByType()");

        try {
            Class<?> groupClass = Class.forName("com.banque.composite.NotificationGroup");
            Class<?> notifInterface = Class.forName("com.banque.composite.Notification");
            Class<?> smsClass = Class.forName("com.banque.composite.SMSNotification");
            Class<?> emailClass = Class.forName("com.banque.composite.EmailNotification");
            Class<?> pushClass = Class.forName("com.banque.composite.PushNotification");

            Constructor<?> groupCtor = groupClass.getConstructor(String.class);
            Constructor<?> smsCtor = smsClass.getConstructor(String.class, String.class);
            Constructor<?> emailCtor = emailClass.getConstructor(String.class, String.class, String.class);
            Constructor<?> pushCtor = pushClass.getConstructor(String.class, String.class, String.class);
            Method addMethod = groupClass.getMethod("add", notifInterface);

            Object group = groupCtor.newInstance("Count Test");

            // Ajouter 3 SMS
            addMethod.invoke(group, smsCtor.newInstance("+237690001111", "SMS 1"));
            addMethod.invoke(group, smsCtor.newInstance("+237690001112", "SMS 2"));
            addMethod.invoke(group, smsCtor.newInstance("+237690001113", "SMS 3"));

            // Ajouter 2 Emails
            addMethod.invoke(group, emailCtor.newInstance("a@b.com", "S", "B"));
            addMethod.invoke(group, emailCtor.newInstance("c@d.com", "S", "B"));

            // Ajouter 1 Push
            addMethod.invoke(group, pushCtor.newInstance("token", "T", "B"));

            // countByType
            Method countByType = groupClass.getMethod("countByType");
            @SuppressWarnings("unchecked")
            Map<Object, Integer> counts = (Map<Object, Integer>) countByType.invoke(group);

            // Verifier les comptages
            int smsCount = 0, emailCount = 0, pushCount = 0;
            for (Map.Entry<Object, Integer> entry : counts.entrySet()) {
                String typeName = entry.getKey().toString();
                if (typeName.contains("SMS")) smsCount = entry.getValue();
                if (typeName.contains("EMAIL")) emailCount = entry.getValue();
                if (typeName.contains("PUSH")) pushCount = entry.getValue();
            }

            assertEquals("3 SMS", 3, smsCount);
            assertEquals("2 Emails", 2, emailCount);
            assertEquals("1 Push", 1, pushCount);

            System.out.println("    [OK] countByType() compte par type correctement\n");
        } catch (Exception e) {
            failTest("countByType", e);
        }
    }

    // ==================== TEST 10: NotificationResult ====================
    static void testNotificationResult() {
        printTestHeader("Test 10: NotificationResult");

        try {
            Class<?> resultClass = Class.forName("com.banque.composite.Notification$NotificationResult");
            Class<?> typeClass = Class.forName("com.banque.composite.Notification$NotificationType");

            // Factory method success
            Method success = resultClass.getMethod("success", String.class, typeClass,
                String.class, String.class);

            Object smsType = null;
            for (Object constant : typeClass.getEnumConstants()) {
                if (constant.toString().equals("SMS")) {
                    smsType = constant;
                    break;
                }
            }

            Object successResult = success.invoke(null, "SMS-123", smsType,
                "+237690000000", "Envoye");

            Method isSuccess = resultClass.getMethod("isSuccess");
            assertTrue("success() retourne succes=true", (Boolean) isSuccess.invoke(successResult));

            Method getTotalSent = resultClass.getMethod("getTotalSent");
            assertEquals("totalSent=1 pour success", 1, (Integer) getTotalSent.invoke(successResult));

            // Factory method failure
            Method failure = resultClass.getMethod("failure", String.class, typeClass,
                String.class, String.class);
            Object failResult = failure.invoke(null, "SMS-456", smsType,
                "invalid", "Erreur");

            assertFalse("failure() retourne succes=false", (Boolean) isSuccess.invoke(failResult));

            Method getTotalFailed = resultClass.getMethod("getTotalFailed");
            assertEquals("totalFailed=1 pour failure", 1, (Integer) getTotalFailed.invoke(failResult));

            // Test successRate
            Method getSuccessRate = resultClass.getMethod("getSuccessRate");
            double rate = (Double) getSuccessRate.invoke(successResult);
            assertEquals("successRate=100 pour success", 100.0, rate, 0.01);

            System.out.println("    [OK] NotificationResult fonctionne correctement\n");
        } catch (Exception e) {
            failTest("NotificationResult", e);
        }
    }

    // ==================== TEST 11: NotificationTemplate ====================
    static void testNotificationTemplate() {
        printTestHeader("Test 11: NotificationTemplate");

        try {
            Class<?> templateClass = Class.forName("com.banque.composite.NotificationTemplate");
            Class<?> smsClass = Class.forName("com.banque.composite.SMSNotification");
            Class<?> emailClass = Class.forName("com.banque.composite.EmailNotification");

            // Utiliser un template pre-defini
            Method otpMethod = templateClass.getMethod("otpCode");
            Object otpTemplate = otpMethod.invoke(null);
            assertNotNull("Template OTP existe", otpTemplate);

            // Creer SMS depuis template
            Method createSMS = templateClass.getMethod("createSMS", String.class, Map.class);
            Map<String, String> vars = new HashMap<>();
            vars.put("code", "123456");
            vars.put("duree", "5");

            Object sms = createSMS.invoke(otpTemplate, "+237690123456", vars);
            assertNotNull("SMS cree depuis template", sms);

            Method getContent = smsClass.getMethod("getContent");
            String content = (String) getContent.invoke(sms);
            assertTrue("Message contient le code", content.contains("123456"));
            assertTrue("Message contient la duree", content.contains("5"));

            // Creer Email depuis template
            Method createEmail = templateClass.getMethod("createEmail", String.class, Map.class);
            vars.put("nom", "Kamga Jean");
            vars.put("operation", "Virement");

            Object email = createEmail.invoke(otpTemplate, "jean@mail.com", vars);
            assertNotNull("Email cree depuis template", email);

            Method getSubject = emailClass.getMethod("getSubject");
            String subject = (String) getSubject.invoke(email);
            assertTrue("Sujet contient verification", subject.toLowerCase().contains("verification"));

            System.out.println("    [OK] NotificationTemplate avec substitution de variables\n");
        } catch (Exception e) {
            failTest("NotificationTemplate", e);
        }
    }

    // ==================== TEST 12: Factory Methods ====================
    static void testMultiChannelFactory() {
        printTestHeader("Test 12: Factory Methods (multiChannel, smsBroadcast)");

        try {
            Class<?> groupClass = Class.forName("com.banque.composite.NotificationGroup");

            // Test multiChannel
            Method multiChannel = groupClass.getMethod("multiChannel",
                String.class, String.class, String.class, String.class, String.class);

            Object mcGroup = multiChannel.invoke(null, "Test Multi",
                "+237690123456", "test@mail.com", "device-token", "Message test");

            Method getCount = groupClass.getMethod("getNotificationCount");
            int mcCount = (Integer) getCount.invoke(mcGroup);
            assertEquals("MultiChannel = 3 notifications", 3, mcCount);

            // Test smsBroadcast
            Method smsBroadcast = groupClass.getMethod("smsBroadcast",
                String.class, List.class, String.class);

            List<String> phones = Arrays.asList("+237690001111", "+237690002222", "+237690003333");
            Object broadcastGroup = smsBroadcast.invoke(null, "Broadcast", phones, "Message diffusion");

            int broadcastCount = (Integer) getCount.invoke(broadcastGroup);
            assertEquals("Broadcast = 3 SMS", 3, broadcastCount);

            // Envoyer et verifier
            Method send = groupClass.getMethod("send");
            Object result = send.invoke(broadcastGroup);

            Method getTotalSent = result.getClass().getMethod("getTotalSent");
            assertEquals("3 SMS envoyes", 3, (Integer) getTotalSent.invoke(result));

            System.out.println("    [OK] Factory methods creent des groupes corrects\n");
        } catch (Exception e) {
            failTest("Factory Methods", e);
        }
    }

    // ==================== METHODES UTILITAIRES ====================

    static void printTestHeader(String testName) {
        System.out.println(">> " + testName);
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
            failedTestNames.add(message + " (attendu: " + expected + ", obtenu: " + actual + ")");
            System.out.println("    [FAIL] " + message + " (attendu: " + expected + ", obtenu: " + actual + ")");
        }
    }

    static void assertEquals(String message, double expected, double actual, double delta) {
        boolean equal = Math.abs(expected - actual) <= delta;
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

    static void assertNotNull(String message, Object obj) {
        assertTrue(message, obj != null);
    }

    static void failTest(String testName, Exception e) {
        totalTests++;
        failedTests++;
        failedTestNames.add(testName + ": " + e.getMessage());
        System.out.println("    [FAIL] " + testName + ": " + e.getMessage());
        e.printStackTrace();
    }

    static void printSummary() {
        System.out.println("\n================================================================");
        System.out.println("                    RESUME DES TESTS");
        System.out.println("================================================================");
        System.out.println();
        System.out.println("  Total des tests:    " + totalTests);
        System.out.println("  Tests reussis:      " + passedTests);
        System.out.println("  Tests echoues:      " + failedTests);
        System.out.println();

        double percentage = (totalTests > 0) ? (passedTests * 100.0 / totalTests) : 0;
        System.out.printf("  Taux de reussite:   %.1f%%\n", percentage);
        System.out.println();

        if (failedTests == 0) {
            System.out.println("  *** TOUS LES TESTS SONT PASSES! ***");
            System.out.println();
            System.out.println("  PATTERN COMPOSITE IMPLEMENTE AVEC SUCCES:");
            System.out.println("  - Interface Notification (Component)");
            System.out.println("  - SMSNotification, EmailNotification, PushNotification (Leaf)");
            System.out.println("  - NotificationGroup (Composite)");
            System.out.println("  - Groupes imbriques supportes");
            System.out.println("  - Templates avec substitution de variables");
            System.out.println("  - Factory methods pour creation simplifiee");
        } else {
            System.out.println("  Tests echoues:");
            for (String name : failedTestNames) {
                System.out.println("    - " + name);
            }
        }

        System.out.println();
        System.out.println("================================================================");
        System.out.println("             FIN DES TESTS OBJECTIF 7");
        System.out.println("================================================================\n");
    }
}
