import java.math.BigDecimal;
import java.util.*;
import java.lang.reflect.*;

/**
 * ================================================================
 *         TESTS OBJECTIF 8 - PATTERN DECORATOR
 *           Comptes avec Fonctionnalites Dynamiques
 * ================================================================
 *
 * Ce fichier teste l'implementation du pattern Decorator pour
 * les comptes bancaires avec fonctionnalites additionnelles.
 *
 * Compilation et execution:
 *   cd standalone-test
 *   javac -encoding UTF-8 Objectif8Test.java
 *   java Objectif8Test
 */
public class Objectif8Test {

    // Compteurs de tests
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static List<String> failedTestNames = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("\n================================================================");
        System.out.println("         TESTS OBJECTIF 8 - PATTERN DECORATOR");
        System.out.println("           Comptes avec Fonctionnalites Dynamiques");
        System.out.println("================================================================\n");

        // Execution des tests
        testAccountInterface();
        testBasicAccount();
        testAccountDecorator();
        testInterestDecorator();
        testOverdraftDecorator();
        testInsuranceDecorator();
        testLoyaltyDecorator();
        testFeeDecorator();
        testNotificationDecorator();
        testMultipleDecorators();
        testDecoratorChaining();
        testDecoratorMethods();

        // Resume final
        printSummary();
    }

    // ==================== TEST 1: Interface Account ====================
    static void testAccountInterface() {
        printTestHeader("Test 1: Interface Account (Component)");

        try {
            Class<?> accountClass = Class.forName("com.banque.decorator.Account");

            assertTrue("Account est une interface", accountClass.isInterface());

            // Verifier les methodes requises
            Method getAccountNumber = accountClass.getMethod("getAccountNumber");
            assertNotNull("Methode getAccountNumber() existe", getAccountNumber);

            Method getBalance = accountClass.getMethod("getBalance");
            assertNotNull("Methode getBalance() existe", getBalance);

            Method deposit = accountClass.getMethod("deposit", BigDecimal.class);
            assertNotNull("Methode deposit(BigDecimal) existe", deposit);

            Method withdraw = accountClass.getMethod("withdraw", BigDecimal.class);
            assertNotNull("Methode withdraw(BigDecimal) existe", withdraw);

            Method getDescription = accountClass.getMethod("getDescription");
            assertNotNull("Methode getDescription() existe", getDescription);

            Method getMonthlyFees = accountClass.getMethod("getMonthlyFees");
            assertNotNull("Methode getMonthlyFees() existe", getMonthlyFees);

            Method getMonthlyBonus = accountClass.getMethod("getMonthlyBonus");
            assertNotNull("Methode getMonthlyBonus() existe", getMonthlyBonus);

            System.out.println("    [OK] Interface Account bien definie comme Component\n");
        } catch (Exception e) {
            failTest("Interface Account", e);
        }
    }

    // ==================== TEST 2: BasicAccount (ConcreteComponent) ====================
    static void testBasicAccount() {
        printTestHeader("Test 2: BasicAccount (ConcreteComponent)");

        try {
            Class<?> basicClass = Class.forName("com.banque.decorator.BasicAccount");
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");
            Class<?> accountTypeClass = Class.forName("com.banque.decorator.Account$AccountType");

            assertTrue("BasicAccount implemente Account",
                accountInterface.isAssignableFrom(basicClass));

            // Creer un compte de base
            Object courantType = getEnumConstant(accountTypeClass, "COURANT");
            Constructor<?> ctor = basicClass.getConstructor(String.class, String.class,
                accountTypeClass, BigDecimal.class);
            Object account = ctor.newInstance("CM001", "Jean Kamga", courantType,
                new BigDecimal("100000"));

            // Verifier les methodes
            Method getBalance = basicClass.getMethod("getBalance");
            BigDecimal balance = (BigDecimal) getBalance.invoke(account);
            assertEquals("Solde initial correct", new BigDecimal("100000"), balance);

            Method getMonthlyFees = basicClass.getMethod("getMonthlyFees");
            BigDecimal fees = (BigDecimal) getMonthlyFees.invoke(account);
            assertEquals("Pas de frais pour compte de base", BigDecimal.ZERO, fees);

            Method getMonthlyBonus = basicClass.getMethod("getMonthlyBonus");
            BigDecimal bonus = (BigDecimal) getMonthlyBonus.invoke(account);
            assertEquals("Pas de bonus pour compte de base", BigDecimal.ZERO, bonus);

            // Test depot
            Method deposit = basicClass.getMethod("deposit", BigDecimal.class);
            Object result = deposit.invoke(account, new BigDecimal("50000"));

            Method isSuccess = result.getClass().getMethod("isSuccess");
            assertTrue("Depot reussi", (Boolean) isSuccess.invoke(result));

            balance = (BigDecimal) getBalance.invoke(account);
            assertEquals("Nouveau solde apres depot", new BigDecimal("150000"), balance);

            System.out.println("    [OK] BasicAccount fonctionne comme ConcreteComponent\n");
        } catch (Exception e) {
            failTest("BasicAccount", e);
        }
    }

    // ==================== TEST 3: AccountDecorator (Abstract Decorator) ====================
    static void testAccountDecorator() {
        printTestHeader("Test 3: AccountDecorator (Abstract Decorator)");

        try {
            Class<?> decoratorClass = Class.forName("com.banque.decorator.AccountDecorator");
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");

            assertTrue("AccountDecorator implemente Account",
                accountInterface.isAssignableFrom(decoratorClass));

            assertTrue("AccountDecorator est abstrait",
                Modifier.isAbstract(decoratorClass.getModifiers()));

            // Verifier le constructeur avec Account
            Constructor<?>[] ctors = decoratorClass.getConstructors();
            boolean hasAccountCtor = false;
            for (Constructor<?> ctor : ctors) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length == 1 && accountInterface.isAssignableFrom(params[0])) {
                    hasAccountCtor = true;
                    break;
                }
            }
            assertTrue("Constructeur avec Account existe", hasAccountCtor);

            // Verifier les methodes utilitaires
            Method getWrapped = decoratorClass.getMethod("getWrappedAccount");
            assertNotNull("Methode getWrappedAccount() existe", getWrapped);

            Method getBase = decoratorClass.getMethod("getBaseAccount");
            assertNotNull("Methode getBaseAccount() existe", getBase);

            Method getCount = decoratorClass.getMethod("getDecoratorCount");
            assertNotNull("Methode getDecoratorCount() existe", getCount);

            System.out.println("    [OK] AccountDecorator est un Decorator abstrait valide\n");
        } catch (Exception e) {
            failTest("AccountDecorator", e);
        }
    }

    // ==================== TEST 4: InterestDecorator ====================
    static void testInterestDecorator() {
        printTestHeader("Test 4: InterestDecorator (Interets)");

        try {
            Class<?> interestClass = Class.forName("com.banque.decorator.InterestDecorator");
            Class<?> decoratorClass = Class.forName("com.banque.decorator.AccountDecorator");
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");

            assertTrue("InterestDecorator etend AccountDecorator",
                decoratorClass.isAssignableFrom(interestClass));

            // Creer un compte de base
            Object account = createBasicAccount("CM-INT-001", "Test Interest", new BigDecimal("500000"));

            // Decorer avec interets
            Constructor<?> intCtor = interestClass.getConstructor(accountInterface, BigDecimal.class);
            Object decorated = intCtor.newInstance(account, new BigDecimal("6.0")); // 6% par an

            // Verifier le bonus mensuel
            Method getMonthlyBonus = interestClass.getMethod("getMonthlyBonus");
            BigDecimal bonus = (BigDecimal) getMonthlyBonus.invoke(decorated);

            // 500000 * 6% / 12 = 2500
            assertTrue("Bonus mensuel calcule (interets)", bonus.compareTo(BigDecimal.ZERO) > 0);

            // Verifier la description
            Method getDescription = interestClass.getMethod("getDescription");
            String desc = (String) getDescription.invoke(decorated);
            assertTrue("Description contient 'Interets'", desc.contains("Interets"));

            System.out.println("    [OK] InterestDecorator ajoute le calcul des interets\n");
        } catch (Exception e) {
            failTest("InterestDecorator", e);
        }
    }

    // ==================== TEST 5: OverdraftDecorator ====================
    static void testOverdraftDecorator() {
        printTestHeader("Test 5: OverdraftDecorator (Decouvert)");

        try {
            Class<?> overdraftClass = Class.forName("com.banque.decorator.OverdraftDecorator");
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");

            // Creer un compte de base avec solde faible
            Object account = createBasicAccount("CM-OVD-001", "Test Overdraft", new BigDecimal("50000"));

            // Decorer avec decouvert
            Constructor<?> ovdCtor = overdraftClass.getConstructor(accountInterface, BigDecimal.class);
            Object decorated = ovdCtor.newInstance(account, new BigDecimal("100000")); // 100k de decouvert

            // Tenter un retrait superieur au solde mais dans la limite du decouvert
            Method withdraw = overdraftClass.getMethod("withdraw", BigDecimal.class);
            Object result = withdraw.invoke(decorated, new BigDecimal("80000"));

            Method isSuccess = result.getClass().getMethod("isSuccess");
            assertTrue("Retrait avec decouvert reussi", (Boolean) isSuccess.invoke(result));

            // Verifier le solde negatif
            Method getBalance = overdraftClass.getMethod("getBalance");
            BigDecimal balance = (BigDecimal) getBalance.invoke(decorated);
            assertTrue("Solde est negatif (decouvert)", balance.compareTo(BigDecimal.ZERO) < 0);

            // Verifier isInOverdraft
            Method isInOverdraft = overdraftClass.getMethod("isInOverdraft");
            assertTrue("isInOverdraft() retourne true", (Boolean) isInOverdraft.invoke(decorated));

            System.out.println("    [OK] OverdraftDecorator permet le decouvert\n");
        } catch (Exception e) {
            failTest("OverdraftDecorator", e);
        }
    }

    // ==================== TEST 6: InsuranceDecorator ====================
    static void testInsuranceDecorator() {
        printTestHeader("Test 6: InsuranceDecorator (Assurance)");

        try {
            Class<?> insuranceClass = Class.forName("com.banque.decorator.InsuranceDecorator");
            Class<?> insuranceTypeClass = Class.forName("com.banque.decorator.InsuranceDecorator$InsuranceType");
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");

            Object account = createBasicAccount("CM-INS-001", "Test Insurance", new BigDecimal("100000"));

            // Decorer avec assurance STANDARD
            Object standardType = getEnumConstant(insuranceTypeClass, "STANDARD");
            Constructor<?> insCtor = insuranceClass.getConstructor(accountInterface, insuranceTypeClass);
            Object decorated = insCtor.newInstance(account, standardType);

            // Verifier les frais mensuels (prime d'assurance)
            Method getMonthlyFees = insuranceClass.getMethod("getMonthlyFees");
            BigDecimal fees = (BigDecimal) getMonthlyFees.invoke(decorated);
            assertTrue("Frais mensuels > 0 (prime assurance)", fees.compareTo(BigDecimal.ZERO) > 0);

            // Verifier isCovered
            Method isCovered = insuranceClass.getMethod("isCovered", String.class);
            assertTrue("Fraude est couverte", (Boolean) isCovered.invoke(decorated, "fraude"));
            assertTrue("Deces est couvert (STANDARD)", (Boolean) isCovered.invoke(decorated, "deces"));

            System.out.println("    [OK] InsuranceDecorator ajoute l'assurance\n");
        } catch (Exception e) {
            failTest("InsuranceDecorator", e);
        }
    }

    // ==================== TEST 7: LoyaltyDecorator ====================
    static void testLoyaltyDecorator() {
        printTestHeader("Test 7: LoyaltyDecorator (Fidelite)");

        try {
            Class<?> loyaltyClass = Class.forName("com.banque.decorator.LoyaltyDecorator");
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");

            Object account = createBasicAccount("CM-LOY-001", "Test Loyalty", new BigDecimal("100000"));

            // Decorer avec programme fidelite
            Constructor<?> loyCtor = loyaltyClass.getConstructor(accountInterface);
            Object decorated = loyCtor.newInstance(account);

            // Verifier les points initiaux
            Method getPoints = loyaltyClass.getMethod("getLoyaltyPoints");
            int initialPoints = (Integer) getPoints.invoke(decorated);
            assertEquals("Points initiaux = 0", 0, initialPoints);

            // Effectuer un depot pour gagner des points
            Method deposit = loyaltyClass.getMethod("deposit", BigDecimal.class);
            deposit.invoke(decorated, new BigDecimal("100000"));

            int pointsAfter = (Integer) getPoints.invoke(decorated);
            assertTrue("Points gagnes apres depot", pointsAfter > initialPoints);

            // Verifier le niveau
            Method getTier = loyaltyClass.getMethod("getCurrentTier");
            Object tier = getTier.invoke(decorated);
            assertNotNull("Niveau de fidelite existe", tier);

            System.out.println("    [OK] LoyaltyDecorator ajoute la fidelite\n");
        } catch (Exception e) {
            failTest("LoyaltyDecorator", e);
        }
    }

    // ==================== TEST 8: FeeDecorator ====================
    static void testFeeDecorator() {
        printTestHeader("Test 8: FeeDecorator (Frais)");

        try {
            Class<?> feeClass = Class.forName("com.banque.decorator.FeeDecorator");
            Class<?> feeTypeClass = Class.forName("com.banque.decorator.FeeDecorator$FeeType");
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");

            Object account = createBasicAccount("CM-FEE-001", "Test Fees", new BigDecimal("100000"));

            // Decorer avec frais STANDARD
            Object standardType = getEnumConstant(feeTypeClass, "STANDARD");
            Constructor<?> feeCtor = feeClass.getConstructor(accountInterface, feeTypeClass);
            Object decorated = feeCtor.newInstance(account, standardType);

            // Verifier les frais mensuels
            Method getMonthlyFees = feeClass.getMethod("getMonthlyFees");
            BigDecimal fees = (BigDecimal) getMonthlyFees.invoke(decorated);
            assertTrue("Frais mensuels > 0", fees.compareTo(BigDecimal.ZERO) > 0);

            // Calculer les frais de retrait
            Method calcFees = feeClass.getMethod("calculateWithdrawalFees", BigDecimal.class);
            BigDecimal withdrawalFees = (BigDecimal) calcFees.invoke(decorated, new BigDecimal("50000"));
            assertTrue("Frais de retrait calcules", withdrawalFees.compareTo(BigDecimal.ZERO) > 0);

            System.out.println("    [OK] FeeDecorator ajoute les frais\n");
        } catch (Exception e) {
            failTest("FeeDecorator", e);
        }
    }

    // ==================== TEST 9: NotificationDecorator ====================
    static void testNotificationDecorator() {
        printTestHeader("Test 9: NotificationDecorator (Alertes)");

        try {
            Class<?> notifClass = Class.forName("com.banque.decorator.NotificationDecorator");
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");

            Object account = createBasicAccount("CM-NOT-001", "Test Notif", new BigDecimal("100000"));

            // Decorer avec notifications
            Constructor<?> notifCtor = notifClass.getConstructor(accountInterface, String.class);
            Object decorated = notifCtor.newInstance(account, "+237690123456");

            // Verifier les parametres
            Method isSmsEnabled = notifClass.getMethod("isSmsEnabled");
            assertTrue("SMS active", (Boolean) isSmsEnabled.invoke(decorated));

            // Effectuer un depot (devrait envoyer une notification)
            Method deposit = notifClass.getMethod("deposit", BigDecimal.class);
            deposit.invoke(decorated, new BigDecimal("25000"));

            // Verifier le compteur de notifications
            Method getCount = notifClass.getMethod("getNotificationsSent");
            int sent = (Integer) getCount.invoke(decorated);
            assertTrue("Au moins 1 notification envoyee", sent >= 1);

            System.out.println("    [OK] NotificationDecorator envoie des alertes\n");
        } catch (Exception e) {
            failTest("NotificationDecorator", e);
        }
    }

    // ==================== TEST 10: Multiple Decorators ====================
    static void testMultipleDecorators() {
        printTestHeader("Test 10: Plusieurs Decorateurs Combines");

        try {
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");
            Class<?> interestClass = Class.forName("com.banque.decorator.InterestDecorator");
            Class<?> overdraftClass = Class.forName("com.banque.decorator.OverdraftDecorator");
            Class<?> loyaltyClass = Class.forName("com.banque.decorator.LoyaltyDecorator");
            Class<?> decoratorClass = Class.forName("com.banque.decorator.AccountDecorator");

            // Creer un compte de base
            Object account = createBasicAccount("CM-MULTI-001", "Test Multi", new BigDecimal("200000"));

            // Ajouter InterestDecorator
            Constructor<?> intCtor = interestClass.getConstructor(accountInterface, BigDecimal.class);
            Object step1 = intCtor.newInstance(account, new BigDecimal("4.0"));

            // Ajouter OverdraftDecorator
            Constructor<?> ovdCtor = overdraftClass.getConstructor(accountInterface, BigDecimal.class);
            Object step2 = ovdCtor.newInstance(step1, new BigDecimal("150000"));

            // Ajouter LoyaltyDecorator
            Constructor<?> loyCtor = loyaltyClass.getConstructor(accountInterface);
            Object finalAccount = loyCtor.newInstance(step2);

            // Verifier le nombre de decorateurs
            Method getDecoratorCount = decoratorClass.getMethod("getDecoratorCount");
            int count = (Integer) getDecoratorCount.invoke(finalAccount);
            assertEquals("3 decorateurs appliques", 3, count);

            // Verifier que toutes les fonctionnalites sont presentes
            Method hasDecorator = decoratorClass.getMethod("hasDecorator", Class.class);
            assertTrue("A InterestDecorator", (Boolean) hasDecorator.invoke(finalAccount, interestClass));
            assertTrue("A OverdraftDecorator", (Boolean) hasDecorator.invoke(finalAccount, overdraftClass));
            assertTrue("A LoyaltyDecorator", (Boolean) hasDecorator.invoke(finalAccount, loyaltyClass));

            // Verifier que les bonus/frais se cumulent
            Method getMonthlyBonus = accountInterface.getMethod("getMonthlyBonus");
            BigDecimal bonus = (BigDecimal) getMonthlyBonus.invoke(finalAccount);
            assertTrue("Bonus cumules > 0", bonus.compareTo(BigDecimal.ZERO) > 0);

            System.out.println("    [OK] Decorateurs multiples fonctionnent ensemble\n");
        } catch (Exception e) {
            failTest("Multiple Decorators", e);
        }
    }

    // ==================== TEST 11: Decorator Chaining ====================
    static void testDecoratorChaining() {
        printTestHeader("Test 11: Chaine de Decorateurs");

        try {
            Class<?> decoratorClass = Class.forName("com.banque.decorator.AccountDecorator");
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");
            Class<?> interestClass = Class.forName("com.banque.decorator.InterestDecorator");

            // Creer un compte avec decorateur
            Object account = createBasicAccount("CM-CHAIN-001", "Test Chain", new BigDecimal("100000"));
            Constructor<?> intCtor = interestClass.getConstructor(accountInterface, BigDecimal.class);
            Object decorated = intCtor.newInstance(account, new BigDecimal("3.0"));

            // Verifier getWrappedAccount
            Method getWrapped = decoratorClass.getMethod("getWrappedAccount");
            Object wrapped = getWrapped.invoke(decorated);
            assertNotNull("getWrappedAccount retourne le compte enveloppe", wrapped);

            // Verifier getBaseAccount
            Method getBase = decoratorClass.getMethod("getBaseAccount");
            Object base = getBase.invoke(decorated);
            assertNotNull("getBaseAccount retourne le compte de base", base);

            // Le base doit etre le BasicAccount original
            assertEquals("Base est le compte original",
                "CM-CHAIN-001",
                accountInterface.getMethod("getAccountNumber").invoke(base));

            System.out.println("    [OK] Chaine de decorateurs navigable\n");
        } catch (Exception e) {
            failTest("Decorator Chaining", e);
        }
    }

    // ==================== TEST 12: Decorator Methods ====================
    static void testDecoratorMethods() {
        printTestHeader("Test 12: Methodes Specifiques des Decorateurs");

        try {
            Class<?> accountInterface = Class.forName("com.banque.decorator.Account");
            Class<?> interestClass = Class.forName("com.banque.decorator.InterestDecorator");
            Class<?> overdraftClass = Class.forName("com.banque.decorator.OverdraftDecorator");

            // Test InterestDecorator.projectInterest
            Object account1 = createBasicAccount("CM-SPEC-001", "Test Spec 1", new BigDecimal("1000000"));
            Constructor<?> intCtor = interestClass.getConstructor(accountInterface, BigDecimal.class);
            Object interestAccount = intCtor.newInstance(account1, new BigDecimal("12.0")); // 12% par an

            Method projectInterest = interestClass.getMethod("projectInterest", int.class);
            BigDecimal projected = (BigDecimal) projectInterest.invoke(interestAccount, 12);
            assertTrue("Projection interets 12 mois > 0", projected.compareTo(BigDecimal.ZERO) > 0);

            // Test OverdraftDecorator.getTotalAvailable
            Object account2 = createBasicAccount("CM-SPEC-002", "Test Spec 2", new BigDecimal("50000"));
            Constructor<?> ovdCtor = overdraftClass.getConstructor(accountInterface, BigDecimal.class);
            Object overdraftAccount = ovdCtor.newInstance(account2, new BigDecimal("100000"));

            Method getTotalAvailable = overdraftClass.getMethod("getTotalAvailable");
            BigDecimal available = (BigDecimal) getTotalAvailable.invoke(overdraftAccount);
            assertEquals("Total disponible = solde + decouvert",
                new BigDecimal("150000"), available);

            System.out.println("    [OK] Methodes specifiques des decorateurs fonctionnent\n");
        } catch (Exception e) {
            failTest("Decorator Methods", e);
        }
    }

    // ==================== METHODES UTILITAIRES ====================

    static Object createBasicAccount(String number, String holder, BigDecimal balance) throws Exception {
        Class<?> basicClass = Class.forName("com.banque.decorator.BasicAccount");
        Class<?> accountTypeClass = Class.forName("com.banque.decorator.Account$AccountType");

        Object courantType = getEnumConstant(accountTypeClass, "COURANT");
        Constructor<?> ctor = basicClass.getConstructor(String.class, String.class,
            accountTypeClass, BigDecimal.class);

        return ctor.newInstance(number, holder, courantType, balance);
    }

    static Object getEnumConstant(Class<?> enumClass, String name) {
        for (Object constant : enumClass.getEnumConstants()) {
            if (constant.toString().equals(name)) {
                return constant;
            }
        }
        return null;
    }

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

    static void assertEquals(String message, int expected, int actual) {
        assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
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
            System.out.println("  PATTERN DECORATOR IMPLEMENTE AVEC SUCCES:");
            System.out.println("  - Interface Account (Component)");
            System.out.println("  - BasicAccount (ConcreteComponent)");
            System.out.println("  - AccountDecorator (Decorator abstrait)");
            System.out.println("  - InterestDecorator (Interets)");
            System.out.println("  - OverdraftDecorator (Decouvert)");
            System.out.println("  - InsuranceDecorator (Assurance)");
            System.out.println("  - LoyaltyDecorator (Fidelite)");
            System.out.println("  - FeeDecorator (Frais)");
            System.out.println("  - NotificationDecorator (Alertes)");
            System.out.println("  - Decorateurs combinables dynamiquement");
        } else {
            System.out.println("  Tests echoues:");
            for (String name : failedTestNames) {
                System.out.println("    - " + name);
            }
        }

        System.out.println();
        System.out.println("================================================================");
        System.out.println("             FIN DES TESTS OBJECTIF 8");
        System.out.println("================================================================\n");
    }
}
