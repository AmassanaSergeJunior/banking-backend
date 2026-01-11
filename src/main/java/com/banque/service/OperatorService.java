package com.banque.service;

import com.banque.domain.enums.OperatorType;
import com.banque.factory.operator.OperatorFactory;
import com.banque.factory.operator.OperatorFactoryProvider;
import com.banque.factory.operator.products.AccountValidator;
import com.banque.factory.operator.products.ExternalSystemAdapter;
import com.banque.factory.operator.products.NotificationModule;
import com.banque.factory.operator.products.RateCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service métier pour les opérations liées aux opérateurs.
 *
 * OBJECTIF 2: Ce service utilise le pattern Abstract Factory via le
 * OperatorFactoryProvider pour créer des objets cohérents par opérateur.
 *
 * Le basculement d'un opérateur à un autre se fait simplement en changeant
 * le type d'opérateur passé en paramètre.
 */
@Service
public class OperatorService {

    private final OperatorFactoryProvider factoryProvider;

    public OperatorService(OperatorFactoryProvider factoryProvider) {
        this.factoryProvider = factoryProvider;
    }

    /**
     * Valide la création d'un compte pour un opérateur donné.
     */
    public AccountValidator.ValidationResult validateAccountCreation(
            OperatorType operatorType,
            String accountNumber,
            String clientId,
            BigDecimal initialDeposit) {

        OperatorFactory factory = factoryProvider.getFactory(operatorType);
        AccountValidator validator = factory.createAccountValidator();

        return validator.validateAccountCreation(accountNumber, clientId, initialDeposit);
    }

    /**
     * Calcule les frais de transaction pour un opérateur donné.
     */
    public BigDecimal calculateTransactionFee(
            OperatorType operatorType,
            BigDecimal amount,
            String transactionType) {

        OperatorFactory factory = factoryProvider.getFactory(operatorType);
        RateCalculator calculator = factory.createRateCalculator();

        return calculator.calculateTransactionFee(amount, transactionType);
    }

    /**
     * Envoie une notification de transaction via l'opérateur approprié.
     */
    public NotificationModule.NotificationResult sendTransactionNotification(
            OperatorType operatorType,
            String phoneNumber,
            String transactionType,
            BigDecimal amount,
            BigDecimal newBalance) {

        OperatorFactory factory = factoryProvider.getFactory(operatorType);
        NotificationModule notificationModule = factory.createNotificationModule();

        return notificationModule.sendTransactionNotification(
            phoneNumber, transactionType, amount, newBalance
        );
    }

    /**
     * Exécute un transfert externe via l'adaptateur de l'opérateur.
     */
    public ExternalSystemAdapter.TransferResult executeExternalTransfer(
            OperatorType operatorType,
            String destinationAccount,
            BigDecimal amount,
            String reference) {

        OperatorFactory factory = factoryProvider.getFactory(operatorType);
        ExternalSystemAdapter adapter = factory.createExternalSystemAdapter();

        return adapter.executeExternalTransfer(destinationAccount, amount, reference);
    }

    /**
     * Retourne les informations sur un opérateur.
     */
    public OperatorInfo getOperatorInfo(OperatorType operatorType) {
        OperatorFactory factory = factoryProvider.getFactory(operatorType);

        AccountValidator validator = factory.createAccountValidator();
        RateCalculator calculator = factory.createRateCalculator();
        ExternalSystemAdapter adapter = factory.createExternalSystemAdapter();

        return new OperatorInfo(
            factory.getOperatorName(),
            operatorType,
            validator.getMinimumDeposit(),
            calculator.getBaseRate(),
            adapter.getExternalSystemName(),
            adapter.getProtocol()
        );
    }

    /**
     * Retourne les infos de tous les opérateurs disponibles.
     */
    public List<OperatorInfo> getAllOperatorsInfo() {
        return factoryProvider.getAvailableOperatorTypes().stream()
            .map(this::getOperatorInfo)
            .toList();
    }

    /**
     * Classe pour encapsuler les informations d'un opérateur.
     */
    public record OperatorInfo(
        String name,
        OperatorType type,
        BigDecimal minimumDeposit,
        BigDecimal baseRate,
        String externalSystem,
        String protocol
    ) {}
}
