package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.in.SessaoTreinoDtoIn;
import com.inatel.prototipo_ia.dto.out.MensagemSessaoDtoOut;
import com.inatel.prototipo_ia.dto.out.SessaoTreinoHistoryDtoOut;
import com.inatel.prototipo_ia.service.SessaoTreinoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessao-treino")
@CrossOrigin(origins = "*")
@Tag(name = "Sessão de Treino", description = "Endpoints para gerenciar sessões de treino conversacionais")
public class SessaoTreinoController {

    @Autowired
    private SessaoTreinoService sessaoService;

    /**
     * PASSO 1: Iniciar uma nova sessão de treino
     * POST /api/sessao-treino/iniciar
     *
     * Retorna: Saudação + Instrução + 3 palavras do primeiro ciclo
     */
    @Operation(
            summary = "Iniciar sessão de treino",
            description = "Inicia uma nova sessão de treino. Retorna saudação inicial e as 3 primeiras palavras."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessão iniciada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente ou especialista não encontrado")
    })
    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciarSessao(@RequestBody SessaoTreinoDtoIn dto) {
        try {
            if (dto.getClienteId() == null) {
                return ResponseEntity.badRequest().body(criarErro("clienteId é obrigatório"));
            }
            if (dto.getEspecialistaId() == null) {
                return ResponseEntity.badRequest().body(criarErro("especialistaId é obrigatório"));
            }

            List<MensagemSessaoDtoOut> mensagens = sessaoService.iniciarSessao(dto);
            return ResponseEntity.ok(mensagens);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarErro("Erro ao iniciar sessão: " + e.getMessage()));
        }
    }

    /**
     * PASSO 2: Enviar áudio para análise
     * POST /api/sessao-treino/{sessaoId}/audio
     *
     * Retorna: Feedback do ciclo atual + próximas 3 palavras (ou resumo final)
     */
    @Operation(
            summary = "Enviar áudio para análise",
            description = "Envia o áudio gravado para análise. Retorna feedback e próximas palavras ou resumo final."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Áudio processado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Áudio inválido ou sessão não está aguardando áudio"),
            @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    })
    @PostMapping(value = "/{sessaoId}/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enviarAudio(
            @Parameter(description = "ID da sessão de treino", required = true)
            @PathVariable Long sessaoId,

            @Parameter(description = "Arquivo de áudio com as palavras faladas", required = true)
            @RequestParam("audio") MultipartFile audioFile,

            @Parameter(description = "Usar Gemini para análise (mais preciso, porém mais lento)")
            @RequestParam(value = "usarGemini", defaultValue = "false") boolean usarGemini) {

        try {
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(criarErro("Arquivo de áudio é obrigatório"));
            }

            byte[] audioBytes = audioFile.getBytes();
            List<MensagemSessaoDtoOut> mensagens = sessaoService.processarAudio(sessaoId, audioBytes, usarGemini);
            return ResponseEntity.ok(mensagens);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarErro("Erro ao processar áudio: " + e.getMessage()));
        }
    }

    /**
     * Consultar estado atual da sessão
     * GET /api/sessao-treino/{sessaoId}/estado
     */
    @Operation(
            summary = "Consultar estado da sessão",
            description = "Retorna o estado atual da sessão (ciclo, palavras pendentes, etc.)"
    )
    @GetMapping("/{sessaoId}/estado")
    public ResponseEntity<?> buscarEstado(@PathVariable Long sessaoId) {
        try {
            MensagemSessaoDtoOut estado = sessaoService.buscarEstadoSessao(sessaoId);
            return ResponseEntity.ok(estado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(criarErro("Sessão não encontrada: " + sessaoId));
        }
    }

    /**
     * Cancelar sessão
     * POST /api/sessao-treino/{sessaoId}/cancelar
     */
    @Operation(
            summary = "Cancelar sessão",
            description = "Cancela uma sessão em andamento"
    )
    @PostMapping("/{sessaoId}/cancelar")
    public ResponseEntity<?> cancelarSessao(@PathVariable Long sessaoId) {
        try {
            MensagemSessaoDtoOut resultado = sessaoService.cancelarSessao(sessaoId);
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(criarErro("Sessão não encontrada: " + sessaoId));
        }
    }

    /**
     * Buscar histórico de sessões de um cliente
     * GET /api/sessao-treino/historico/cliente/{clienteId}
     */
    @Operation(
            summary = "Buscar histórico de sessões do cliente",
            description = "Retorna a lista de sessões realizadas pelo cliente com seus resultados"
    )
    @GetMapping("/historico/cliente/{clienteId}")
    public ResponseEntity<?> buscarHistorico(@PathVariable Long clienteId) {
        try {
            List<SessaoTreinoHistoryDtoOut> historico = sessaoService.buscarHistoricoPorCliente(clienteId);
            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarErro("Erro ao buscar histórico: " + e.getMessage()));
        }
    }

    /**
     * Helper para criar mensagens de erro padronizadas
     */
    private Map<String, String> criarErro(String mensagem) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", mensagem);
        erro.put("timestamp", java.time.LocalDateTime.now().toString());
        return erro;
    }
}