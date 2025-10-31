package com.inatel.prototipo_ia.dto.in;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetalheErroDtoIn {
    private String fonemaEsperado;
    private String fonemaProduzido;
    private Float scoreDesvio;
    private Long relatorioId;
}