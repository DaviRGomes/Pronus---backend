package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.ConteudoTesteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConteudoTesteRepository extends JpaRepository<ConteudoTesteEntity, Long> {
    List<ConteudoTesteEntity> findByDificuldade(String dificuldade);
    List<ConteudoTesteEntity> findByIdioma(String idioma);
    List<ConteudoTesteEntity> findByDificuldadeAndIdioma(String dificuldade, String idioma);
}