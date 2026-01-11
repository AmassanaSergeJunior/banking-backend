package com.banque.decorator;

import java.math.BigDecimal;
import java.util.List;

/**
 * PATTERN DECORATOR - Decorator Abstrait
 *
 * OBJECTIF 8: Classe abstraite qui enveloppe un compte et delegue
 * les operations au compte enveloppe. Les decorateurs concrets
 * etendent cette classe pour ajouter des fonctionnalites.
 *
 * PRINCIPE:
 * - Contient une reference vers le composant (Account)
 * - Implemente la meme interface (Account)
 * - Delegue par defaut au composant enveloppe
 * - Les sous-classes peuvent surcharger pour ajouter des comportements
 */
public abstract class AccountDecorator implements Account {

    protected final Account wrappedAccount;

    public AccountDecorator(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Le compte a decorer ne peut pas etre null");
        }
        this.wrappedAccount = account;
    }

    /**
     * Retourne le compte enveloppe.
     */
    public Account getWrappedAccount() {
        return wrappedAccount;
    }

    /**
     * Retourne le compte de base (le plus interne).
     */
    public Account getBaseAccount() {
        if (wrappedAccount instanceof AccountDecorator) {
            return ((AccountDecorator) wrappedAccount).getBaseAccount();
        }
        return wrappedAccount;
    }

    // ==================== DELEGATION PAR DEFAUT ====================

    @Override
    public String getAccountNumber() {
        return wrappedAccount.getAccountNumber();
    }

    @Override
    public String getAccountHolder() {
        return wrappedAccount.getAccountHolder();
    }

    @Override
    public BigDecimal getBalance() {
        return wrappedAccount.getBalance();
    }

    @Override
    public TransactionResult deposit(BigDecimal amount) {
        return wrappedAccount.deposit(amount);
    }

    @Override
    public TransactionResult withdraw(BigDecimal amount) {
        return wrappedAccount.withdraw(amount);
    }

    @Override
    public TransactionResult transfer(Account target, BigDecimal amount) {
        return wrappedAccount.transfer(target, amount);
    }

    @Override
    public String getDescription() {
        return wrappedAccount.getDescription();
    }

    @Override
    public AccountType getAccountType() {
        return wrappedAccount.getAccountType();
    }

    @Override
    public List<Transaction> getTransactionHistory() {
        return wrappedAccount.getTransactionHistory();
    }

    @Override
    public BigDecimal getMonthlyFees() {
        return wrappedAccount.getMonthlyFees();
    }

    @Override
    public BigDecimal getMonthlyBonus() {
        return wrappedAccount.getMonthlyBonus();
    }

    @Override
    public BigDecimal getWithdrawalLimit() {
        return wrappedAccount.getWithdrawalLimit();
    }

    @Override
    public boolean isActive() {
        return wrappedAccount.isActive();
    }

    /**
     * Retourne le nom du decorateur pour la description.
     */
    protected abstract String getDecoratorName();

    /**
     * Retourne le nom complet avec tous les decorateurs.
     */
    public String getFullDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseAccount().getDescription());
        sb.append(" + [");
        appendDecorators(sb);
        sb.append("]");
        return sb.toString();
    }

    private void appendDecorators(StringBuilder sb) {
        if (wrappedAccount instanceof AccountDecorator) {
            ((AccountDecorator) wrappedAccount).appendDecorators(sb);
            sb.append(", ");
        }
        sb.append(getDecoratorName());
    }

    /**
     * Compte le nombre de decorateurs appliques.
     */
    public int getDecoratorCount() {
        int count = 1;
        if (wrappedAccount instanceof AccountDecorator) {
            count += ((AccountDecorator) wrappedAccount).getDecoratorCount();
        }
        return count;
    }

    /**
     * Verifie si un decorateur specifique est present.
     */
    public boolean hasDecorator(Class<? extends AccountDecorator> decoratorClass) {
        if (decoratorClass.isInstance(this)) {
            return true;
        }
        if (wrappedAccount instanceof AccountDecorator) {
            return ((AccountDecorator) wrappedAccount).hasDecorator(decoratorClass);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[wrapped=%s]", getDecoratorName(), wrappedAccount.getAccountNumber());
    }
}
