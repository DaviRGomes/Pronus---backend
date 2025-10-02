package com.inatel.prototipo_ia.repository;

import com.inatel.prototipo_ia.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    // Buscar clientes por nível
    List<ClienteEntity> findByNivel(String nivel);
    
    // Buscar clientes maiores de idade
    @Query("SELECT c FROM ClienteEntity c WHERE c.idade >= 18")
    List<ClienteEntity> findClientesMaioresDeIdade();
    
    // Buscar clientes por nível e idade mínima
    @Query("SELECT c FROM ClienteEntity c WHERE c.nivel = :nivel AND c.idade >= :idade")
    List<ClienteEntity> findByNivelAndIdadeMinima(@Param("nivel") String nivel, @Param("idade") Integer idade);
}
