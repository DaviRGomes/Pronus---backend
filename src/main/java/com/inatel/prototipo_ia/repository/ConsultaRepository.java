package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.entity.ConsultaEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {
    List<ConsultaEntity> findByCliente(ClienteEntity cliente);

    List<ConsultaEntity> findByEspecialista(EspecialistaEntity especialista);

    List<ConsultaEntity> findByData(LocalDate data);

    List<ConsultaEntity> findByClienteId(Long clienteId);

    List<ConsultaEntity> findByEspecialistaId(Long especialistaId);

    List<ConsultaEntity> findByStatus(String status);

    boolean existsByEspecialistaId(Long especialistaId);
}