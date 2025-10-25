package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ConteudoTeste")
@Getter
@Setter
public class ConteudoTesteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String textoFrase;

    @Column(columnDefinition = "JSONB")
    private String fonemasChave; // JSON armazenado como String

    private String dificuldade;

    private String idioma;
}