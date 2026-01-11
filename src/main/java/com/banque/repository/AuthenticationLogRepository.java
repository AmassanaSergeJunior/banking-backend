package com.banque.repository;

import com.banque.domain.enums.AuthenticationType;
import com.banque.domain.model.AuthenticationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository JPA pour l'entité AuthenticationLog.
 *
 * Permet de tracer et analyser les tentatives d'authentification.
 */
@Repository
public interface AuthenticationLogRepository extends JpaRepository<AuthenticationLog, Long> {

    /**
     * Recherche les logs d'authentification d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Liste des logs
     */
    List<AuthenticationLog> findByUserIdOrderByTimestampDesc(String userId);

    /**
     * Recherche les tentatives échouées récentes d'un utilisateur.
     * Utile pour détecter les tentatives de brute force.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param since Date/heure minimale
     * @return Liste des échecs récents
     */
    List<AuthenticationLog> findByUserIdAndSuccessFalseAndTimestampAfter(
        String userId,
        LocalDateTime since
    );

    /**
     * Compte les tentatives d'authentification par type.
     *
     * @param authType Le type d'authentification
     * @return Nombre de tentatives
     */
    long countByAuthType(AuthenticationType authType);

    /**
     * Recherche les logs par type d'authentification.
     *
     * @param authType Le type d'authentification
     * @return Liste des logs
     */
    List<AuthenticationLog> findByAuthTypeOrderByTimestampDesc(AuthenticationType authType);
}
