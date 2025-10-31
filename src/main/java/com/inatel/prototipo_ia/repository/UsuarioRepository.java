package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    // Buscar usuários por idade exata
    List<UsuarioEntity> findByIdade(Integer idade);

    // Buscar nome independente se está maiúsculo ou minúscolo
    List<UsuarioEntity> findByNomeContainingIgnoreCase(String nome);
}
