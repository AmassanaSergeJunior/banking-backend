package com.banque.repository;

import com.banque.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité User.
 *
 * Fournit les méthodes CRUD standard + requêtes personnalisées.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son email.
     *
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé ou Optional vide
     */
    Optional<User> findByEmail(String email);

    /**
     * Recherche un utilisateur par son téléphone.
     *
     * @param telephone Le numéro de téléphone
     * @return L'utilisateur trouvé ou Optional vide
     */
    Optional<User> findByTelephone(String telephone);

    /**
     * Vérifie si un email existe déjà.
     *
     * @param email L'email à vérifier
     * @return true si l'email existe
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un téléphone existe déjà.
     *
     * @param telephone Le téléphone à vérifier
     * @return true si le téléphone existe
     */
    boolean existsByTelephone(String telephone);
}
