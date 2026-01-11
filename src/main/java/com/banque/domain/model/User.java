package com.banque.domain.model;

import com.banque.domain.enums.AuthenticationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité représentant un utilisateur du système.
 *
 * Chaque utilisateur peut avoir une méthode d'authentification préférée
 * qui sera utilisée par défaut lors de la connexion.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true)
    private String telephone;

    /**
     * Mot de passe hashé (pour authentification PASSWORD)
     */
    @Column(name = "password_hash")
    private String passwordHash;

    /**
     * Template d'empreinte digitale encodé (pour authentification FINGERPRINT)
     */
    @Column(name = "fingerprint_template", columnDefinition = "TEXT")
    private String fingerprintTemplate;

    /**
     * Données de reconnaissance faciale encodées (pour FACIAL_RECOGNITION)
     */
    @Column(name = "facial_data", columnDefinition = "TEXT")
    private String facialData;

    /**
     * Pattern AR de l'utilisateur (pour AUGMENTED_REALITY)
     */
    @Column(name = "ar_pattern", columnDefinition = "TEXT")
    private String arPattern;

    /**
     * Méthode d'authentification préférée de l'utilisateur
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_auth_method")
    private AuthenticationType preferredAuthMethod;

    /**
     * Indique si l'utilisateur est actif
     */
    @Column(nullable = false)
    private boolean actif = true;

    /**
     * Date de création du compte
     */
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    /**
     * Date de dernière connexion
     */
    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    /**
     * Référence vers l'opérateur auquel appartient l'utilisateur
     */
    @Column(name = "operator_id")
    private Long operatorId;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        if (preferredAuthMethod == null) {
            preferredAuthMethod = AuthenticationType.PASSWORD;
        }
    }
}
