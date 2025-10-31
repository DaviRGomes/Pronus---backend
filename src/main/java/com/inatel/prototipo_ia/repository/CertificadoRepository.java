package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.CertificadoEntity;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CertificadoRepository extends JpaRepository<CertificadoEntity, Long> {
    List<CertificadoEntity> findByCliente(ClienteEntity cliente);

    List<CertificadoEntity> findByClienteId(Long clienteId);

    List<CertificadoEntity> findByNivelAlcancado(String nivelAlcancado);

    List<CertificadoEntity> findByDataEmissaoAfter(LocalDate data);
}