package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Secretaria")
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "usuario_id")
public class SecretariaEntity extends UsuarioEntity {

    private String email;
}