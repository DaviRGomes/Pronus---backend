package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EspecialistaRepository extends JpaRepository<EspecialistaEntity, Long> {
    List<EspecialistaEntity> findByEspecialidade(String especialidade);

    Optional<EspecialistaEntity> findByCrmFono(String crmFono);

    @Query("SELECT e FROM EspecialistaEntity e WHERE e.idade >= 18")
    List<EspecialistaEntity> findEspecialistasMaioresDeIdade();
}