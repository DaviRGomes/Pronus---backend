package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatDtoOut {
    private Long id;
    private Integer duracao;
    private String conversa;
    private Long clienteId;
    private Long profissionalId;
    private Long relatorioId;
}