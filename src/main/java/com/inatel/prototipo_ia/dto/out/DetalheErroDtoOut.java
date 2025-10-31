package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetalheErroDtoOut {
    private Long id;
    private String fonemaEsperado;
    private String fonemaProduzido;
    private Float scoreDesvio;
    private Long relatorioId;
}