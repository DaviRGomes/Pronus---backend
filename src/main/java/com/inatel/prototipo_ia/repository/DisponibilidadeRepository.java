package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.DisponibilidadeEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadeRepository extends JpaRepository<DisponibilidadeEntity, Long> {
    List<DisponibilidadeEntity> findByEspecialista(EspecialistaEntity especialista);
    List<DisponibilidadeEntity> findByData(LocalDate data);
}