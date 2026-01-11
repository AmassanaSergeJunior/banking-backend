package com.banque.security.strategy.impl;

import com.banque.domain.enums.AuthenticationType;
import com.banque.security.strategy.AuthenticationCredentials;
import com.banque.security.strategy.AuthenticationResult;
import com.banque.security.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * STRATEGY CONCRETE: Authentification par OTP (One-Time Password)
 *
 * Implémente la stratégie d'authentification par code à usage unique.
 * Le code OTP est généralement envoyé par SMS ou email.
 */
@Component
public class OTPAuthenticationStrategy implements AuthenticationStrategy {

    // Stockage temporaire des OTP générés (en production: utiliser Redis ou cache)
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();

    @Override
    public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
        // Validation des données requises
        if (credentials.getUserId() == null || credentials.getUserId().isEmpty()) {
            return AuthenticationResult.failure(
                "Identifiant utilisateur requis",
                AuthenticationType.OTP
            );
        }

        if (credentials.getOtpCode() == null || credentials.getOtpCode().isEmpty()) {
            return AuthenticationResult.failure(
                "Code OTP requis",
                AuthenticationType.OTP
            );
        }

        // Vérification du code OTP
        boolean isValid = validateOTP(credentials.getUserId(), credentials.getOtpCode());

        if (isValid) {
            // Supprimer l'OTP après utilisation (usage unique)
            otpStore.remove(credentials.getUserId());

            String token = generateToken(credentials.getUserId());
            return AuthenticationResult.success(
                credentials.getUserId(),
                AuthenticationType.OTP,
                token
            );
        } else {
            return AuthenticationResult.failure(
                "Code OTP invalide ou expiré",
                AuthenticationType.OTP
            );
        }
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.OTP;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials.getAuthenticationType() == AuthenticationType.OTP
            && credentials.getOtpCode() != null;
    }

    /**
     * Génère et stocke un nouveau code OTP pour un utilisateur.
     * En production, ce code serait envoyé par SMS via le service de notification.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Le code OTP généré (6 chiffres)
     */
    public String generateOTP(String userId) {
        // Génère un code à 6 chiffres
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        otpStore.put(userId, otp);
        return otp;
    }

    /**
     * Valide le code OTP fourni.
     */
    private boolean validateOTP(String userId, String otpCode) {
        String storedOtp = otpStore.get(userId);
        return storedOtp != null && storedOtp.equals(otpCode);
    }

    /**
     * Génère un token d'accès.
     */
    private String generateToken(String userId) {
        return "OTP-" + UUID.randomUUID().toString();
    }
}
