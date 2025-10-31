package com.inatel.prototipo_ia.dto.in;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatDtoIn {
    private Integer duracao;
    private String conversa;
    private Long clienteId;
    private Long profissionalId;
}