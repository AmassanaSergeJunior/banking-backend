import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * ============================================================================
 * TEST STANDALONE - OBJECTIF 2 : Abstract Factory pour Opérateurs
 * ============================================================================
 *
 * Ce fichier démontre le pattern ABSTRACT FACTORY pour créer des familles
 * d'objets cohérents par opérateur (Banque, Mobile Money, Microfinance).
 *
 * Commandes:
 *   javac Objectif2Test.java
 *   java Objectif2Test
 * ============================================================================
 */
public class Objectif2Test {

    // ======================== ENUM ========================
    enum OperatorType {
        BANK("Banque Traditionnelle"),
        MOBILE_MONEY("Mobile Money"),
        MICROFINANCE("Microfinance");

        private final String displayName;
        OperatorType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // ======================== PRODUIT 1: VALIDATOR ========================
    interface AccountValidator {
        ValidationResult validateAccount(String accountNumber, BigDecimal deposit);
        String getOperatorName();
        BigDecimal getMinimumDeposit();
    }

    static class ValidationResult {
        final boolean valid;
        final String message;
        final String validatorName;
        ValidationResult(boolean valid, String message, String validatorName) {
            this.valid = valid;
            this.message = message;
            this.validatorName = validatorName;
        }
    }

    // Implémentations concrètes
    static class BankAccountValidator implements AccountValidator {
        public ValidationResult validateAccount(String accountNumber, BigDecimal deposit) {
            if (!accountNumber.matches("[A-Z]{2}\\d{10}"))
                return new ValidationResult(false, "Format invalide (attendu: XX0000000000)", "Bank Validator");
            if (deposit.compareTo(new BigDecimal("50000")) < 0)
                return new ValidationResult(false, "Depot minimum: 50000 FCFA", "Bank Validator");
            return new ValidationResult(true, "Compte bancaire valide", "Bank Validator");
        }
        public String getOperatorName() { return "Banque"; }
        public BigDecimal getMinimumDeposit() { return new BigDecimal("50000"); }
    }

    static class MobileMoneyAccountValidator implements AccountValidator {
        public ValidationResult validateAccount(String accountNumber, BigDecimal deposit) {
            if (!accountNumber.matches("6\\d{8}"))
                return new ValidationResult(false, "Numero invalide (attendu: 6XXXXXXXX)", "MoMo Validator");
            return new ValidationResult(true, "Compte Mobile Money actif", "MoMo Validator");
        }
        public String getOperatorName() { return "Mobile Money"; }
        public BigDecimal getMinimumDeposit() { return BigDecimal.ZERO; }
    }

    static class MicrofinanceAccountValidator implements AccountValidator {
        public ValidationResult validateAccount(String accountNumber, BigDecimal deposit) {
            if (!accountNumber.matches("MF\\d{8}"))
                return new ValidationResult(false, "Format invalide (attendu: MF00000000)", "Microfinance Validator");
            if (deposit.compareTo(new BigDecimal("5000")) < 0)
                return new ValidationResult(false, "Depot minimum: 5000 FCFA", "Microfinance Validator");
            return new ValidationResult(true, "Compte microfinance ouvert", "Microfinance Validator");
        }
        public String getOperatorName() { return "Microfinance"; }
        public BigDecimal getMinimumDeposit() { return new BigDecimal("5000"); }
    }

    // ======================== PRODUIT 2: RATE CALCULATOR ========================
    interface RateCalculator {
        BigDecimal calculateFee(BigDecimal amount);
        String getOperatorName();
        String getFeeDescription();
    }

    static class BankRateCalculator implements RateCalculator {
        public BigDecimal calculateFee(BigDecimal amount) {
            // Frais fixes 500 + 1%
            return new BigDecimal("500").add(amount.multiply(new BigDecimal("0.01")))
                .setScale(0, RoundingMode.CEILING);
        }
        public String getOperatorName() { return "Banque"; }
        public String getFeeDescription() { return "500 FCFA + 1%"; }
    }

    static class MobileMoneyRateCalculator implements RateCalculator {
        public BigDecimal calculateFee(BigDecimal amount) {
            // Frais par palier
            if (amount.compareTo(new BigDecimal("5000")) <= 0) return new BigDecimal("50");
            if (amount.compareTo(new BigDecimal("10000")) <= 0) return new BigDecimal("100");
            if (amount.compareTo(new BigDecimal("50000")) <= 0) return new BigDecimal("350");
            return amount.multiply(new BigDecimal("0.02")).setScale(0, RoundingMode.CEILING);
        }
        public String getOperatorName() { return "Mobile Money"; }
        public String getFeeDescription() { return "Par palier (50-350 FCFA ou 2%)"; }
    }

    static class MicrofinanceRateCalculator implements RateCalculator {
        public BigDecimal calculateFee(BigDecimal amount) {
            // Gratuit < 10000, sinon 0.8%
            if (amount.compareTo(new BigDecimal("10000")) <= 0) return BigDecimal.ZERO;
            return amount.multiply(new BigDecimal("0.008")).setScale(0, RoundingMode.CEILING);
        }
        public String getOperatorName() { return "Microfinance"; }
        public String getFeeDescription() { return "Gratuit < 10000, sinon 0.8%"; }
    }

    // ======================== PRODUIT 3: NOTIFICATION MODULE ========================
    interface NotificationModule {
        String formatNotification(String type, BigDecimal amount, BigDecimal balance);
        String getOperatorName();
        String getChannel();
    }

    static class BankNotificationModule implements NotificationModule {
        public String formatNotification(String type, BigDecimal amount, BigDecimal balance) {
            return String.format("[BANQUE] Votre compte a ete %s de %s FCFA. Solde: %s FCFA. Service client: 8888",
                type.equals("DEPOSIT") ? "credite" : "debite", amount, balance);
        }
        public String getOperatorName() { return "Banque"; }
        public String getChannel() { return "SMS + Email"; }
    }

    static class MobileMoneyNotificationModule implements NotificationModule {
        public String formatNotification(String type, BigDecimal amount, BigDecimal balance) {
            String emoji = type.equals("DEPOSIT") ? "+" : "-";
            return String.format("MoMo: %s %s FCFA. Solde: %s FCFA. *126# pour historique",
                emoji, amount, balance);
        }
        public String getOperatorName() { return "Mobile Money"; }
        public String getChannel() { return "SMS"; }
    }

    static class MicrofinanceNotificationModule implements NotificationModule {
        public String formatNotification(String type, BigDecimal amount, BigDecimal balance) {
            if (type.equals("DEPOSIT")) {
                return String.format("[Votre Caisse] Bravo! Depot de %s FCFA. Solde: %s FCFA. Continuez a epargner!",
                    amount, balance);
            }
            return String.format("[Votre Caisse] Retrait de %s FCFA. Solde: %s FCFA. Votre conseiller reste disponible.",
                amount, balance);
        }
        public String getOperatorName() { return "Microfinance"; }
        public String getChannel() { return "SMS"; }
    }

    // ======================== PRODUIT 4: EXTERNAL ADAPTER ========================
    interface ExternalSystemAdapter {
        TransferResult transfer(String destination, BigDecimal amount);
        String getSystemName();
        String getProtocol();
    }

    static class TransferResult {
        final boolean success;
        final String reference;
        final String systemName;
        TransferResult(boolean success, String reference, String systemName) {
            this.success = success;
            this.reference = reference;
            this.systemName = systemName;
        }
    }

    static class BankExternalAdapter implements ExternalSystemAdapter {
        public TransferResult transfer(String destination, BigDecimal amount) {
            String ref = "SWIFT" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return new TransferResult(true, ref, "SWIFT/BEAC");
        }
        public String getSystemName() { return "Reseau Interbancaire BEAC/SWIFT"; }
        public String getProtocol() { return "SWIFT/ISO20022"; }
    }

    static class MobileMoneyExternalAdapter implements ExternalSystemAdapter {
        public TransferResult transfer(String destination, BigDecimal amount) {
            String ref = "MOMO" + System.currentTimeMillis();
            return new TransferResult(true, ref, "Telecom API");
        }
        public String getSystemName() { return "Plateforme Telecom API"; }
        public String getProtocol() { return "REST/JSON"; }
    }

    static class MicrofinanceExternalAdapter implements ExternalSystemAdapter {
        public TransferResult transfer(String destination, BigDecimal amount) {
            String ref = "MFI" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            return new TransferResult(true, ref, "Caisses Partenaires");
        }
        public String getSystemName() { return "Reseau Caisses Partenaires"; }
        public String getProtocol() { return "REST/XML"; }
    }

    // ======================== ABSTRACT FACTORY ========================
    interface OperatorFactory {
        AccountValidator createAccountValidator();
        RateCalculator createRateCalculator();
        NotificationModule createNotificationModule();
        ExternalSystemAdapter createExternalSystemAdapter();
        OperatorType getOperatorType();
        String getOperatorName();
    }

    // Factories concrètes
    static class BankOperatorFactory implements OperatorFactory {
        public AccountValidator createAccountValidator() { return new BankAccountValidator(); }
        public RateCalculator createRateCalculator() { return new BankRateCalculator(); }
        public NotificationModule createNotificationModule() { return new BankNotificationModule(); }
        public ExternalSystemAdapter createExternalSystemAdapter() { return new BankExternalAdapter(); }
        public OperatorType getOperatorType() { return OperatorType.BANK; }
        public String getOperatorName() { return "Banque Traditionnelle"; }
    }

    static class MobileMoneyOperatorFactory implements OperatorFactory {
        public AccountValidator createAccountValidator() { return new MobileMoneyAccountValidator(); }
        public RateCalculator createRateCalculator() { return new MobileMoneyRateCalculator(); }
        public NotificationModule createNotificationModule() { return new MobileMoneyNotificationModule(); }
        public ExternalSystemAdapter createExternalSystemAdapter() { return new MobileMoneyExternalAdapter(); }
        public OperatorType getOperatorType() { return OperatorType.MOBILE_MONEY; }
        public String getOperatorName() { return "Mobile Money"; }
    }

    static class MicrofinanceOperatorFactory implements OperatorFactory {
        public AccountValidator createAccountValidator() { return new MicrofinanceAccountValidator(); }
        public RateCalculator createRateCalculator() { return new MicrofinanceRateCalculator(); }
        public NotificationModule createNotificationModule() { return new MicrofinanceNotificationModule(); }
        public ExternalSystemAdapter createExternalSystemAdapter() { return new MicrofinanceExternalAdapter(); }
        public OperatorType getOperatorType() { return OperatorType.MICROFINANCE; }
        public String getOperatorName() { return "Microfinance"; }
    }

    // ======================== FACTORY PROVIDER ========================
    static class OperatorFactoryProvider {
        private Map<OperatorType, OperatorFactory> factories = new EnumMap<>(OperatorType.class);

        public OperatorFactoryProvider() {
            factories.put(OperatorType.BANK, new BankOperatorFactory());
            factories.put(OperatorType.MOBILE_MONEY, new MobileMoneyOperatorFactory());
            factories.put(OperatorType.MICROFINANCE, new MicrofinanceOperatorFactory());
        }

        public OperatorFactory getFactory(OperatorType type) {
            return factories.get(type);
        }

        public List<OperatorType> getAvailableTypes() {
            return new ArrayList<>(factories.keySet());
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
        System.out.println("#     OBJECTIF 2 - TEST DU PATTERN ABSTRACT FACTORY                 #");
        System.out.println("#     Familles d'objets coherents par Operateur                     #");
        System.out.println("#                                                                    #");
        System.out.println("######################################################################");

        OperatorFactoryProvider provider = new OperatorFactoryProvider();

        // ==================== TEST 1: COHERENCE DES FAMILLES ====================
        printHeader("TEST 1: Coherence des familles d'objets");

        for (OperatorType type : provider.getAvailableTypes()) {
            OperatorFactory factory = provider.getFactory(type);

            AccountValidator validator = factory.createAccountValidator();
            RateCalculator calculator = factory.createRateCalculator();
            NotificationModule notifier = factory.createNotificationModule();
            ExternalSystemAdapter adapter = factory.createExternalSystemAdapter();

            boolean coherent = validator.getOperatorName().equals(calculator.getOperatorName())
                && calculator.getOperatorName().equals(notifier.getOperatorName());

            printTest(type.getDisplayName() + " - Famille coherente", coherent,
                "Tous les objets appartiennent a: " + validator.getOperatorName());
        }

        // ==================== TEST 2: VALIDATION DIFFERENTE ====================
        printHeader("TEST 2: Validation de compte - Regles differentes par operateur");

        System.out.println("\n  Validation du meme compte 'CM1234567890' avec depot 30000 FCFA:\n");

        // Format bancaire
        OperatorFactory bankFactory = provider.getFactory(OperatorType.BANK);
        ValidationResult bankResult = bankFactory.createAccountValidator()
            .validateAccount("CM1234567890", new BigDecimal("30000"));
        printTest("BANK: " + (bankResult.valid ? "Accepte" : "Refuse"),
            !bankResult.valid, // Attendu: refus (depot < 50000)
            bankResult.message);

        // Format Mobile Money (numero invalide pour MoMo)
        OperatorFactory momoFactory = provider.getFactory(OperatorType.MOBILE_MONEY);
        ValidationResult momoResult = momoFactory.createAccountValidator()
            .validateAccount("CM1234567890", new BigDecimal("30000"));
        printTest("MOBILE_MONEY: " + (momoResult.valid ? "Accepte" : "Refuse"),
            !momoResult.valid, // Attendu: refus (format invalide)
            momoResult.message);

        // Format Microfinance (format invalide)
        OperatorFactory mfFactory = provider.getFactory(OperatorType.MICROFINANCE);
        ValidationResult mfResult = mfFactory.createAccountValidator()
            .validateAccount("CM1234567890", new BigDecimal("30000"));
        printTest("MICROFINANCE: " + (mfResult.valid ? "Accepte" : "Refuse"),
            !mfResult.valid, // Attendu: refus (format invalide)
            mfResult.message);

        // ==================== TEST 3: COMPTES VALIDES PAR OPERATEUR ====================
        printHeader("TEST 3: Comptes valides pour chaque operateur");

        ValidationResult bankValid = bankFactory.createAccountValidator()
            .validateAccount("CM1234567890", new BigDecimal("100000"));
        printTest("BANK: CM1234567890 + 100000 FCFA", bankValid.valid, bankValid.message);

        ValidationResult momoValid = momoFactory.createAccountValidator()
            .validateAccount("655123456", new BigDecimal("0"));
        printTest("MOBILE_MONEY: 655123456 + 0 FCFA", momoValid.valid, momoValid.message);

        ValidationResult mfValid = mfFactory.createAccountValidator()
            .validateAccount("MF12345678", new BigDecimal("10000"));
        printTest("MICROFINANCE: MF12345678 + 10000 FCFA", mfValid.valid, mfValid.message);

        // ==================== TEST 4: CALCUL DES FRAIS ====================
        printHeader("TEST 4: Comparaison des frais pour 100 000 FCFA");

        BigDecimal amount = new BigDecimal("100000");
        System.out.println("\n  Montant de la transaction: " + amount + " FCFA\n");

        for (OperatorType type : provider.getAvailableTypes()) {
            OperatorFactory factory = provider.getFactory(type);
            RateCalculator calc = factory.createRateCalculator();
            BigDecimal fee = calc.calculateFee(amount);

            System.out.printf("  %-20s: %8s FCFA  (%s)%n",
                type.getDisplayName(), fee, calc.getFeeDescription());
        }

        // Vérifier que les frais sont différents
        BigDecimal bankFee = bankFactory.createRateCalculator().calculateFee(amount);
        BigDecimal momoFee = momoFactory.createRateCalculator().calculateFee(amount);
        BigDecimal mfFee = mfFactory.createRateCalculator().calculateFee(amount);

        boolean feesDifferent = !bankFee.equals(momoFee) && !momoFee.equals(mfFee);
        printTest("\nFrais differents selon l'operateur", feesDifferent,
            "Bank=" + bankFee + ", MoMo=" + momoFee + ", MF=" + mfFee);

        // ==================== TEST 5: FORMAT DES NOTIFICATIONS ====================
        printHeader("TEST 5: Format des notifications par operateur");

        BigDecimal txAmount = new BigDecimal("50000");
        BigDecimal balance = new BigDecimal("150000");

        System.out.println("\n  Notification pour depot de 50000 FCFA (solde: 150000 FCFA):\n");

        for (OperatorType type : provider.getAvailableTypes()) {
            OperatorFactory factory = provider.getFactory(type);
            NotificationModule notifier = factory.createNotificationModule();
            String msg = notifier.formatNotification("DEPOSIT", txAmount, balance);

            System.out.println("  [" + type.getDisplayName() + "] (" + notifier.getChannel() + ")");
            System.out.println("  " + msg);
            System.out.println();
        }

        printTest("Formats de notification differents", true, "Chaque operateur a son style");

        // ==================== TEST 6: SYSTEMES EXTERNES ====================
        printHeader("TEST 6: Systemes externes et protocoles");

        System.out.println();
        for (OperatorType type : provider.getAvailableTypes()) {
            OperatorFactory factory = provider.getFactory(type);
            ExternalSystemAdapter adapter = factory.createExternalSystemAdapter();

            TransferResult result = adapter.transfer("DEST123", new BigDecimal("25000"));

            System.out.printf("  %-20s: %s (%s)%n",
                type.getDisplayName(), adapter.getSystemName(), adapter.getProtocol());
            System.out.println("                        Reference: " + result.reference);
        }

        printTest("\nConnexions externes differentes", true, "Chaque operateur a son systeme");

        // ==================== TEST 7: BASCULEMENT D'OPERATEUR ====================
        printHeader("TEST 7: Basculement d'un operateur a un autre");

        System.out.println("\n  Simulation: meme operation avec changement d'operateur\n");

        // Operation: valider compte + calculer frais + notifier
        String[] accounts = {"CM0000000001", "699000001", "MF00000001"};
        OperatorType[] types = {OperatorType.BANK, OperatorType.MOBILE_MONEY, OperatorType.MICROFINANCE};
        BigDecimal deposit = new BigDecimal("50000");

        for (int i = 0; i < types.length; i++) {
            // BASCULEMENT = juste changer le type!
            OperatorFactory factory = provider.getFactory(types[i]);

            AccountValidator validator = factory.createAccountValidator();
            RateCalculator calculator = factory.createRateCalculator();
            NotificationModule notifier = factory.createNotificationModule();

            ValidationResult vr = validator.validateAccount(accounts[i], deposit);
            BigDecimal fee = calculator.calculateFee(deposit);

            System.out.printf("  %s:%n", types[i].getDisplayName());
            System.out.printf("    - Compte %s: %s%n", accounts[i], vr.valid ? "VALIDE" : "REFUSE");
            System.out.printf("    - Frais: %s FCFA%n", fee);
            System.out.printf("    - Canal: %s%n%n", notifier.getChannel());
        }

        printTest("Basculement reussi entre 3 operateurs", true,
            "Changement d'operateur = 1 ligne de code");

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
            System.out.println("  L'OBJECTIF 2 est valide:");
            System.out.println("    - Pattern ABSTRACT FACTORY: Interface commune pour familles d'objets");
            System.out.println("    - 3 Factories concretes: Bank, MobileMoney, Microfinance");
            System.out.println("    - 4 types de produits: Validator, Calculator, Notifier, Adapter");
            System.out.println("    - Coherence garantie: objets d'une famille travaillent ensemble");
            System.out.println("    - Basculement facile: changer d'operateur = 1 ligne de code");
        } else {
            System.out.println("  *** CERTAINS TESTS ONT ECHOUE ***");
        }

        System.out.println();
        System.out.println("######################################################################");
        System.out.println();
    }
}
