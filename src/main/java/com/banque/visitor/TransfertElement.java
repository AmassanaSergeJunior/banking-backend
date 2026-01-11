package com.banque.visitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATTERN VISITOR - ConcreteElement (Transfert)
 */
public class TransfertElement implements TransactionElement {

    private final String id;
    private final String sourceAccount;
    private final String targetAccount;
    private final BigDecimal amount;
    private final BigDecimal fees;
    private final LocalDateTime timestamp;

    public TransfertElement(String sourceAccount, String targetAccount,
                            BigDecimal amount, BigDecimal fees) {
        this.id = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.amount = amount;
        this.fees = fees != null ? fees : BigDecimal.ZERO;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public void accept(TransactionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ElementType getElementType() {
        return ElementType.TRANSFERT;
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

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public BigDecimal getTotalAmount() {
        return amount.add(fees);
    }

    @Override
    public String toString() {
        return String.format("Transfert[id=%s, de=%s, vers=%s, montant=%.0f, frais=%.0f]",
            id, sourceAccount, targetAccount, amount, fees);
    }
}
