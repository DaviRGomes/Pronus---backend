package com.inatel.prototipo_ia.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.inatel.prototipo_ia.dto.out.BatchPronunciationAnalysisDTO;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service para análise de pronúncia EM LOTE usando Gemini
 */
@Service
public class GeminiAudioAnalysisService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final OkHttpClient httpClient;

    public GeminiAudioAnalysisService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Analisa múltiplas palavras em um áudio usando Gemini
     * @param audioBytes bytes do áudio
     * @param palavrasEsperadas lista de palavras que deveriam ser faladas
     * @return análise em lote com resultado de cada palavra
     */
    public BatchPronunciationAnalysisDTO analisarPronunciaEmLote(byte[] audioBytes, List<String> palavrasEsperadas) {
        try {
            // Converter áudio para base64
            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

            // Construir prompt para análise em lote
            String prompt = construirPromptBatch(palavrasEsperadas);

            // Chamar Gemini
            String respostaGemini = chamarGeminiComAudio(prompt, audioBase64);

            // Parsear resposta
            return parsearRespostaBatch(respostaGemini, palavrasEsperadas);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao analisar áudio com Gemini: " + e.getMessage(), e);
        }
    }

    /**
     * Constrói prompt para análise de múltiplas palavras
     */
    private String construirPromptBatch(List<String> palavrasEsperadas) {
        String listaPalavras = String.join(", ", palavrasEsperadas);

        return String.format(
                "Você é um fonoaudiólogo especialista em análise de pronúncia do português brasileiro.\n\n" +
                        "TAREFA: Analise este áudio onde a pessoa deveria falar as seguintes palavras NA ORDEM:\n" +
                        "%s\n\n" +
                        "INSTRUÇÕES:\n" +
                        "1. Identifique cada palavra pronunciada no áudio\n" +
                        "2. Compare com a lista de palavras esperadas\n" +
                        "3. Para cada palavra, avalie se foi pronunciada corretamente\n" +
                        "4. Dê uma pontuação de 0-100 para cada palavra\n" +
                        "5. Seja tolerante com sotaques regionais do Brasil\n" +
                        "6. Identifique erros específicos quando houver\n\n" +
                        "RESPONDA APENAS EM FORMATO JSON (sem markdown, sem ```json):\n" +
                        "{\n" +
                        "  \"transcricaoCompleta\": \"todas as palavras que você ouviu\",\n" +
                        "  \"resultados\": [\n" +
                        "    {\n" +
                        "      \"palavraEsperada\": \"palavra da lista\",\n" +
                        "      \"palavraTranscrita\": \"o que você ouviu\",\n" +
                        "      \"acertou\": true/false,\n" +
                        "      \"similaridade\": 0-100,\n" +
                        "      \"feedback\": \"feedback específico\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"feedbackGeral\": \"análise geral do desempenho\"\n" +
                        "}",
                listaPalavras
        );
    }

    /**
     * Chama Gemini API com áudio
     */
    private String chamarGeminiComAudio(String prompt, String audioBase64) throws IOException {
        JsonObject requestBody = new JsonObject();

        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();

        // Texto do prompt
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);

        // Áudio em base64
        JsonObject audioPart = new JsonObject();
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", "audio/wav");
        inlineData.addProperty("data", audioBase64);
        audioPart.add("inline_data", inlineData);
        parts.add(audioPart);

        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Generation config
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.3);
        generationConfig.addProperty("topK", 32);
        generationConfig.addProperty("topP", 1);
        generationConfig.addProperty("maxOutputTokens", 4096);
        requestBody.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Sem detalhes";
                throw new IOException("Erro na API Gemini: " + response.code() + " - " + errorBody);
            }

            String jsonResponse = response.body().string();
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            return json.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        }
    }

    /**
     * Parseia resposta do Gemini e cria BatchDTO
     */
    private BatchPronunciationAnalysisDTO parsearRespostaBatch(String respostaGemini, List<String> palavrasEsperadas) {
        try {
            // Limpar resposta
            String jsonLimpo = respostaGemini.trim();
            if (jsonLimpo.contains("```json")) {
                int start = jsonLimpo.indexOf("{");
                int end = jsonLimpo.lastIndexOf("}") + 1;
                if (start >= 0 && end > start) {
                    jsonLimpo = jsonLimpo.substring(start, end);
                }
            } else if (jsonLimpo.startsWith("```")) {
                jsonLimpo = jsonLimpo.substring(3);
            }
            if (jsonLimpo.endsWith("```")) {
                jsonLimpo = jsonLimpo.substring(0, jsonLimpo.length() - 3);
            }
            jsonLimpo = jsonLimpo.trim();

            // Parsear JSON
            JsonObject json = JsonParser.parseString(jsonLimpo).getAsJsonObject();

            String transcricaoCompleta = json.has("transcricaoCompleta") ?
                    json.get("transcricaoCompleta").getAsString() : "";

            String feedbackGeral = json.has("feedbackGeral") ?
                    json.get("feedbackGeral").getAsString() : "Análise concluída";

            // Processar resultados
            List<BatchPronunciationAnalysisDTO.ResultadoPalavra> resultados = new ArrayList<>();
            int acertos = 0;
            double somaSimilaridades = 0.0;

            if (json.has("resultados")) {
                JsonArray resultadosArray = json.getAsJsonArray("resultados");

                for (int i = 0; i < resultadosArray.size(); i++) {
                    JsonObject resultado = resultadosArray.get(i).getAsJsonObject();

                    String palavraEsperada = resultado.has("palavraEsperada") ?
                            resultado.get("palavraEsperada").getAsString() :
                            (i < palavrasEsperadas.size() ? palavrasEsperadas.get(i) : "");

                    String palavraTranscrita = resultado.has("palavraTranscrita") ?
                            resultado.get("palavraTranscrita").getAsString() : "";

                    boolean acertou = resultado.has("acertou") ?
                            resultado.get("acertou").getAsBoolean() : false;

                    double similaridade = resultado.has("similaridade") ?
                            resultado.get("similaridade").getAsDouble() : 0.0;

                    String feedback = resultado.has("feedback") ?
                            resultado.get("feedback").getAsString() : "";

                    if (acertou) acertos++;
                    somaSimilaridades += similaridade;

                    resultados.add(new BatchPronunciationAnalysisDTO.ResultadoPalavra(
                            palavraEsperada,
                            palavraTranscrita,
                            acertou,
                            similaridade,
                            feedback
                    ));
                }
            }

            // Calcular métricas
            double pontuacaoGeral = resultados.isEmpty() ? 0.0 : (somaSimilaridades / resultados.size());
            double porcentagemAcerto = resultados.isEmpty() ? 0.0 : ((double) acertos / resultados.size()) * 100;

            // Criar DTO
            BatchPronunciationAnalysisDTO dto = new BatchPronunciationAnalysisDTO();
            dto.setPalavrasEsperadas(palavrasEsperadas);
            dto.setTranscricaoCompleta(transcricaoCompleta);
            dto.setResultados(resultados);
            dto.setPontuacaoGeral(pontuacaoGeral);
            dto.setTotalAcertos(acertos);
            dto.setTotalPalavras(palavrasEsperadas.size());
            dto.setPorcentagemAcerto(porcentagemAcerto);
            dto.setFeedbackGeral(feedbackGeral);

            return dto;

        } catch (Exception e) {
            System.err.println("Erro ao parsear resposta do Gemini: " + e.getMessage());
            System.err.println("Resposta original: " + respostaGemini);

            // Fallback
            BatchPronunciationAnalysisDTO dto = new BatchPronunciationAnalysisDTO();
            dto.setPalavrasEsperadas(palavrasEsperadas);
            dto.setTranscricaoCompleta("Erro ao processar");
            dto.setResultados(new ArrayList<>());
            dto.setPontuacaoGeral(0.0);
            dto.setTotalAcertos(0);
            dto.setTotalPalavras(palavrasEsperadas.size());
            dto.setPorcentagemAcerto(0.0);
            dto.setFeedbackGeral("Erro ao processar análise: " + e.getMessage());

            return dto;
        }
    }
}