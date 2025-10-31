package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertificadoDtoOut {
    private Long id;
    private String nome;
    private LocalDate dataEmissao;
    private String nivelAlcancado;
    private Long clienteId;
}