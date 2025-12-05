package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Cliente")
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "usuario_id")
public class ClienteEntity extends UsuarioEntity {

    private String nivel;

    // Relacionamento com Chat (um cliente pode ter vários chats)
    @OneToMany(mappedBy = "cliente")
    private List<ChatEntity> chats;

    @OneToMany(mappedBy = "cliente")
    private List<ConsultaEntity> consultas;

    @OneToMany(mappedBy = "cliente")
    private List<CertificadoEntity> certificados;
}
