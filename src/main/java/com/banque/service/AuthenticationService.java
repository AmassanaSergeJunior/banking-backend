package com.banque.service;

import com.banque.domain.enums.AuthenticationType;
import com.banque.domain.model.AuthenticationLog;
import com.banque.domain.model.User;
import com.banque.repository.AuthenticationLogRepository;
import com.banque.repository.UserRepository;
import com.banque.security.context.AuthenticationContext;
import com.banque.security.strategy.AuthenticationCredentials;
import com.banque.security.strategy.AuthenticationResult;
import com.banque.security.strategy.impl.OTPAuthenticationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service métier pour l'authentification.
 *
 * Orchestre le processus d'authentification en utilisant le pattern Strategy
 * via le AuthenticationContext, et journalise les tentatives.
 */
@Service
@Transactional
public class AuthenticationService {

    private final AuthenticationContext authContext;
    private final UserRepository userRepository;
    private final AuthenticationLogRepository logRepository;
    private final OTPAuthenticationStrategy otpStrategy;

    // Nombre max de tentatives échouées avant blocage temporaire
    private static final int MAX_FAILED_ATTEMPTS = 5;
    // Durée de blocage en minutes
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    public AuthenticationService(
            AuthenticationContext authContext,
            UserRepository userRepository,
            AuthenticationLogRepository logRepository,
            OTPAuthenticationStrategy otpStrategy) {
        this.authContext = authContext;
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.otpStrategy = otpStrategy;
    }

    /**
     * Authentifie un utilisateur avec les credentials fournis.
     *
     * @param credentials Les informations d'authentification
     * @param ipAddress Adresse IP de la requête
     * @param userAgent User-Agent du client
     * @return Résultat de l'authentification
     */
    public AuthenticationResult authenticate(
            AuthenticationCredentials credentials,
            String ipAddress,
            String userAgent) {

        // Vérifier si l'utilisateur n'est pas bloqué (trop de tentatives)
        if (isUserLockedOut(credentials.getUserId())) {
            AuthenticationResult result = AuthenticationResult.failure(
                "Compte temporairement bloqué suite à trop de tentatives échouées. " +
                "Réessayez dans " + LOCKOUT_DURATION_MINUTES + " minutes.",
                credentials.getAuthenticationType()
            );
            logAttempt(credentials, result, ipAddress, userAgent);
            return result;
        }

        // Effectuer l'authentification via le contexte (pattern Strategy)
        AuthenticationResult result = authContext.authenticate(credentials);

        // Journaliser la tentative
        logAttempt(credentials, result, ipAddress, userAgent);

        // Si succès, mettre à jour la dernière connexion
        if (result.isSuccess()) {
            updateLastLogin(credentials.getUserId());
        }

        return result;
    }

    /**
     * Génère et envoie un code OTP pour un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Le code OTP (en production, ne pas le retourner, l'envoyer par SMS)
     */
    public String generateAndSendOTP(String userId) {
        String otp = otpStrategy.generateOTP(userId);
        // TODO: Envoyer le code par SMS via le service de notification
        // notificationService.sendSMS(userId, "Votre code OTP: " + otp);
        return otp; // En développement seulement, pour les tests
    }

    /**
     * Retourne les méthodes d'authentification disponibles.
     *
     * @return Liste des types d'authentification
     */
    public List<AuthenticationType> getAvailableMethods() {
        return authContext.getAvailableMethods();
    }

    /**
     * Retourne la méthode préférée d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Le type préféré ou PASSWORD par défaut
     */
    public AuthenticationType getPreferredMethod(String userId) {
        return userRepository.findByEmail(userId)
            .map(User::getPreferredAuthMethod)
            .orElse(AuthenticationType.PASSWORD);
    }

    /**
     * Met à jour la méthode d'authentification préférée d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param method La nouvelle méthode préférée
     */
    public void updatePreferredMethod(String userId, AuthenticationType method) {
        userRepository.findByEmail(userId).ifPresent(user -> {
            user.setPreferredAuthMethod(method);
            userRepository.save(user);
        });
    }

    /**
     * Vérifie si un utilisateur est temporairement bloqué.
     */
    private boolean isUserLockedOut(String userId) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(LOCKOUT_DURATION_MINUTES);
        List<AuthenticationLog> recentFailures =
            logRepository.findByUserIdAndSuccessFalseAndTimestampAfter(userId, since);
        return recentFailures.size() >= MAX_FAILED_ATTEMPTS;
    }

    /**
     * Journalise une tentative d'authentification.
     */
    private void logAttempt(
            AuthenticationCredentials credentials,
            AuthenticationResult result,
            String ipAddress,
            String userAgent) {

        AuthenticationLog log = AuthenticationLog.builder()
            .userId(credentials.getUserId())
            .authType(credentials.getAuthenticationType())
            .success(result.isSuccess())
            .message(result.getMessage())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();

        logRepository.save(log);
    }

    /**
     * Met à jour la date de dernière connexion.
     */
    private void updateLastLogin(String userId) {
        userRepository.findByEmail(userId).ifPresent(user -> {
            user.setDerniereConnexion(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
