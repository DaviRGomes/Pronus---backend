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

    private Integer quantidadeDia;

    private String tipoTratamento;

    // Muitos tratamentos para um profissional
    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private ProfissionalEntity profissional;
}