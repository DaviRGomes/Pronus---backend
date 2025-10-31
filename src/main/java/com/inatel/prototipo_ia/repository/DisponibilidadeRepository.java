package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.DisponibilidadeEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadeRepository extends JpaRepository<DisponibilidadeEntity, Long> {
    List<DisponibilidadeEntity> findByEspecialista(EspecialistaEntity especialista);

    List<DisponibilidadeEntity> findByData(LocalDate data);

    List<DisponibilidadeEntity> findByEspecialistaId(Long especialistaId);

    List<DisponibilidadeEntity> findByStatus(String status);

    @Query("SELECT d FROM DisponibilidadeEntity d WHERE d.status = 'disponível'")
    List<DisponibilidadeEntity> findDisponibilidadesDisponiveis();

    boolean existsByEspecialistaId(Long especialistaId);
}