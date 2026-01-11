package com.banque.security.strategy;

import com.banque.domain.enums.AuthenticationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Résultat d'une tentative d'authentification.
 *
 * Contient les informations sur le succès ou l'échec de l'authentification,
 * ainsi que les métadonnées associées.
 */
@Data
@Builder
public class AuthenticationResult {

    /**
     * Indique si l'authentification a réussi
     */
    private boolean success;

    /**
     * Message décrivant le résultat (succès ou raison de l'échec)
     */
    private String message;

    /**
     * Identifiant de l'utilisateur authentifié
     */
    private String userId;

    /**
     * Type d'authentification utilisé
     */
    private AuthenticationType authenticationType;

    /**
     * Token d'accès généré en cas de succès
     */
    private String accessToken;

    /**
     * Date et heure de l'authentification
     */
    private LocalDateTime timestamp;

    /**
     * Durée de validité du token en secondes
     */
    private Long tokenValidity;

    /**
     * Crée un résultat de succès
     */
    public static AuthenticationResult success(String userId, AuthenticationType type, String token) {
        return AuthenticationResult.builder()
                .success(true)
                .message("Authentification réussie")
                .userId(userId)
                .authenticationType(type)
                .accessToken(token)
                .timestamp(LocalDateTime.now())
                .tokenValidity(3600L) // 1 heure par défaut
                .build();
    }

    /**
     * Crée un résultat d'échec
     */
    public static AuthenticationResult failure(String message, AuthenticationType type) {
        return AuthenticationResult.builder()
                .success(false)
                .message(message)
                .authenticationType(type)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
