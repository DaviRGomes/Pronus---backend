package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "conteudoteste")
@Getter
@Setter
public class ConteudoTesteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "textofrase", columnDefinition = "TEXT")
    private String textoFrase;

    @Column(name = "fonemaschave", columnDefinition = "TEXT")
    private String fonemasChave; // JSON armazenado como String

    private String dificuldade;

    private String idioma;
}