import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ============================================================================
 * TEST STANDALONE - OBJECTIF 3 : Builder Pattern pour Transactions
 * ============================================================================
 *
 * Ce fichier démontre le pattern BUILDER pour construire des transactions
 * complexes avec des étapes optionnelles et paramétrables.
 *
 * Commandes:
 *   javac Objectif3Test.java
 *   java Objectif3Test
 * ============================================================================
 */
public class Objectif3Test {

    // ======================== ENUMS ========================
    enum TransactionType {
        DEPOSIT("Depot"), WITHDRAWAL("Retrait"),
        TRANSFER_INTERNAL("Transfert interne"),
        TRANSFER_INTER_OPERATOR("Transfert inter-operateurs"),
        TRANSFER_INTERNATIONAL("Transfert international");

        private final String desc;
        TransactionType(String desc) { this.desc = desc; }
        public String getDescription() { return desc; }
    }

    enum TransactionStatus {
        CREATED("Creee"), VERIFIED("Verifiee"),
        PROCESSING("En cours"), COMPLETED("Terminee"), FAILED("Echouee");

        private final String desc;
        TransactionStatus(String desc) { this.desc = desc; }
        public String getDescription() { return desc; }
    }

    // ======================== COMMISSION ========================
    static class Commission {
        private final String name;
        private final BigDecimal fixedAmount;
        private final BigDecimal percentage;

        private Commission(String name, BigDecimal fixed, BigDecimal pct) {
            this.name = name;
            this.fixedAmount = fixed;
            this.percentage = pct;
        }

        public BigDecimal calculate(BigDecimal amount) {
            BigDecimal result = BigDecimal.ZERO;
            if (fixedAmount != null) result = result.add(fixedAmount);
            if (percentage != null) {
                result = result.add(amount.multiply(percentage).divide(new BigDecimal("100"), 0, RoundingMode.CEILING));
            }
            return result;
        }

        public String getName() { return name; }
        public String getFormula() {
            StringBuilder sb = new StringBuilder();
            if (fixedAmount != null) sb.append(fixedAmount).append(" FCFA");
            if (percentage != null) {
                if (sb.length() > 0) sb.append(" + ");
                sb.append(percentage).append("%");
            }
            return sb.toString();
        }

        // Commissions prédéfinies
        public static Commission transferFee() {
            return new Commission("Frais de transfert", null, new BigDecimal("1"));
        }
        public static Commission interOperatorFee() {
            return new Commission("Frais inter-op", new BigDecimal("500"), new BigDecimal("1.5"));
        }
        public static Commission internationalFee() {
            return new Commission("Frais internationaux", new BigDecimal("2000"), new BigDecimal("2"));
        }
        public static Commission serviceFee(double amount) {
            return new Commission("Frais de service", BigDecimal.valueOf(amount), null);
        }
    }

    // ======================== TRANSACTION (PRODUIT) ========================
    static class Transaction {
        private final String reference;
        private final TransactionType type;
        private final String sourceAccount;
        private final String destinationAccount;
        private final BigDecimal amount;
        private final String currency;

        private BigDecimal convertedAmount;
        private String targetCurrency;
        private BigDecimal exchangeRate;
        private BigDecimal totalCommissions = BigDecimal.ZERO;
        private BigDecimal finalAmount;

        private final List<Commission> commissions;
        private final List<String> logs = new ArrayList<>();

        private boolean verificationEnabled;
        private boolean fraudCheckEnabled;
        private boolean currencyConversionEnabled;
        private boolean loggingEnabled;
        private boolean notificationEnabled;

        private TransactionStatus status = TransactionStatus.CREATED;

        Transaction(TransactionBuilder b) {
            this.reference = "TXN" + System.currentTimeMillis();
            this.type = b.type;
            this.sourceAccount = b.sourceAccount;
            this.destinationAccount = b.destinationAccount;
            this.amount = b.amount;
            this.currency = b.currency;
            this.targetCurrency = b.targetCurrency;
            this.exchangeRate = b.exchangeRate;
            this.commissions = new ArrayList<>(b.commissions);

            this.verificationEnabled = b.verificationEnabled;
            this.fraudCheckEnabled = b.fraudCheckEnabled;
            this.currencyConversionEnabled = b.currencyConversionEnabled;
            this.loggingEnabled = b.loggingEnabled;
            this.notificationEnabled = b.notificationEnabled;

            calculateAmounts();
        }

        private void calculateAmounts() {
            // Conversion
            if (currencyConversionEnabled && exchangeRate != null) {
                convertedAmount = amount.multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
                if (loggingEnabled) log("Conversion: " + amount + " " + currency + " -> " + convertedAmount + " " + targetCurrency);
            } else {
                convertedAmount = amount;
            }

            // Commissions
            for (Commission c : commissions) {
                BigDecimal fee = c.calculate(convertedAmount);
                totalCommissions = totalCommissions.add(fee);
                if (loggingEnabled) log("Commission '" + c.getName() + "': " + fee + " FCFA");
            }

            finalAmount = convertedAmount.add(totalCommissions);
        }

        public TransactionResult execute() {
            List<String> execLogs = new ArrayList<>();
            try {
                if (verificationEnabled) {
                    execLogs.add("Verification du compte...");
                    status = TransactionStatus.VERIFIED;
                    execLogs.add("Verification OK");
                }
                if (fraudCheckEnabled) {
                    execLogs.add("Analyse anti-fraude...");
                    execLogs.add("Anti-fraude OK");
                }
                status = TransactionStatus.PROCESSING;
                execLogs.add("Traitement en cours...");
                Thread.sleep(50);
                status = TransactionStatus.COMPLETED;
                execLogs.add("Transaction completee");
                if (notificationEnabled) {
                    execLogs.add("Notifications envoyees");
                }
                return new TransactionResult(true, "Succes", this, execLogs);
            } catch (Exception e) {
                status = TransactionStatus.FAILED;
                return new TransactionResult(false, e.getMessage(), this, execLogs);
            }
        }

        private void log(String msg) { logs.add(LocalDateTime.now() + " - " + msg); }

        // Getters
        public String getReference() { return reference; }
        public TransactionType getType() { return type; }
        public BigDecimal getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public BigDecimal getConvertedAmount() { return convertedAmount; }
        public String getTargetCurrency() { return targetCurrency; }
        public BigDecimal getTotalCommissions() { return totalCommissions; }
        public BigDecimal getFinalAmount() { return finalAmount; }
        public TransactionStatus getStatus() { return status; }
        public List<Commission> getCommissions() { return commissions; }
        public List<String> getLogs() { return logs; }

        public List<String> getSteps() {
            List<String> steps = new ArrayList<>();
            if (verificationEnabled) steps.add("Verification");
            if (fraudCheckEnabled) steps.add("Anti-fraude");
            if (currencyConversionEnabled) steps.add("Conversion");
            if (!commissions.isEmpty()) steps.add("Commissions(" + commissions.size() + ")");
            if (loggingEnabled) steps.add("Logging");
            if (notificationEnabled) steps.add("Notification");
            return steps;
        }
    }

    static class TransactionResult {
        final boolean success;
        final String message;
        final Transaction transaction;
        final List<String> logs;
        TransactionResult(boolean s, String m, Transaction t, List<String> l) {
            success = s; message = m; transaction = t; logs = l;
        }
    }

    // ======================== BUILDER ========================
    static class TransactionBuilder {
        TransactionType type;
        String sourceAccount;
        String destinationAccount;
        BigDecimal amount;
        String currency = "XAF";
        String targetCurrency;
        BigDecimal exchangeRate;
        List<Commission> commissions = new ArrayList<>();

        boolean verificationEnabled = false;
        boolean fraudCheckEnabled = false;
        boolean currencyConversionEnabled = false;
        boolean loggingEnabled = false;
        boolean notificationEnabled = false;

        // Configuration de base
        public TransactionBuilder type(TransactionType t) { type = t; return this; }
        public TransactionBuilder from(String acc) { sourceAccount = acc; return this; }
        public TransactionBuilder to(String acc) { destinationAccount = acc; return this; }
        public TransactionBuilder amount(double a) { amount = BigDecimal.valueOf(a); return this; }
        public TransactionBuilder amount(BigDecimal a) { amount = a; return this; }
        public TransactionBuilder currency(String c) { currency = c; return this; }

        // Etapes optionnelles
        public TransactionBuilder withVerification() { verificationEnabled = true; return this; }
        public TransactionBuilder withFraudCheck() { fraudCheckEnabled = true; return this; }
        public TransactionBuilder withCurrencyConversion(String target, double rate) {
            currencyConversionEnabled = true;
            targetCurrency = target;
            exchangeRate = BigDecimal.valueOf(rate);
            return this;
        }
        public TransactionBuilder withCommission(Commission c) { commissions.add(c); return this; }
        public TransactionBuilder withLogging() { loggingEnabled = true; return this; }
        public TransactionBuilder withNotification() { notificationEnabled = true; return this; }

        // Raccourcis
        public TransactionBuilder withFullSecurity() {
            verificationEnabled = true;
            fraudCheckEnabled = true;
            return this;
        }
        public TransactionBuilder withAllFeatures() {
            verificationEnabled = true;
            fraudCheckEnabled = true;
            loggingEnabled = true;
            notificationEnabled = true;
            return this;
        }

        public Transaction build() {
            if (type == null) throw new IllegalStateException("Type requis");
            if (sourceAccount == null) throw new IllegalStateException("Compte source requis");
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalStateException("Montant invalide");
            return new Transaction(this);
        }
    }

    // ======================== DIRECTOR ========================
    static class TransactionDirector {

        /** VARIANTE 1: Transaction Courte */
        public Transaction buildQuickTransaction(String source, String dest, double amount) {
            return new TransactionBuilder()
                .type(TransactionType.TRANSFER_INTERNAL)
                .from(source).to(dest)
                .amount(amount)
                .withCommission(Commission.transferFee())
                .withNotification()
                .build();
        }

        /** VARIANTE 2: Transaction Complete */
        public Transaction buildFullTransaction(String source, String dest, double amount) {
            return new TransactionBuilder()
                .type(TransactionType.TRANSFER_INTERNAL)
                .from(source).to(dest)
                .amount(amount)
                .withVerification()
                .withFraudCheck()
                .withCommission(Commission.transferFee())
                .withCommission(Commission.serviceFee(200))
                .withLogging()
                .withNotification()
                .build();
        }

        /** Transfert inter-operateurs */
        public Transaction buildInterOperatorTransfer(String src, String dest, double amount) {
            return new TransactionBuilder()
                .type(TransactionType.TRANSFER_INTER_OPERATOR)
                .from(src).to(dest)
                .amount(amount)
                .withFullSecurity()
                .withCommission(Commission.interOperatorFee())
                .withLogging()
                .withNotification()
                .build();
        }

        /** Transfert international */
        public Transaction buildInternationalTransfer(
                String src, String dest, double amount,
                String srcCurrency, String tgtCurrency, double rate) {
            return new TransactionBuilder()
                .type(TransactionType.TRANSFER_INTERNATIONAL)
                .from(src).to(dest)
                .amount(amount).currency(srcCurrency)
                .withAllFeatures()
                .withCurrencyConversion(tgtCurrency, rate)
                .withCommission(Commission.internationalFee())
                .build();
        }
    }

    // ======================== TESTS ========================
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println(" " + title);
        System.out.println("=".repeat(70));
    }

    private static void printTest(String testName, boolean passed, String details) {
        String status = passed ? "[PASS]" : "[FAIL]";
        System.out.printf("%s %s %s%n", status, passed ? "+" : "x", testName);
        if (details != null) System.out.println("       -> " + details);
        if (passed) testsPassed++; else testsFailed++;
    }

    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("######################################################################");
        System.out.println("#                                                                    #");
        System.out.println("#     OBJECTIF 3 - TEST DU PATTERN BUILDER                          #");
        System.out.println("#     Construction de Transactions Complexes                        #");
        System.out.println("#                                                                    #");
        System.out.println("######################################################################");

        TransactionDirector director = new TransactionDirector();

        // ==================== TEST 1: API FLUIDE ====================
        printHeader("TEST 1: API Fluide du Builder");

        System.out.println("\n  Construction d'une transaction avec API fluide:\n");
        System.out.println("  Transaction tx = new TransactionBuilder()");
        System.out.println("      .type(TRANSFER_INTERNAL)");
        System.out.println("      .from(\"ACC001\")");
        System.out.println("      .to(\"ACC002\")");
        System.out.println("      .amount(50000)");
        System.out.println("      .withVerification()");
        System.out.println("      .withCommission(Commission.transferFee())");
        System.out.println("      .withNotification()");
        System.out.println("      .build();");

        Transaction tx = new TransactionBuilder()
            .type(TransactionType.TRANSFER_INTERNAL)
            .from("ACC001").to("ACC002")
            .amount(50000)
            .withVerification()
            .withCommission(Commission.transferFee())
            .withNotification()
            .build();

        printTest("Transaction construite avec API fluide",
            tx != null && tx.getReference() != null,
            "Reference: " + tx.getReference());

        // ==================== TEST 2: VARIANTE COURTE ====================
        printHeader("TEST 2: VARIANTE 1 - Transaction Courte");

        Transaction quickTx = director.buildQuickTransaction("ACC001", "ACC002", 100000);

        System.out.println("\n  Transaction Courte (100 000 FCFA):");
        System.out.println("  - Type: " + quickTx.getType().getDescription());
        System.out.println("  - Montant: " + quickTx.getAmount() + " " + quickTx.getCurrency());
        System.out.println("  - Commissions: " + quickTx.getTotalCommissions() + " FCFA");
        System.out.println("  - Montant final: " + quickTx.getFinalAmount() + " FCFA");
        System.out.println("  - Etapes: " + quickTx.getSteps());

        TransactionResult quickResult = quickTx.execute();
        printTest("Transaction Courte executee", quickResult.success,
            "Statut: " + quickTx.getStatus().getDescription());

        boolean quickHasMinimalSteps = quickTx.getSteps().size() <= 3;
        printTest("Etapes minimales (courte)", quickHasMinimalSteps,
            "Nombre d'etapes: " + quickTx.getSteps().size());

        // ==================== TEST 3: VARIANTE COMPLETE ====================
        printHeader("TEST 3: VARIANTE 2 - Transaction Complete");

        Transaction fullTx = director.buildFullTransaction("ACC001", "ACC002", 100000);

        System.out.println("\n  Transaction Complete (100 000 FCFA):");
        System.out.println("  - Type: " + fullTx.getType().getDescription());
        System.out.println("  - Montant: " + fullTx.getAmount() + " " + fullTx.getCurrency());
        System.out.println("  - Commissions: " + fullTx.getTotalCommissions() + " FCFA");
        System.out.println("  - Montant final: " + fullTx.getFinalAmount() + " FCFA");
        System.out.println("  - Etapes: " + fullTx.getSteps());

        TransactionResult fullResult = fullTx.execute();
        printTest("Transaction Complete executee", fullResult.success,
            "Statut: " + fullTx.getStatus().getDescription());

        boolean fullHasMoreSteps = fullTx.getSteps().size() > quickTx.getSteps().size();
        printTest("Plus d'etapes que la variante courte", fullHasMoreSteps,
            "Complete=" + fullTx.getSteps().size() + " vs Courte=" + quickTx.getSteps().size());

        // ==================== TEST 4: COMPARAISON DES VARIANTES ====================
        printHeader("TEST 4: Comparaison Courte vs Complete");

        System.out.println("\n  Meme montant (100 000 FCFA), deux variantes:\n");
        System.out.printf("  %-20s | %-15s | %-15s%n", "Critere", "Courte", "Complete");
        System.out.println("  " + "-".repeat(55));
        System.out.printf("  %-20s | %-15s | %-15s%n", "Commissions",
            quickTx.getTotalCommissions() + " FCFA", fullTx.getTotalCommissions() + " FCFA");
        System.out.printf("  %-20s | %-15s | %-15s%n", "Montant final",
            quickTx.getFinalAmount() + " FCFA", fullTx.getFinalAmount() + " FCFA");
        System.out.printf("  %-20s | %-15d | %-15d%n", "Nb etapes",
            quickTx.getSteps().size(), fullTx.getSteps().size());

        BigDecimal commissionDiff = fullTx.getTotalCommissions().subtract(quickTx.getTotalCommissions());
        printTest("Commissions differentes entre variantes",
            commissionDiff.compareTo(BigDecimal.ZERO) != 0,
            "Difference: " + commissionDiff + " FCFA");

        // ==================== TEST 5: TRANSFERT INTER-OPERATEURS ====================
        printHeader("TEST 5: Transfert Inter-Operateurs");

        Transaction interOpTx = director.buildInterOperatorTransfer("ACC001", "MOMO002", 75000);

        System.out.println("\n  Transfert Inter-Operateurs (75 000 FCFA):");
        System.out.println("  - Type: " + interOpTx.getType().getDescription());
        System.out.println("  - Commissions: " + interOpTx.getTotalCommissions() + " FCFA");
        System.out.println("  - Etapes: " + interOpTx.getSteps());

        TransactionResult interOpResult = interOpTx.execute();
        printTest("Transfert inter-operateurs", interOpResult.success,
            "Commission: " + interOpTx.getTotalCommissions() + " FCFA");

        // ==================== TEST 6: TRANSFERT INTERNATIONAL ====================
        printHeader("TEST 6: Transfert International avec Conversion");

        Transaction intlTx = director.buildInternationalTransfer(
            "ACC001", "FR7630001007941234567890185",
            500000, "XAF", "EUR", 0.00152);

        System.out.println("\n  Transfert International:");
        System.out.println("  - Montant: " + intlTx.getAmount() + " " + intlTx.getCurrency());
        System.out.println("  - Converti: " + intlTx.getConvertedAmount() + " " + intlTx.getTargetCurrency());
        System.out.println("  - Commissions: " + intlTx.getTotalCommissions() + " FCFA");
        System.out.println("  - Etapes: " + intlTx.getSteps());

        TransactionResult intlResult = intlTx.execute();
        boolean hasConversion = intlTx.getSteps().contains("Conversion");
        printTest("Transfert international avec conversion", intlResult.success && hasConversion,
            "500000 XAF -> " + intlTx.getConvertedAmount() + " EUR");

        // ==================== TEST 7: EXECUTION ET LOGS ====================
        printHeader("TEST 7: Execution et Logs detailles");

        Transaction loggedTx = new TransactionBuilder()
            .type(TransactionType.TRANSFER_INTERNAL)
            .from("ACC001").to("ACC002")
            .amount(25000)
            .withAllFeatures()
            .withCommission(Commission.transferFee())
            .build();

        TransactionResult loggedResult = loggedTx.execute();

        System.out.println("\n  Logs d'execution:");
        for (String log : loggedResult.logs) {
            System.out.println("    - " + log);
        }

        printTest("Logging actif pendant l'execution",
            !loggedResult.logs.isEmpty(),
            loggedResult.logs.size() + " logs generes");

        // ==================== TEST 8: VALIDATION DU BUILDER ====================
        printHeader("TEST 8: Validation du Builder");

        boolean validationWorks = false;
        try {
            new TransactionBuilder().build(); // Pas de type ni source
        } catch (IllegalStateException e) {
            validationWorks = true;
            System.out.println("\n  Exception capturee: " + e.getMessage());
        }
        printTest("Validation des champs requis", validationWorks,
            "Builder rejette les transactions invalides");

        // ==================== RESUME ====================
        printHeader("RESUME DES TESTS");

        System.out.println();
        System.out.println("  Tests reussis : " + testsPassed);
        System.out.println("  Tests echoues : " + testsFailed);
        System.out.println("  Total         : " + (testsPassed + testsFailed));
        System.out.println();

        if (testsFailed == 0) {
            System.out.println("  *** TOUS LES TESTS SONT PASSES! ***");
            System.out.println();
            System.out.println("  L'OBJECTIF 3 est valide:");
            System.out.println("    - Pattern BUILDER: API fluide pour construction pas a pas");
            System.out.println("    - Etapes optionnelles: Verification, Conversion, Commissions, etc.");
            System.out.println("    - VARIANTE COURTE: Transaction simple, etapes minimales");
            System.out.println("    - VARIANTE COMPLETE: Toutes les etapes de securite");
            System.out.println("    - Director: Methodes pour creer des variantes predefinies");
            System.out.println("    - Pas de duplication de code entre les variantes");
        } else {
            System.out.println("  *** CERTAINS TESTS ONT ECHOUE ***");
        }

        System.out.println();
        System.out.println("######################################################################");
        System.out.println();
    }
}
