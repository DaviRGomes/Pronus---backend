package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConteudoTesteDtoOut {
    private Long id;
    private String textoFrase;
    private String fonemasChave;
    private String dificuldade;
    private String idioma;
}