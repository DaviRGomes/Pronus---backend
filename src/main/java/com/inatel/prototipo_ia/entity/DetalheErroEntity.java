package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DetalheErro")
@Getter
@Setter
public class DetalheErroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fonemaEsperado;

    private String fonemaProduzido;

    private Float scoreDesvio;

    @ManyToOne
    @JoinColumn(name = "relatorio_id", nullable = false)
    private RelatorioEntity relatorio;
}