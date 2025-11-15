package com.inatel.prototipo_ia.controller;

import com.inatel.prototipo_ia.dto.out.BatchPronunciationAnalysisDTO;
import com.inatel.prototipo_ia.service.PronunciationAnalysisService;
import com.inatel.prototipo_ia.service.AIWordGeneratorService;
import com.inatel.prototipo_ia.service.GeminiAudioAnalysisService;
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
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pronunciation")
@CrossOrigin(origins = "*")
@Tag(name = "Análise de Pronúncia", description = "Endpoints para análise de pronúncia com IA")
public class PronunciationController {

    @Autowired
    private PronunciationAnalysisService pronunciationService;

    @Autowired
    private AIWordGeneratorService aiWordGeneratorService;

    @Autowired
    private GeminiAudioAnalysisService geminiAudioAnalysisService;

    /**
     * Endpoint para analisar pronúncia em lote usando DEEPGRAM
     * POST /api/pronunciation/analyze-batch-deepgram
     */
    @Operation(
            summary = "Analisar pronúncia em lote (Deepgram)",
            description = "Usa Deepgram para transcrever e analisar múltiplas palavras"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Análise em lote realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro no processamento")
    })
    @PostMapping(value = "/analyze-batch-deepgram", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analisarPronunciaDeepgram(
            @Parameter(description = "Arquivo de áudio com todas as palavras", required = true)
            @RequestParam("audio") MultipartFile audioFile,

            @Parameter(description = "Lista de palavras esperadas (separadas por vírgula)",
                    example = "rato,carro,terra", required = true)
            @RequestParam("palavrasEsperadas") String palavrasEsperadas) {

        try {
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(criarErro("Arquivo de áudio não pode estar vazio"));
            }

            if (palavrasEsperadas == null || palavrasEsperadas.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(criarErro("Lista de palavras não pode estar vazia"));
            }

            List<String> listaPalavras = Arrays.stream(palavrasEsperadas.split(","))
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.toList());

            if (listaPalavras.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(criarErro("Nenhuma palavra válida foi fornecida"));
            }

            byte[] audioBytes = audioFile.getBytes();

            BatchPronunciationAnalysisDTO resultado = pronunciationService
                    .analisarPronunciaEmLote(audioBytes, listaPalavras);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarErro("Erro ao processar áudio com Deepgram: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para analisar pronúncia em lote usando GEMINI
     * POST /api/pronunciation/analyze-batch-gemini
     */
    @Operation(
            summary = "Analisar pronúncia em lote (Gemini)",
            description = "Usa Gemini AI para analisar múltiplas palavras diretamente do áudio (análise mais rica)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Análise em lote realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro no processamento")
    })
    @PostMapping(value = "/analyze-batch-gemini", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analisarPronunciaGemini(
            @Parameter(description = "Arquivo de áudio com todas as palavras", required = true)
            @RequestParam("audio") MultipartFile audioFile,

            @Parameter(description = "Lista de palavras esperadas (separadas por vírgula)",
                    example = "rato,carro,terra", required = true)
            @RequestParam("palavrasEsperadas") String palavrasEsperadas) {

        try {
            if (audioFile == null || audioFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(criarErro("Arquivo de áudio não pode estar vazio"));
            }

            if (palavrasEsperadas == null || palavrasEsperadas.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(criarErro("Lista de palavras não pode estar vazia"));
            }

            List<String> listaPalavras = Arrays.stream(palavrasEsperadas.split(","))
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.toList());

            if (listaPalavras.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(criarErro("Nenhuma palavra válida foi fornecida"));
            }

            byte[] audioBytes = audioFile.getBytes();

            BatchPronunciationAnalysisDTO resultado = geminiAudioAnalysisService
                    .analisarPronunciaEmLote(audioBytes, listaPalavras);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarErro("Erro ao processar áudio com Gemini: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para gerar palavras personalizadas
     * GET /api/pronunciation/words
     */
    @Operation(
            summary = "Gerar palavras personalizadas",
            description = "Gera lista de palavras baseada na idade e dificuldade do usuário"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Palavras geradas com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao gerar palavras")
    })
    @GetMapping("/words")
    public ResponseEntity<?> gerarPalavras(
            @Parameter(description = "Idade do usuário", example = "8", required = true)
            @RequestParam int idade,

            @Parameter(description = "Tipo de dificuldade (R, L, S, CH, LH, GERAL)", example = "R", required = true)
            @RequestParam String dificuldade,

            @Parameter(description = "Quantidade de palavras", example = "10")
            @RequestParam(defaultValue = "10") int quantidade) {

        try {
            List<String> palavras = aiWordGeneratorService
                    .gerarPalavrasComIA(idade, dificuldade, quantidade);

            Map<String, Object> response = new HashMap<>();
            response.put("palavras", palavras);
            response.put("total", palavras.size());
            response.put("idade", idade);
            response.put("dificuldade", dificuldade);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(criarErro("Erro ao gerar palavras com IA: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para listar dificuldades disponíveis
     * GET /api/pronunciation/difficulties
     */
    @Operation(
            summary = "Listar dificuldades disponíveis",
            description = "Retorna lista de todas as dificuldades de pronúncia disponíveis no sistema"
    )
    @GetMapping("/difficulties")
    public ResponseEntity<List<String>> listarDificuldades() {
        return ResponseEntity.ok(aiWordGeneratorService.getDificuldadesDisponiveis());
    }

    /**
     * Health check da API de pronúncia
     */
    @Operation(
            summary = "Health Check",
            description = "Verifica se a API está funcionando"
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Pronunciation Analysis API");
        return ResponseEntity.ok(status);
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