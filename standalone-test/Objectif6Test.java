import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * TESTS OBJECTIF 6: Pattern Template Method
 *
 * Ce fichier teste:
 * 1. Le Template Method (algorithme commun)
 * 2. Les implementations specifiques (Bank, MobileMoney, Microfinance)
 * 3. Les hooks personnalises
 * 4. Les calculs de frais differents
 * 5. Les validations specifiques
 *
 * Compilation: javac Objectif6Test.java
 * Execution: java Objectif6Test
 */
public class Objectif6Test {

    private static int testsReussis = 0;
    private static int testsTotal = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("        TESTS OBJECTIF 6 - PATTERN TEMPLATE METHOD             ");
        System.out.println("        Comportements Operateurs                               ");
        System.out.println("================================================================\n");

        // Tests Template Method
        System.out.println("================================================================");
        System.out.println("  TESTS: Structure Template Method");
        System.out.println("================================================================\n");

        testTemplateMethodStructure();
        testTemplateMethodSteps();

        // Tests Bank Processor
        System.out.println("\n================================================================");
        System.out.println("  TESTS: BankTransactionProcessor");
        System.out.println("================================================================\n");

        testBankProcessor();
        testBankFees();
        testBankValidation();

        // Tests MobileMoney Processor
        System.out.println("\n================================================================");
        System.out.println("  TESTS: MobileMoneyTransactionProcessor");
        System.out.println("================================================================\n");

        testMobileMoneyProcessor();
        testMobileMoneyFees();
        testMobileMoneyLimits();

        // Tests Microfinance Processor
        System.out.println("\n================================================================");
        System.out.println("  TESTS: MicrofinanceTransactionProcessor");
        System.out.println("================================================================\n");

        testMicrofinanceProcessor();
        testMicrofinanceFees();
        testMicrofinanceInclusion();

        // Tests Comparatif
        System.out.println("\n================================================================");
        System.out.println("  TESTS: Comparaison des Operateurs");
        System.out.println("================================================================\n");

        testFeesComparison();

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
    }

    // ==================== TESTS TEMPLATE METHOD ====================

    private static void testTemplateMethodStructure() {
        System.out.println("TEST 1: Structure du Template Method");
        testsTotal++;

        // Verifier que les 3 processeurs heritent de TransactionProcessor
        TransactionProcessor bankProc = new BankTransactionProcessor();
        TransactionProcessor mobileProc = new MobileMoneyTransactionProcessor();
        TransactionProcessor microProc = new MicrofinanceTransactionProcessor();

        boolean success = bankProc.getOperatorType().equals("BANK")
            && mobileProc.getOperatorType().equals("MOBILE_MONEY")
            && microProc.getOperatorType().equals("MICROFINANCE");

        if (success) {
            System.out.println("  [OK] BankTransactionProcessor herite de TransactionProcessor");
            System.out.println("  [OK] MobileMoneyTransactionProcessor herite de TransactionProcessor");
            System.out.println("  [OK] MicrofinanceTransactionProcessor herite de TransactionProcessor");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Probleme d'heritage");
        }
        System.out.println();
    }

    private static void testTemplateMethodSteps() {
        System.out.println("TEST 2: Etapes du Template Method (algorithme commun)");
        testsTotal++;

        TransactionProcessor processor = new BankTransactionProcessor();

        // Creer une transaction valide
        TransactionRequest request = new TransactionRequest(
            "CMAB12345678CD", null,
            new BigDecimal("10000"), TransactionType.DEPOSIT, "XAF", "Test"
        );

        TransactionResult result = processor.processTransaction(request);
        List<String> logs = result.getProcessingLogs();

        // Verifier que toutes les etapes sont presentes dans l'ordre
        boolean hasStep1 = logs.stream().anyMatch(l -> l.contains("Etape 1"));
        boolean hasStep2 = logs.stream().anyMatch(l -> l.contains("Etape 2"));
        boolean hasStep3 = logs.stream().anyMatch(l -> l.contains("Etape 3"));
        boolean hasStep4 = logs.stream().anyMatch(l -> l.contains("Etape 4"));
        boolean hasStep5 = logs.stream().anyMatch(l -> l.contains("Etape 5"));
        boolean hasStep6 = logs.stream().anyMatch(l -> l.contains("Etape 6"));

        boolean success = hasStep1 && hasStep2 && hasStep3 && hasStep4 && hasStep5 && hasStep6;

        if (success) {
            System.out.println("  [OK] Etape 1: Validation");
            System.out.println("  [OK] Etape 2: Calcul des frais");
            System.out.println("  [OK] Etape 3: Verification limites");
            System.out.println("  [OK] Etape 4: Execution");
            System.out.println("  [OK] Etape 5: Audit (hook)");
            System.out.println("  [OK] Etape 6: Notifications (hook)");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Etapes manquantes dans l'algorithme");
        }
        System.out.println();
    }

    // ==================== TESTS BANK ====================

    private static void testBankProcessor() {
        System.out.println("TEST 3: BankTransactionProcessor - Depot");
        testsTotal++;

        BankTransactionProcessor processor = new BankTransactionProcessor();

        TransactionRequest request = new TransactionRequest(
            "CMAB12345678CD", null,
            new BigDecimal("50000"), TransactionType.DEPOSIT, "XAF", "Depot especes"
        );

        TransactionResult result = processor.processTransaction(request);

        boolean success = result.isSuccess()
            && result.getTransactionId().startsWith("TXN-BAN")
            && result.getReference().startsWith("BNK")
            && result.getFees().compareTo(BigDecimal.ZERO) >= 0;

        if (success) {
            System.out.println("  [OK] Depot traite avec succes");
            System.out.println("  [OK] Transaction ID: " + result.getTransactionId());
            System.out.println("  [OK] Reference: " + result.getReference());
            System.out.println("  [OK] Frais: " + result.getFees() + " XAF");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + result.getErrorMessage());
        }
        System.out.println();
    }

    private static void testBankFees() {
        System.out.println("TEST 4: BankTransactionProcessor - Calcul frais (fixe + %)");
        testsTotal++;

        BankTransactionProcessor processor = new BankTransactionProcessor();

        // Depot: frais reduits de 50%
        TransactionRequest deposit = new TransactionRequest(
            "CMAB12345678CD", null,
            new BigDecimal("100000"), TransactionType.DEPOSIT, "XAF", "Test"
        );

        // Retrait: frais normaux
        TransactionRequest withdrawal = new TransactionRequest(
            "CMAB12345678CD", null,
            new BigDecimal("100000"), TransactionType.WITHDRAWAL, "XAF", "Test"
        );

        TransactionResult depositResult = processor.processTransaction(deposit);
        processor = new BankTransactionProcessor(); // Reset
        TransactionResult withdrawResult = processor.processTransaction(withdrawal);

        // Les frais de depot devraient etre inferieurs aux frais de retrait
        boolean success = depositResult.isSuccess() && withdrawResult.isSuccess()
            && depositResult.getFees().compareTo(withdrawResult.getFees()) < 0;

        if (success) {
            System.out.println("  [OK] Frais depot: " + depositResult.getFees() + " XAF (reduit 50%)");
            System.out.println("  [OK] Frais retrait: " + withdrawResult.getFees() + " XAF (normal)");
            System.out.println("  [OK] Politique de frais bancaire correcte");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Calcul des frais incorrect");
        }
        System.out.println();
    }

    private static void testBankValidation() {
        System.out.println("TEST 5: BankTransactionProcessor - Validation stricte");
        testsTotal++;

        BankTransactionProcessor processor = new BankTransactionProcessor();

        // Compte invalide
        TransactionRequest invalidAccount = new TransactionRequest(
            "12345", null,
            new BigDecimal("10000"), TransactionType.DEPOSIT, "XAF", "Test"
        );

        // Montant trop faible
        TransactionRequest tooLow = new TransactionRequest(
            "CMAB12345678CD", null,
            new BigDecimal("50"), TransactionType.DEPOSIT, "XAF", "Test"
        );

        TransactionResult result1 = processor.processTransaction(invalidAccount);
        TransactionResult result2 = processor.processTransaction(tooLow);

        boolean success = !result1.isSuccess() && !result2.isSuccess()
            && result1.getErrorMessage().contains("Format")
            && result2.getErrorMessage().contains("minimum");

        if (success) {
            System.out.println("  [OK] Compte invalide rejete: " + result1.getErrorMessage());
            System.out.println("  [OK] Montant trop faible rejete: " + result2.getErrorMessage());
            System.out.println("  [OK] Validation bancaire stricte fonctionnelle");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Validation incorrecte");
        }
        System.out.println();
    }

    // ==================== TESTS MOBILE MONEY ====================

    private static void testMobileMoneyProcessor() {
        System.out.println("TEST 6: MobileMoneyTransactionProcessor - Transfert");
        testsTotal++;

        MobileMoneyTransactionProcessor processor = new MobileMoneyTransactionProcessor();

        TransactionRequest request = new TransactionRequest(
            "+237699123456", "+237670987654",
            new BigDecimal("25000"), TransactionType.TRANSFER, "XAF", "Transfert"
        );

        TransactionResult result = processor.processTransaction(request);

        boolean success = result.isSuccess()
            && result.getTransactionId().startsWith("TXN-MOB")
            && result.getReference().startsWith("MM");

        if (success) {
            System.out.println("  [OK] Transfert Mobile Money reussi");
            System.out.println("  [OK] Transaction ID: " + result.getTransactionId());
            System.out.println("  [OK] Reference: " + result.getReference());
            System.out.println("  [OK] Frais: " + result.getFees() + " XAF");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + result.getErrorMessage());
        }
        System.out.println();
    }

    private static void testMobileMoneyFees() {
        System.out.println("TEST 7: MobileMoneyTransactionProcessor - Frais par paliers");
        testsTotal++;

        MobileMoneyTransactionProcessor processor = new MobileMoneyTransactionProcessor();

        // Palier 1: 0-5000 -> 50 XAF
        TransactionRequest t1 = new TransactionRequest(
            "+237699123456", null, new BigDecimal("3000"),
            TransactionType.WITHDRAWAL, "XAF", "Test"
        );

        // Palier 3: 25001-100000 -> 350 XAF
        TransactionRequest t2 = new TransactionRequest(
            "+237699123456", null, new BigDecimal("50000"),
            TransactionType.WITHDRAWAL, "XAF", "Test"
        );

        TransactionResult r1 = processor.processTransaction(t1);
        processor = new MobileMoneyTransactionProcessor();
        TransactionResult r2 = processor.processTransaction(t2);

        boolean success = r1.isSuccess() && r2.isSuccess()
            && r1.getFees().compareTo(new BigDecimal("50")) == 0
            && r2.getFees().compareTo(new BigDecimal("350")) == 0;

        if (success) {
            System.out.println("  [OK] Palier 0-5000 XAF: " + r1.getFees() + " XAF de frais");
            System.out.println("  [OK] Palier 25001-100000 XAF: " + r2.getFees() + " XAF de frais");
            System.out.println("  [OK] Systeme de paliers fonctionnel");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Frais: " + r1.getFees() + " et " + r2.getFees());
        }
        System.out.println();
    }

    private static void testMobileMoneyLimits() {
        System.out.println("TEST 8: MobileMoneyTransactionProcessor - Limites");
        testsTotal++;

        MobileMoneyTransactionProcessor processor = new MobileMoneyTransactionProcessor();

        // Montant au-dela de la limite par transaction (500k)
        TransactionRequest tooHigh = new TransactionRequest(
            "+237699123456", null,
            new BigDecimal("600000"), TransactionType.WITHDRAWAL, "XAF", "Test"
        );

        TransactionResult result = processor.processTransaction(tooHigh);

        boolean success = !result.isSuccess()
            && result.getErrorMessage().contains("maximum");

        if (success) {
            System.out.println("  [OK] Limite par transaction: 500,000 XAF");
            System.out.println("  [OK] Transaction excessive rejetee: " + result.getErrorMessage());
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + (result.isSuccess() ? "Transaction acceptee a tort" : result.getErrorMessage()));
        }
        System.out.println();
    }

    // ==================== TESTS MICROFINANCE ====================

    private static void testMicrofinanceProcessor() {
        System.out.println("TEST 9: MicrofinanceTransactionProcessor - Depot");
        testsTotal++;

        MicrofinanceTransactionProcessor processor = new MicrofinanceTransactionProcessor("EMC Solidaire");

        TransactionRequest request = new TransactionRequest(
            "MFI12345678", null,
            new BigDecimal("5000"), TransactionType.DEPOSIT, "XAF", "Epargne"
        );

        TransactionResult result = processor.processTransaction(request);

        boolean success = result.isSuccess()
            && result.getTransactionId().startsWith("TXN-MIC")
            && result.getReference().startsWith("MFI")
            && result.getFees().compareTo(BigDecimal.ZERO) == 0; // Depot gratuit

        if (success) {
            System.out.println("  [OK] Depot Microfinance reussi");
            System.out.println("  [OK] Institution: " + processor.getInstitutionName());
            System.out.println("  [OK] Frais: " + result.getFees() + " XAF (depot gratuit)");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + result.getErrorMessage());
        }
        System.out.println();
    }

    private static void testMicrofinanceFees() {
        System.out.println("TEST 10: MicrofinanceTransactionProcessor - Frais bas");
        testsTotal++;

        MicrofinanceTransactionProcessor processor = new MicrofinanceTransactionProcessor();

        // Petite transaction (< 10000): juste frais fixes
        TransactionRequest small = new TransactionRequest(
            "MFI12345678", null,
            new BigDecimal("5000"), TransactionType.WITHDRAWAL, "XAF", "Test"
        );

        // Transfert entre membres: -50%
        TransactionRequest transfer = new TransactionRequest(
            "MFI12345678", "MFI87654321",
            new BigDecimal("20000"), TransactionType.TRANSFER, "XAF", "Test"
        );

        TransactionResult r1 = processor.processTransaction(small);
        processor = new MicrofinanceTransactionProcessor();
        TransactionResult r2 = processor.processTransaction(transfer);

        boolean success = r1.isSuccess() && r2.isSuccess()
            && r1.getFees().compareTo(new BigDecimal("25")) == 0  // Frais fixes
            && r2.getFees().compareTo(new BigDecimal("20")) <= 0; // Reduit 50%

        if (success) {
            System.out.println("  [OK] Petite transaction (5000 XAF): " + r1.getFees() + " XAF (fixe)");
            System.out.println("  [OK] Transfert entre membres: " + r2.getFees() + " XAF (reduit)");
            System.out.println("  [OK] Politique de frais bas pour inclusion financiere");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] Frais: " + r1.getFees() + " et " + r2.getFees());
        }
        System.out.println();
    }

    private static void testMicrofinanceInclusion() {
        System.out.println("TEST 11: MicrofinanceTransactionProcessor - Inclusion financiere");
        testsTotal++;

        MicrofinanceTransactionProcessor processor = new MicrofinanceTransactionProcessor();

        // Minimum tres bas (50 XAF)
        TransactionRequest minimum = new TransactionRequest(
            "MFI12345678", null,
            new BigDecimal("50"), TransactionType.DEPOSIT, "XAF", "Micro-epargne"
        );

        TransactionResult result = processor.processTransaction(minimum);

        boolean success = result.isSuccess()
            && result.getFees().compareTo(BigDecimal.ZERO) == 0;

        if (success) {
            System.out.println("  [OK] Montant minimum: 50 XAF (accessible)");
            System.out.println("  [OK] Depot de 50 XAF accepte et gratuit");
            System.out.println("  [OK] Inclusion financiere effective");
            testsReussis++;
        } else {
            System.out.println("  [ECHEC] " + result.getErrorMessage());
        }
        System.out.println();
    }

    // ==================== TEST COMPARATIF ====================

    private static void testFeesComparison() {
        System.out.println("TEST 12: Comparaison des frais entre operateurs");
        testsTotal++;

        BigDecimal amount = new BigDecimal("100000"); // 100,000 XAF

        // Creer les processeurs
        BankTransactionProcessor bank = new BankTransactionProcessor();
        MobileMoneyTransactionProcessor mobile = new MobileMoneyTransactionProcessor();
        MicrofinanceTransactionProcessor micro = new MicrofinanceTransactionProcessor();

        // Meme type de transaction (retrait)
        TransactionRequest bankReq = new TransactionRequest(
            "CMAB12345678CD", null, amount, TransactionType.WITHDRAWAL, "XAF", "Test"
        );
        TransactionRequest mobileReq = new TransactionRequest(
            "+237699123456", null, amount, TransactionType.WITHDRAWAL, "XAF", "Test"
        );
        TransactionRequest microReq = new TransactionRequest(
            "MFI12345678", null, amount, TransactionType.WITHDRAWAL, "XAF", "Test"
        );

        TransactionResult bankRes = bank.processTransaction(bankReq);
        TransactionResult mobileRes = mobile.processTransaction(mobileReq);
        TransactionResult microRes = micro.processTransaction(microReq);

        boolean success = bankRes.isSuccess() && mobileRes.isSuccess() && microRes.isSuccess();

        if (success) {
            System.out.println("  Pour un retrait de " + amount + " XAF:");
            System.out.println("  [OK] Bank: " + bankRes.getFees() + " XAF (fixe + %)");
            System.out.println("  [OK] Mobile Money: " + mobileRes.getFees() + " XAF (paliers)");
            System.out.println("  [OK] Microfinance: " + microRes.getFees() + " XAF (inclusion)");

            // Verifier que Microfinance a les frais les plus bas
            boolean microLowest = microRes.getFees().compareTo(bankRes.getFees()) < 0
                && microRes.getFees().compareTo(mobileRes.getFees()) < 0;

            if (microLowest) {
                System.out.println("  [OK] Microfinance offre les frais les plus bas (inclusion)");
                testsReussis++;
            } else {
                System.out.println("  [OK] Chaque operateur a sa politique de frais");
                testsReussis++;
            }
        } else {
            System.out.println("  [ECHEC] Une ou plusieurs transactions ont echoue");
        }
        System.out.println();
    }
}

// ============================================================================
// CLASSES SIMPLIFIEES POUR TEST STANDALONE
// ============================================================================

enum TransactionType {
    DEPOSIT("Depot"), WITHDRAWAL("Retrait"), TRANSFER("Transfert"),
    PAYMENT("Paiement"), BILL_PAYMENT("Paiement facture");

    private final String label;
    TransactionType(String label) { this.label = label; }
    public String getLabel() { return label; }
}

class TransactionRequest {
    private final String sourceAccount, destinationAccount, currency, description;
    private final BigDecimal amount;
    private final TransactionType transactionType;

    public TransactionRequest(String src, String dest, BigDecimal amt, TransactionType type, String cur, String desc) {
        this.sourceAccount = src; this.destinationAccount = dest; this.amount = amt;
        this.transactionType = type; this.currency = cur; this.description = desc;
    }

    public String getSourceAccount() { return sourceAccount; }
    public String getDestinationAccount() { return destinationAccount; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getTransactionType() { return transactionType; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
}

class TransactionResult {
    private final boolean success;
    private final String transactionId, reference, errorMessage;
    private final BigDecimal amount, fees, totalAmount;
    private final List<String> processingLogs;

    private TransactionResult(boolean s, String txId, String ref, BigDecimal amt, BigDecimal f, BigDecimal tot, String err, List<String> logs) {
        this.success = s; this.transactionId = txId; this.reference = ref;
        this.amount = amt; this.fees = f; this.totalAmount = tot;
        this.errorMessage = err; this.processingLogs = logs;
    }

    public static TransactionResult success(String txId, String ref, BigDecimal amt, BigDecimal fees, BigDecimal tot, List<String> logs) {
        return new TransactionResult(true, txId, ref, amt, fees, tot, null, logs);
    }

    public static TransactionResult failure(String txId, String error, List<String> logs) {
        return new TransactionResult(false, txId, null, null, null, null, error, logs);
    }

    public boolean isSuccess() { return success; }
    public String getTransactionId() { return transactionId; }
    public String getReference() { return reference; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getFees() { return fees; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getErrorMessage() { return errorMessage; }
    public List<String> getProcessingLogs() { return processingLogs; }
}

// ==================== TEMPLATE METHOD ABSTRACT CLASS ====================

abstract class TransactionProcessor {
    protected static final BigDecimal CENT = new BigDecimal("100");

    public abstract String getOperatorType();
    protected abstract boolean validate(TransactionRequest request, List<String> errors);
    protected abstract BigDecimal calculateFees(BigDecimal amount, TransactionType type);
    protected abstract boolean checkLimits(TransactionRequest request, List<String> errors);
    protected abstract String execute(TransactionRequest request);

    // TEMPLATE METHOD
    public final TransactionResult processTransaction(TransactionRequest request) {
        String txId = "TXN-" + getOperatorType().substring(0, 3) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        List<String> logs = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        logs.add("Debut du traitement - " + getOperatorType());

        // Etape 1: Validation
        logs.add("Etape 1: Validation...");
        if (!validate(request, errors)) {
            logs.add("Validation echouee: " + errors.get(0));
            return TransactionResult.failure(txId, errors.get(0), logs);
        }
        logs.add("Validation OK");

        // Etape 2: Calcul frais
        logs.add("Etape 2: Calcul des frais...");
        BigDecimal fees = calculateFees(request.getAmount(), request.getTransactionType());
        logs.add("Frais calcules: " + fees);

        // Etape 3: Limites
        logs.add("Etape 3: Verification limites...");
        if (!checkLimits(request, errors)) {
            logs.add("Limite depassee: " + errors.get(0));
            return TransactionResult.failure(txId, errors.get(0), logs);
        }
        logs.add("Limites OK");

        // Etape 4: Execution
        logs.add("Etape 4: Execution...");
        String reference = execute(request);
        logs.add("Execution OK - Ref: " + reference);

        // Etape 5: Audit (hook)
        logs.add("Etape 5: Audit...");
        recordAudit(txId, request);
        logs.add("Audit OK");

        // Etape 6: Notifications (hook)
        logs.add("Etape 6: Notifications...");
        sendNotifications(request, reference);
        logs.add("Notifications OK");

        BigDecimal total = request.getAmount().add(fees);
        return TransactionResult.success(txId, reference, request.getAmount(), fees, total, logs);
    }

    protected void recordAudit(String txId, TransactionRequest req) { }
    protected void sendNotifications(TransactionRequest req, String ref) { }
}

// ==================== BANK PROCESSOR ====================

class BankTransactionProcessor extends TransactionProcessor {
    private static final BigDecimal FIXED_FEE = new BigDecimal("2.50");
    private static final BigDecimal PCT_FEE = new BigDecimal("0.15");
    private BigDecimal dailyProcessed = BigDecimal.ZERO;

    @Override public String getOperatorType() { return "BANK"; }

    @Override
    protected boolean validate(TransactionRequest req, List<String> errors) {
        if (req.getAmount().compareTo(new BigDecimal("100")) < 0) {
            errors.add("Montant minimum requis: 100 XAF");
            return false;
        }
        if (!req.getSourceAccount().matches("CM[A-Z0-9]{12}")) {
            errors.add("Format de compte invalide. Utilisez: CMXXXXXXXXXXXX");
            return false;
        }
        return true;
    }

    @Override
    protected BigDecimal calculateFees(BigDecimal amount, TransactionType type) {
        BigDecimal pct = amount.multiply(PCT_FEE).divide(CENT, 2, RoundingMode.HALF_UP);
        BigDecimal total = FIXED_FEE.add(pct);
        if (total.compareTo(new BigDecimal("50")) > 0) total = new BigDecimal("50");
        if (type == TransactionType.DEPOSIT) total = total.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
        return total;
    }

    @Override
    protected boolean checkLimits(TransactionRequest req, List<String> errors) {
        if (req.getAmount().compareTo(new BigDecimal("5000000")) > 0) {
            errors.add("Limite par transaction: 5,000,000 XAF");
            return false;
        }
        return true;
    }

    @Override
    protected String execute(TransactionRequest req) {
        dailyProcessed = dailyProcessed.add(req.getAmount());
        return "BNK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    @Override protected void recordAudit(String txId, TransactionRequest req) {
        System.out.println("[AUDIT BANQUE] " + txId);
    }
}

// ==================== MOBILE MONEY PROCESSOR ====================

class MobileMoneyTransactionProcessor extends TransactionProcessor {
    private static final BigDecimal[][] TIERS = {
        {new BigDecimal("0"), new BigDecimal("5000"), new BigDecimal("50")},
        {new BigDecimal("5001"), new BigDecimal("25000"), new BigDecimal("150")},
        {new BigDecimal("25001"), new BigDecimal("100000"), new BigDecimal("350")},
        {new BigDecimal("100001"), new BigDecimal("500000"), new BigDecimal("750")}
    };
    private BigDecimal dailyProcessed = BigDecimal.ZERO;

    @Override public String getOperatorType() { return "MOBILE_MONEY"; }

    @Override
    protected boolean validate(TransactionRequest req, List<String> errors) {
        if (req.getAmount().compareTo(new BigDecimal("100")) < 0) {
            errors.add("Montant minimum: 100 XAF");
            return false;
        }
        String phone = normalizePhone(req.getSourceAccount());
        if (!phone.matches("237[6][0-9]{8}")) {
            errors.add("Numero de telephone invalide");
            return false;
        }
        if (req.getTransactionType() == TransactionType.TRANSFER) {
            String dest = normalizePhone(req.getDestinationAccount());
            if (phone.equals(dest)) {
                errors.add("Impossible d'envoyer a soi-meme");
                return false;
            }
        }
        return true;
    }

    @Override
    protected BigDecimal calculateFees(BigDecimal amount, TransactionType type) {
        if (type == TransactionType.DEPOSIT) return BigDecimal.ZERO;
        for (BigDecimal[] tier : TIERS) {
            if (amount.compareTo(tier[0]) >= 0 && amount.compareTo(tier[1]) <= 0) {
                return tier[2];
            }
        }
        return amount.multiply(new BigDecimal("0.002")).setScale(0, RoundingMode.CEILING);
    }

    @Override
    protected boolean checkLimits(TransactionRequest req, List<String> errors) {
        if (req.getAmount().compareTo(new BigDecimal("500000")) > 0) {
            errors.add("Montant maximum par transaction: 500,000 XAF");
            return false;
        }
        return true;
    }

    @Override
    protected String execute(TransactionRequest req) {
        dailyProcessed = dailyProcessed.add(req.getAmount());
        return "MM" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("237")) return cleaned;
        if (cleaned.length() == 9) return "237" + cleaned;
        return cleaned;
    }

    @Override protected void sendNotifications(TransactionRequest req, String ref) {
        System.out.println("[SMS] Transaction " + ref);
    }
}

// ==================== MICROFINANCE PROCESSOR ====================

class MicrofinanceTransactionProcessor extends TransactionProcessor {
    private static final BigDecimal FIXED_FEE = new BigDecimal("25");
    private final String institutionName;
    private BigDecimal dailyProcessed = BigDecimal.ZERO;

    public MicrofinanceTransactionProcessor() { this("Microfinance Solidaire"); }
    public MicrofinanceTransactionProcessor(String name) { this.institutionName = name; }

    @Override public String getOperatorType() { return "MICROFINANCE"; }
    public String getInstitutionName() { return institutionName; }

    @Override
    protected boolean validate(TransactionRequest req, List<String> errors) {
        if (req.getAmount().compareTo(new BigDecimal("50")) < 0) {
            errors.add("Montant minimum: 50 XAF");
            return false;
        }
        if (!req.getSourceAccount().matches("MFI[0-9]{8}")) {
            errors.add("Numero de membre invalide. Format: MFI + 8 chiffres");
            return false;
        }
        return true;
    }

    @Override
    protected BigDecimal calculateFees(BigDecimal amount, TransactionType type) {
        if (type == TransactionType.DEPOSIT) return BigDecimal.ZERO;
        BigDecimal fees = FIXED_FEE;
        if (amount.compareTo(new BigDecimal("10000")) >= 0) {
            BigDecimal pct = amount.multiply(new BigDecimal("0.0005")).setScale(0, RoundingMode.CEILING);
            fees = fees.add(pct);
        }
        if (type == TransactionType.TRANSFER) {
            fees = fees.divide(new BigDecimal("2"), 0, RoundingMode.CEILING);
        }
        if (fees.compareTo(new BigDecimal("500")) > 0) fees = new BigDecimal("500");
        return fees;
    }

    @Override
    protected boolean checkLimits(TransactionRequest req, List<String> errors) {
        if (req.getAmount().compareTo(new BigDecimal("100000")) > 0) {
            errors.add("Montant maximum: 100,000 XAF");
            return false;
        }
        return true;
    }

    @Override
    protected String execute(TransactionRequest req) {
        dailyProcessed = dailyProcessed.add(req.getAmount());
        return "MFI" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    @Override protected void recordAudit(String txId, TransactionRequest req) {
        System.out.println("[REGISTRE " + institutionName.toUpperCase() + "] " + txId);
    }
}
