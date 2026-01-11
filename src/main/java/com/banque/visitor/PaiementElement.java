package com.banque.visitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATTERN VISITOR - ConcreteElement (Paiement)
 */
public class PaiementElement implements TransactionElement {

    private final String id;
    private final String accountNumber;
    private final BigDecimal amount;
    private final String merchant;
    private final String category;
    private final LocalDateTime timestamp;

    public PaiementElement(String accountNumber, BigDecimal amount,
                           String merchant, String category) {
        this.id = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.merchant = merchant;
        this.category = category;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public void accept(TransactionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ElementType getElementType() {
        return ElementType.PAIEMENT;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getMerchant() {
        return merchant;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return String.format("Paiement[id=%s, compte=%s, montant=%.0f, marchand=%s]",
            id, accountNumber, amount, merchant);
    }
}
