package com.banque.controller.dto;

import com.banque.domain.enums.AuthenticationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO pour les réponses d'authentification REST.
 */
@Data
@Builder
public class AuthenticationResponse {

    private boolean success;
    private String message;
    private String userId;
    private AuthenticationType authenticationType;
    private String accessToken;
    private LocalDateTime timestamp;
    private Long tokenValiditySeconds;
}
