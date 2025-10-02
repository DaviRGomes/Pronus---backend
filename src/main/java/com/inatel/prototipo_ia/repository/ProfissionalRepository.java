package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.ProfissionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfissionalRepository extends JpaRepository<ProfissionalEntity, Long> {

    // Buscar profissionais com experiência maior que X anos
    List<ProfissionalEntity> findByExperienciaGreaterThan(Integer anos);
    
    // Métodos de busca específicos temporariamente removidos
    
    // Buscar profissionais experientes (mais de 5 anos)
    @Query("SELECT p FROM ProfissionalEntity p WHERE p.experiencia >= 5")
    List<ProfissionalEntity> findProfissionaisExperientes();
    
    // Buscar profissionais por experiência mínima e idade mínima
    @Query("SELECT p FROM ProfissionalEntity p WHERE p.experiencia >= :experiencia AND p.idade >= :idade")
    List<ProfissionalEntity> findByExperienciaAndIdadeMinima(@Param("experiencia") Integer experiencia, @Param("idade") Integer idade);
}