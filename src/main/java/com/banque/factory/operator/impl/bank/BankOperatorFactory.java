package com.banque.factory.operator.impl.bank;

import com.banque.domain.enums.OperatorType;
import com.banque.factory.operator.OperatorFactory;
import com.banque.factory.operator.products.AccountValidator;
import com.banque.factory.operator.products.ExternalSystemAdapter;
import com.banque.factory.operator.products.NotificationModule;
import com.banque.factory.operator.products.RateCalculator;
import org.springframework.stereotype.Component;

/**
 * CONCRETE FACTORY: Fabrique pour Banque Traditionnelle
 *
 * Cette factory crée une famille cohérente d'objets pour une banque:
 * - Validateur strict avec dépôt minimum élevé
 * - Calculateur avec frais fixes + pourcentage
 * - Notifications formelles par SMS/Email
 * - Connexion SWIFT/BEAC
 */
@Component
public class BankOperatorFactory implements OperatorFactory {

    private static final String OPERATOR_NAME = "Banque Traditionnelle";

    @Override
    public AccountValidator createAccountValidator() {
        return new BankAccountValidator();
    }

    @Override
    public RateCalculator createRateCalculator() {
        return new BankRateCalculator();
    }

    @Override
    public NotificationModule createNotificationModule() {
        return new BankNotificationModule();
    }

    @Override
    public ExternalSystemAdapter createExternalSystemAdapter() {
        return new BankExternalSystemAdapter();
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.BANK;
    }

    @Override
    public String getOperatorName() {
        return OPERATOR_NAME;
    }
}
