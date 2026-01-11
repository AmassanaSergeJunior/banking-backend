package com.banque.security.strategy;

import com.banque.domain.enums.AuthenticationType;

/**
 * PATTERN STRATEGY - Interface de stratégie d'authentification
 *
 * OBJECTIF 1: Cette interface définit le contrat commun pour toutes les méthodes
 * d'authentification. Le code client utilise cette interface sans connaître
 * l'implémentation concrète à la compilation.
 *
 * POURQUOI STRATEGY?
 * - Permet de changer l'algorithme d'authentification à l'exécution
 * - Respecte le principe Open/Closed (ouvert à l'extension, fermé à la modification)
 * - Facilite l'ajout de nouvelles méthodes sans modifier le code existant
 * - Élimine les conditionnels complexes (if/else ou switch)
 */
public interface AuthenticationStrategy {

    /**
     * Authentifie un utilisateur avec les credentials fournis.
     *
     * @param credentials Les informations d'authentification (format dépend de l'implémentation)
     * @return Le résultat de l'authentification
     */
    AuthenticationResult authenticate(AuthenticationCredentials credentials);

    /**
     * Retourne le type d'authentification de cette stratégie.
     *
     * @return Le type d'authentification
     */
    AuthenticationType getType();

    /**
     * Vérifie si cette stratégie supporte le type de credentials donné.
     *
     * @param credentials Les credentials à vérifier
     * @return true si les credentials sont supportés
     */
    boolean supports(AuthenticationCredentials credentials);

    /**
     * Retourne une description de cette méthode d'authentification.
     *
     * @return Description textuelle
     */
    default String getDescription() {
        return getType().getDescription();
    }
}
