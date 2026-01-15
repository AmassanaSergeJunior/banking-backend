package com.banque.observer;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN OBSERVER - ConcreteObserver (Logging)
 *
 * OBJECTIF 10: Observateur qui enregistre tous les evenements dans un log.
 */
public class LoggingObserver implements EventObserver {

    private final List<String> logs;
    private final DateTimeFormatter formatter;
    private BankEvent.EventSeverity minimumSeverity;

    public LoggingObserver() {
        this(BankEvent.EventSeverity.DEBUG);
    }

    public LoggingObserver(BankEvent.EventSeverity minimumSeverity) {
        this.logs = new ArrayList<>();
        this.formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        this.minimumSeverity = minimumSeverity;
    }

    @Override
    public void onEvent(BankEvent event) {
        if (event.getSeverity().ordinal() < minimumSeverity.ordinal()) {
            return;
        }

        String logEntry = formatLogEntry(event);
        logs.add(logEntry);

        // Afficher selon les niveaux
        String prefix = getPrefix(event.getSeverity());
        System.out.println("  [LOG] " + prefix + " " + event.getEventType().getDescription());
    }

    @Override
    public String getObserverName() {
        return "LoggingObserver";
    }

    private String formatLogEntry(BankEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(event.getTimestamp().format(formatter)).append("] ");
        sb.append("[").append(event.getSeverity()).append("] ");
        sb.append("[").append(event.getEventType()).append("] ");
        sb.append(event.getSource()).append(" - ");
        sb.append(event.getEventId());

        if (event.getAccount() != null) {
            sb.append(" | Compte: ").append(event.getAccount());
        }
        if (event.getAmount() != null) {
            sb.append(" | Montant: ").append(event.getAmount()).append(" FCFA");
        }

        return sb.toString();
    }

    private String getPrefix(BankEvent.EventSeverity severity) {
        switch (severity) {
            case CRITICAL: return "[!!!]";
            case ERROR: return "[ERR]";
            case WARNING: return "[WRN]";
            case INFO: return "[INF]";
            default: return "[DBG]";
        }
    }

    public List<String> getLogs() {
        return new ArrayList<>(logs);
    }

    public void clearLogs() {
        logs.clear();
    }

    public int getLogCount() {
        return logs.size();
    }

    public void setMinimumSeverity(BankEvent.EventSeverity severity) {
        this.minimumSeverity = severity;
    }
}
