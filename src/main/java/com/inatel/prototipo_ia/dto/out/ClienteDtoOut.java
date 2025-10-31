package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClienteDtoOut {
    private Long id;
    private String nome;
    private Integer idade;
    private String endereco;
    private String nivel;
    private List<Long> chatIds;
}