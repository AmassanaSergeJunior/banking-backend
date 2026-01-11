package com.banque.decorator;

import com.banque.decorator.Account.AccountType;
import com.banque.decorator.Account.TransactionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * PATTERN DECORATOR - Controller REST pour les Comptes
 *
 * OBJECTIF 8: Expose les fonctionnalites du pattern Decorator via API REST.
 *
 * ENDPOINTS:
 * - POST /api/accounts - Cree un compte
 * - POST /api/accounts/{id}/decorators - Ajoute un decorateur
 * - POST /api/accounts/{id}/deposit - Depot
 * - POST /api/accounts/{id}/withdraw - Retrait
 * - POST /api/accounts/{id}/transfer - Transfert
 * - GET /api/accounts/{id} - Details du compte
 * - GET /api/accounts/demo - Demonstration
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // ==================== CREATION ====================

    /**
     * Cree un compte.
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        try {
            Account account = accountService.buildAccount(request.getHolder(), request.getType())
                .withInitialBalance(request.getInitialBalance())
                .build();

            return ResponseEntity.ok(AccountResponse.from(account));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AccountResponse.error(e.getMessage()));
        }
    }

    /**
     * Cree un compte avec decorateurs.
     */
    @PostMapping("/with-features")
    public ResponseEntity<AccountResponse> createAccountWithFeatures(
            @RequestBody CreateAccountWithFeaturesRequest request) {
        try {
            AccountService.AccountBuilder builder = accountService
                .buildAccount(request.getHolder(), request.getType())
                .withInitialBalance(request.getInitialBalance());

            // Appliquer les decorateurs demandes
            if (request.isWithInterest()) {
                builder.withInterest(request.getInterestRate());
            }
            if (request.isWithOverdraft()) {
                builder.withOverdraft(request.getOverdraftLimit());
            }
            if (request.isWithInsurance()) {
                builder.withInsurance(request.getInsuranceType());
            }
            if (request.isWithLoyalty()) {
                builder.withLoyalty();
            }
            if (request.isWithFees()) {
                builder.withFees(request.getFeeType());
            }
            if (request.isWithNotifications()) {
                builder.withNotifications(request.getPhone(), request.getEmail());
            }

            Account account = builder.build();
            return ResponseEntity.ok(AccountResponse.from(account));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AccountResponse.error(e.getMessage()));
        }
    }

    // ==================== OPERATIONS ====================

    /**
     * Effectue un depot.
     */
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable String accountNumber,
            @RequestBody AmountRequest request) {

        TransactionResult result = accountService.deposit(accountNumber, request.getAmount());
        return ResponseEntity.ok(TransactionResponse.from(result));
    }

    /**
     * Effectue un retrait.
     */
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable String accountNumber,
            @RequestBody AmountRequest request) {

        TransactionResult result = accountService.withdraw(accountNumber, request.getAmount());
        return ResponseEntity.ok(TransactionResponse.from(result));
    }

    /**
     * Effectue un transfert.
     */
    @PostMapping("/{accountNumber}/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable String accountNumber,
            @RequestBody TransferRequest request) {

        TransactionResult result = accountService.transfer(
            accountNumber,
            request.getToAccount(),
            request.getAmount()
        );
        return ResponseEntity.ok(TransactionResponse.from(result));
    }

    // ==================== CONSULTATION ====================

    /**
     * Retourne les details d'un compte.
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        Account account = accountService.getAccount(accountNumber);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(AccountResponse.from(account));
    }

    /**
     * Liste tous les comptes.
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> responses = new ArrayList<>();
        for (Account account : accountService.getAllAccounts()) {
            responses.add(AccountResponse.from(account));
        }
        return ResponseEntity.ok(responses);
    }

    // ==================== DEMONSTRATION ====================

    /**
     * Demonstration du pattern Decorator.
     */
    @GetMapping("/demo")
    public ResponseEntity<DemoResponse> demo() {
        System.out.println("\n========================================");
        System.out.println("  DEMONSTRATION PATTERN DECORATOR");
        System.out.println("  Comptes avec Fonctionnalites");
        System.out.println("========================================\n");

        DemoResponse response = new DemoResponse();

        // 1. Compte de base (sans decorateur)
        System.out.println(">>> ETAPE 1: Compte de Base (ConcreteComponent)");
        System.out.println("------------------------------------------");

        Account basicAccount = accountService.createBasicAccount(
            "Kamga Jean",
            AccountType.COURANT,
            new BigDecimal("100000")
        );

        System.out.println("Compte cree: " + basicAccount.getAccountNumber());
        System.out.println("Description: " + basicAccount.getDescription());
        System.out.println("Solde: " + basicAccount.getBalance() + " FCFA");
        System.out.println("Frais mensuels: " + basicAccount.getMonthlyFees() + " FCFA");
        System.out.println("Bonus mensuel: " + basicAccount.getMonthlyBonus() + " FCFA");

        response.addStep("Compte de base", basicAccount.getDescription(),
            basicAccount.getBalance(), basicAccount.getMonthlyFees());

        // 2. Compte avec interets
        System.out.println("\n>>> ETAPE 2: Ajout Decorateur Interets");
        System.out.println("------------------------------------------");

        Account savingsAccount = accountService.buildAccount("Nguema Marie", AccountType.EPARGNE)
            .withInitialBalance(new BigDecimal("500000"))
            .withInterest(new BigDecimal("4.5"))
            .build();

        System.out.println("Compte epargne: " + savingsAccount.getAccountNumber());
        System.out.println("Description: " + savingsAccount.getDescription());
        System.out.println("Solde: " + savingsAccount.getBalance() + " FCFA");
        System.out.println("Bonus mensuel (interets): " + savingsAccount.getMonthlyBonus() + " FCFA");

        response.addStep("Compte avec interets", savingsAccount.getDescription(),
            savingsAccount.getBalance(), savingsAccount.getMonthlyFees());

        // 3. Compte avec decouvert
        System.out.println("\n>>> ETAPE 3: Ajout Decorateur Decouvert");
        System.out.println("------------------------------------------");

        Account overdraftAccount = accountService.buildAccount("Biya Paul", AccountType.COURANT)
            .withInitialBalance(new BigDecimal("50000"))
            .withOverdraft(new BigDecimal("200000"))
            .build();

        System.out.println("Compte avec decouvert: " + overdraftAccount.getAccountNumber());
        System.out.println("Description: " + overdraftAccount.getDescription());

        // Tenter un retrait superieur au solde
        System.out.println("\nTest retrait de 150000 FCFA (solde: 50000):");
        TransactionResult overdraftResult = overdraftAccount.withdraw(new BigDecimal("150000"));
        System.out.println("Resultat: " + overdraftResult);
        System.out.println("Nouveau solde: " + overdraftAccount.getBalance() + " FCFA");

        response.addStep("Compte avec decouvert", overdraftAccount.getDescription(),
            overdraftAccount.getBalance(), overdraftAccount.getMonthlyFees());

        // 4. Compte avec plusieurs decorateurs
        System.out.println("\n>>> ETAPE 4: Compte Multi-Decorateurs");
        System.out.println("------------------------------------------");

        Account premiumAccount = accountService.buildAccount("Foe Marc", AccountType.PROFESSIONNEL)
            .withInitialBalance(new BigDecimal("1000000"))
            .withInterest(new BigDecimal("2.5"))
            .withOverdraft(new BigDecimal("500000"))
            .withInsurance(InsuranceDecorator.InsuranceType.PREMIUM)
            .withLoyalty()
            .withFees(FeeDecorator.FeeType.BUSINESS)
            .withNotifications("+237699001122", "foe.marc@business.cm")
            .build();

        System.out.println("Compte premium: " + premiumAccount.getAccountNumber());
        if (premiumAccount instanceof AccountDecorator) {
            AccountDecorator decorator = (AccountDecorator) premiumAccount;
            System.out.println("Description complete: " + decorator.getFullDescription());
            System.out.println("Nombre de decorateurs: " + decorator.getDecoratorCount());
        }
        System.out.println("Frais mensuels totaux: " + premiumAccount.getMonthlyFees() + " FCFA");
        System.out.println("Bonus mensuels totaux: " + premiumAccount.getMonthlyBonus() + " FCFA");

        // Test d'un depot (devrait declencher fidelite + notification)
        System.out.println("\nTest depot de 200000 FCFA:");
        TransactionResult depositResult = premiumAccount.deposit(new BigDecimal("200000"));
        System.out.println("Resultat: " + depositResult);

        response.addStep("Compte premium multi-decorateurs",
            premiumAccount.getDescription(),
            premiumAccount.getBalance(), premiumAccount.getMonthlyFees());

        // 5. Demonstration de la flexibilite
        System.out.println("\n>>> ETAPE 5: Flexibilite du Pattern Decorator");
        System.out.println("------------------------------------------");
        System.out.println("Le pattern Decorator permet de:");
        System.out.println("  - Ajouter des fonctionnalites dynamiquement");
        System.out.println("  - Combiner les decorateurs dans n'importe quel ordre");
        System.out.println("  - Respecter l'interface Account partout");
        System.out.println("  - Calculer automatiquement frais et bonus cumules");

        // Resume
        System.out.println("\n========================================");
        System.out.println("  RESUME DE LA DEMONSTRATION");
        System.out.println("========================================");
        System.out.println("Comptes crees: " + accountService.getAllAccounts().size());

        response.setSuccess(true);
        response.setMessage("Demonstration complete - Pattern Decorator");

        return ResponseEntity.ok(response);
    }

    // ==================== DTOs ====================

    public static class CreateAccountRequest {
        private String holder;
        private AccountType type;
        private BigDecimal initialBalance = BigDecimal.ZERO;

        public String getHolder() { return holder; }
        public void setHolder(String holder) { this.holder = holder; }
        public AccountType getType() { return type; }
        public void setType(AccountType type) { this.type = type; }
        public BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(BigDecimal initialBalance) {
            this.initialBalance = initialBalance;
        }
    }

    public static class CreateAccountWithFeaturesRequest extends CreateAccountRequest {
        private boolean withInterest;
        private BigDecimal interestRate = new BigDecimal("3.5");
        private boolean withOverdraft;
        private BigDecimal overdraftLimit = new BigDecimal("100000");
        private boolean withInsurance;
        private InsuranceDecorator.InsuranceType insuranceType = InsuranceDecorator.InsuranceType.BASIC;
        private boolean withLoyalty;
        private boolean withFees;
        private FeeDecorator.FeeType feeType = FeeDecorator.FeeType.STANDARD;
        private boolean withNotifications;
        private String phone;
        private String email;

        // Getters and setters
        public boolean isWithInterest() { return withInterest; }
        public void setWithInterest(boolean withInterest) { this.withInterest = withInterest; }
        public BigDecimal getInterestRate() { return interestRate; }
        public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
        public boolean isWithOverdraft() { return withOverdraft; }
        public void setWithOverdraft(boolean withOverdraft) { this.withOverdraft = withOverdraft; }
        public BigDecimal getOverdraftLimit() { return overdraftLimit; }
        public void setOverdraftLimit(BigDecimal overdraftLimit) { this.overdraftLimit = overdraftLimit; }
        public boolean isWithInsurance() { return withInsurance; }
        public void setWithInsurance(boolean withInsurance) { this.withInsurance = withInsurance; }
        public InsuranceDecorator.InsuranceType getInsuranceType() { return insuranceType; }
        public void setInsuranceType(InsuranceDecorator.InsuranceType insuranceType) {
            this.insuranceType = insuranceType;
        }
        public boolean isWithLoyalty() { return withLoyalty; }
        public void setWithLoyalty(boolean withLoyalty) { this.withLoyalty = withLoyalty; }
        public boolean isWithFees() { return withFees; }
        public void setWithFees(boolean withFees) { this.withFees = withFees; }
        public FeeDecorator.FeeType getFeeType() { return feeType; }
        public void setFeeType(FeeDecorator.FeeType feeType) { this.feeType = feeType; }
        public boolean isWithNotifications() { return withNotifications; }
        public void setWithNotifications(boolean withNotifications) { this.withNotifications = withNotifications; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class AmountRequest {
        private BigDecimal amount;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class TransferRequest {
        private String toAccount;
        private BigDecimal amount;

        public String getToAccount() { return toAccount; }
        public void setToAccount(String toAccount) { this.toAccount = toAccount; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class AccountResponse {
        private String accountNumber;
        private String holder;
        private String type;
        private String description;
        private BigDecimal balance;
        private BigDecimal monthlyFees;
        private BigDecimal monthlyBonus;
        private int decoratorCount;
        private String error;

        public static AccountResponse from(Account account) {
            AccountResponse response = new AccountResponse();
            response.accountNumber = account.getAccountNumber();
            response.holder = account.getAccountHolder();
            response.type = account.getAccountType().getLabel();
            response.description = account.getDescription();
            response.balance = account.getBalance();
            response.monthlyFees = account.getMonthlyFees();
            response.monthlyBonus = account.getMonthlyBonus();

            if (account instanceof AccountDecorator) {
                response.decoratorCount = ((AccountDecorator) account).getDecoratorCount();
            } else {
                response.decoratorCount = 0;
            }

            return response;
        }

        public static AccountResponse error(String message) {
            AccountResponse response = new AccountResponse();
            response.error = message;
            return response;
        }

        // Getters
        public String getAccountNumber() { return accountNumber; }
        public String getHolder() { return holder; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public BigDecimal getBalance() { return balance; }
        public BigDecimal getMonthlyFees() { return monthlyFees; }
        public BigDecimal getMonthlyBonus() { return monthlyBonus; }
        public int getDecoratorCount() { return decoratorCount; }
        public String getError() { return error; }
    }

    public static class TransactionResponse {
        private boolean success;
        private String transactionId;
        private String message;
        private BigDecimal amount;
        private BigDecimal fees;
        private BigDecimal newBalance;

        public static TransactionResponse from(TransactionResult result) {
            TransactionResponse response = new TransactionResponse();
            response.success = result.isSuccess();
            response.transactionId = result.getTransactionId();
            response.message = result.getMessage();
            response.amount = result.getAmount();
            response.fees = result.getFees();
            response.newBalance = result.getNewBalance();
            return response;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getTransactionId() { return transactionId; }
        public String getMessage() { return message; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getFees() { return fees; }
        public BigDecimal getNewBalance() { return newBalance; }
    }

    public static class DemoResponse {
        private boolean success;
        private String message;
        private List<DemoStep> steps = new ArrayList<>();

        public void addStep(String name, String description, BigDecimal balance, BigDecimal fees) {
            steps.add(new DemoStep(name, description, balance, fees));
        }

        public void setSuccess(boolean success) { this.success = success; }
        public void setMessage(String message) { this.message = message; }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<DemoStep> getSteps() { return steps; }
    }

    public static class DemoStep {
        private String name;
        private String description;
        private BigDecimal balance;
        private BigDecimal monthlyFees;

        public DemoStep(String name, String description, BigDecimal balance, BigDecimal fees) {
            this.name = name;
            this.description = description;
            this.balance = balance;
            this.monthlyFees = fees;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public BigDecimal getBalance() { return balance; }
        public BigDecimal getMonthlyFees() { return monthlyFees; }
    }
}
