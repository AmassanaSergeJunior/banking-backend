package com.banque.visitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATTERN VISITOR - ConcreteElement (Frais)
 */
public class FraisElement implements TransactionElement {

    private final String id;
    private final String accountNumber;
    private final BigDecimal amount;
    private final String feeType;
    private final String description;
    private final LocalDateTime timestamp;

    public FraisElement(String accountNumber, BigDecimal amount,
                        String feeType, String description) {
        this.id = "FEE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.feeType = feeType;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public void accept(TransactionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ElementType getElementType() {
        return ElementType.FRAIS;
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

    public String getFeeType() {
        return feeType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("Frais[id=%s, compte=%s, montant=%.0f, type=%s]",
            id, accountNumber, amount, feeType);
    }
}
