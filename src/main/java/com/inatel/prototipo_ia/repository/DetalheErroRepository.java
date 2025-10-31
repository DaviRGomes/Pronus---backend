package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.DetalheErroEntity;
import com.inatel.prototipo_ia.entity.RelatorioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalheErroRepository extends JpaRepository<DetalheErroEntity, Long> {
    List<DetalheErroEntity> findByRelatorio(RelatorioEntity relatorio);

    List<DetalheErroEntity> findByRelatorioId(Long relatorioId);

    List<DetalheErroEntity> findByFonemaEsperado(String fonemaEsperado);

    List<DetalheErroEntity> findByScoreDesvioGreaterThan(Float score);
}