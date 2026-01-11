import java.math.BigDecimal;
import java.util.*;
import java.lang.reflect.*;

/**
 * ================================================================
 *         TESTS OBJECTIF 9 - PATTERN VISITOR
 *              Analytics et Rapports
 * ================================================================
 */
public class Objectif9Test {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static List<String> failedTestNames = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("\n================================================================");
        System.out.println("         TESTS OBJECTIF 9 - PATTERN VISITOR");
        System.out.println("              Analytics et Rapports");
        System.out.println("================================================================\n");

        testTransactionElementInterface();
        testTransactionVisitorInterface();
        testConcreteElements();
        testStatisticsVisitor();
        testTaxVisitor();
        testAuditVisitor();
        testMultipleVisitors();
        testVisitorOnCollection();

        printSummary();
    }

    static void testTransactionElementInterface() {
        printTestHeader("Test 1: Interface TransactionElement");
        try {
            Class<?> elementClass = Class.forName("com.banque.visitor.TransactionElement");
            assertTrue("TransactionElement est une interface", elementClass.isInterface());

            Method accept = elementClass.getMethod("accept",
                Class.forName("com.banque.visitor.TransactionVisitor"));
            assertNotNull("Methode accept(Visitor) existe", accept);

            Method getAmount = elementClass.getMethod("getAmount");
            assertNotNull("Methode getAmount() existe", getAmount);

            Method getElementType = elementClass.getMethod("getElementType");
            assertNotNull("Methode getElementType() existe", getElementType);

            System.out.println("    [OK] Interface Element bien definie\n");
        } catch (Exception e) {
            failTest("TransactionElement", e);
        }
    }

    static void testTransactionVisitorInterface() {
        printTestHeader("Test 2: Interface TransactionVisitor");
        try {
            Class<?> visitorClass = Class.forName("com.banque.visitor.TransactionVisitor");
            assertTrue("TransactionVisitor est une interface", visitorClass.isInterface());

            // Verifier les methodes visit()
            Class<?> depotClass = Class.forName("com.banque.visitor.DepotElement");
            Class<?> retraitClass = Class.forName("com.banque.visitor.RetraitElement");
            Class<?> transfertClass = Class.forName("com.banque.visitor.TransfertElement");

            assertNotNull("visit(DepotElement) existe",
                visitorClass.getMethod("visit", depotClass));
            assertNotNull("visit(RetraitElement) existe",
                visitorClass.getMethod("visit", retraitClass));
            assertNotNull("visit(TransfertElement) existe",
                visitorClass.getMethod("visit", transfertClass));

            System.out.println("    [OK] Interface Visitor bien definie\n");
        } catch (Exception e) {
            failTest("TransactionVisitor", e);
        }
    }

    static void testConcreteElements() {
        printTestHeader("Test 3: Elements Concrets");
        try {
            Class<?> elementInterface = Class.forName("com.banque.visitor.TransactionElement");

            // DepotElement
            Class<?> depotClass = Class.forName("com.banque.visitor.DepotElement");
            assertTrue("DepotElement implemente TransactionElement",
                elementInterface.isAssignableFrom(depotClass));

            Constructor<?> depotCtor = depotClass.getConstructor(
                String.class, BigDecimal.class, String.class);
            Object depot = depotCtor.newInstance("CM001", new BigDecimal("100000"), "Virement");

            Method getAmount = depotClass.getMethod("getAmount");
            assertEquals("Montant depot correct", new BigDecimal("100000"),
                (BigDecimal) getAmount.invoke(depot));

            // RetraitElement
            Class<?> retraitClass = Class.forName("com.banque.visitor.RetraitElement");
            assertTrue("RetraitElement implemente TransactionElement",
                elementInterface.isAssignableFrom(retraitClass));

            // TransfertElement
            Class<?> transfertClass = Class.forName("com.banque.visitor.TransfertElement");
            assertTrue("TransfertElement implemente TransactionElement",
                elementInterface.isAssignableFrom(transfertClass));

            // PaiementElement
            Class<?> paiementClass = Class.forName("com.banque.visitor.PaiementElement");
            assertTrue("PaiementElement implemente TransactionElement",
                elementInterface.isAssignableFrom(paiementClass));

            System.out.println("    [OK] Tous les elements concrets implementent l'interface\n");
        } catch (Exception e) {
            failTest("ConcreteElements", e);
        }
    }

    static void testStatisticsVisitor() {
        printTestHeader("Test 4: StatisticsVisitor");
        try {
            Class<?> statsClass = Class.forName("com.banque.visitor.StatisticsVisitor");
            Class<?> visitorInterface = Class.forName("com.banque.visitor.TransactionVisitor");

            assertTrue("StatisticsVisitor implemente TransactionVisitor",
                visitorInterface.isAssignableFrom(statsClass));

            Object stats = statsClass.getDeclaredConstructor().newInstance();

            // Creer des elements et les visiter
            Object depot = createDepot("CM001", new BigDecimal("100000"), "Salaire");
            Object retrait = createRetrait("CM001", new BigDecimal("30000"), "ATM");

            // Accepter le visiteur
            Class<?> depotClass = Class.forName("com.banque.visitor.DepotElement");
            Class<?> retraitClass = Class.forName("com.banque.visitor.RetraitElement");

            depotClass.getMethod("accept", visitorInterface).invoke(depot, stats);
            retraitClass.getMethod("accept", visitorInterface).invoke(retrait, stats);

            // Verifier les resultats
            Method getTotalTx = statsClass.getMethod("getTotalTransactions");
            assertEquals("2 transactions analysees", 2, ((Integer) getTotalTx.invoke(stats)).intValue());

            Method getTotalCredits = statsClass.getMethod("getTotalCredits");
            BigDecimal credits = (BigDecimal) getTotalCredits.invoke(stats);
            assertEquals("Credits = 100000", new BigDecimal("100000"), credits);

            Method getTotalDebits = statsClass.getMethod("getTotalDebits");
            BigDecimal debits = (BigDecimal) getTotalDebits.invoke(stats);
            assertEquals("Debits = 30000", new BigDecimal("30000"), debits);

            System.out.println("    [OK] StatisticsVisitor calcule correctement\n");
        } catch (Exception e) {
            failTest("StatisticsVisitor", e);
        }
    }

    static void testTaxVisitor() {
        printTestHeader("Test 5: TaxVisitor");
        try {
            Class<?> taxClass = Class.forName("com.banque.visitor.TaxVisitor");
            Class<?> visitorInterface = Class.forName("com.banque.visitor.TransactionVisitor");

            assertTrue("TaxVisitor implemente TransactionVisitor",
                visitorInterface.isAssignableFrom(taxClass));

            Object tax = taxClass.getDeclaredConstructor().newInstance();

            // Creer un depot salarial (taxable)
            Object depot = createDepot("CM001", new BigDecimal("500000"), "Salaire mensuel");

            Class<?> depotClass = Class.forName("com.banque.visitor.DepotElement");
            depotClass.getMethod("accept", visitorInterface).invoke(depot, tax);

            Method getTaxableIncome = taxClass.getMethod("getTaxableIncome");
            BigDecimal income = (BigDecimal) getTaxableIncome.invoke(tax);
            assertTrue("Revenu taxable > 0", income.compareTo(BigDecimal.ZERO) > 0);

            Method getVisitorName = taxClass.getMethod("getVisitorName");
            assertEquals("Nom du visiteur", "TaxVisitor", getVisitorName.invoke(tax));

            System.out.println("    [OK] TaxVisitor calcule les impots\n");
        } catch (Exception e) {
            failTest("TaxVisitor", e);
        }
    }

    static void testAuditVisitor() {
        printTestHeader("Test 6: AuditVisitor");
        try {
            Class<?> auditClass = Class.forName("com.banque.visitor.AuditVisitor");
            Class<?> visitorInterface = Class.forName("com.banque.visitor.TransactionVisitor");

            assertTrue("AuditVisitor implemente TransactionVisitor",
                visitorInterface.isAssignableFrom(auditClass));

            Object audit = auditClass.getDeclaredConstructor().newInstance();

            // Creer un retrait important (devrait generer une alerte)
            Object retrait = createRetrait("CM001", new BigDecimal("2000000"), "Guichet");

            Class<?> retraitClass = Class.forName("com.banque.visitor.RetraitElement");
            retraitClass.getMethod("accept", visitorInterface).invoke(retrait, audit);

            Method getTotalEntries = auditClass.getMethod("getTotalEntries");
            assertEquals("1 entree d'audit", 1, ((Integer) getTotalEntries.invoke(audit)).intValue());

            Method getAlertCount = auditClass.getMethod("getAlertCount");
            int alerts = (Integer) getAlertCount.invoke(audit);
            assertTrue("Alerte generee pour montant eleve", alerts >= 1);

            System.out.println("    [OK] AuditVisitor genere les logs et alertes\n");
        } catch (Exception e) {
            failTest("AuditVisitor", e);
        }
    }

    static void testMultipleVisitors() {
        printTestHeader("Test 7: Plusieurs Visiteurs");
        try {
            Class<?> visitorInterface = Class.forName("com.banque.visitor.TransactionVisitor");
            Class<?> depotClass = Class.forName("com.banque.visitor.DepotElement");

            // Creer les visiteurs
            Object stats = Class.forName("com.banque.visitor.StatisticsVisitor")
                .getDeclaredConstructor().newInstance();
            Object tax = Class.forName("com.banque.visitor.TaxVisitor")
                .getDeclaredConstructor().newInstance();
            Object audit = Class.forName("com.banque.visitor.AuditVisitor")
                .getDeclaredConstructor().newInstance();

            // Creer un element
            Object depot = createDepot("CM001", new BigDecimal("100000"), "Test");

            // Accepter tous les visiteurs
            Method accept = depotClass.getMethod("accept", visitorInterface);
            accept.invoke(depot, stats);
            accept.invoke(depot, tax);
            accept.invoke(depot, audit);

            // Verifier que chaque visiteur a traite l'element
            Method getTotal = stats.getClass().getMethod("getTotalTransactions");
            assertEquals("Stats: 1 transaction", 1, ((Integer) getTotal.invoke(stats)).intValue());

            Method getEntries = audit.getClass().getMethod("getTotalEntries");
            assertEquals("Audit: 1 entree", 1, ((Integer) getEntries.invoke(audit)).intValue());

            System.out.println("    [OK] Plusieurs visiteurs peuvent traiter le meme element\n");
        } catch (Exception e) {
            failTest("Multiple Visitors", e);
        }
    }

    static void testVisitorOnCollection() {
        printTestHeader("Test 8: Visiteur sur Collection");
        try {
            Class<?> visitorInterface = Class.forName("com.banque.visitor.TransactionVisitor");
            Class<?> elementInterface = Class.forName("com.banque.visitor.TransactionElement");

            // Creer une liste d'elements
            List<Object> elements = new ArrayList<>();
            elements.add(createDepot("CM001", new BigDecimal("100000"), "Depot1"));
            elements.add(createDepot("CM001", new BigDecimal("50000"), "Depot2"));
            elements.add(createRetrait("CM001", new BigDecimal("30000"), "ATM"));
            elements.add(createRetrait("CM001", new BigDecimal("20000"), "Guichet"));

            // Creer un visiteur
            Object stats = Class.forName("com.banque.visitor.StatisticsVisitor")
                .getDeclaredConstructor().newInstance();

            // Visiter tous les elements
            Method accept = elementInterface.getMethod("accept", visitorInterface);
            for (Object element : elements) {
                accept.invoke(element, stats);
            }

            // Verifier les resultats
            Method getTotal = stats.getClass().getMethod("getTotalTransactions");
            assertEquals("4 transactions traitees", 4, ((Integer) getTotal.invoke(stats)).intValue());

            Method getCredits = stats.getClass().getMethod("getTotalCredits");
            assertEquals("Credits = 150000", new BigDecimal("150000"),
                (BigDecimal) getCredits.invoke(stats));

            Method getDebits = stats.getClass().getMethod("getTotalDebits");
            assertEquals("Debits = 50000", new BigDecimal("50000"),
                (BigDecimal) getDebits.invoke(stats));

            System.out.println("    [OK] Visiteur traite une collection d'elements\n");
        } catch (Exception e) {
            failTest("Visitor on Collection", e);
        }
    }

    // ==================== HELPERS ====================

    static Object createDepot(String account, BigDecimal amount, String source) throws Exception {
        Class<?> depotClass = Class.forName("com.banque.visitor.DepotElement");
        Constructor<?> ctor = depotClass.getConstructor(String.class, BigDecimal.class, String.class);
        return ctor.newInstance(account, amount, source);
    }

    static Object createRetrait(String account, BigDecimal amount, String channel) throws Exception {
        Class<?> retraitClass = Class.forName("com.banque.visitor.RetraitElement");
        Constructor<?> ctor = retraitClass.getConstructor(String.class, BigDecimal.class, String.class);
        return ctor.newInstance(account, amount, channel);
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
            System.out.println("\n  *** PATTERN VISITOR IMPLEMENTE AVEC SUCCES! ***");
        } else {
            System.out.println("\n  Echecs: " + failedTestNames);
        }
        System.out.println("\n================================================================\n");
    }
}
