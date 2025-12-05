package com.inatel.prototipo_ia.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.inatel.prototipo_ia.dto.out.BatchPronunciationAnalysisDTO;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public BatchPronunciationAnalysisDTO analisarPronunciaEmLote(byte[] audioBytes, List<String> palavrasEsperadas) {
        System.out.println(">>> TENTATIVA NA API DE PRODUÇÃO (v1) <<<");
        System.out.println("Tamanho do áudio: " + audioBytes.length + " bytes");
        
        try {
            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);
            String prompt = construirPromptBatch(palavrasEsperadas);
            String respostaGemini = chamarGeminiComAudio(prompt, audioBase64);

            return parsearRespostaBatch(respostaGemini, palavrasEsperadas);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro IA: " + e.getMessage(), e);
        }
    }

    private String chamarGeminiComAudio(String prompt, String audioBase64) throws IOException {
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);

        JsonObject audioPart = new JsonObject();
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", "audio/mp3");
        inlineData.addProperty("data", audioBase64);
        audioPart.add("inline_data", inlineData);
        parts.add(audioPart);

        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.4);
        requestBody.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        // --- MUDANÇA CRÍTICA: USANDO A VERSÃO v1 (PRODUÇÃO) ---
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + geminiApiKey;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        System.out.println("Enviando para URL v1: " + url.substring(0, 55) + "...");

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Sem detalhes";
                System.err.println(">>> ERRO GEMINI v1 (CODE " + response.code() + "): " + errorBody);
                throw new IOException("Google API Error: " + response.code() + " " + errorBody);
            }

            String jsonResponse = response.body().string();
            System.out.println(">>> SUCESSO! Resposta recebida.");
            
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();

            try {
                return json.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            } catch (Exception e) {
                System.err.println("JSON inesperado: " + jsonResponse);
                throw e;
            }
        }
    }

    private String construirPromptBatch(List<String> palavrasEsperadas) {
        String listaPalavras = String.join(", ", palavrasEsperadas);
        return "Analise a pronúncia em PT-BR: " + listaPalavras + 
               ". Responda APENAS JSON: { \"resultados\": [ { \"palavraEsperada\": \"...\", \"palavraTranscrita\": \"...\", \"acertou\": true, \"similaridade\": 100, \"feedback\": \"...\" } ], \"feedbackGeral\": \"...\", \"pontuacaoGeral\": 100.0 }";
    }

    private BatchPronunciationAnalysisDTO parsearRespostaBatch(String respostaGemini, List<String> palavrasEsperadas) {
        try {
            String jsonLimpo = respostaGemini.trim();
            if (jsonLimpo.startsWith("```json")) {
                int startIndex = jsonLimpo.indexOf("{");
                int endIndex = jsonLimpo.lastIndexOf("}");
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    jsonLimpo = jsonLimpo.substring(startIndex, endIndex + 1);
                }
            } else if (jsonLimpo.startsWith("```")) {
                jsonLimpo = jsonLimpo.substring(3);
            }
            if (jsonLimpo.endsWith("```")) {
                jsonLimpo = jsonLimpo.substring(0, jsonLimpo.length() - 3);
            }
            
            JsonObject json = JsonParser.parseString(jsonLimpo).getAsJsonObject();
            BatchPronunciationAnalysisDTO dto = new BatchPronunciationAnalysisDTO();
            
            dto.setPalavrasEsperadas(palavrasEsperadas);
            dto.setFeedbackGeral(json.has("feedbackGeral") ? json.get("feedbackGeral").getAsString() : "Análise ok");
            dto.setPontuacaoGeral(json.has("pontuacaoGeral") ? json.get("pontuacaoGeral").getAsDouble() : 0.0);
            
            List<BatchPronunciationAnalysisDTO.ResultadoPalavra> resultados = new ArrayList<>();
            int totalAcertos = 0;
            int totalPalavras = 0;

            if (json.has("resultados") && json.get("resultados").isJsonArray()) {
                JsonArray resultadosArray = json.getAsJsonArray("resultados");
                totalPalavras = resultadosArray.size();

                for (int i = 0; i < resultadosArray.size(); i++) {
                    JsonObject resultadoJson = resultadosArray.get(i).getAsJsonObject();
                    BatchPronunciationAnalysisDTO.ResultadoPalavra resultadoPalavra = new BatchPronunciationAnalysisDTO.ResultadoPalavra();

                    resultadoPalavra.setPalavraEsperada(resultadoJson.has("palavraEsperada") ? resultadoJson.get("palavraEsperada").getAsString() : "");
                    resultadoPalavra.setPalavraTranscrita(resultadoJson.has("palavraTranscrita") ? resultadoJson.get("palavraTranscrita").getAsString() : "");
                    resultadoPalavra.setAcertou(resultadoJson.has("acertou") ? resultadoJson.get("acertou").getAsBoolean() : false);
                    resultadoPalavra.setSimilaridade(resultadoJson.has("similaridade") ? resultadoJson.get("similaridade").getAsDouble() : 0.0);
                    resultadoPalavra.setFeedback(resultadoJson.has("feedback") ? resultadoJson.get("feedback").getAsString() : "");
                    
                    if (resultadoPalavra.getAcertou()) {
                        totalAcertos++;
                    }
                    resultados.add(resultadoPalavra);
                }
            }
            dto.setResultados(resultados);
            dto.setTotalAcertos(totalAcertos);
            dto.setTotalPalavras(totalPalavras);
            dto.setPorcentagemAcerto(totalPalavras > 0 ? (double) totalAcertos / totalPalavras * 100.0 : 0.0);
            
            // Assuming transcricaoCompleta is not directly provided by Gemini for now.
            // It could be constructed by joining individual palavraTranscrita if needed.
            dto.setTranscricaoCompleta(palavrasEsperadas.stream().collect(Collectors.joining(" ")));


            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao parsear resposta Gemini: " + respostaGemini);
            // In case of parsing error, return a DTO with error feedback
            BatchPronunciationAnalysisDTO errorDto = new BatchPronunciationAnalysisDTO();
            errorDto.setFeedbackGeral("Erro ao processar a resposta da IA: " + e.getMessage());
            errorDto.setResultados(new ArrayList<>());
            return errorDto;
        }
    }
}