package com.banque.visitor;

/**
 * PATTERN VISITOR - Visitor Interface
 *
 * OBJECTIF 9: Interface definissant les methodes de visite
 * pour chaque type d'element concret.
 *
 * Chaque visiteur concret implemente cette interface pour
 * effectuer une operation specifique sur les elements.
 */
public interface TransactionVisitor {

    /**
     * Visite un depot.
     */
    void visit(DepotElement depot);

    /**
     * Visite un retrait.
     */
    void visit(RetraitElement retrait);

    /**
     * Visite un transfert.
     */
    void visit(TransfertElement transfert);

    /**
     * Visite un paiement.
     */
    void visit(PaiementElement paiement);

    /**
     * Visite des frais.
     */
    void visit(FraisElement frais);

    /**
     * Visite un remboursement.
     */
    void visit(RemboursementElement remboursement);

    /**
     * Retourne le nom du visiteur.
     */
    String getVisitorName();

    /**
     * Reinitialise le visiteur.
     */
    void reset();
}
