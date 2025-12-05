package com.inatel.prototipo_ia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessaotreino")
@Getter
@Setter
public class SessaoTreinoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @ManyToOne
    @JoinColumn(name = "especialista_id", nullable = false)
    private EspecialistaEntity especialista;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    // Configurações da sessão
    @Column(nullable = false)
    private String dificuldade;

    @Column(name = "idade_cliente", nullable = false)
    private Integer idadeCliente;

    // Conteúdo do treino
    @Column(name = "trava_lingua", columnDefinition = "TEXT")
    private String travaLingua;

    // Resultado da análise (JSON serializado)
    @Column(name = "resultado", columnDefinition = "TEXT")
    private String resultado;

    // Estado atual da sessão
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSessao status;

    // Métricas gerais
    @Column(name = "total_palavras")
    private Integer totalPalavras;

    @Column(name = "total_acertos")
    private Integer totalAcertos;

    @Column(name = "pontuacao_geral")
    private Double pontuacaoGeral;

    // Timestamps
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    // Conversa completa (para histórico)
    @Column(name = "historico_conversa", columnDefinition = "TEXT")
    private String historicoConversa;

    public enum StatusSessao {
        INICIADA,
        AGUARDANDO_AUDIO,
        PROCESSANDO,
        FINALIZADA,
        CANCELADA
    }

    // Construtor padrão
    public SessaoTreinoEntity() {
        this.dataInicio = LocalDateTime.now();
        this.status = StatusSessao.INICIADA;
        this.totalPalavras = 0;
        this.totalAcertos = 0;
        this.pontuacaoGeral = 0.0;
        this.historicoConversa = "";
    }

    // Métodos auxiliares
    public void adicionarAoHistorico(String remetente, String mensagem) {
        String timestamp = LocalDateTime.now().toString();
        String novaLinha = "[" + timestamp + "] " + remetente + ": " + mensagem + "\n";
        this.historicoConversa = (this.historicoConversa == null ? "" : this.historicoConversa) + novaLinha;
    }
}