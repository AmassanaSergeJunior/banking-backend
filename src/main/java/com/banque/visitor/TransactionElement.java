package com.banque.visitor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PATTERN VISITOR - Element Interface
 *
 * OBJECTIF 9: Interface que tous les elements visitables doivent implementer.
 * Definit la methode accept() qui recoit un visiteur.
 *
 * POURQUOI VISITOR?
 * - Separer les algorithmes de la structure des objets
 * - Ajouter de nouvelles operations sans modifier les classes existantes
 * - Regrouper les operations liees dans des visiteurs
 * - Utile pour les rapports, analytics, exports
 */
public interface TransactionElement {

    /**
     * Accepte un visiteur pour traiter cet element.
     */
    void accept(TransactionVisitor visitor);

    /**
     * Retourne le type d'element.
     */
    ElementType getElementType();

    /**
     * Retourne le montant associe.
     */
    BigDecimal getAmount();

    /**
     * Retourne la date de creation.
     */
    LocalDateTime getTimestamp();

    /**
     * Retourne l'identifiant unique.
     */
    String getId();

    /**
     * Types d'elements.
     */
    enum ElementType {
        DEPOT("Depot", "DEP"),
        RETRAIT("Retrait", "RET"),
        TRANSFERT("Transfert", "TRF"),
        PAIEMENT("Paiement", "PAY"),
        FRAIS("Frais", "FEE"),
        REMBOURSEMENT("Remboursement", "RMB");

        private final String label;
        private final String code;

        ElementType(String label, String code) {
            this.label = label;
            this.code = code;
        }

        public String getLabel() { return label; }
        public String getCode() { return code; }
    }
}
