package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Tratamento")
@Getter
@Setter
public class TratamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

            @Column(name = "quantidadedia")
    private Integer quantidadeDia;

    @Column(name = "tipotratamento")
    private String tipoTratamento;

    private Boolean personalizado;

    @ManyToOne
    @JoinColumn(name = "conteudoteste_id")
    private ConteudoTesteEntity conteudoTeste;

    // Muitos tratamentos para um profissional
    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalEntity profissional;
}