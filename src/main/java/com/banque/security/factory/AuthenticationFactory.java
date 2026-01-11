package com.banque.security.factory;

import com.banque.domain.enums.AuthenticationType;
import com.banque.security.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * PATTERN FACTORY - Fabrique de stratégies d'authentification
 *
 * OBJECTIF 1: Cette factory permet de créer/obtenir la bonne stratégie
 * d'authentification à l'exécution, sans que le code client connaisse
 * les implémentations concrètes.
 *
 * POURQUOI FACTORY?
 * - Centralise la création des objets stratégie
 * - Découple le code client des classes concrètes
 * - Facilite l'ajout de nouvelles méthodes d'authentification
 * - Permet une sélection dynamique basée sur la configuration
 *
 * INTEGRATION SPRING BOOT:
 * - Utilise l'injection de dépendances pour récupérer toutes les stratégies
 * - Les stratégies sont automatiquement enregistrées grâce à @Component
 */
@Component
public class AuthenticationFactory {

    private final Map<AuthenticationType, AuthenticationStrategy> strategies;

    /**
     * Constructeur avec injection automatique de toutes les stratégies.
     * Spring injecte automatiquement toutes les implémentations de AuthenticationStrategy.
     *
     * @param strategyList Liste de toutes les stratégies disponibles (injectées par Spring)
     */
    public AuthenticationFactory(List<AuthenticationStrategy> strategyList) {
        this.strategies = new EnumMap<>(AuthenticationType.class);

        // Enregistrer chaque stratégie par son type
        for (AuthenticationStrategy strategy : strategyList) {
            strategies.put(strategy.getType(), strategy);
        }
    }

    /**
     * Obtient la stratégie d'authentification correspondant au type demandé.
     *
     * @param type Le type d'authentification souhaité
     * @return La stratégie correspondante
     * @throws IllegalArgumentException si le type n'est pas supporté
     */
    public AuthenticationStrategy getStrategy(AuthenticationType type) {
        AuthenticationStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException(
                "Méthode d'authentification non supportée: " + type
            );
        }

        return strategy;
    }

    /**
     * Vérifie si un type d'authentification est supporté.
     *
     * @param type Le type à vérifier
     * @return true si le type est supporté
     */
    public boolean isSupported(AuthenticationType type) {
        return strategies.containsKey(type);
    }

    /**
     * Retourne la liste des types d'authentification disponibles.
     *
     * @return Les types supportés
     */
    public List<AuthenticationType> getAvailableTypes() {
        return List.copyOf(strategies.keySet());
    }

    /**
     * Enregistre une nouvelle stratégie d'authentification.
     * Permet d'ajouter dynamiquement de nouvelles méthodes.
     *
     * @param strategy La stratégie à enregistrer
     */
    public void registerStrategy(AuthenticationStrategy strategy) {
        strategies.put(strategy.getType(), strategy);
    }
}
