package com.banque.observer;

import java.math.BigDecimal;
import java.util.*;

/**
 * PATTERN OBSERVER - ConcreteObserver (Statistiques)
 *
 * OBJECTIF 10: Observateur qui collecte des statistiques en temps reel.
 */
public class StatisticsObserver implements EventObserver {

    private int totalEvents;
    private Map<BankEvent.EventType, Integer> eventCounts;
    private Map<BankEvent.EventSeverity, Integer> severityCounts;
    private BigDecimal totalTransactionAmount;
    private int transactionCount;

    public StatisticsObserver() {
        reset();
    }

    public void reset() {
        totalEvents = 0;
        eventCounts = new EnumMap<>(BankEvent.EventType.class);
        severityCounts = new EnumMap<>(BankEvent.EventSeverity.class);
        totalTransactionAmount = BigDecimal.ZERO;
        transactionCount = 0;

        for (BankEvent.EventType type : BankEvent.EventType.values()) {
            eventCounts.put(type, 0);
        }
        for (BankEvent.EventSeverity sev : BankEvent.EventSeverity.values()) {
            severityCounts.put(sev, 0);
        }
    }

    @Override
    public void onEvent(BankEvent event) {
        totalEvents++;

        // Compter par type
        eventCounts.merge(event.getEventType(), 1, Integer::sum);

        // Compter par severite
        severityCounts.merge(event.getSeverity(), 1, Integer::sum);

        // Aggreger les montants de transactions
        BigDecimal amount = event.getAmount();
        if (amount != null) {
            totalTransactionAmount = totalTransactionAmount.add(amount);
            transactionCount++;
        }

        System.out.println("  [STATS] Evenement #" + totalEvents + " (" +
            event.getEventType() + ")");
    }

    @Override
    public String getObserverName() {
        return "StatisticsObserver";
    }

    // ==================== RESULTATS ====================

    public int getTotalEvents() {
        return totalEvents;
    }

    public Map<BankEvent.EventType, Integer> getEventCounts() {
        return new EnumMap<>(eventCounts);
    }

    public Map<BankEvent.EventSeverity, Integer> getSeverityCounts() {
        return new EnumMap<>(severityCounts);
    }

    public BigDecimal getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public BigDecimal getAverageTransactionAmount() {
        if (transactionCount == 0) return BigDecimal.ZERO;
        return totalTransactionAmount.divide(
            new BigDecimal(transactionCount), 0, java.math.RoundingMode.HALF_UP);
    }

    public int getCountForType(BankEvent.EventType type) {
        return eventCounts.getOrDefault(type, 0);
    }

    public int getCountForSeverity(BankEvent.EventSeverity severity) {
        return severityCounts.getOrDefault(severity, 0);
    }

    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== STATISTIQUES EN TEMPS REEL ===\n");
        sb.append("Total evenements: ").append(totalEvents).append("\n");
        sb.append("Transactions: ").append(transactionCount).append("\n");
        sb.append("Montant total: ").append(totalTransactionAmount).append(" FCFA\n");
        sb.append("Montant moyen: ").append(getAverageTransactionAmount()).append(" FCFA\n");

        sb.append("\nPar severite:\n");
        for (BankEvent.EventSeverity sev : BankEvent.EventSeverity.values()) {
            int count = severityCounts.get(sev);
            if (count > 0) {
                sb.append("  ").append(sev).append(": ").append(count).append("\n");
            }
        }

        return sb.toString();
    }
}
