package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.RelatorioEntity;
import com.inatel.prototipo_ia.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelatorioRepository extends JpaRepository<RelatorioEntity, Long> {
    
    // Buscar relatório por ID do chat
    Optional<RelatorioEntity> findByChatId(Long chatId);

    // Verifica s existe um relatório para um chat.
    boolean existsByChatId(Long chatId);
}