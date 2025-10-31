package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.SecretariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SecretariaRepository extends JpaRepository<SecretariaEntity, Long> {
    Optional<SecretariaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<SecretariaEntity> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT s FROM SecretariaEntity s WHERE s.idade >= 18")
    List<SecretariaEntity> findSecretariasMaioresDeIdade();
}