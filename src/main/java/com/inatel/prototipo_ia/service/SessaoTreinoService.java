package com.inatel.prototipo_ia.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inatel.prototipo_ia.adapter.LocalDateTimeAdapter;
import com.inatel.prototipo_ia.dto.in.SessaoTreinoDtoIn;
import com.inatel.prototipo_ia.dto.out.BatchPronunciationAnalysisDTO;
import com.inatel.prototipo_ia.dto.out.DashboardDtoOut;
import com.inatel.prototipo_ia.dto.out.MensagemSessaoDtoOut;
import com.inatel.prototipo_ia.dto.out.MensagemSessaoDtoOut.ResumoSessao;
import com.inatel.prototipo_ia.dto.out.SessaoTreinoHistoryDtoOut;
import com.inatel.prototipo_ia.entity.ClienteEntity;
import com.inatel.prototipo_ia.entity.EspecialistaEntity;
import com.inatel.prototipo_ia.entity.SessaoTreinoEntity;
import com.inatel.prototipo_ia.entity.SessaoTreinoEntity.StatusSessao;
import com.inatel.prototipo_ia.repository.ClienteRepository;
import com.inatel.prototipo_ia.repository.EspecialistaRepository;
import com.inatel.prototipo_ia.repository.SessaoTreinoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
@Transactional
public class SessaoTreinoService {

    private final SessaoTreinoRepository sessaoRepository;
    private final ClienteRepository clienteRepository;
    private final EspecialistaRepository especialistaRepository;
    private final AIWordGeneratorService wordGeneratorService;
    private final GeminiAudioAnalysisService geminiService;
    private final Gson gson;

    public SessaoTreinoService(
            SessaoTreinoRepository sessaoRepository,
            ClienteRepository clienteRepository,
            EspecialistaRepository especialistaRepository,
            AIWordGeneratorService wordGeneratorService,
            GeminiAudioAnalysisService geminiService) {
        this.sessaoRepository = sessaoRepository;
        this.clienteRepository = clienteRepository;
        this.especialistaRepository = especialistaRepository;
        this.wordGeneratorService = wordGeneratorService;
        this.geminiService = geminiService;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    // ... (campos e construtor iguais)

    /**
     * Retorna estatísticas para o dashboard do cliente
     */
    public DashboardDtoOut buscarDashboard(Long clienteId) {
        // Buscar todas as sessões finalizadas do cliente
        List<SessaoTreinoEntity> sessoes = sessaoRepository.findByClienteIdAndStatus(
                clienteId, StatusSessao.FINALIZADA);

        // Ordenar por data (mais antiga -> mais recente)
        sessoes.sort(Comparator.comparing(SessaoTreinoEntity::getDataInicio));

        // 1. Total de sessões realizadas
        int totalSessoes = sessoes.size();

        // 2. Pontuação média (0 a 100)
        double somaPontuacao = sessoes.stream()
                .mapToDouble(s -> s.getPontuacaoGeral() != null ? s.getPontuacaoGeral() : 0.0)
                .sum();
        int pontuacaoMedia = totalSessoes > 0 ? (int) Math.round(somaPontuacao / totalSessoes) : 0;

        // 3. Evolução (comparar média das 3 últimas vs média das 3 primeiras ou vs histórico geral)
        int evolucao = 0;
        if (totalSessoes >= 2) {
            double ultimaPontuacao = sessoes.get(totalSessoes - 1).getPontuacaoGeral();
            // Média das anteriores (excluindo a última)
            double somaAnteriores = sessoes.subList(0, totalSessoes - 1).stream()
                    .mapToDouble(s -> s.getPontuacaoGeral() != null ? s.getPontuacaoGeral() : 0.0)
                    .sum();
            double mediaAnteriores = somaAnteriores / (totalSessoes - 1);
            
            // Diferença percentual em pontos
            evolucao = (int) Math.round(ultimaPontuacao - mediaAnteriores);
        }
        
        // 4. Cálculo de Dias Seguidos (Streak)
        int diasSeguidos = 0;
        if (totalSessoes > 0) {
            // Pega datas únicas de treino, ordenadas decrescente (da mais recente para antiga)
            List<LocalDate> datasTreino = sessoes.stream()
                    .map(s -> s.getDataInicio().toLocalDate())
                    .distinct()
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());

            LocalDate hoje = LocalDate.now();
            LocalDate ontem = hoje.minusDays(1);
            LocalDate ultimaData = datasTreino.get(0);

            // Se a última data for hoje ou ontem, o streak está ativo
            if (ultimaData.isEqual(hoje) || ultimaData.isEqual(ontem)) {
                diasSeguidos = 1; // Pelo menos 1 dia (o último treino)
                LocalDate dataAtualStreak = ultimaData;

                for (int i = 1; i < datasTreino.size(); i++) {
                    LocalDate dataAnterior = datasTreino.get(i);
                    // Se a data anterior for exatamente 1 dia antes da data atual do streak
                    if (dataAnterior.isEqual(dataAtualStreak.minusDays(1))) {
                        diasSeguidos++;
                        dataAtualStreak = dataAnterior;
                    } else {
                        break; // Quebrou a sequência
                    }
                }
            }
        }

        // 5. Observação dinâmica
        String observacao;
        if (totalSessoes == 0) {
            observacao = "Você ainda não realizou nenhuma sessão. Comece agora para ver seu progresso!";
        } else if (diasSeguidos >= 3) {
            observacao = "Incrível! Você está treinando há " + diasSeguidos + " dias seguidos. Mantenha o foco!";
        } else if (evolucao > 5) {
            observacao = "Parabéns! Você teve uma evolução notável na última sessão comparado à sua média histórica.";
        } else if (evolucao < -5) {
            observacao = "Sua última sessão foi um pouco abaixo da sua média. Continue praticando para recuperar o ritmo!";
        } else if (pontuacaoMedia >= 80) {
            observacao = "Você mantém um desempenho consistentemente alto. Excelente trabalho!";
        } else {
            observacao = "Você está mantendo um ritmo constante. Tente aumentar um pouco a dificuldade na próxima vez.";
        }

        return new DashboardDtoOut(totalSessoes, pontuacaoMedia, evolucao, observacao, diasSeguidos);
    }


    /**
     * Inicia uma nova sessão de treino baseada em trava-língua.
     */
    public List<MensagemSessaoDtoOut> iniciarSessao(SessaoTreinoDtoIn dto) {
        List<MensagemSessaoDtoOut> mensagens = new ArrayList<>();

        ClienteEntity cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + dto.getClienteId()));

        EspecialistaEntity especialista = especialistaRepository.findById(dto.getEspecialistaId())
                .orElseThrow(() -> new EntityNotFoundException("Especialista não encontrado: " + dto.getEspecialistaId()));

        // Se já houver sessão ativa, recupera em vez de criar uma nova.
        List<StatusSessao> statusAtivos = Arrays.asList(StatusSessao.INICIADA, StatusSessao.AGUARDANDO_AUDIO, StatusSessao.PROCESSANDO);
        List<SessaoTreinoEntity> sessoesAtivas = sessaoRepository.findSessoesAtivasByClienteId(dto.getClienteId());
        if (!sessoesAtivas.isEmpty()) {
            return recuperarEstadoSessao(sessoesAtivas.get(0));
        }

        SessaoTreinoEntity sessao = new SessaoTreinoEntity();
        sessao.setCliente(cliente);
        sessao.setEspecialista(especialista);
        sessao.setDificuldade(dto.getDificuldade() != null ? dto.getDificuldade() : "GERAL");
        sessao.setIdadeCliente(dto.getIdade() != null ? dto.getIdade() : cliente.getIdade());

        // Gera um trava-língua
        List<String> travaLinguaList = wordGeneratorService.gerarPalavrasComIA(
                sessao.getIdadeCliente(),
                sessao.getDificuldade(),
                1 // Quantidade é ignorada, mas passamos 1 por clareza
        );

        if (travaLinguaList == null || travaLinguaList.isEmpty()) {
            throw new IllegalStateException("A IA não conseguiu gerar um trava-língua.");
        }
        sessao.setTravaLingua(travaLinguaList.get(0));
        
        sessao = sessaoRepository.save(sessao);

        // Monta mensagens de saudação
        MensagemSessaoDtoOut saudacao = MensagemSessaoDtoOut.saudacao(sessao.getId(), cliente.getNome());
        sessao.adicionarAoHistorico("SISTEMA", saudacao.getMensagem());
        mensagens.add(saudacao);

        // Adiciona instrução
        MensagemSessaoDtoOut instrucao = MensagemSessaoDtoOut.instrucao(sessao.getId(), 1, 1); // Ciclo único
        sessao.adicionarAoHistorico("SISTEMA", instrucao.getMensagem());
        mensagens.add(instrucao);
        
        // Adiciona o trava-língua
        MensagemSessaoDtoOut palavras = MensagemSessaoDtoOut.palavras(sessao.getId(), 1, 1, travaLinguaList);
        sessao.adicionarAoHistorico("SISTEMA", "Trava-língua: " + sessao.getTravaLingua());
        mensagens.add(palavras);

        // Mensagem aguardando áudio
        MensagemSessaoDtoOut aguardando = MensagemSessaoDtoOut.aguardandoAudio(sessao.getId(), 1, 1);
        mensagens.add(aguardando);

        sessao.setStatus(StatusSessao.AGUARDANDO_AUDIO);
        sessaoRepository.save(sessao);

        return mensagens;
    }

    /**
     * Processa o áudio do trava-língua e finaliza a sessão.
     */
    public List<MensagemSessaoDtoOut> processarAudio(Long sessaoId, byte[] audioBytes, boolean usarGemini) {
        SessaoTreinoEntity sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new EntityNotFoundException("Sessão não encontrada: " + sessaoId));

        if (sessao.getStatus() != StatusSessao.AGUARDANDO_AUDIO) {
            return Collections.singletonList(MensagemSessaoDtoOut.erro(sessaoId, "Sessão não está aguardando áudio. Status atual: " + sessao.getStatus()));
        }

        sessao.setStatus(StatusSessao.PROCESSANDO);
        sessaoRepository.save(sessao);

        try {
            // Pega o trava-língua e quebra em palavras para análise
            String travaLingua = sessao.getTravaLingua();
            List<String> palavrasEsperadas = Arrays.asList(travaLingua.replaceAll("[^\\p{L}\\s]", "").toLowerCase().split("\\s+"));

            // Analisa a pronúncia
            BatchPronunciationAnalysisDTO resultado = geminiService.analisarPronunciaEmLote(audioBytes, palavrasEsperadas);

            sessao.adicionarAoHistorico("CLIENTE", "[ÁUDIO ENVIADO]");
            sessao.setResultado(gson.toJson(resultado));
            
            // Atualiza totais
            sessao.setTotalPalavras(resultado.getTotalPalavras() != null ? resultado.getTotalPalavras() : 0);
            sessao.setTotalAcertos(resultado.getTotalAcertos() != null ? resultado.getTotalAcertos() : 0);
            
            // Finaliza a sessão com o resultado
            return finalizarSessao(sessao, resultado);

        } catch (Exception e) {
            sessao.setStatus(StatusSessao.AGUARDANDO_AUDIO); // Volta para aguardando
            sessaoRepository.save(sessao);
            return Collections.singletonList(MensagemSessaoDtoOut.erro(sessaoId, "Erro ao processar áudio: " + e.getMessage() + ". Por favor, tente enviar novamente."));
        }
    }

    /**
     * Finaliza a sessão e retorna o resumo.
     */
    private List<MensagemSessaoDtoOut> finalizarSessao(SessaoTreinoEntity sessao, BatchPronunciationAnalysisDTO resultadoAnalise) {
        sessao.setStatus(StatusSessao.FINALIZADA);
        sessao.setDataFim(LocalDateTime.now());

        double pontuacaoGeral = resultadoAnalise.getPontuacaoGeral() != null ? resultadoAnalise.getPontuacaoGeral() : 0.0;
        sessao.setPontuacaoGeral(pontuacaoGeral);

        // Monta o resumo final
        ResumoSessao resumo = new ResumoSessao();
        resumo.setTotalPalavras(sessao.getTotalPalavras());
        resumo.setTotalAcertos(sessao.getTotalAcertos());
        resumo.setPontuacaoGeral(pontuacaoGeral);
        resumo.setPorcentagemAcerto(pontuacaoGeral);
        resumo.setDuracaoMinutos((int) Duration.between(sessao.getDataInicio(), sessao.getDataFim()).toMinutes());
        
        List<String> pontosFortes = new ArrayList<>();
        List<String> pontosAMelhorar = new ArrayList<>();

        if (pontuacaoGeral >= 80) {
            pontosFortes.add("Excelente articulação geral");
            pontosFortes.add("Boa pronúncia do fonema " + sessao.getDificuldade());
        } else if (pontuacaoGeral >= 60) {
            pontosFortes.add("Boa evolução durante a sessão");
            pontosAMelhorar.add("Pratique mais o fonema " + sessao.getDificuldade());
        } else {
            pontosAMelhorar.add("Foque na articulação do fonema " + sessao.getDificuldade());
            pontosAMelhorar.add("Pratique falar mais devagar");
        }
        resumo.setPontosFortes(pontosFortes);
        resumo.setPontosAMelhorar(pontosAMelhorar);
        resumo.setFeedbackGeral(resultadoAnalise.getFeedbackGeral());

        MensagemSessaoDtoOut msgFinal = MensagemSessaoDtoOut.resumoFinal(sessao.getId(), resumo);
        sessao.adicionarAoHistorico("SISTEMA", "Sessão finalizada. Pontuação: " + pontuacaoGeral);
        
        sessaoRepository.save(sessao);

        // Retorna o feedback do resultado da analise + o resumo final
        return Arrays.asList(
                MensagemSessaoDtoOut.feedbackAnalise(sessao.getId(), resultadoAnalise),
                msgFinal
        );
    }
    
    /**
     * Recupera o estado de uma sessão de trava-língua existente.
     */
    private List<MensagemSessaoDtoOut> recuperarEstadoSessao(SessaoTreinoEntity sessao) {
        List<MensagemSessaoDtoOut> mensagens = new ArrayList<>();

        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessao.getId());
        msg.setTipo(MensagemSessaoDtoOut.TipoMensagem.INSTRUCAO);
        msg.setMensagem("Ei, você tem uma sessão em andamento! 👋 Vamos continuar de onde paramos?");
        mensagens.add(msg);

        // Adiciona o trava-língua atual
        MensagemSessaoDtoOut palavras = MensagemSessaoDtoOut.palavras(
                sessao.getId(), 1, 1, Arrays.asList(sessao.getTravaLingua()));
        mensagens.add(palavras);

        MensagemSessaoDtoOut aguardando = MensagemSessaoDtoOut.aguardandoAudio(
                sessao.getId(), 1, 1);
        mensagens.add(aguardando);

        return mensagens;
    }

    public MensagemSessaoDtoOut buscarEstadoSessao(Long sessaoId) {
        SessaoTreinoEntity sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new EntityNotFoundException("Sessão não encontrada: " + sessaoId));

        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessaoId);
        msg.setSessaoFinalizada(sessao.getStatus() == StatusSessao.FINALIZADA);

        if (sessao.getStatus() == StatusSessao.AGUARDANDO_AUDIO) {
            msg.setTipo(MensagemSessaoDtoOut.TipoMensagem.AGUARDANDO_AUDIO);
            msg.setPalavras(Arrays.asList(sessao.getTravaLingua()));
            msg.setMensagem("Aguardando seu áudio... 🎤");
        } else if (sessao.getStatus() == StatusSessao.FINALIZADA) {
            msg.setTipo(MensagemSessaoDtoOut.TipoMensagem.RESUMO_FINAL);
            msg.setMensagem("Sessão finalizada! Pontuação: " + String.format("%.0f", sessao.getPontuacaoGeral()) + "%");
        } else {
            msg.setTipo(MensagemSessaoDtoOut.TipoMensagem.INSTRUCAO);
            msg.setMensagem("Status: " + sessao.getStatus());
        }
        return msg;
    }

    public MensagemSessaoDtoOut cancelarSessao(Long sessaoId) {
        SessaoTreinoEntity sessao = sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new EntityNotFoundException("Sessão não encontrada: " + sessaoId));

        sessao.setStatus(StatusSessao.CANCELADA);
        sessao.setDataFim(LocalDateTime.now());
        sessao.adicionarAoHistorico("SISTEMA", "Sessão cancelada pelo usuário");
        sessaoRepository.save(sessao);

        MensagemSessaoDtoOut msg = new MensagemSessaoDtoOut();
        msg.setSessaoId(sessaoId);
        msg.setTipo(MensagemSessaoDtoOut.TipoMensagem.INSTRUCAO);
        msg.setMensagem("Sessão cancelada. Até a próxima! 👋");
        msg.setSessaoFinalizada(true);
        return msg;
    }

    public List<SessaoTreinoHistoryDtoOut> buscarHistoricoPorCliente(Long clienteId) {
        List<SessaoTreinoEntity> sessoes = sessaoRepository.findByClienteId(clienteId);
        List<SessaoTreinoHistoryDtoOut> historico = new ArrayList<>();
        for (SessaoTreinoEntity s : sessoes) {
            String feedback = null;
            List<BatchPronunciationAnalysisDTO.ResultadoPalavra> detalhes = null;

            if (s.getResultado() != null && !s.getResultado().isEmpty()) {
                try {
                    BatchPronunciationAnalysisDTO analise = gson.fromJson(s.getResultado(), BatchPronunciationAnalysisDTO.class);
                    feedback = analise.getFeedbackGeral();
                    detalhes = analise.getResultados();
                } catch (Exception e) {
                    e.printStackTrace();
                    feedback = "Erro ao processar detalhes da sessão.";
                    detalhes = new ArrayList<>();
                    BatchPronunciationAnalysisDTO.ResultadoPalavra erro = new BatchPronunciationAnalysisDTO.ResultadoPalavra();
                    erro.setPalavraEsperada("ERRO_SISTEMA");
                    erro.setPalavraTranscrita("JSON Inválido");
                    erro.setAcertou(false);
                    erro.setFeedback("Erro: " + e.getMessage() + " | JSON: " + (s.getResultado().length() > 50 ? s.getResultado().substring(0, 50) + "..." : s.getResultado()));
                    detalhes.add(erro);
                }
            }

            historico.add(new SessaoTreinoHistoryDtoOut(
                    s.getId(),
                    s.getDataInicio(),
                    s.getDataFim(),
                    s.getPontuacaoGeral(),
                    s.getTotalAcertos(),
                    s.getTotalPalavras(),
                    s.getDificuldade(),
                    s.getStatus().name(),
                    feedback,
                    detalhes
            ));
        }
        return historico;
    }
}
