package com.banque.factory.operator.impl.mobilemoney;

import com.banque.domain.enums.OperatorType;
import com.banque.factory.operator.OperatorFactory;
import com.banque.factory.operator.products.AccountValidator;
import com.banque.factory.operator.products.ExternalSystemAdapter;
import com.banque.factory.operator.products.NotificationModule;
import com.banque.factory.operator.products.RateCalculator;
import org.springframework.stereotype.Component;

/**
 * CONCRETE FACTORY: Fabrique pour Mobile Money
 *
 * Cette factory crée une famille cohérente d'objets pour Mobile Money:
 * - Validateur simple (numéro de téléphone)
 * - Calculateur par paliers
 * - Notifications SMS instantanées avec USSD
 * - API Telecom REST
 */
@Component
public class MobileMoneyOperatorFactory implements OperatorFactory {

    private static final String OPERATOR_NAME = "Mobile Money";

    @Override
    public AccountValidator createAccountValidator() {
        return new MobileMoneyAccountValidator();
    }

    @Override
    public RateCalculator createRateCalculator() {
        return new MobileMoneyRateCalculator();
    }

    @Override
    public NotificationModule createNotificationModule() {
        return new MobileMoneyNotificationModule();
    }

    @Override
    public ExternalSystemAdapter createExternalSystemAdapter() {
        return new MobileMoneyExternalSystemAdapter();
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.MOBILE_MONEY;
    }

    @Override
    public String getOperatorName() {
        return OPERATOR_NAME;
    }
}
