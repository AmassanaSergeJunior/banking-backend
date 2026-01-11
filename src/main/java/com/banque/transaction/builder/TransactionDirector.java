package com.banque.transaction.builder;

import com.banque.transaction.model.Commission;
import com.banque.transaction.model.Transaction;
import com.banque.transaction.model.TransactionBuilder;
import com.banque.transaction.model.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * PATTERN BUILDER - Director
 *
 * OBJECTIF 3: Le Director utilise le Builder pour construire des variantes
 * prédéfinies de transactions sans dupliquer le code.
 *
 * Variantes disponibles:
 * - Transaction courte (simple, sans vérifications)
 * - Transaction complète (toutes les étapes)
 * - Transfert inter-opérateurs
 * - Transfert international
 * - Dépôt simple
 * - Retrait simple
 */
@Component
public class TransactionDirector {

    // ==================== VARIANTE 1: TRANSACTION COURTE ====================

    /**
     * Construit une transaction courte/simple.
     *
     * Caractéristiques:
     * - Pas de vérification supplémentaire
     * - Pas de logging détaillé
     * - Commission minimale
     * - Notification simple
     *
     * Idéal pour: petits montants, transferts internes rapides
     */
    public Transaction buildQuickTransaction(
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount) {

        return new TransactionBuilder()
            .type(TransactionType.TRANSFER_INTERNAL)
            .from(sourceAccount)
            .to(destinationAccount)
            .amount(amount)
            .withCommission(Commission.transferFee())
            .withNotification()
            .description("Transfert rapide")
            .build();
    }

    /**
     * Construit une transaction courte avec type personnalisé.
     */
    public Transaction buildQuickTransaction(
            TransactionType type,
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount) {

        return new TransactionBuilder()
            .type(type)
            .from(sourceAccount)
            .to(destinationAccount)
            .amount(amount)
            .withCommission(Commission.transferFee())
            .withNotification()
            .build();
    }

    // ==================== VARIANTE 2: TRANSACTION COMPLETE ====================

    /**
     * Construit une transaction complète avec toutes les étapes.
     *
     * Caractéristiques:
     * - Vérification du compte et du solde
     * - Vérification anti-fraude
     * - Commissions multiples
     * - Logging détaillé
     * - Notifications complètes
     *
     * Idéal pour: gros montants, transferts sensibles
     */
    public Transaction buildFullTransaction(
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount) {

        return new TransactionBuilder()
            .type(TransactionType.TRANSFER_INTERNAL)
            .from(sourceAccount)
            .to(destinationAccount)
            .amount(amount)
            .withVerification()
            .withFraudCheck()
            .withCommissions(
                Commission.transferFee(),
                Commission.serviceFee(200)
            )
            .withLogging()
            .withNotification()
            .description("Transfert complet sécurisé")
            .build();
    }

    // ==================== VARIANTE 3: TRANSFERT INTER-OPERATEURS ====================

    /**
     * Construit un transfert entre opérateurs différents.
     *
     * Caractéristiques:
     * - Vérification complète
     * - Commission inter-opérateurs
     * - Logging pour traçabilité
     * - Notifications aux deux parties
     */
    public Transaction buildInterOperatorTransfer(
            String sourceAccount,
            String sourceOperator,
            String destinationAccount,
            String destinationOperator,
            BigDecimal amount) {

        return new TransactionBuilder()
            .type(TransactionType.TRANSFER_INTER_OPERATOR)
            .from(sourceAccount, sourceOperator)
            .to(destinationAccount, destinationOperator)
            .amount(amount)
            .withVerification()
            .withFraudCheck()
            .withCommission(Commission.interOperatorFee())
            .withLogging()
            .withNotification()
            .description("Transfert de " + sourceOperator + " vers " + destinationOperator)
            .build();
    }

    // ==================== VARIANTE 4: TRANSFERT INTERNATIONAL ====================

    /**
     * Construit un transfert international avec conversion de devise.
     *
     * Caractéristiques:
     * - Toutes les vérifications de sécurité
     * - Conversion de devise
     * - Commissions internationales
     * - Logging complet
     * - Notifications détaillées
     */
    public Transaction buildInternationalTransfer(
            String sourceAccount,
            String destinationAccount,
            BigDecimal amount,
            String sourceCurrency,
            String targetCurrency,
            BigDecimal exchangeRate) {

        return new TransactionBuilder()
            .type(TransactionType.TRANSFER_INTERNATIONAL)
            .from(sourceAccount)
            .to(destinationAccount)
            .amount(amount)
            .currency(sourceCurrency)
            .withVerification()
            .withFraudCheck()
            .withCurrencyConversion(targetCurrency, exchangeRate)
            .withCommissions(
                Commission.internationalFee(),
                Commission.builder("Frais de change")
                    .percentage(0.5)
                    .minimum(500)
                    .build()
            )
            .withLogging()
            .withNotification()
            .description("Transfert international " + sourceCurrency + " -> " + targetCurrency)
            .build();
    }

    // ==================== VARIANTE 5: DEPOT SIMPLE ====================

    /**
     * Construit un dépôt simple sur un compte.
     */
    public Transaction buildDeposit(String account, BigDecimal amount) {
        return new TransactionBuilder()
            .type(TransactionType.DEPOSIT)
            .from(account)
            .to(account)
            .amount(amount)
            .withLogging()
            .withNotification()
            .description("Dépôt sur compte")
            .build();
    }

    /**
     * Construit un dépôt avec vérification (gros montants).
     */
    public Transaction buildVerifiedDeposit(String account, BigDecimal amount) {
        return new TransactionBuilder()
            .type(TransactionType.DEPOSIT)
            .from(account)
            .to(account)
            .amount(amount)
            .withVerification()
            .withFraudCheck()
            .withLogging()
            .withNotification()
            .description("Dépôt vérifié")
            .build();
    }

    // ==================== VARIANTE 6: RETRAIT SIMPLE ====================

    /**
     * Construit un retrait simple.
     */
    public Transaction buildWithdrawal(String account, BigDecimal amount) {
        return new TransactionBuilder()
            .type(TransactionType.WITHDRAWAL)
            .from(account)
            .amount(amount)
            .withVerification()
            .withCommission("Frais de retrait", 0.5)
            .withLogging()
            .withNotification()
            .description("Retrait")
            .build();
    }

    // ==================== VARIANTE 7: PAIEMENT DE FACTURE ====================

    /**
     * Construit un paiement de facture.
     */
    public Transaction buildBillPayment(
            String sourceAccount,
            String merchantAccount,
            BigDecimal amount,
            String billReference) {

        return new TransactionBuilder()
            .type(TransactionType.BILL_PAYMENT)
            .from(sourceAccount)
            .to(merchantAccount)
            .amount(amount)
            .reference(billReference)
            .withVerification()
            .withFixedCommission("Frais de paiement", 100)
            .withLogging()
            .withNotification()
            .description("Paiement facture: " + billReference)
            .build();
    }

    // ==================== METHODES UTILITAIRES ====================

    /**
     * Crée un nouveau builder pré-configuré pour les transferts.
     */
    public TransactionBuilder transferBuilder() {
        return new TransactionBuilder()
            .type(TransactionType.TRANSFER_INTERNAL)
            .withVerification()
            .withLogging()
            .withNotification();
    }

    /**
     * Crée un nouveau builder pré-configuré pour les paiements.
     */
    public TransactionBuilder paymentBuilder() {
        return new TransactionBuilder()
            .type(TransactionType.PAYMENT)
            .withVerification()
            .withCommission(Commission.serviceFee(100))
            .withLogging()
            .withNotification();
    }
}
