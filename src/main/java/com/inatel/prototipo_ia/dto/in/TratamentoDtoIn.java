package com.inatel.prototipo_ia.dto.in;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TratamentoDtoIn {
    private Integer quantidadeDia;
    private String tipoTratamento;
    private Boolean personalizado;
    private Long conteudoTesteId;
    private Long profissionalId;
}