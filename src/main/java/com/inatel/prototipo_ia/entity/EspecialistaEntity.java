package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "Especialista")
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "usuario_id")
public class EspecialistaEntity extends UsuarioEntity {

    @Column(name = "crmfono")
    private String crmFono;

    private String especialidade;

    @OneToMany(mappedBy = "especialista")
    private List<ConsultaEntity> consultas;

    @OneToMany(mappedBy = "especialista")
    private List<DisponibilidadeEntity> disponibilidades;

    @OneToMany(mappedBy = "especialista")
    private List<ChatEntity> chats;

    @OneToMany(mappedBy = "especialista")
    private List<RelatorioEntity> relatorios;
}
