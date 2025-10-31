package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelatorioDtoOut {
    private Long id;
    private Float acuracia;
    private String analiseFono;
    private Long chatId;
}