package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Especialista")
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "usuario_id")
public class EspecialistaEntity extends UsuarioEntity {

    @Column(name = "crmfono")
    private String crmFono;

    private String especialidade;
}