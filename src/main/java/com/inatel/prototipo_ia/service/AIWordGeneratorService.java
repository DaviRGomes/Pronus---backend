package com.inatel.prototipo_ia.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AIWordGeneratorService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final OkHttpClient httpClient;
    private final Gson gson;

    public AIWordGeneratorService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * Gera palavras personalizadas usando IA (Google Gemini)
     * @param idade idade do usuário
     * @param dificuldade tipo de dificuldade (R, L, S, CH, LH)
     * @param quantidade quantas palavras gerar
     * @return lista de palavras geradas pela IA
     */
    public List<String> gerarPalavrasComIA(int idade, String dificuldade, int quantidade) {
        try {
            String prompt = construirPrompt(idade, dificuldade, quantidade);
            String response = chamarGeminiAPI(prompt);
            return extrairPalavras(response);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar palavras com IA: " + e.getMessage(), e);
        }
    }

    /**
     * Constrói o prompt personalizado para a IA
     */
    private String construirPrompt(int idade, String dificuldade, int quantidade) {
        String faixaEtaria = determinarFaixaEtaria(idade);
        String descricaoDificuldade = descricaoDaDificuldade(dificuldade);

        return String.format(
                "Você é um fonoaudiólogo especialista em terapia da fala para falantes de português brasileiro. " +
                        "Gere exatamente %d palavras para exercício de pronúncia.\n\n" +
                        "PERFIL DO PACIENTE:\n" +
                        "- Idade: %d anos (%s)\n" +
                        "- Dificuldade: %s\n" +
                        "- Nível: %s\n\n" +
                        "REQUISITOS OBRIGATÓRIOS:\n" +
                        "1. As palavras DEVEM conter o som/fonema da dificuldade especificada\n" +
                        "2. Palavras apropriadas para a idade (vocabulário que o paciente conhece)\n" +
                        "3. Variar a posição do som: início, meio e fim da palavra\n" +
                        "4. Incluir diferentes níveis de complexidade silábica\n" +
                        "5. Palavras do cotidiano brasileiro (coisas que a pessoa vê no dia a dia)\n" +
                        "6. Evitar palavras muito técnicas ou raras\n\n" +
                        "FORMATO DA RESPOSTA:\n" +
                        "Retorne APENAS um array JSON com as palavras, SEM nenhum texto adicional antes ou depois.\n" +
                        "Formato: [\"palavra1\", \"palavra2\", \"palavra3\"]\n" +
                        "NÃO adicione explicações, comentários ou markdown.\n\n" +
                        "Gere exatamente %d palavras agora:",
                quantidade, idade, faixaEtaria, descricaoDificuldade, getNivelComplexidade(idade), quantidade
        );
    }

    /**
     * Chama a API do Google Gemini
     */
    private String chamarGeminiAPI(String prompt) throws IOException {
        // Construir corpo da requisição
        JsonObject requestBody = new JsonObject();

        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();

        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);

        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        // Configurações de geração
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.9);
        generationConfig.addProperty("topK", 40);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 1024);
        requestBody.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        // URL da API (alterado para gemini-pro, que é o modelo estável desta API)
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

            // Extrair o texto da resposta do Gemini
            return json.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        }
    }

    /**
     * Extrai as palavras da resposta da IA
     */
    private List<String> extrairPalavras(String response) {
        List<String> palavras = new ArrayList<>();

        try {
            // Limpar a resposta
            response = response.trim();

            // Remover possíveis marcadores de código
            if (response.contains("```json")) {
                int start = response.indexOf("[");
                int end = response.lastIndexOf("]") + 1;
                if (start >= 0 && end > start) {
                    response = response.substring(start, end);
                }
            } else if (response.startsWith("```")) {
                response = response.substring(3);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }
            response = response.trim();

            // Tentar parsear como JSON
            JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
            jsonArray.forEach(element -> {
                String palavra = element.getAsString().toLowerCase().trim();
                if (!palavra.isEmpty()) {
                    palavras.add(palavra);
                }
            });

        } catch (Exception e) {
            // Fallback: extrair palavras manualmente
            System.err.println("Erro ao parsear JSON, tentando extração manual: " + e.getMessage());
            String[] linhas = response.split("[\\n,]");
            for (String linha : linhas) {
                String palavra = linha.trim()
                        .replaceAll("[\\[\\]\"'`]", "")
                        .toLowerCase()
                        .trim();
                if (!palavra.isEmpty() && palavra.matches("[a-záàâãéèêíïóôõöúçñ]+")) {
                    palavras.add(palavra);
                }
            }
        }

        return palavras;
    }

    /**
     * Determina faixa etária
     */
    private String determinarFaixaEtaria(int idade) {
        if (idade <= 6) return "infantil (pré-escolar)";
        if (idade <= 12) return "juvenil (ensino fundamental)";
        if (idade <= 17) return "adolescente";
        return "adulto";
    }

    /**
     * Descrição detalhada da dificuldade para português brasileiro
     */
    private String descricaoDaDificuldade(String dificuldade) {
        switch (dificuldade.toUpperCase()) {
            case "R":
                return "Som /R/ vibrante (r forte) - como em 'rato', 'carro', 'marreta', 'terra'";
            case "L":
                return "Som /L/ lateral - como em 'lua', 'bola', 'palma', 'sol'";
            case "S":
                return "Som /S/ fricativa surda - como em 'sapo', 'massa', 'osso', 'paz'";
            case "CH":
                return "Som /CH/ fricativa alveopalatal surda - como em 'chuva', 'bicho', 'cochicho'";
            case "LH":
                return "Som /LH/ lateral palatal - como em 'palha', 'filho', 'coelho', 'milho'";
            case "RR":
                return "Som /RR/ r duplo forte - como em 'carro', 'barro', 'corrida', 'terra'";
            case "Z":
                return "Som /Z/ fricativa sonora - como em 'zebra', 'casa', 'fazer', 'azul'";
            case "J":
                return "Som /J/ fricativa alveopalatal sonora - como em 'janela', 'caju', 'loja'";
            case "NH":
                return "Som /NH/ nasal palatal - como em 'ninho', 'aranha', 'sonho', 'ganho'";
            case "V":
                return "Som /V/ fricativa labiodental - como em 'vaca', 'ave', 'livro', 'uva'";
            case "F":
                return "Som /F/ fricativa labiodental surda - como em 'faca', 'afe', 'café'";
            case "X":
                return "Som /X/ (sh) - como em 'xícara', 'peixe', 'roxo', 'caixa'";
            default:
                return "Exercício geral de pronúncia com variedade de sons";
        }
    }

    /**
     * Determina nível de complexidade baseado na idade
     */
    private String getNivelComplexidade(int idade) {
        if (idade <= 6) return "palavras simples e curtas (2-3 sílabas, dissílabas e trissílabas)";
        if (idade <= 12) return "palavras de complexidade média (2-4 sílabas, podem ter encontros consonantais simples)";
        return "palavras de qualquer complexidade (incluindo polissílabas e encontros consonantais complexos)";
    }

    /**
     * Retorna dificuldades disponíveis
     */
    public List<String> getDificuldadesDisponiveis() {
        return List.of("R", "L", "S", "CH", "LH", "RR", "Z", "J", "NH", "V", "F", "X", "GERAL");
    }
}