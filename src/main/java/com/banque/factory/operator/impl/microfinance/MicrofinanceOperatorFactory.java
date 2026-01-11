package com.banque.factory.operator.impl.microfinance;

import com.banque.domain.enums.OperatorType;
import com.banque.factory.operator.OperatorFactory;
import com.banque.factory.operator.products.AccountValidator;
import com.banque.factory.operator.products.ExternalSystemAdapter;
import com.banque.factory.operator.products.NotificationModule;
import com.banque.factory.operator.products.RateCalculator;
import org.springframework.stereotype.Component;

/**
 * CONCRETE FACTORY: Fabrique pour Microfinance
 *
 * Cette factory crée une famille cohérente d'objets pour la microfinance:
 * - Validateur accessible (dépôt minimum bas)
 * - Calculateur social (petites transactions gratuites)
 * - Notifications personnalisées et chaleureuses
 * - Connexion au réseau des caisses partenaires
 */
@Component
public class MicrofinanceOperatorFactory implements OperatorFactory {

    private static final String OPERATOR_NAME = "Microfinance";

    @Override
    public AccountValidator createAccountValidator() {
        return new MicrofinanceAccountValidator();
    }

    @Override
    public RateCalculator createRateCalculator() {
        return new MicrofinanceRateCalculator();
    }

    @Override
    public NotificationModule createNotificationModule() {
        return new MicrofinanceNotificationModule();
    }

    @Override
    public ExternalSystemAdapter createExternalSystemAdapter() {
        return new MicrofinanceExternalSystemAdapter();
    }

    @Override
    public OperatorType getOperatorType() {
        return OperatorType.MICROFINANCE;
    }

    @Override
    public String getOperatorName() {
        return OPERATOR_NAME;
    }
}
