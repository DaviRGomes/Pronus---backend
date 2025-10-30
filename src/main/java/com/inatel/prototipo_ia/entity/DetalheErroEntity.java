package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "detalherro")
@Getter
@Setter
public class DetalheErroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fonemaesperado")
    private String fonemaEsperado;

    @Column(name = "fonemaproduzido")
    private String fonemaProduzido;

    @Column(name = "scoredesvio")
    private Float scoreDesvio;

    @ManyToOne
    @JoinColumn(name = "relatorio_id", nullable = false)
    private RelatorioEntity relatorio;
}