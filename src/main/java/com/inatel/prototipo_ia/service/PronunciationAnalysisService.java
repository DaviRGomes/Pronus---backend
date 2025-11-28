package com.inatel.prototipo_ia.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.inatel.prototipo_ia.dto.out.BatchPronunciationAnalysisDTO;
import okhttp3.*;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    public BatchPronunciationAnalysisDTO analisarPronunciaEmLote(byte[] audioBytes, List<String> palavrasEsperadas) {
        try {
            // 1. Transcrição (Agora com menos viés)
            String transcricaoCompleta = transcreverAudio(audioBytes, palavrasEsperadas);
            System.out.println("📝 Transcrição IA (O que ela ouviu): " + transcricaoCompleta);

            // 2. Normalização
            List<String> palavrasTranscritas = Arrays.stream(transcricaoCompleta.split("\\s+"))
                    .map(this::normalizarTexto)
                    .filter(p -> !p.isEmpty())
                    .collect(Collectors.toList());

            List<BatchPronunciationAnalysisDTO.ResultadoPalavra> resultados = new ArrayList<>();
            int acertos = 0;
            double somaSimilaridades = 0.0;

            boolean[] palavrasUsadas = new boolean[palavrasTranscritas.size()];

            for (String palavraEsperada : palavrasEsperadas) {
                String pEsperadaNorm = normalizarTexto(palavraEsperada);
                
                String melhorPalavraEncontrada = "";
                double melhorScore = 0.0;
                int indiceMelhorMatch = -1;

                // Busca a melhor correspondência na frase falada
                for (int i = 0; i < palavrasTranscritas.size(); i++) {
                    if (palavrasUsadas[i]) continue;

                    String pTranscrita = palavrasTranscritas.get(i);
                    
                    // Agora usamos comparação direta e fonética leve
                    double scoreOrtografico = calcularSimilaridade(pEsperadaNorm, pTranscrita);
                    double scoreFonetico = calcularSimilaridade(fonetizarTexto(pEsperadaNorm), fonetizarTexto(pTranscrita));
                    
                    // Pega o melhor dos dois mundos, mas sem exagerar
                    double score = Math.max(scoreOrtografico, scoreFonetico);

                    if (score > melhorScore) {
                        melhorScore = score;
                        melhorPalavraEncontrada = pTranscrita;
                        indiceMelhorMatch = i;
                    }
                }

                System.out.printf("🔍 '%s' vs '%s' -> Score: %.2f%%%n", 
                        palavraEsperada, melhorPalavraEncontrada, melhorScore * 100);

                // --- CRITÉRIOS MAIS RIGOROSOS ---
                // Só marca como usada se tiver certeza (score alto)
                if (indiceMelhorMatch != -1 && melhorScore >= 0.6) {
                    palavrasUsadas[indiceMelhorMatch] = true;
                }

                // Nota final sem arredondamento bonzinho
                double scoreFinal = melhorScore * 100;

                // Régua de aprovação: precisa de 80% para "Acertou"
                boolean acertou = scoreFinal >= 80.0;
                
                if (acertou) acertos++;
                somaSimilaridades += scoreFinal;

                String feedback = gerarFeedbackPalavra(scoreFinal, palavraEsperada, melhorPalavraEncontrada);

                resultados.add(new BatchPronunciationAnalysisDTO.ResultadoPalavra(
                        palavraEsperada,
                        melhorPalavraEncontrada.isEmpty() ? "(não identifiquei)" : melhorPalavraEncontrada,
                        acertou,
                        scoreFinal,
                        feedback
                ));
            }

            double pontuacaoGeral = palavrasEsperadas.isEmpty() ? 0.0 : (somaSimilaridades / palavrasEsperadas.size());
            double porcentagemAcerto = palavrasEsperadas.isEmpty() ? 0.0 : ((double) acertos / palavrasEsperadas.size()) * 100;

            String feedbackGeral = gerarFeedbackGeral(acertos, palavrasEsperadas.size(), pontuacaoGeral);

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
            e.printStackTrace();
            throw new RuntimeException("Erro análise: " + e.getMessage(), e);
        }
    }

    private String transcreverAudio(byte[] audioBytes, List<String> palavrasChave) throws IOException {
        RequestBody body = RequestBody.create(audioBytes, MediaType.parse("audio/*"));
        
        // Reduzimos o Boost para 3.0 (ajuda a identificar, mas não força alucinação)
        StringBuilder urlBuilder = new StringBuilder("https://api.deepgram.com/v1/listen?model=nova-2&language=pt-BR&smart_format=true&punctuate=false&diarize=false");
        
        if (palavrasChave != null) {
            for (String palavra : palavrasChave) {
                try {
                    String encodedWord = URLEncoder.encode(palavra.trim(), StandardCharsets.UTF_8.toString());
                    if (!encodedWord.isEmpty()) {
                        // MUDANÇA: Boost 3.0 (Era 20.0)
                        urlBuilder.append("&keywords=").append(encodedWord).append(":3.0");
                    }
                } catch (Exception e) {}
            }
        }

        Request request = new Request.Builder()
                .url(urlBuilder.toString())
                .addHeader("Authorization", "Token " + deepgramApiKey)
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Erro API: " + response.code());
            String jsonResponse = response.body().string();
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            if (!json.has("results")) return "";
            try {
                 return json.getAsJsonObject("results")
                    .getAsJsonArray("channels").get(0).getAsJsonObject()
                    .getAsJsonArray("alternatives").get(0).getAsJsonObject()
                    .get("transcript").getAsString();
            } catch (Exception e) { return ""; }
        }
    }

    // Fonetização mais leve para não misturar palavras muito diferentes
    private String fonetizarTexto(String texto) {
        String t = normalizarTexto(texto);
        t = t.replace("ch", "x");
        t = t.replace("lh", "li");
        t = t.replace("nh", "ni");
        t = t.replace("ç", "s");
        t = t.replace("ss", "s");
        return t;
    }

    private double calcularSimilaridade(String p1, String p2) {
        if (p1.equals(p2)) return 1.0;
        if (p1.isEmpty() || p2.isEmpty()) return 0.0;

        int distancia = levenshtein.apply(p1, p2);
        int maxLen = Math.max(p1.length(), p2.length());
        return 1.0 - ((double) distancia / maxLen);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[^\\p{ASCII}]", "");
        normalized = normalized.replaceAll("[.,!?]", ""); 
        return normalized.toLowerCase().trim();
    }

    private String gerarFeedbackPalavra(double pontuacao, String esperada, String transcrita) {
        // Régua mais rigorosa
        if (pontuacao >= 90) return "Perfeito!";
        else if (pontuacao >= 80) return "Muito bom!";
        else if (pontuacao >= 60) return "Bom, mas pode melhorar (ouvi: " + transcrita + ")";
        else return "Tente novamente (ouvi: " + transcrita + ")";
    }

    private String gerarFeedbackGeral(int acertos, int total, double pontuacaoGeral) {
        if (pontuacaoGeral >= 90) return "Excelente! Pronúncia muito clara. 🌟";
        else if (pontuacaoGeral >= 70) return "Bom trabalho! Continue treinando. 👍";
        else return "Atenção à articulação. Vamos tentar de novo? 💪";
    }
}