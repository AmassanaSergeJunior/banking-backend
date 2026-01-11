package com.banque.decorator;

import com.banque.decorator.Account.AccountType;
import com.banque.decorator.Account.TransactionResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PATTERN DECORATOR - Service de gestion des comptes
 *
 * OBJECTIF 8: Service qui permet de creer des comptes et de leur
 * ajouter dynamiquement des fonctionnalites via les decorateurs.
 *
 * FONCTIONNALITES:
 * - Creation de comptes de base
 * - Ajout de decorateurs (Interets, Decouvert, Assurance, etc.)
 * - Operations bancaires
 * - Builder fluent pour composer les fonctionnalites
 */
@Service
public class AccountService {

    private final Map<String, Account> accounts;
    private int accountCounter;

    public AccountService() {
        this.accounts = new ConcurrentHashMap<>();
        this.accountCounter = 1000;
    }

    // ==================== CREATION DE COMPTES ====================

    /**
     * Cree un compte de base.
     */
    public Account createBasicAccount(String holder, AccountType type) {
        return createBasicAccount(holder, type, BigDecimal.ZERO);
    }

    /**
     * Cree un compte de base avec solde initial.
     */
    public Account createBasicAccount(String holder, AccountType type, BigDecimal initialBalance) {
        String accountNumber = generateAccountNumber(type);
        BasicAccount account = new BasicAccount(accountNumber, holder, type, initialBalance);
        accounts.put(accountNumber, account);
        return account;
    }

    /**
     * Retourne un builder fluent pour creer un compte avec decorateurs.
     */
    public AccountBuilder buildAccount(String holder, AccountType type) {
        return new AccountBuilder(this, holder, type);
    }

    // ==================== AJOUT DE DECORATEURS ====================

    /**
     * Ajoute des interets a un compte.
     */
    public Account addInterest(Account account, BigDecimal annualRate) {
        Account decorated = new InterestDecorator(account, annualRate);
        updateAccountReference(account.getAccountNumber(), decorated);
        return decorated;
    }

    /**
     * Ajoute un decouvert a un compte.
     */
    public Account addOverdraft(Account account, BigDecimal limit) {
        Account decorated = new OverdraftDecorator(account, limit);
        updateAccountReference(account.getAccountNumber(), decorated);
        return decorated;
    }

    /**
     * Ajoute une assurance a un compte.
     */
    public Account addInsurance(Account account, InsuranceDecorator.InsuranceType type) {
        Account decorated = new InsuranceDecorator(account, type);
        updateAccountReference(account.getAccountNumber(), decorated);
        return decorated;
    }

    /**
     * Ajoute le programme de fidelite a un compte.
     */
    public Account addLoyalty(Account account) {
        Account decorated = new LoyaltyDecorator(account);
        updateAccountReference(account.getAccountNumber(), decorated);
        return decorated;
    }

    /**
     * Ajoute des frais a un compte.
     */
    public Account addFees(Account account, FeeDecorator.FeeType type) {
        Account decorated = new FeeDecorator(account, type);
        updateAccountReference(account.getAccountNumber(), decorated);
        return decorated;
    }

    /**
     * Ajoute des notifications a un compte.
     */
    public Account addNotifications(Account account, String phone, String email) {
        Account decorated = NotificationDecorator.withBoth(account, phone, email);
        updateAccountReference(account.getAccountNumber(), decorated);
        return decorated;
    }

    // ==================== OPERATIONS BANCAIRES ====================

    /**
     * Effectue un depot.
     */
    public TransactionResult deposit(String accountNumber, BigDecimal amount) {
        Account account = getAccount(accountNumber);
        if (account == null) {
            return TransactionResult.failure("DEP-ERR", "Compte non trouve: " + accountNumber);
        }
        return account.deposit(amount);
    }

    /**
     * Effectue un retrait.
     */
    public TransactionResult withdraw(String accountNumber, BigDecimal amount) {
        Account account = getAccount(accountNumber);
        if (account == null) {
            return TransactionResult.failure("RET-ERR", "Compte non trouve: " + accountNumber);
        }
        return account.withdraw(amount);
    }

    /**
     * Effectue un transfert.
     */
    public TransactionResult transfer(String fromAccount, String toAccount, BigDecimal amount) {
        Account source = getAccount(fromAccount);
        Account target = getAccount(toAccount);

        if (source == null) {
            return TransactionResult.failure("TRF-ERR", "Compte emetteur non trouve");
        }
        if (target == null) {
            return TransactionResult.failure("TRF-ERR", "Compte destinataire non trouve");
        }

        return source.transfer(target, amount);
    }

    // ==================== CONSULTATION ====================

    /**
     * Retourne un compte par son numero.
     */
    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    /**
     * Retourne tous les comptes.
     */
    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    /**
     * Verifie si un compte existe.
     */
    public boolean accountExists(String accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    /**
     * Supprime un compte.
     */
    public boolean deleteAccount(String accountNumber) {
        return accounts.remove(accountNumber) != null;
    }

    // ==================== METHODES UTILITAIRES ====================

    private String generateAccountNumber(AccountType type) {
        return String.format("CM%s%06d", type.getCode(), ++accountCounter);
    }

    private void updateAccountReference(String accountNumber, Account newAccount) {
        accounts.put(accountNumber, newAccount);
    }

    // ==================== BUILDER FLUENT ====================

    /**
     * Builder pour creer des comptes avec decorateurs de maniere fluide.
     */
    public static class AccountBuilder {
        private final AccountService service;
        private final String holder;
        private final AccountType type;
        private BigDecimal initialBalance = BigDecimal.ZERO;

        // Options de decorateurs
        private boolean withInterest = false;
        private BigDecimal interestRate = new BigDecimal("3.5");

        private boolean withOverdraft = false;
        private BigDecimal overdraftLimit = new BigDecimal("100000");

        private boolean withInsurance = false;
        private InsuranceDecorator.InsuranceType insuranceType = InsuranceDecorator.InsuranceType.BASIC;

        private boolean withLoyalty = false;

        private boolean withFees = false;
        private FeeDecorator.FeeType feeType = FeeDecorator.FeeType.BASIC;

        private boolean withNotifications = false;
        private String phone;
        private String email;

        public AccountBuilder(AccountService service, String holder, AccountType type) {
            this.service = service;
            this.holder = holder;
            this.type = type;
        }

        public AccountBuilder withInitialBalance(BigDecimal balance) {
            this.initialBalance = balance;
            return this;
        }

        public AccountBuilder withInterest(BigDecimal rate) {
            this.withInterest = true;
            this.interestRate = rate;
            return this;
        }

        public AccountBuilder withInterest() {
            return withInterest(new BigDecimal("3.5"));
        }

        public AccountBuilder withOverdraft(BigDecimal limit) {
            this.withOverdraft = true;
            this.overdraftLimit = limit;
            return this;
        }

        public AccountBuilder withOverdraft() {
            return withOverdraft(new BigDecimal("100000"));
        }

        public AccountBuilder withInsurance(InsuranceDecorator.InsuranceType type) {
            this.withInsurance = true;
            this.insuranceType = type;
            return this;
        }

        public AccountBuilder withInsurance() {
            return withInsurance(InsuranceDecorator.InsuranceType.BASIC);
        }

        public AccountBuilder withLoyalty() {
            this.withLoyalty = true;
            return this;
        }

        public AccountBuilder withFees(FeeDecorator.FeeType type) {
            this.withFees = true;
            this.feeType = type;
            return this;
        }

        public AccountBuilder withFees() {
            return withFees(FeeDecorator.FeeType.STANDARD);
        }

        public AccountBuilder withNotifications(String phone, String email) {
            this.withNotifications = true;
            this.phone = phone;
            this.email = email;
            return this;
        }

        public AccountBuilder withSMSNotifications(String phone) {
            return withNotifications(phone, null);
        }

        public AccountBuilder withEmailNotifications(String email) {
            return withNotifications(null, email);
        }

        /**
         * Construit le compte avec tous les decorateurs configures.
         */
        public Account build() {
            // Creer le compte de base
            Account account = service.createBasicAccount(holder, type, initialBalance);

            // Appliquer les decorateurs dans l'ordre
            if (withFees) {
                account = new FeeDecorator(account, feeType);
            }
            if (withOverdraft) {
                account = new OverdraftDecorator(account, overdraftLimit);
            }
            if (withInterest) {
                account = new InterestDecorator(account, interestRate);
            }
            if (withInsurance) {
                account = new InsuranceDecorator(account, insuranceType);
            }
            if (withLoyalty) {
                account = new LoyaltyDecorator(account);
            }
            if (withNotifications) {
                if (phone != null && email != null) {
                    account = NotificationDecorator.withBoth(account, phone, email);
                } else if (phone != null) {
                    account = new NotificationDecorator(account, phone);
                } else if (email != null) {
                    account = NotificationDecorator.withEmail(account, email);
                }
            }

            // Mettre a jour la reference dans le service
            service.updateAccountReference(account.getAccountNumber(), account);

            return account;
        }
    }
}
