package com.inatel.prototipo_ia.dto.out;

import java.time.LocalDateTime;

/**
 * DTO para análise de pronúncia usando Gemini
 */
public class GeminiPronunciationAnalysisDTO {

    private String palavraEsperada;
    private String palavraIdentificada;
    private Boolean acertou;
    private Double pontuacao;
    private String feedback;
    private String analiseDeTalhada;
    private LocalDateTime dataAnalise;
    private String metodoAnalise = "GEMINI";

    public GeminiPronunciationAnalysisDTO() {
        this.dataAnalise = LocalDateTime.now();
    }

    public GeminiPronunciationAnalysisDTO(String palavraEsperada, String palavraIdentificada,
                                          Boolean acertou, Double pontuacao,
                                          String feedback, String analiseDeTalhada) {
        this.palavraEsperada = palavraEsperada;
        this.palavraIdentificada = palavraIdentificada;
        this.acertou = acertou;
        this.pontuacao = pontuacao;
        this.feedback = feedback;
        this.analiseDeTalhada = analiseDeTalhada;
        this.dataAnalise = LocalDateTime.now();
    }

    // Getters e Setters
    public String getPalavraEsperada() {
        return palavraEsperada;
    }

    public void setPalavraEsperada(String palavraEsperada) {
        this.palavraEsperada = palavraEsperada;
    }

    public String getPalavraIdentificada() {
        return palavraIdentificada;
    }

    public void setPalavraIdentificada(String palavraIdentificada) {
        this.palavraIdentificada = palavraIdentificada;
    }

    public Boolean getAcertou() {
        return acertou;
    }

    public void setAcertou(Boolean acertou) {
        this.acertou = acertou;
    }

    public Double getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(Double pontuacao) {
        this.pontuacao = pontuacao;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getAnaliseDeTalhada() {
        return analiseDeTalhada;
    }

    public void setAnaliseDeTalhada(String analiseDeTalhada) {
        this.analiseDeTalhada = analiseDeTalhada;
    }

    public LocalDateTime getDataAnalise() {
        return dataAnalise;
    }

    public void setDataAnalise(LocalDateTime dataAnalise) {
        this.dataAnalise = dataAnalise;
    }

    public String getMetodoAnalise() {
        return metodoAnalise;
    }

    public void setMetodoAnalise(String metodoAnalise) {
        this.metodoAnalise = metodoAnalise;
    }
}