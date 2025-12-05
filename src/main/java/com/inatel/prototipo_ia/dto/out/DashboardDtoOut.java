package com.inatel.prototipo_ia.dto.out;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDtoOut {
    private Integer sessoesRealizadas;
    private Integer pontuacaoMedia; // 0 a 100
    private Integer evolucao; // Diferença percentual
    private String observacao;
}
