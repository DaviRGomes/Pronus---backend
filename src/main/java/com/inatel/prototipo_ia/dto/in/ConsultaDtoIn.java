package com.inatel.prototipo_ia.dto.in;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ConsultaDtoIn {
    private LocalDate data;
    private LocalTime hora;
    private String tipo;
    private String status;
    private Long clienteId;
    private Long especialistaId;
}