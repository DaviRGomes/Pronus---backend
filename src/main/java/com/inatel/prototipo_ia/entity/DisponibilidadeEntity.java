package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Disponibilidade")
@Getter
@Setter
public class DisponibilidadeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;

    @Column(name = "horainicio")
    private LocalTime horaInicio;

    @Column(name = "horafim")
    private LocalTime horaFim;

    private String status;

    @ManyToOne
    @JoinColumn(name = "especialista_id", nullable = false)
    private EspecialistaEntity especialista;
}