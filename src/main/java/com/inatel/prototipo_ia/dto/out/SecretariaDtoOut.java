package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SecretariaDtoOut {
    private Long id;
    private String nome;
    private Integer idade;
    private String endereco;
    private String email;
}