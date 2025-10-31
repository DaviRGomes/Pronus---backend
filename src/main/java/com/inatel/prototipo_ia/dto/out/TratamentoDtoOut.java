package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TratamentoDtoOut {
    private Long id;
    private Integer quantidadeDia;
    private String tipoTratamento;
    private Boolean personalizado;
    private Long conteudoTesteId;
    private Long profissionalId;
}