package com.banque.visitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATTERN VISITOR - ConcreteElement (Depot)
 */
public class DepotElement implements TransactionElement {

    private final String id;
    private final String accountNumber;
    private final BigDecimal amount;
    private final String source;
    private final LocalDateTime timestamp;

    public DepotElement(String accountNumber, BigDecimal amount, String source) {
        this.id = "DEP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.source = source;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public void accept(TransactionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ElementType getElementType() {
        return ElementType.DEPOT;
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

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return String.format("Depot[id=%s, compte=%s, montant=%.0f, source=%s]",
            id, accountNumber, amount, source);
    }
}
