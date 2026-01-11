package com.banque.visitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATTERN VISITOR - ConcreteElement (Retrait)
 */
public class RetraitElement implements TransactionElement {

    private final String id;
    private final String accountNumber;
    private final BigDecimal amount;
    private final String channel; // ATM, Guichet, Mobile
    private final LocalDateTime timestamp;

    public RetraitElement(String accountNumber, BigDecimal amount, String channel) {
        this.id = "RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.channel = channel;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public void accept(TransactionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ElementType getElementType() {
        return ElementType.RETRAIT;
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

    public String getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return String.format("Retrait[id=%s, compte=%s, montant=%.0f, canal=%s]",
            id, accountNumber, amount, channel);
    }
}
