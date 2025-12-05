package com.inatel.prototipo_ia.dto.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MensagemSessaoDtoOut {

    public enum TipoMensagem {
        SAUDACAO,
        INSTRUCAO,
        PALAVRAS,
        FEEDBACK_ANALISE, // Alterado de FEEDBACK_CICLO
        RESUMO_FINAL,
        AGUARDANDO_AUDIO,
        ERRO
    }

    private Long sessaoId;
    private TipoMensagem tipo;
    private String mensagem;
    private List<String> palavras;
    private Integer cicloAtual; // Mantido para compatibilidade (sempre 1)
    private Integer totalCiclos; // Mantido para compatibilidade (sempre 1)
    private BatchPronunciationAnalysisDTO analise; // Novo campo para o resultado completo
    private ResumoSessao resumoSessao;
    private LocalDateTime timestamp;
    private Boolean sessaoFinalizada;

    // Classe interna para resultado de cada palavra (usado em BatchPronunciationAnalysisDTO)
    @Getter
    @Setter
    public static class ResultadoPalavra {
        private String palavraEsperada;
        private String palavraTranscrita;
        private Boolean acertou;
        private Double similaridade;
        private String feedback;
    }

    // Classe interna para resumo final
    @Getter
    @Setter
    public static class ResumoSessao {
        private Integer totalPalavras;
        private Integer totalAcertos;
        private Double pontuacaoGeral;
        private Double porcentagemAcerto;
        private String feedbackGeral;
        private List<String> pontosFortes;
        private List<String> pontosAMelhorar;
        private Integer duracaoMinutos;
    }

    public MensagemSessaoDtoOut() {
        this.timestamp = LocalDateTime.now();
        this.sessaoFinalizada = false;
    }

    // Factory methods atualizados
    public static MensagemSessaoDtoOut saudacao(Long sessaoId, String nomeCliente) {
        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessaoId);
        msg.setTipo(TipoMensagem.SAUDACAO);
        msg.setMensagem("Olá, " + nomeCliente + "! 👋 Que bom ter você aqui. Preparei um trava-língua para aquecermos!");
        msg.setCicloAtual(1);
        msg.setTotalCiclos(1);
        return msg;
    }

    public static MensagemSessaoDtoOut instrucao(Long sessaoId, int ciclo, int totalCiclos) {
        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessaoId);
        msg.setTipo(TipoMensagem.INSTRUCAO);
        msg.setCicloAtual(ciclo);
        msg.setTotalCiclos(totalCiclos);
        msg.setMensagem("Vamos começar! 🎯 Fale o trava-língua a seguir com calma e clareza.");
        return msg;
    }

    public static MensagemSessaoDtoOut palavras(Long sessaoId, int ciclo, int totalCiclos, List<String> palavras) {
        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessaoId);
        msg.setTipo(TipoMensagem.PALAVRAS);
        msg.setCicloAtual(ciclo);
        msg.setTotalCiclos(totalCiclos);
        msg.setPalavras(palavras);
        msg.setMensagem("Leia em voz alta:");
        return msg;
    }

    public static MensagemSessaoDtoOut aguardandoAudio(Long sessaoId, int ciclo, int totalCiclos) {
        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessaoId);
        msg.setTipo(TipoMensagem.AGUARDANDO_AUDIO);
        msg.setCicloAtual(ciclo);
        msg.setTotalCiclos(totalCiclos);
        msg.setMensagem("Estou ouvindo... 🎤 Grave seu áudio quando estiver pronto!");
        return msg;
    }

    public static MensagemSessaoDtoOut feedbackAnalise(Long sessaoId, BatchPronunciationAnalysisDTO resultado) {
        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessaoId);
        msg.setTipo(TipoMensagem.FEEDBACK_ANALISE);
        msg.setCicloAtual(1);
        msg.setTotalCiclos(1);
        msg.setAnalise(resultado);
        msg.setMensagem(resultado.getFeedbackGeral());
        return msg;
    }

    public static MensagemSessaoDtoOut resumoFinal(Long sessaoId, ResumoSessao resumo) {
        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessaoId);
        msg.setTipo(TipoMensagem.RESUMO_FINAL);
        msg.setResumoSessao(resumo);
        msg.setSessaoFinalizada(true);

        String feedback;
        if (resumo.getPontuacaoGeral() >= 80) {
            feedback = "🎉 Parabéns! Sessão finalizada com sucesso! Você foi incrível! Pontuação: " +
                    String.format("%.0f", resumo.getPorcentagemAcerto()) + "%.";
        } else if (resumo.getPontuacaoGeral() >= 60) {
            feedback = "👏 Muito bem! Sessão concluída! Continue praticando e vai melhorar cada vez mais!";
        } else {
            feedback = "💪 Sessão finalizada! A prática constante vai te ajudar a evoluir!";
        }

        msg.setMensagem(feedback);
        return msg;
    }

    public static MensagemSessaoDtoOut erro(Long sessaoId, String mensagemErro) {
        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessaoId);
        msg.setTipo(TipoMensagem.ERRO);
        msg.setMensagem("Ops! 😅 " + mensagemErro);
        return msg;
    }
}