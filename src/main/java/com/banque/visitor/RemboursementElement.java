package com.banque.visitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATTERN VISITOR - ConcreteElement (Remboursement)
 */
public class RemboursementElement implements TransactionElement {

    private final String id;
    private final String accountNumber;
    private final BigDecimal amount;
    private final String originalTransactionId;
    private final String reason;
    private final LocalDateTime timestamp;

    public RemboursementElement(String accountNumber, BigDecimal amount,
                                String originalTransactionId, String reason) {
        this.id = "RMB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.originalTransactionId = originalTransactionId;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public void accept(TransactionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ElementType getElementType() {
        return ElementType.REMBOURSEMENT;
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

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return String.format("Remboursement[id=%s, compte=%s, montant=%.0f, original=%s]",
            id, accountNumber, amount, originalTransactionId);
    }
}
