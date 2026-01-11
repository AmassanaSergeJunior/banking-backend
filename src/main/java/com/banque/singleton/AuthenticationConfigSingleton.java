package com.banque.singleton;

import com.banque.domain.enums.AuthenticationType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PATTERN SINGLETON - Configuration d'Authentification par Défaut
 *
 * OBJECTIF 4: Ce singleton centralise la configuration d'authentification
 * pour tout le système, garantissant une seule source de vérité.
 *
 * POURQUOI SINGLETON?
 * - Configuration unique et cohérente
 * - Paramètres partagés entre tous les modules
 * - Modification en temps réel sans redémarrage
 * - Thread-safe pour lecture/écriture concurrentes
 *
 * IMPLEMENTATION: Initialization-on-demand holder idiom
 * - Lazy initialization automatique par la JVM
 * - Thread-safe sans synchronisation explicite
 * - Simple et performant
 */
public class AuthenticationConfigSingleton {

    /**
     * Holder pattern - La classe interne n'est chargée que lors du premier accès.
     * La JVM garantit que le chargement est thread-safe.
     */
    private static class Holder {
        private static final AuthenticationConfigSingleton INSTANCE = new AuthenticationConfigSingleton();
    }

    // Configuration par défaut
    private volatile AuthenticationType defaultAuthMethod;
    private volatile int maxFailedAttempts;
    private volatile int lockoutDurationMinutes;
    private volatile int sessionTimeoutMinutes;
    private volatile int otpValiditySeconds;
    private volatile boolean multiFactorEnabled;

    // Configuration par opérateur (thread-safe)
    private final ConcurrentHashMap<String, AuthenticationType> operatorDefaults;

    // Méthodes d'authentification activées
    private final Set<AuthenticationType> enabledMethods;

    // Métadonnées
    private final LocalDateTime createdAt;
    private volatile LocalDateTime lastModified;
    private final AtomicInteger accessCount;
    private final AtomicInteger modificationCount;

    /**
     * Constructeur privé avec valeurs par défaut.
     */
    private AuthenticationConfigSingleton() {
        this.createdAt = LocalDateTime.now();
        this.lastModified = this.createdAt;
        this.accessCount = new AtomicInteger(0);
        this.modificationCount = new AtomicInteger(0);

        // Valeurs par défaut
        this.defaultAuthMethod = AuthenticationType.PASSWORD;
        this.maxFailedAttempts = 5;
        this.lockoutDurationMinutes = 15;
        this.sessionTimeoutMinutes = 30;
        this.otpValiditySeconds = 300; // 5 minutes
        this.multiFactorEnabled = false;

        // Initialiser les collections thread-safe
        this.operatorDefaults = new ConcurrentHashMap<>();
        this.enabledMethods = Collections.synchronizedSet(
            EnumSet.allOf(AuthenticationType.class)
        );
    }

    /**
     * Obtient l'instance unique (Holder pattern).
     */
    public static AuthenticationConfigSingleton getInstance() {
        AuthenticationConfigSingleton instance = Holder.INSTANCE;
        instance.accessCount.incrementAndGet();
        return instance;
    }

    // ==================== GETTERS ====================

    public AuthenticationType getDefaultAuthMethod() {
        return defaultAuthMethod;
    }

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public int getLockoutDurationMinutes() {
        return lockoutDurationMinutes;
    }

    public int getSessionTimeoutMinutes() {
        return sessionTimeoutMinutes;
    }

    public int getOtpValiditySeconds() {
        return otpValiditySeconds;
    }

    public boolean isMultiFactorEnabled() {
        return multiFactorEnabled;
    }

    public Set<AuthenticationType> getEnabledMethods() {
        return Collections.unmodifiableSet(enabledMethods);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public int getAccessCount() {
        return accessCount.get();
    }

    public int getModificationCount() {
        return modificationCount.get();
    }

    // ==================== SETTERS (avec tracking) ====================

    public synchronized void setDefaultAuthMethod(AuthenticationType method) {
        this.defaultAuthMethod = method;
        markModified();
    }

    public synchronized void setMaxFailedAttempts(int max) {
        if (max < 1) throw new IllegalArgumentException("Max doit être >= 1");
        this.maxFailedAttempts = max;
        markModified();
    }

    public synchronized void setLockoutDurationMinutes(int minutes) {
        if (minutes < 1) throw new IllegalArgumentException("Durée doit être >= 1");
        this.lockoutDurationMinutes = minutes;
        markModified();
    }

    public synchronized void setSessionTimeoutMinutes(int minutes) {
        if (minutes < 1) throw new IllegalArgumentException("Timeout doit être >= 1");
        this.sessionTimeoutMinutes = minutes;
        markModified();
    }

    public synchronized void setOtpValiditySeconds(int seconds) {
        if (seconds < 30) throw new IllegalArgumentException("Validité OTP doit être >= 30s");
        this.otpValiditySeconds = seconds;
        markModified();
    }

    public synchronized void setMultiFactorEnabled(boolean enabled) {
        this.multiFactorEnabled = enabled;
        markModified();
    }

    // ==================== GESTION DES METHODES ====================

    /**
     * Active une méthode d'authentification.
     */
    public void enableMethod(AuthenticationType method) {
        enabledMethods.add(method);
        markModified();
    }

    /**
     * Désactive une méthode d'authentification.
     */
    public void disableMethod(AuthenticationType method) {
        if (method == defaultAuthMethod) {
            throw new IllegalStateException(
                "Impossible de désactiver la méthode par défaut. Changez d'abord la méthode par défaut."
            );
        }
        enabledMethods.remove(method);
        markModified();
    }

    /**
     * Vérifie si une méthode est activée.
     */
    public boolean isMethodEnabled(AuthenticationType method) {
        return enabledMethods.contains(method);
    }

    // ==================== CONFIGURATION PAR OPERATEUR ====================

    /**
     * Définit la méthode par défaut pour un opérateur.
     */
    public void setOperatorDefault(String operatorId, AuthenticationType method) {
        operatorDefaults.put(operatorId, method);
        markModified();
    }

    /**
     * Obtient la méthode par défaut pour un opérateur.
     */
    public AuthenticationType getOperatorDefault(String operatorId) {
        return operatorDefaults.getOrDefault(operatorId, defaultAuthMethod);
    }

    /**
     * Retourne toutes les configurations par opérateur.
     */
    public Map<String, AuthenticationType> getAllOperatorDefaults() {
        return Collections.unmodifiableMap(operatorDefaults);
    }

    // ==================== UTILITAIRES ====================

    private void markModified() {
        this.lastModified = LocalDateTime.now();
        this.modificationCount.incrementAndGet();
    }

    /**
     * Réinitialise la configuration aux valeurs par défaut.
     */
    public synchronized void resetToDefaults() {
        this.defaultAuthMethod = AuthenticationType.PASSWORD;
        this.maxFailedAttempts = 5;
        this.lockoutDurationMinutes = 15;
        this.sessionTimeoutMinutes = 30;
        this.otpValiditySeconds = 300;
        this.multiFactorEnabled = false;
        this.operatorDefaults.clear();
        this.enabledMethods.clear();
        this.enabledMethods.addAll(EnumSet.allOf(AuthenticationType.class));
        markModified();
    }

    /**
     * Retourne un résumé de la configuration.
     */
    public ConfigSummary getSummary() {
        return new ConfigSummary(
            defaultAuthMethod,
            maxFailedAttempts,
            lockoutDurationMinutes,
            sessionTimeoutMinutes,
            otpValiditySeconds,
            multiFactorEnabled,
            enabledMethods.size(),
            operatorDefaults.size(),
            accessCount.get(),
            modificationCount.get(),
            createdAt,
            lastModified
        );
    }

    /**
     * Résumé de la configuration.
     */
    public static class ConfigSummary {
        private final AuthenticationType defaultMethod;
        private final int maxAttempts;
        private final int lockoutMinutes;
        private final int sessionTimeout;
        private final int otpValidity;
        private final boolean mfaEnabled;
        private final int enabledMethodsCount;
        private final int operatorConfigsCount;
        private final int accessCount;
        private final int modificationCount;
        private final LocalDateTime createdAt;
        private final LocalDateTime lastModified;

        public ConfigSummary(AuthenticationType def, int max, int lockout, int session, int otp,
                           boolean mfa, int methods, int operators, int access, int mods,
                           LocalDateTime created, LocalDateTime modified) {
            this.defaultMethod = def;
            this.maxAttempts = max;
            this.lockoutMinutes = lockout;
            this.sessionTimeout = session;
            this.otpValidity = otp;
            this.mfaEnabled = mfa;
            this.enabledMethodsCount = methods;
            this.operatorConfigsCount = operators;
            this.accessCount = access;
            this.modificationCount = mods;
            this.createdAt = created;
            this.lastModified = modified;
        }

        // Getters
        public AuthenticationType getDefaultMethod() { return defaultMethod; }
        public int getMaxAttempts() { return maxAttempts; }
        public int getLockoutMinutes() { return lockoutMinutes; }
        public int getSessionTimeout() { return sessionTimeout; }
        public int getOtpValidity() { return otpValidity; }
        public boolean isMfaEnabled() { return mfaEnabled; }
        public int getEnabledMethodsCount() { return enabledMethodsCount; }
        public int getOperatorConfigsCount() { return operatorConfigsCount; }
        public int getAccessCount() { return accessCount; }
        public int getModificationCount() { return modificationCount; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastModified() { return lastModified; }
    }
}
