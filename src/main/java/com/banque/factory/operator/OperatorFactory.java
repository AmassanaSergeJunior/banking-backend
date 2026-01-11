package com.banque.factory.operator;

import com.banque.domain.enums.OperatorType;
import com.banque.factory.operator.products.AccountValidator;
import com.banque.factory.operator.products.ExternalSystemAdapter;
import com.banque.factory.operator.products.NotificationModule;
import com.banque.factory.operator.products.RateCalculator;

/**
 * PATTERN ABSTRACT FACTORY - Interface de fabrique d'opérateurs
 *
 * OBJECTIF 2: Cette interface définit les méthodes pour créer une famille
 * complète d'objets cohérents pour un opérateur donné.
 *
 * POURQUOI ABSTRACT FACTORY?
 * - Garantit la cohérence des objets créés (un validateur Bank fonctionne
 *   avec un calculateur Bank, pas avec un calculateur MobileMoney)
 * - Permet de basculer facilement d'un opérateur à un autre
 * - Isole le code client des classes concrètes
 * - Respecte le principe de substitution de Liskov
 *
 * FAMILLE DE PRODUITS:
 * - AccountValidator: Valide les comptes et transactions
 * - RateCalculator: Calcule les frais et taux
 * - NotificationModule: Envoie les notifications
 * - ExternalSystemAdapter: Communique avec les systèmes externes
 */
public interface OperatorFactory {

    /**
     * Crée le validateur de compte pour cet opérateur.
     *
     * @return Instance de AccountValidator cohérente avec cet opérateur
     */
    AccountValidator createAccountValidator();

    /**
     * Crée le calculateur de taux pour cet opérateur.
     *
     * @return Instance de RateCalculator cohérente avec cet opérateur
     */
    RateCalculator createRateCalculator();

    /**
     * Crée le module de notification pour cet opérateur.
     *
     * @return Instance de NotificationModule cohérente avec cet opérateur
     */
    NotificationModule createNotificationModule();

    /**
     * Crée l'adaptateur système externe pour cet opérateur.
     *
     * @return Instance de ExternalSystemAdapter cohérente avec cet opérateur
     */
    ExternalSystemAdapter createExternalSystemAdapter();

    /**
     * Retourne le type d'opérateur de cette factory.
     *
     * @return Le type d'opérateur
     */
    OperatorType getOperatorType();

    /**
     * Retourne le nom de l'opérateur.
     *
     * @return Nom de l'opérateur
     */
    String getOperatorName();

    /**
     * Retourne une description de l'opérateur.
     *
     * @return Description
     */
    default String getDescription() {
        return getOperatorType().getDescription();
    }
}
