package com.banque.observer;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

/**
 * PATTERN OBSERVER - Service d'Evenements
 *
 * OBJECTIF 10: Service centralise pour la gestion des evenements.
 */
@Service
public class EventService {

    private final EventPublisher publisher;
    private final LoggingObserver loggingObserver;
    private final NotificationObserver notificationObserver;
    private final SecurityObserver securityObserver;
    private final StatisticsObserver statisticsObserver;

    public EventService() {
        this.publisher = new EventPublisher();

        // Creer les observateurs
        this.loggingObserver = new LoggingObserver();
        this.notificationObserver = new NotificationObserver();
        this.securityObserver = new SecurityObserver();
        this.statisticsObserver = new StatisticsObserver();

        // Enregistrer les observateurs
        publisher.subscribe(loggingObserver);
        publisher.subscribe(notificationObserver);
        publisher.subscribe(securityObserver);
        publisher.subscribe(statisticsObserver);
    }

    // ==================== PUBLICATION D'EVENEMENTS ====================

    public void publishEvent(BankEvent event) {
        publisher.publish(event);
    }

    public void publishDeposit(String account, BigDecimal amount) {
        publisher.publish(BankEvent.depositMade(account, amount));
    }

    public void publishWithdrawal(String account, BigDecimal amount) {
        publisher.publish(BankEvent.withdrawalMade(account, amount));
    }

    public void publishLowBalance(String account, BigDecimal balance, BigDecimal threshold) {
        publisher.publish(BankEvent.lowBalance(account, balance, threshold));
    }

    public void publishLoginSuccess(String userId, String ipAddress) {
        publisher.publish(BankEvent.loginSuccess(userId, ipAddress));
    }

    public void publishLoginFailed(String userId, String reason) {
        publisher.publish(BankEvent.loginFailed(userId, reason));
    }

    public void publishSuspiciousActivity(String account, String description) {
        publisher.publish(BankEvent.suspiciousActivity(account, description));
    }

    public void publishFraudDetected(String account, String details) {
        publisher.publish(BankEvent.fraudDetected(account, details));
    }

    // ==================== GESTION DES CONTACTS ====================

    public void registerContact(String account, String phone, String email) {
        notificationObserver.registerContact(account, phone, email);
    }

    // ==================== ACCES AUX OBSERVATEURS ====================

    public EventPublisher getPublisher() {
        return publisher;
    }

    public LoggingObserver getLoggingObserver() {
        return loggingObserver;
    }

    public NotificationObserver getNotificationObserver() {
        return notificationObserver;
    }

    public SecurityObserver getSecurityObserver() {
        return securityObserver;
    }

    public StatisticsObserver getStatisticsObserver() {
        return statisticsObserver;
    }
}
