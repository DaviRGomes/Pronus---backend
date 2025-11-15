package com.inatel.prototipo_ia.dto.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para análise em lote de múltiplas palavras
 */
public class BatchPronunciationAnalysisDTO {

    private List<String> palavrasEsperadas;
    private String transcricaoCompleta;
    private List<ResultadoPalavra> resultados;
    private Double pontuacaoGeral;
    private Integer totalAcertos;
    private Integer totalPalavras;
    private Double porcentagemAcerto;
    private String feedbackGeral;
    private LocalDateTime dataAnalise;

    public BatchPronunciationAnalysisDTO() {
        this.dataAnalise = LocalDateTime.now();
    }

    // Classe interna para resultado individual
    public static class ResultadoPalavra {
        private String palavraEsperada;
        private String palavraTranscrita;
        private Boolean acertou;
        private Double similaridade;
        private String feedback;

        public ResultadoPalavra() {}

        public ResultadoPalavra(String palavraEsperada, String palavraTranscrita,
                                Boolean acertou, Double similaridade, String feedback) {
            this.palavraEsperada = palavraEsperada;
            this.palavraTranscrita = palavraTranscrita;
            this.acertou = acertou;
            this.similaridade = similaridade;
            this.feedback = feedback;
        }

        // Getters e Setters
        public String getPalavraEsperada() {
            return palavraEsperada;
        }

        public void setPalavraEsperada(String palavraEsperada) {
            this.palavraEsperada = palavraEsperada;
        }

        public String getPalavraTranscrita() {
            return palavraTranscrita;
        }

        public void setPalavraTranscrita(String palavraTranscrita) {
            this.palavraTranscrita = palavraTranscrita;
        }

        public Boolean getAcertou() {
            return acertou;
        }

        public void setAcertou(Boolean acertou) {
            this.acertou = acertou;
        }

        public Double getSimilaridade() {
            return similaridade;
        }

        public void setSimilaridade(Double similaridade) {
            this.similaridade = similaridade;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }
    }

    // Getters e Setters principais
    public List<String> getPalavrasEsperadas() {
        return palavrasEsperadas;
    }

    public void setPalavrasEsperadas(List<String> palavrasEsperadas) {
        this.palavrasEsperadas = palavrasEsperadas;
    }

    public String getTranscricaoCompleta() {
        return transcricaoCompleta;
    }

    public void setTranscricaoCompleta(String transcricaoCompleta) {
        this.transcricaoCompleta = transcricaoCompleta;
    }

    public List<ResultadoPalavra> getResultados() {
        return resultados;
    }

    public void setResultados(List<ResultadoPalavra> resultados) {
        this.resultados = resultados;
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

    public Double getPorcentagemAcerto() {
        return porcentagemAcerto;
    }

    public void setPorcentagemAcerto(Double porcentagemAcerto) {
        this.porcentagemAcerto = porcentagemAcerto;
    }

    public String getFeedbackGeral() {
        return feedbackGeral;
    }

    public void setFeedbackGeral(String feedbackGeral) {
        this.feedbackGeral = feedbackGeral;
    }

    public LocalDateTime getDataAnalise() {
        return dataAnalise;
    }

    public void setDataAnalise(LocalDateTime dataAnalise) {
        this.dataAnalise = dataAnalise;
    }
}