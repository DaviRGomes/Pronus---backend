package com.inatel.prototipo_ia.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.inatel.prototipo_ia.dto.out.BatchPronunciationAnalysisDTO;
import okhttp3.*;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PronunciationAnalysisService {

    @Value("${deepgram.api.key}")
    private String deepgramApiKey;

    private final OkHttpClient httpClient;
    private final LevenshteinDistance levenshtein;

    public PronunciationAnalysisService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.levenshtein = new LevenshteinDistance();
    }

    /**
     * Analisa múltiplas palavras em um único áudio (METODO NOVO)
     * @param audioBytes bytes do arquivo de áudio
     * @param palavrasEsperadas lista de palavras que deveriam ser pronunciadas
     * @return análise em lote com resultado de cada palavra
     */
    public BatchPronunciationAnalysisDTO analisarPronunciaEmLote(byte[] audioBytes, List<String> palavrasEsperadas) {
        try {
            // 1. Transcrever o áudio completo
            String transcricaoCompleta = transcreverAudio(audioBytes);

            // 2. Separar as palavras transcritas
            List<String> palavrasTranscritas = Arrays.stream(transcricaoCompleta.split("\\s+"))
                    .map(this::normalizarTexto)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.toList());

            // 3. Analisar cada palavra esperada
            List<BatchPronunciationAnalysisDTO.ResultadoPalavra> resultados = new ArrayList<>();
            int acertos = 0;
            double somaSimilaridades = 0.0;

            for (int i = 0; i < palavrasEsperadas.size(); i++) {
                String palavraEsperada = palavrasEsperadas.get(i);
                String palavraTranscrita = i < palavrasTranscritas.size() ?
                        palavrasTranscritas.get(i) : "";

                double similaridade = calcularSimilaridade(palavraEsperada, palavraTranscrita);
                boolean acertou = similaridade >= 0.7; // 70% de similaridade

                if (acertou) acertos++;
                somaSimilaridades += similaridade;

                String feedback = gerarFeedbackPalavra(similaridade, palavraEsperada, palavraTranscrita);

                resultados.add(new BatchPronunciationAnalysisDTO.ResultadoPalavra(
                        palavraEsperada,
                        palavraTranscrita,
                        acertou,
                        similaridade * 100,
                        feedback
                ));
            }

            // 4. Calcular métricas gerais
            double pontuacaoGeral = (somaSimilaridades / palavrasEsperadas.size()) * 100;
            double porcentagemAcerto = ((double) acertos / palavrasEsperadas.size()) * 100;

            // 5. Gerar feedback geral
            String feedbackGeral = gerarFeedbackGeral(acertos, palavrasEsperadas.size(), pontuacaoGeral);

            // 6. Criar DTO de resposta
            BatchPronunciationAnalysisDTO resultado = new BatchPronunciationAnalysisDTO();
            resultado.setPalavrasEsperadas(palavrasEsperadas);
            resultado.setTranscricaoCompleta(transcricaoCompleta);
            resultado.setResultados(resultados);
            resultado.setPontuacaoGeral(pontuacaoGeral);
            resultado.setTotalAcertos(acertos);
            resultado.setTotalPalavras(palavrasEsperadas.size());
            resultado.setPorcentagemAcerto(porcentagemAcerto);
            resultado.setFeedbackGeral(feedbackGeral);

            return resultado;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao analisar pronúncia em lote: " + e.getMessage(), e);
        }
    }

    /**
     * Transcreve o áudio usando Deepgram API
     */
    private String transcreverAudio(byte[] audioBytes) throws IOException {
        RequestBody body = RequestBody.create(audioBytes, MediaType.parse("audio/wav"));

        Request request = new Request.Builder()
                .url("https://api.deepgram.com/v1/listen?language=pt-BR&punctuate=false&diarize=false")
                .addHeader("Authorization", "Token " + deepgramApiKey)
                .addHeader("Content-Type", "audio/wav")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erro na API: " + response.code());
            }

            String jsonResponse = response.body().string();
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            String transcript = json.getAsJsonObject("results")
                    .getAsJsonArray("channels")
                    .get(0).getAsJsonObject()
                    .getAsJsonArray("alternatives")
                    .get(0).getAsJsonObject()
                    .get("transcript").getAsString();

            return normalizarTexto(transcript);
        }
    }

    /**
     * Calcula similaridade entre duas palavras
     */
    private double calcularSimilaridade(String palavra1, String palavra2) {
        String p1 = normalizarTexto(palavra1);
        String p2 = normalizarTexto(palavra2);

        if (p1.equals(p2)) {
            return 1.0;
        }

        int distancia = levenshtein.apply(p1, p2);
        int maxLen = Math.max(p1.length(), p2.length());

        return 1.0 - ((double) distancia / maxLen);
    }

    /**
     * Remove acentos e normaliza texto
     */
    private String normalizarTexto(String texto) {
        if (texto == null) return "";

        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[^\\p{ASCII}]", "");
        return normalized.toLowerCase().trim();
    }

    /**
     * Gera feedback para uma palavra individual na análise em lote
     */
    private String gerarFeedbackPalavra(double similaridade, String esperada, String transcrita) {
        double pontuacao = similaridade * 100;

        if (pontuacao >= 90) {
            return "Perfeito!";
        } else if (pontuacao >= 70) {
            return "Muito bom!";
        } else if (pontuacao >= 50) {
            return "Quase lá";
        } else {
            return "Tente novamente";
        }
    }

    /**
     * Gera feedback geral para análise em lote
     */
    private String gerarFeedbackGeral(int acertos, int total, double pontuacaoGeral) {
        double porcentagem = ((double) acertos / total) * 100;

        if (porcentagem >= 90) {
            return String.format("Excelente desempenho! Você acertou %d de %d palavras (%.1f%%). Continue assim! 🎉",
                    acertos, total, porcentagem);
        } else if (porcentagem >= 70) {
            return String.format("Muito bom! Você acertou %d de %d palavras (%.1f%%). Está progredindo bem! 👏",
                    acertos, total, porcentagem);
        } else if (porcentagem >= 50) {
            return String.format("Bom trabalho! Você acertou %d de %d palavras (%.1f%%). Continue praticando! 👍",
                    acertos, total, porcentagem);
        } else {
            return String.format("Você acertou %d de %d palavras (%.1f%%). Vamos praticar mais essas palavras! 💪",
                    acertos, total, porcentagem);
        }
    }
}