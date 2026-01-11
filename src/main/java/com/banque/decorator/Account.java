package com.banque.decorator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PATTERN DECORATOR - Interface Component
 *
 * OBJECTIF 8: Cette interface definit le contrat de base pour tous les comptes.
 * Elle est implementee par le compte de base (ConcreteComponent) et
 * par le decorateur abstrait (Decorator).
 *
 * POURQUOI DECORATOR?
 * - Ajouter des fonctionnalites dynamiquement aux comptes
 * - Combiner plusieurs fonctionnalites de maniere flexible
 * - Respecter le principe Open/Closed (ouvert a l'extension, ferme a la modification)
 * - Alternative a l'heritage multiple
 *
 * FONCTIONNALITES DECORABLES:
 * - Interets (epargne, placement)
 * - Decouvert autorise
 * - Assurance compte
 * - Programme de fidelite
 * - Frais de gestion
 * - Notifications automatiques
 */
public interface Account {

    /**
     * Retourne le numero de compte unique.
     */
    String getAccountNumber();

    /**
     * Retourne le nom du titulaire.
     */
    String getAccountHolder();

    /**
     * Retourne le solde actuel.
     */
    BigDecimal getBalance();

    /**
     * Effectue un depot.
     *
     * @param amount Montant a deposer
     * @return Resultat de l'operation
     */
    TransactionResult deposit(BigDecimal amount);

    /**
     * Effectue un retrait.
     *
     * @param amount Montant a retirer
     * @return Resultat de l'operation
     */
    TransactionResult withdraw(BigDecimal amount);

    /**
     * Effectue un transfert vers un autre compte.
     *
     * @param target Compte destinataire
     * @param amount Montant a transferer
     * @return Resultat de l'operation
     */
    TransactionResult transfer(Account target, BigDecimal amount);

    /**
     * Retourne la description du compte avec toutes ses fonctionnalites.
     */
    String getDescription();

    /**
     * Retourne le type de compte.
     */
    AccountType getAccountType();

    /**
     * Retourne l'historique des transactions.
     */
    List<Transaction> getTransactionHistory();

    /**
     * Calcule les frais mensuels totaux.
     */
    BigDecimal getMonthlyFees();

    /**
     * Calcule les bonus/interets mensuels.
     */
    BigDecimal getMonthlyBonus();

    /**
     * Retourne la limite de retrait.
     */
    BigDecimal getWithdrawalLimit();

    /**
     * Verifie si le compte est actif.
     */
    boolean isActive();

    // ==================== TYPES ET CLASSES INTERNES ====================

    /**
     * Types de comptes.
     */
    enum AccountType {
        COURANT("Compte Courant", "CC"),
        EPARGNE("Compte Epargne", "CE"),
        PROFESSIONNEL("Compte Professionnel", "CP"),
        JEUNE("Compte Jeune", "CJ");

        private final String label;
        private final String code;

        AccountType(String label, String code) {
            this.label = label;
            this.code = code;
        }

        public String getLabel() { return label; }
        public String getCode() { return code; }
    }

    /**
     * Resultat d'une transaction.
     */
    class TransactionResult {
        private final boolean success;
        private final String transactionId;
        private final String message;
        private final BigDecimal amount;
        private final BigDecimal fees;
        private final BigDecimal newBalance;
        private final LocalDateTime timestamp;

        public TransactionResult(boolean success, String transactionId, String message,
                                  BigDecimal amount, BigDecimal fees, BigDecimal newBalance) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
            this.amount = amount;
            this.fees = fees;
            this.newBalance = newBalance;
            this.timestamp = LocalDateTime.now();
        }

        public static TransactionResult success(String id, String msg, BigDecimal amount,
                                                 BigDecimal fees, BigDecimal balance) {
            return new TransactionResult(true, id, msg, amount, fees, balance);
        }

        public static TransactionResult failure(String id, String msg) {
            return new TransactionResult(false, id, msg, BigDecimal.ZERO, BigDecimal.ZERO, null);
        }

        public boolean isSuccess() { return success; }
        public String getTransactionId() { return transactionId; }
        public String getMessage() { return message; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getFees() { return fees; }
        public BigDecimal getNewBalance() { return newBalance; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            if (success) {
                return String.format("Transaction[id=%s, montant=%.0f, frais=%.0f, solde=%.0f]",
                    transactionId, amount, fees, newBalance);
            }
            return String.format("Transaction[ECHEC, id=%s, raison=%s]", transactionId, message);
        }
    }

    /**
     * Transaction enregistree.
     */
    class Transaction {
        private final String id;
        private final TransactionType type;
        private final BigDecimal amount;
        private final BigDecimal fees;
        private final BigDecimal balanceAfter;
        private final String description;
        private final LocalDateTime timestamp;

        public Transaction(String id, TransactionType type, BigDecimal amount,
                          BigDecimal fees, BigDecimal balanceAfter, String description) {
            this.id = id;
            this.type = type;
            this.amount = amount;
            this.fees = fees;
            this.balanceAfter = balanceAfter;
            this.description = description;
            this.timestamp = LocalDateTime.now();
        }

        public String getId() { return id; }
        public TransactionType getType() { return type; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getFees() { return fees; }
        public BigDecimal getBalanceAfter() { return balanceAfter; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("%s: %.0f FCFA (frais: %.0f) - %s",
                type, amount, fees, description);
        }
    }

    /**
     * Types de transactions.
     */
    enum TransactionType {
        DEPOT("Depot"),
        RETRAIT("Retrait"),
        TRANSFERT_EMIS("Transfert emis"),
        TRANSFERT_RECU("Transfert recu"),
        FRAIS("Frais"),
        INTERET("Interet"),
        BONUS("Bonus");

        private final String label;

        TransactionType(String label) {
            this.label = label;
        }

        public String getLabel() { return label; }
    }
}
