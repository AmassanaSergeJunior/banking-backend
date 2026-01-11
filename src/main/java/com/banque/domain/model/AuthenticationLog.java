package com.banque.domain.model;

import com.banque.domain.enums.AuthenticationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entité pour journaliser les tentatives d'authentification.
 *
 * Permet de tracer toutes les connexions (réussies ou échouées)
 * pour des raisons de sécurité et d'audit.
 */
@Entity
@Table(name = "authentication_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifiant de l'utilisateur (email ou téléphone)
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * Méthode d'authentification utilisée
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false)
    private AuthenticationType authType;

    /**
     * Résultat de la tentative (succès ou échec)
     */
    @Column(nullable = false)
    private boolean success;

    /**
     * Message descriptif (erreur ou confirmation)
     */
    @Column(columnDefinition = "TEXT")
    private String message;

    /**
     * Adresse IP de la requête
     */
    @Column(name = "ip_address")
    private String ipAddress;

    /**
     * User-Agent du navigateur/application
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Date et heure de la tentative
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
