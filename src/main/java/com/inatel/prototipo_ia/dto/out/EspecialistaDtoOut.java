package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EspecialistaDtoOut {
    private Long id;
    private String nome;
    private Integer idade;
    private String endereco;
    private String crmFono;
    private String especialidade;
    private java.util.List<Long> consultaIds;
    private java.util.List<Long> disponibilidadeIds;
    private java.util.List<Long> chatIds;
    private java.util.List<Long> relatorioIds;
}
