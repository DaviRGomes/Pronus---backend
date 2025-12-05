package com.inatel.prototipo_ia.dto.out;

import java.time.LocalDateTime;
import java.util.List;

public class SessaoTreinoHistoryDtoOut {

    private Long id;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private Double pontuacaoGeral;
    private Integer totalAcertos;
    private Integer totalPalavras;
    private String dificuldade;
    private String status;
    private String feedbackGeral;
    private List<BatchPronunciationAnalysisDTO.ResultadoPalavra> detalhes;

    public SessaoTreinoHistoryDtoOut() {}

    public SessaoTreinoHistoryDtoOut(Long id, LocalDateTime dataInicio, LocalDateTime dataFim, Double pontuacaoGeral, 
                                     Integer totalAcertos, Integer totalPalavras, String dificuldade, String status,
                                     String feedbackGeral, List<BatchPronunciationAnalysisDTO.ResultadoPalavra> detalhes) {
        this.id = id;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.pontuacaoGeral = pontuacaoGeral;
        this.totalAcertos = totalAcertos;
        this.totalPalavras = totalPalavras;
        this.dificuldade = dificuldade;
        this.status = status;
        this.feedbackGeral = feedbackGeral;
        this.detalhes = detalhes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Double getPontuacaoGeral() {
        return pontuacaoGeral;
    }

    public void setPontuacaoGeral(Double pontuacaoGeral) {
        this.pontuacaoGeral = pontuacaoGeral;
    }

    public Integer getTotalAcertos() {
        return totalAcertos;
    }

    public void setTotalAcertos(Integer totalAcertos) {
        this.totalAcertos = totalAcertos;
    }

    public Integer getTotalPalavras() {
        return totalPalavras;
    }

    public void setTotalPalavras(Integer totalPalavras) {
        this.totalPalavras = totalPalavras;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(String dificuldade) {
        this.dificuldade = dificuldade;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeedbackGeral() {
        return feedbackGeral;
    }

    public void setFeedbackGeral(String feedbackGeral) {
        this.feedbackGeral = feedbackGeral;
    }

    public List<BatchPronunciationAnalysisDTO.ResultadoPalavra> getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(List<BatchPronunciationAnalysisDTO.ResultadoPalavra> detalhes) {
        this.detalhes = detalhes;
    }
}
