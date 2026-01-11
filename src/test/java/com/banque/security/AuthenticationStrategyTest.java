package com.banque.security;

import com.banque.domain.enums.AuthenticationType;
import com.banque.security.context.AuthenticationContext;
import com.banque.security.factory.AuthenticationFactory;
import com.banque.security.strategy.AuthenticationCredentials;
import com.banque.security.strategy.AuthenticationResult;
import com.banque.security.strategy.AuthenticationStrategy;
import com.banque.security.strategy.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TESTS DE DEMONSTRATION - OBJECTIF 1
 *
 * Ces tests démontrent:
 * 1. Le fonctionnement du pattern Strategy (changement dynamique de méthode)
 * 2. Le pattern Factory (création de la bonne stratégie)
 * 3. L'extensibilité (ajout de la méthode Réalité Augmentée)
 * 4. La substitution dynamique à l'exécution
 */
class AuthenticationStrategyTest {

    private AuthenticationFactory factory;
    private AuthenticationContext context;

    @BeforeEach
    void setUp() {
        // Créer toutes les stratégies
        List<AuthenticationStrategy> strategies = List.of(
            new PasswordAuthenticationStrategy(),
            new OTPAuthenticationStrategy(),
            new FingerprintAuthenticationStrategy(),
            new FacialRecognitionAuthenticationStrategy(),
            new AugmentedRealityAuthenticationStrategy()
        );

        // Initialiser la factory avec toutes les stratégies
        factory = new AuthenticationFactory(strategies);
        context = new AuthenticationContext(factory);
    }

    // ==================== TESTS DU PATTERN STRATEGY ====================

    @Test
    @DisplayName("Test 1: Authentification par mot de passe")
    void testPasswordAuthentication() {
        // GIVEN: credentials avec mot de passe valide
        AuthenticationCredentials credentials = AuthenticationCredentials.builder()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.PASSWORD)
            .password("motdepasse123")
            .build();

        // WHEN: on authentifie via le contexte
        AuthenticationResult result = context.authenticate(credentials);

        // THEN: l'authentification réussit
        assertTrue(result.isSuccess(), "L'authentification par mot de passe devrait réussir");
        assertEquals(AuthenticationType.PASSWORD, result.getAuthenticationType());
        assertNotNull(result.getAccessToken());
        assertTrue(result.getAccessToken().startsWith("PWD-"));
        System.out.println("✓ Authentification PASSWORD réussie - Token: " + result.getAccessToken());
    }

    @Test
    @DisplayName("Test 2: Authentification par OTP")
    void testOTPAuthentication() {
        // GIVEN: on génère un OTP pour l'utilisateur
        OTPAuthenticationStrategy otpStrategy =
            (OTPAuthenticationStrategy) factory.getStrategy(AuthenticationType.OTP);
        String userId = "user@example.com";
        String otp = otpStrategy.generateOTP(userId);

        AuthenticationCredentials credentials = AuthenticationCredentials.builder()
            .userId(userId)
            .authenticationType(AuthenticationType.OTP)
            .otpCode(otp)
            .build();

        // WHEN: on authentifie avec le bon OTP
        AuthenticationResult result = context.authenticate(credentials);

        // THEN: l'authentification réussit
        assertTrue(result.isSuccess(), "L'authentification OTP devrait réussir");
        assertEquals(AuthenticationType.OTP, result.getAuthenticationType());
        assertTrue(result.getAccessToken().startsWith("OTP-"));
        System.out.println("✓ Authentification OTP réussie - Token: " + result.getAccessToken());
    }

    @Test
    @DisplayName("Test 3: Authentification par empreinte digitale")
    void testFingerprintAuthentication() {
        // GIVEN: credentials avec données d'empreinte
        AuthenticationCredentials credentials = AuthenticationCredentials.builder()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.FINGERPRINT)
            .fingerprintData("base64EncodedFingerprintDataHere")
            .build();

        // WHEN: on authentifie
        AuthenticationResult result = context.authenticate(credentials);

        // THEN: l'authentification réussit
        assertTrue(result.isSuccess(), "L'authentification par empreinte devrait réussir");
        assertEquals(AuthenticationType.FINGERPRINT, result.getAuthenticationType());
        assertTrue(result.getAccessToken().startsWith("FP-"));
        System.out.println("✓ Authentification FINGERPRINT réussie - Token: " + result.getAccessToken());
    }

    @Test
    @DisplayName("Test 4: Authentification par reconnaissance faciale")
    void testFacialRecognitionAuthentication() {
        // GIVEN: credentials avec données faciales suffisantes (>= 100 chars pour haute confiance)
        String facialData = "a".repeat(150); // Simule des données faciales
        AuthenticationCredentials credentials = AuthenticationCredentials.builder()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.FACIAL_RECOGNITION)
            .facialData(facialData)
            .build();

        // WHEN: on authentifie
        AuthenticationResult result = context.authenticate(credentials);

        // THEN: l'authentification réussit
        assertTrue(result.isSuccess(), "L'authentification faciale devrait réussir");
        assertEquals(AuthenticationType.FACIAL_RECOGNITION, result.getAuthenticationType());
        assertTrue(result.getAccessToken().startsWith("FACE-"));
        System.out.println("✓ Authentification FACIAL réussie - " + result.getMessage());
    }

    // ==================== TEST DE L'EXTENSION (Réalité Augmentée) ====================

    @Test
    @DisplayName("Test 5: EXTENSION - Authentification par Réalité Augmentée")
    void testAugmentedRealityAuthentication() {
        // GIVEN: credentials avec données AR (>= 200 chars pour haute confiance)
        String arData = "b".repeat(250); // Simule des données AR
        AuthenticationCredentials credentials = AuthenticationCredentials.builder()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.AUGMENTED_REALITY)
            .arData(arData)
            .build();

        // WHEN: on authentifie
        AuthenticationResult result = context.authenticate(credentials);

        // THEN: l'authentification réussit
        assertTrue(result.isSuccess(), "L'authentification AR devrait réussir");
        assertEquals(AuthenticationType.AUGMENTED_REALITY, result.getAuthenticationType());
        assertTrue(result.getAccessToken().startsWith("AR-"));
        System.out.println("✓ Authentification AUGMENTED_REALITY réussie - " + result.getMessage());
    }

    // ==================== TEST DE SUBSTITUTION DYNAMIQUE ====================

    @Test
    @DisplayName("Test 6: Substitution dynamique - Changer de méthode à l'exécution")
    void testDynamicSubstitution() {
        String userId = "user@example.com";

        System.out.println("\n=== DÉMONSTRATION DE LA SUBSTITUTION DYNAMIQUE ===\n");

        // Authentification 1: PASSWORD
        AuthenticationCredentials pwdCreds = AuthenticationCredentials.builder()
            .userId(userId)
            .authenticationType(AuthenticationType.PASSWORD)
            .password("password123")
            .build();
        AuthenticationResult result1 = context.authenticate(pwdCreds);
        System.out.println("1. Méthode PASSWORD: " + (result1.isSuccess() ? "✓" : "✗"));

        // Authentification 2: FINGERPRINT (même utilisateur, méthode différente)
        AuthenticationCredentials fpCreds = AuthenticationCredentials.builder()
            .userId(userId)
            .authenticationType(AuthenticationType.FINGERPRINT)
            .fingerprintData("fingerprintData123")
            .build();
        AuthenticationResult result2 = context.authenticate(fpCreds);
        System.out.println("2. Méthode FINGERPRINT: " + (result2.isSuccess() ? "✓" : "✗"));

        // Authentification 3: AUGMENTED_REALITY
        AuthenticationCredentials arCreds = AuthenticationCredentials.builder()
            .userId(userId)
            .authenticationType(AuthenticationType.AUGMENTED_REALITY)
            .arData("a".repeat(200))
            .build();
        AuthenticationResult result3 = context.authenticate(arCreds);
        System.out.println("3. Méthode AUGMENTED_REALITY: " + (result3.isSuccess() ? "✓" : "✗"));

        // Vérifier que les trois méthodes ont fonctionné
        assertTrue(result1.isSuccess());
        assertTrue(result2.isSuccess());
        assertTrue(result3.isSuccess());

        // Vérifier que chaque méthode a généré un token différent
        assertNotEquals(result1.getAccessToken(), result2.getAccessToken());
        assertNotEquals(result2.getAccessToken(), result3.getAccessToken());

        System.out.println("\n✓ La substitution dynamique fonctionne correctement!");
        System.out.println("  Le même contexte a utilisé 3 stratégies différentes à l'exécution.");
    }

    // ==================== TEST DE LA FACTORY ====================

    @Test
    @DisplayName("Test 7: Factory - Vérifier que toutes les méthodes sont enregistrées")
    void testFactoryRegistration() {
        System.out.println("\n=== MÉTHODES D'AUTHENTIFICATION DISPONIBLES ===\n");

        List<AuthenticationType> availableTypes = factory.getAvailableTypes();

        // Vérifier que toutes les méthodes sont enregistrées
        assertTrue(factory.isSupported(AuthenticationType.PASSWORD));
        assertTrue(factory.isSupported(AuthenticationType.OTP));
        assertTrue(factory.isSupported(AuthenticationType.FINGERPRINT));
        assertTrue(factory.isSupported(AuthenticationType.FACIAL_RECOGNITION));
        assertTrue(factory.isSupported(AuthenticationType.AUGMENTED_REALITY));

        assertEquals(5, availableTypes.size(), "Il devrait y avoir 5 méthodes");

        for (AuthenticationType type : availableTypes) {
            System.out.println("  - " + type.name() + ": " + type.getDescription());
        }

        System.out.println("\n✓ Toutes les stratégies sont correctement enregistrées dans la Factory");
    }

    // ==================== TESTS D'ECHEC ====================

    @Test
    @DisplayName("Test 8: Échec - Mot de passe trop court")
    void testPasswordTooShort() {
        AuthenticationCredentials credentials = AuthenticationCredentials.builder()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.PASSWORD)
            .password("123") // Trop court
            .build();

        AuthenticationResult result = context.authenticate(credentials);

        assertFalse(result.isSuccess(), "L'authentification devrait échouer");
        System.out.println("✓ Rejet correct du mot de passe invalide: " + result.getMessage());
    }

    @Test
    @DisplayName("Test 9: Échec - OTP invalide")
    void testInvalidOTP() {
        AuthenticationCredentials credentials = AuthenticationCredentials.builder()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.OTP)
            .otpCode("000000") // OTP qui n'existe pas
            .build();

        AuthenticationResult result = context.authenticate(credentials);

        assertFalse(result.isSuccess(), "L'authentification devrait échouer");
        System.out.println("✓ Rejet correct de l'OTP invalide: " + result.getMessage());
    }

    @Test
    @DisplayName("Test 10: Échec - Reconnaissance faciale avec confiance insuffisante")
    void testFacialRecognitionLowConfidence() {
        // Données trop courtes = faible confiance
        AuthenticationCredentials credentials = AuthenticationCredentials.builder()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.FACIAL_RECOGNITION)
            .facialData("short") // Trop court
            .build();

        AuthenticationResult result = context.authenticate(credentials);

        assertFalse(result.isSuccess(), "L'authentification devrait échouer");
        System.out.println("✓ Rejet correct (confiance insuffisante): " + result.getMessage());
    }
}
