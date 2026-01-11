package com.banque.template;

import com.banque.template.TransactionProcessor.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * SERVICE - Gestion des Processeurs de Transactions
 *
 * OBJECTIF 6: Ce service centralise l'acces aux differents processeurs
 * et permet de selectionner le bon processeur selon le type d'operateur.
 */
@Service
public class TransactionProcessorService {

    private final Map<String, TransactionProcessor> processors;

    public TransactionProcessorService() {
        this.processors = new HashMap<>();
        initializeProcessors();
    }

    /**
     * Initialise les processeurs pour chaque type d'operateur.
     */
    private void initializeProcessors() {
        processors.put("BANK", new BankTransactionProcessor());
        processors.put("MOBILE_MONEY", new MobileMoneyTransactionProcessor());
        processors.put("MICROFINANCE", new MicrofinanceTransactionProcessor());
    }

    /**
     * Traite une transaction via le processeur approprie.
     */
    public TransactionResult processTransaction(String operatorType, TransactionRequest request) {
        TransactionProcessor processor = processors.get(operatorType.toUpperCase());
        if (processor == null) {
            throw new IllegalArgumentException("Type d'operateur inconnu: " + operatorType);
        }
        return processor.processTransaction(request);
    }

    /**
     * Traite un depot.
     */
    public TransactionResult deposit(String operatorType, String account,
                                     BigDecimal amount, String description) {
        TransactionRequest request = new TransactionRequest(
            account, null, amount, TransactionType.DEPOSIT, "XAF", description
        );
        return processTransaction(operatorType, request);
    }

    /**
     * Traite un retrait.
     */
    public TransactionResult withdraw(String operatorType, String account,
                                      BigDecimal amount, String description) {
        TransactionRequest request = new TransactionRequest(
            account, null, amount, TransactionType.WITHDRAWAL, "XAF", description
        );
        return processTransaction(operatorType, request);
    }

    /**
     * Traite un transfert.
     */
    public TransactionResult transfer(String operatorType, String sourceAccount,
                                      String destAccount, BigDecimal amount, String description) {
        TransactionRequest request = new TransactionRequest(
            sourceAccount, destAccount, amount, TransactionType.TRANSFER, "XAF", description
        );
        return processTransaction(operatorType, request);
    }

    /**
     * Retourne un processeur specifique.
     */
    public TransactionProcessor getProcessor(String operatorType) {
        return processors.get(operatorType.toUpperCase());
    }

    /**
     * Retourne les statistiques de tous les processeurs.
     */
    public Map<String, ProcessorStats> getAllStats() {
        Map<String, ProcessorStats> stats = new HashMap<>();
        for (Map.Entry<String, TransactionProcessor> entry : processors.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().getStats());
        }
        return stats;
    }

    /**
     * Retourne les types d'operateurs disponibles.
     */
    public String[] getAvailableOperatorTypes() {
        return processors.keySet().toArray(new String[0]);
    }

    /**
     * Calcule les frais pour une transaction (sans l'executer).
     */
    public BigDecimal estimateFees(String operatorType, BigDecimal amount, TransactionType type) {
        TransactionProcessor processor = processors.get(operatorType.toUpperCase());
        if (processor == null) {
            throw new IllegalArgumentException("Type d'operateur inconnu: " + operatorType);
        }

        // Utiliser reflexion ou methode publique pour estimer les frais
        // Pour simplifier, on cree une transaction fictive
        if (processor instanceof BankTransactionProcessor) {
            return ((BankTransactionProcessor) processor).getClass()
                .cast(processor).getStats().getTotalFeesCollected();
        }

        // Estimation basique
        return BigDecimal.ZERO;
    }
}
