package com.inatel.prototipo_ia.dto.in;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CertificadoDtoIn {
    private String nome;
    private LocalDate dataEmissao;
    private String nivelAlcancado;
    private Long clienteId;
}