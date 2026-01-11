package com.banque.factory.operator;

import com.banque.domain.enums.OperatorType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * FACTORY PROVIDER - Sélecteur de factory d'opérateur
 *
 * OBJECTIF 2: Cette classe permet de basculer d'un opérateur A vers un
 * opérateur B avec un minimum de modifications.
 *
 * Le code client utilise uniquement cette classe pour obtenir la factory
 * appropriée, sans connaître les implémentations concrètes.
 *
 * UTILISATION:
 * <pre>
 *     // Obtenir la factory pour un opérateur
 *     OperatorFactory factory = provider.getFactory(OperatorType.BANK);
 *
 *     // Créer les objets de la famille
 *     AccountValidator validator = factory.createAccountValidator();
 *     RateCalculator calculator = factory.createRateCalculator();
 *     // ... tous les objets sont cohérents entre eux
 *
 *     // Basculer vers un autre opérateur = juste changer le type
 *     OperatorFactory mobileFactory = provider.getFactory(OperatorType.MOBILE_MONEY);
 * </pre>
 */
@Component
public class OperatorFactoryProvider {

    private final Map<OperatorType, OperatorFactory> factories;

    /**
     * Constructeur avec injection automatique de toutes les factories.
     * Spring détecte et injecte toutes les implémentations de OperatorFactory.
     *
     * @param factoryList Liste des factories disponibles
     */
    public OperatorFactoryProvider(List<OperatorFactory> factoryList) {
        this.factories = new EnumMap<>(OperatorType.class);

        for (OperatorFactory factory : factoryList) {
            factories.put(factory.getOperatorType(), factory);
        }
    }

    /**
     * Obtient la factory pour un type d'opérateur donné.
     *
     * @param operatorType Le type d'opérateur
     * @return La factory correspondante
     * @throws IllegalArgumentException si le type n'est pas supporté
     */
    public OperatorFactory getFactory(OperatorType operatorType) {
        OperatorFactory factory = factories.get(operatorType);

        if (factory == null) {
            throw new IllegalArgumentException(
                "Opérateur non supporté: " + operatorType +
                ". Types disponibles: " + factories.keySet()
            );
        }

        return factory;
    }

    /**
     * Vérifie si un type d'opérateur est supporté.
     *
     * @param operatorType Le type à vérifier
     * @return true si supporté
     */
    public boolean isSupported(OperatorType operatorType) {
        return factories.containsKey(operatorType);
    }

    /**
     * Retourne la liste des types d'opérateurs disponibles.
     *
     * @return Liste des types supportés
     */
    public List<OperatorType> getAvailableOperatorTypes() {
        return List.copyOf(factories.keySet());
    }

    /**
     * Retourne toutes les factories disponibles.
     *
     * @return Liste des factories
     */
    public List<OperatorFactory> getAllFactories() {
        return List.copyOf(factories.values());
    }
}
