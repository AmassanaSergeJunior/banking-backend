package com.banque.controller.dto;

import com.banque.domain.enums.AuthenticationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO pour les requêtes d'authentification REST.
 */
@Data
public class AuthenticationRequest {

    @NotBlank(message = "L'identifiant utilisateur est requis")
    private String userId;

    @NotNull(message = "Le type d'authentification est requis")
    private AuthenticationType authenticationType;

    // Pour authentification PASSWORD
    private String password;

    // Pour authentification OTP
    private String otpCode;

    // Pour authentification FINGERPRINT
    private String fingerprintData;

    // Pour authentification FACIAL_RECOGNITION
    private String facialData;

    // Pour authentification AUGMENTED_REALITY
    private String arData;
}
