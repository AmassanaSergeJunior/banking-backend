import java.util.*;
import java.time.LocalDateTime;

/**
 * ============================================================================
 * TEST STANDALONE - OBJECTIF 1 : Authentification Multiple
 * ============================================================================
 *
 * Ce fichier démontre les patterns STRATEGY et FACTORY pour l'authentification.
 * Il peut être compilé et exécuté sans aucune dépendance externe.
 *
 * Commandes:
 *   javac Objectif1Test.java
 *   java Objectif1Test
 * ============================================================================
 */
public class Objectif1Test {

    // ======================== ENUM ========================

    enum AuthenticationType {
        PASSWORD("Authentification par mot de passe"),
        FINGERPRINT("Authentification par empreinte digitale"),
        FACIAL_RECOGNITION("Authentification par reconnaissance faciale"),
        OTP("Authentification par code OTP"),
        AUGMENTED_REALITY("Authentification par réalité augmentée");

        private final String description;

        AuthenticationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // ======================== CREDENTIALS ========================

    static class AuthenticationCredentials {
        private String userId;
        private AuthenticationType authenticationType;
        private String password;
        private String otpCode;
        private String fingerprintData;
        private String facialData;
        private String arData;

        // Builder pattern simplifié
        public AuthenticationCredentials userId(String userId) {
            this.userId = userId;
            return this;
        }

        public AuthenticationCredentials authenticationType(AuthenticationType type) {
            this.authenticationType = type;
            return this;
        }

        public AuthenticationCredentials password(String password) {
            this.password = password;
            return this;
        }

        public AuthenticationCredentials otpCode(String otpCode) {
            this.otpCode = otpCode;
            return this;
        }

        public AuthenticationCredentials fingerprintData(String data) {
            this.fingerprintData = data;
            return this;
        }

        public AuthenticationCredentials facialData(String data) {
            this.facialData = data;
            return this;
        }

        public AuthenticationCredentials arData(String data) {
            this.arData = data;
            return this;
        }

        // Getters
        public String getUserId() { return userId; }
        public AuthenticationType getAuthenticationType() { return authenticationType; }
        public String getPassword() { return password; }
        public String getOtpCode() { return otpCode; }
        public String getFingerprintData() { return fingerprintData; }
        public String getFacialData() { return facialData; }
        public String getArData() { return arData; }
    }

    // ======================== RESULT ========================

    static class AuthenticationResult {
        private boolean success;
        private String message;
        private String userId;
        private AuthenticationType authenticationType;
        private String accessToken;
        private LocalDateTime timestamp;

        private AuthenticationResult() {
            this.timestamp = LocalDateTime.now();
        }

        public static AuthenticationResult success(String userId, AuthenticationType type, String token) {
            AuthenticationResult result = new AuthenticationResult();
            result.success = true;
            result.message = "Authentification réussie";
            result.userId = userId;
            result.authenticationType = type;
            result.accessToken = token;
            return result;
        }

        public static AuthenticationResult failure(String message, AuthenticationType type) {
            AuthenticationResult result = new AuthenticationResult();
            result.success = false;
            result.message = message;
            result.authenticationType = type;
            return result;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getUserId() { return userId; }
        public AuthenticationType getAuthenticationType() { return authenticationType; }
        public String getAccessToken() { return accessToken; }

        public void setMessage(String message) { this.message = message; }
    }

    // ======================== STRATEGY INTERFACE ========================

    /**
     * PATTERN STRATEGY - Interface de stratégie d'authentification
     *
     * Cette interface définit le contrat commun pour toutes les méthodes
     * d'authentification. Le code client utilise cette interface sans
     * connaître l'implémentation concrète à la compilation.
     */
    interface AuthenticationStrategy {
        AuthenticationResult authenticate(AuthenticationCredentials credentials);
        AuthenticationType getType();
        boolean supports(AuthenticationCredentials credentials);
    }

    // ======================== STRATEGY IMPLEMENTATIONS ========================

    /**
     * Stratégie 1: Authentification par mot de passe
     */
    static class PasswordAuthenticationStrategy implements AuthenticationStrategy {
        @Override
        public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
            if (credentials.getUserId() == null || credentials.getUserId().isEmpty()) {
                return AuthenticationResult.failure("Identifiant utilisateur requis", AuthenticationType.PASSWORD);
            }
            if (credentials.getPassword() == null || credentials.getPassword().isEmpty()) {
                return AuthenticationResult.failure("Mot de passe requis", AuthenticationType.PASSWORD);
            }

            // Validation: mot de passe >= 6 caractères
            if (credentials.getPassword().length() >= 6) {
                String token = "PWD-" + UUID.randomUUID().toString();
                return AuthenticationResult.success(credentials.getUserId(), AuthenticationType.PASSWORD, token);
            } else {
                return AuthenticationResult.failure("Mot de passe incorrect", AuthenticationType.PASSWORD);
            }
        }

        @Override
        public AuthenticationType getType() { return AuthenticationType.PASSWORD; }

        @Override
        public boolean supports(AuthenticationCredentials credentials) {
            return credentials.getAuthenticationType() == AuthenticationType.PASSWORD;
        }
    }

    /**
     * Stratégie 2: Authentification par OTP
     */
    static class OTPAuthenticationStrategy implements AuthenticationStrategy {
        private Map<String, String> otpStore = new HashMap<>();

        public String generateOTP(String userId) {
            String otp = String.format("%06d", (int) (Math.random() * 1000000));
            otpStore.put(userId, otp);
            return otp;
        }

        @Override
        public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
            if (credentials.getUserId() == null || credentials.getOtpCode() == null) {
                return AuthenticationResult.failure("UserId et OTP requis", AuthenticationType.OTP);
            }

            String storedOtp = otpStore.get(credentials.getUserId());
            if (storedOtp != null && storedOtp.equals(credentials.getOtpCode())) {
                otpStore.remove(credentials.getUserId()); // Usage unique
                String token = "OTP-" + UUID.randomUUID().toString();
                return AuthenticationResult.success(credentials.getUserId(), AuthenticationType.OTP, token);
            }
            return AuthenticationResult.failure("Code OTP invalide ou expiré", AuthenticationType.OTP);
        }

        @Override
        public AuthenticationType getType() { return AuthenticationType.OTP; }

        @Override
        public boolean supports(AuthenticationCredentials credentials) {
            return credentials.getAuthenticationType() == AuthenticationType.OTP;
        }
    }

    /**
     * Stratégie 3: Authentification par empreinte digitale
     */
    static class FingerprintAuthenticationStrategy implements AuthenticationStrategy {
        @Override
        public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
            if (credentials.getUserId() == null || credentials.getFingerprintData() == null) {
                return AuthenticationResult.failure("Données d'empreinte requises", AuthenticationType.FINGERPRINT);
            }

            // Simulation: accepte si données >= 10 caractères
            if (credentials.getFingerprintData().length() >= 10) {
                String token = "FP-" + UUID.randomUUID().toString();
                return AuthenticationResult.success(credentials.getUserId(), AuthenticationType.FINGERPRINT, token);
            }
            return AuthenticationResult.failure("Empreinte non reconnue", AuthenticationType.FINGERPRINT);
        }

        @Override
        public AuthenticationType getType() { return AuthenticationType.FINGERPRINT; }

        @Override
        public boolean supports(AuthenticationCredentials credentials) {
            return credentials.getAuthenticationType() == AuthenticationType.FINGERPRINT;
        }
    }

    /**
     * Stratégie 4: Authentification par reconnaissance faciale
     */
    static class FacialRecognitionAuthenticationStrategy implements AuthenticationStrategy {
        private static final double THRESHOLD = 0.85;

        @Override
        public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
            if (credentials.getUserId() == null || credentials.getFacialData() == null) {
                return AuthenticationResult.failure("Données faciales requises", AuthenticationType.FACIAL_RECOGNITION);
            }

            // Simulation de confiance basée sur la taille des données
            double confidence;
            if (credentials.getFacialData().length() >= 100) {
                confidence = 0.92;
            } else if (credentials.getFacialData().length() >= 50) {
                confidence = 0.87;
            } else {
                confidence = 0.60;
            }

            if (confidence >= THRESHOLD) {
                String token = "FACE-" + UUID.randomUUID().toString();
                AuthenticationResult result = AuthenticationResult.success(
                    credentials.getUserId(), AuthenticationType.FACIAL_RECOGNITION, token);
                result.setMessage(String.format("Reconnaissance faciale réussie (confiance: %.1f%%)", confidence * 100));
                return result;
            }
            return AuthenticationResult.failure(
                String.format("Confiance insuffisante: %.1f%%", confidence * 100),
                AuthenticationType.FACIAL_RECOGNITION);
        }

        @Override
        public AuthenticationType getType() { return AuthenticationType.FACIAL_RECOGNITION; }

        @Override
        public boolean supports(AuthenticationCredentials credentials) {
            return credentials.getAuthenticationType() == AuthenticationType.FACIAL_RECOGNITION;
        }
    }

    /**
     * Stratégie 5: EXTENSION - Authentification par Réalité Augmentée
     *
     * Cette classe démontre comment ajouter une nouvelle méthode
     * SANS MODIFIER le code existant (principe Open/Closed).
     */
    static class AugmentedRealityAuthenticationStrategy implements AuthenticationStrategy {
        private static final double AR_THRESHOLD = 0.80;

        @Override
        public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
            if (credentials.getUserId() == null || credentials.getArData() == null) {
                return AuthenticationResult.failure("Données AR requises", AuthenticationType.AUGMENTED_REALITY);
            }

            // Simulation
            double confidence;
            String patternName;
            if (credentials.getArData().length() >= 200) {
                confidence = 0.95;
                patternName = "Cube Holographique Personnel";
            } else if (credentials.getArData().length() >= 100) {
                confidence = 0.85;
                patternName = "Marqueur QR 3D";
            } else {
                confidence = 0.50;
                patternName = "Inconnu";
            }

            if (confidence >= AR_THRESHOLD) {
                String token = "AR-" + UUID.randomUUID().toString();
                AuthenticationResult result = AuthenticationResult.success(
                    credentials.getUserId(), AuthenticationType.AUGMENTED_REALITY, token);
                result.setMessage(String.format("Pattern '%s' reconnu (confiance: %.1f%%)", patternName, confidence * 100));
                return result;
            }
            return AuthenticationResult.failure(
                String.format("Pattern AR non reconnu (confiance: %.1f%%)", confidence * 100),
                AuthenticationType.AUGMENTED_REALITY);
        }

        @Override
        public AuthenticationType getType() { return AuthenticationType.AUGMENTED_REALITY; }

        @Override
        public boolean supports(AuthenticationCredentials credentials) {
            return credentials.getAuthenticationType() == AuthenticationType.AUGMENTED_REALITY;
        }
    }

    // ======================== FACTORY ========================

    /**
     * PATTERN FACTORY - Fabrique de stratégies d'authentification
     *
     * Centralise la création des objets stratégie et permet
     * la sélection dynamique à l'exécution.
     */
    static class AuthenticationFactory {
        private Map<AuthenticationType, AuthenticationStrategy> strategies = new EnumMap<>(AuthenticationType.class);

        public AuthenticationFactory(List<AuthenticationStrategy> strategyList) {
            for (AuthenticationStrategy strategy : strategyList) {
                strategies.put(strategy.getType(), strategy);
            }
        }

        public AuthenticationStrategy getStrategy(AuthenticationType type) {
            AuthenticationStrategy strategy = strategies.get(type);
            if (strategy == null) {
                throw new IllegalArgumentException("Méthode non supportée: " + type);
            }
            return strategy;
        }

        public boolean isSupported(AuthenticationType type) {
            return strategies.containsKey(type);
        }

        public List<AuthenticationType> getAvailableTypes() {
            return new ArrayList<>(strategies.keySet());
        }
    }

    // ======================== CONTEXT ========================

    /**
     * PATTERN STRATEGY - Contexte d'authentification
     *
     * Utilise la Factory pour obtenir la bonne stratégie à l'exécution.
     * Ne connaît pas les implémentations concrètes à la compilation.
     */
    static class AuthenticationContext {
        private AuthenticationFactory factory;

        public AuthenticationContext(AuthenticationFactory factory) {
            this.factory = factory;
        }

        public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
            AuthenticationStrategy strategy = factory.getStrategy(credentials.getAuthenticationType());
            return strategy.authenticate(credentials);
        }

        public List<AuthenticationType> getAvailableMethods() {
            return factory.getAvailableTypes();
        }
    }

    // ======================== TESTS ========================

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println(" " + title);
        System.out.println("=".repeat(70));
    }

    private static void printTest(String testName, boolean passed, String details) {
        String status = passed ? "[PASS]" : "[FAIL]";
        String icon = passed ? "+" : "x";
        System.out.printf("%s %s %s%n", status, icon, testName);
        if (details != null && !details.isEmpty()) {
            System.out.println("       -> " + details);
        }
        if (passed) testsPassed++; else testsFailed++;
    }

    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("######################################################################");
        System.out.println("#                                                                    #");
        System.out.println("#     OBJECTIF 1 - TEST DES PATTERNS STRATEGY ET FACTORY            #");
        System.out.println("#     Système d'Authentification Multiple                           #");
        System.out.println("#                                                                    #");
        System.out.println("######################################################################");

        // Initialisation
        OTPAuthenticationStrategy otpStrategy = new OTPAuthenticationStrategy();

        List<AuthenticationStrategy> strategies = Arrays.asList(
            new PasswordAuthenticationStrategy(),
            otpStrategy,
            new FingerprintAuthenticationStrategy(),
            new FacialRecognitionAuthenticationStrategy(),
            new AugmentedRealityAuthenticationStrategy()
        );

        AuthenticationFactory factory = new AuthenticationFactory(strategies);
        AuthenticationContext context = new AuthenticationContext(factory);

        // ==================== TEST 1: PASSWORD ====================
        printHeader("TEST 1: Authentification par MOT DE PASSE");

        AuthenticationCredentials pwdCreds = new AuthenticationCredentials()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.PASSWORD)
            .password("motdepasse123");

        AuthenticationResult result1 = context.authenticate(pwdCreds);
        printTest("Authentification PASSWORD valide",
            result1.isSuccess() && result1.getAccessToken().startsWith("PWD-"),
            "Token: " + result1.getAccessToken());

        // Test échec mot de passe trop court
        AuthenticationCredentials pwdCredsFail = new AuthenticationCredentials()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.PASSWORD)
            .password("123");

        AuthenticationResult result1b = context.authenticate(pwdCredsFail);
        printTest("Rejet mot de passe trop court",
            !result1b.isSuccess(),
            result1b.getMessage());

        // ==================== TEST 2: OTP ====================
        printHeader("TEST 2: Authentification par OTP");

        String userId = "user@example.com";
        String otp = otpStrategy.generateOTP(userId);
        System.out.println("       OTP généré: " + otp);

        AuthenticationCredentials otpCreds = new AuthenticationCredentials()
            .userId(userId)
            .authenticationType(AuthenticationType.OTP)
            .otpCode(otp);

        AuthenticationResult result2 = context.authenticate(otpCreds);
        printTest("Authentification OTP valide",
            result2.isSuccess() && result2.getAccessToken().startsWith("OTP-"),
            "Token: " + result2.getAccessToken());

        // Test OTP invalide
        AuthenticationCredentials otpCredsFail = new AuthenticationCredentials()
            .userId(userId)
            .authenticationType(AuthenticationType.OTP)
            .otpCode("000000");

        AuthenticationResult result2b = context.authenticate(otpCredsFail);
        printTest("Rejet OTP invalide",
            !result2b.isSuccess(),
            result2b.getMessage());

        // ==================== TEST 3: FINGERPRINT ====================
        printHeader("TEST 3: Authentification par EMPREINTE DIGITALE");

        AuthenticationCredentials fpCreds = new AuthenticationCredentials()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.FINGERPRINT)
            .fingerprintData("base64EncodedFingerprintData");

        AuthenticationResult result3 = context.authenticate(fpCreds);
        printTest("Authentification FINGERPRINT valide",
            result3.isSuccess() && result3.getAccessToken().startsWith("FP-"),
            "Token: " + result3.getAccessToken());

        // ==================== TEST 4: FACIAL RECOGNITION ====================
        printHeader("TEST 4: Authentification par RECONNAISSANCE FACIALE");

        // Haute confiance
        AuthenticationCredentials faceCreds = new AuthenticationCredentials()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.FACIAL_RECOGNITION)
            .facialData("a".repeat(150));

        AuthenticationResult result4 = context.authenticate(faceCreds);
        printTest("Authentification FACIAL haute confiance",
            result4.isSuccess() && result4.getAccessToken().startsWith("FACE-"),
            result4.getMessage());

        // Faible confiance
        AuthenticationCredentials faceCredsFail = new AuthenticationCredentials()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.FACIAL_RECOGNITION)
            .facialData("short");

        AuthenticationResult result4b = context.authenticate(faceCredsFail);
        printTest("Rejet FACIAL confiance insuffisante",
            !result4b.isSuccess(),
            result4b.getMessage());

        // ==================== TEST 5: AUGMENTED REALITY (EXTENSION) ====================
        printHeader("TEST 5: EXTENSION - Authentification par REALITE AUGMENTEE");

        AuthenticationCredentials arCreds = new AuthenticationCredentials()
            .userId("user@example.com")
            .authenticationType(AuthenticationType.AUGMENTED_REALITY)
            .arData("b".repeat(250));

        AuthenticationResult result5 = context.authenticate(arCreds);
        printTest("Authentification AR (nouvelle méthode)",
            result5.isSuccess() && result5.getAccessToken().startsWith("AR-"),
            result5.getMessage());

        // ==================== TEST 6: SUBSTITUTION DYNAMIQUE ====================
        printHeader("TEST 6: SUBSTITUTION DYNAMIQUE A L'EXECUTION");
        System.out.println("\n  Même utilisateur, 3 méthodes différentes à la suite:\n");

        String testUser = "dynamic@test.com";

        // Méthode 1: PASSWORD
        AuthenticationCredentials dynPwd = new AuthenticationCredentials()
            .userId(testUser)
            .authenticationType(AuthenticationType.PASSWORD)
            .password("password123");
        AuthenticationResult dynResult1 = context.authenticate(dynPwd);
        System.out.println("    1. PASSWORD  -> " + (dynResult1.isSuccess() ? "OK" : "FAIL"));

        // Méthode 2: FINGERPRINT
        AuthenticationCredentials dynFp = new AuthenticationCredentials()
            .userId(testUser)
            .authenticationType(AuthenticationType.FINGERPRINT)
            .fingerprintData("fingerprintData123");
        AuthenticationResult dynResult2 = context.authenticate(dynFp);
        System.out.println("    2. FINGERPRINT -> " + (dynResult2.isSuccess() ? "OK" : "FAIL"));

        // Méthode 3: AUGMENTED_REALITY
        AuthenticationCredentials dynAr = new AuthenticationCredentials()
            .userId(testUser)
            .authenticationType(AuthenticationType.AUGMENTED_REALITY)
            .arData("a".repeat(200));
        AuthenticationResult dynResult3 = context.authenticate(dynAr);
        System.out.println("    3. AUGMENTED_REALITY -> " + (dynResult3.isSuccess() ? "OK" : "FAIL"));

        boolean allDifferentTokens = !dynResult1.getAccessToken().equals(dynResult2.getAccessToken())
            && !dynResult2.getAccessToken().equals(dynResult3.getAccessToken());

        printTest("Substitution dynamique (3 méthodes)",
            dynResult1.isSuccess() && dynResult2.isSuccess() && dynResult3.isSuccess() && allDifferentTokens,
            "Tokens différents générés par chaque stratégie");

        // ==================== TEST 7: FACTORY ====================
        printHeader("TEST 7: VERIFICATION DE LA FACTORY");

        List<AuthenticationType> availableTypes = factory.getAvailableTypes();
        System.out.println("\n  Méthodes enregistrées dans la Factory:\n");
        for (AuthenticationType type : availableTypes) {
            System.out.println("    - " + type.name() + ": " + type.getDescription());
        }

        boolean allSupported = factory.isSupported(AuthenticationType.PASSWORD)
            && factory.isSupported(AuthenticationType.OTP)
            && factory.isSupported(AuthenticationType.FINGERPRINT)
            && factory.isSupported(AuthenticationType.FACIAL_RECOGNITION)
            && factory.isSupported(AuthenticationType.AUGMENTED_REALITY);

        printTest("Toutes les stratégies enregistrées",
            allSupported && availableTypes.size() == 5,
            "5 méthodes disponibles");

        // ==================== RESUME ====================
        printHeader("RESUME DES TESTS");

        System.out.println();
        System.out.println("  Tests réussis : " + testsPassed);
        System.out.println("  Tests échoués : " + testsFailed);
        System.out.println("  Total         : " + (testsPassed + testsFailed));
        System.out.println();

        if (testsFailed == 0) {
            System.out.println("  *** TOUS LES TESTS SONT PASSES! ***");
            System.out.println();
            System.out.println("  L'OBJECTIF 1 est validé:");
            System.out.println("    - Pattern STRATEGY: Interface commune, implémentations multiples");
            System.out.println("    - Pattern FACTORY: Création dynamique des stratégies");
            System.out.println("    - Substitution dynamique: Changement de méthode à l'exécution");
            System.out.println("    - Extension: Nouvelle méthode (AR) ajoutée sans modifier le code existant");
        } else {
            System.out.println("  *** CERTAINS TESTS ONT ECHOUE ***");
        }

        System.out.println();
        System.out.println("######################################################################");
        System.out.println();
    }
}
