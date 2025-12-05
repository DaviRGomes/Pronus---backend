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
            String prompt = construirPromptTravaLingua(idade, dificuldade);
            String response = chamarGeminiAPI(prompt);
            return extrairTravaLingua(response);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar trava-língua com IA: " + e.getMessage(), e);
        }
    }

    /**
     * Constrói o prompt personalizado para a IA gerar um trava-língua
     */
    private String construirPromptTravaLingua(int idade, String dificuldade) {
        if ("X".equalsIgnoreCase(dificuldade)) {
            return "Você é um roteirista de comédia e especialista em cultura pop brasileira. " +
                   "Gere um trava-língua ENGRAÇADO e curto para exercício de pronúncia com o som de 'X'.\n\n" +
                   "TEMA OBRIGATÓRIO:\n" +
                   "O trava-língua DEVE ser sobre a XUXA e sua filha SASHA.\n\n" +
                   "REQUISITOS OBRIGATÓRIOS:\n" +
                   "1. Deve ser engraçado e usar o som do 'X' e 'CH' (que tem som de X) de forma criativa.\n" +
                   "2. Apropriado para todas as idades.\n" +
                   "3. O resultado deve ser diferente a cada vez que for gerado.\n\n" +
                   "FORMATO DA RESPOSTA:\n" +
                   "Retorne APENAS um objeto JSON com uma única chave 'trava_lingua' contendo o texto, SEM nenhum texto adicional antes ou depois.\n" +
                   "Formato: {\"trava_lingua\": \"A Xuxa achou o xale da Sasha roxo.\"}\n" +
                   "NÃO adicione explicações, comentários ou markdown.\n\n" +
                   "Gere um trava-língua NOVO e DIFERENTE a cada vez sobre a Xuxa e a Sasha agora:";
        }

        String faixaEtaria = determinarFaixaEtaria(idade);
        String descricaoDificuldade = descricaoDaDificuldade(dificuldade);

        return String.format(
                "Você é um fonoaudiólogo especialista em terapia da fala para falantes de português brasileiro. " +
                "Gere um trava-língua para exercício de pronúncia.\n\n" +
                "PERFIL DO PACIENTE:\n" +
                "- Idade: %d anos (%s)\n" +
                "- Dificuldade: %s\n\n" +
                "REQUISITOS OBRIGATÓRIOS:\n" +
                "1. O trava-língua DEVE focar no som/fonema da dificuldade especificada.\n" +
                "2. O trava-língua deve ser apropriado para a idade (vocabulário e tema que o paciente conhece).\n" +
                "3. O trava-língua deve ser curto e fácil de memorizar.\n" +
                "4. Use palavras do cotidiano brasileiro.\n" +
                "5. Evitar temas muito complexos ou abstratos.\n\n" +
                "FORMATO DA RESPOSTA:\n" +
                "Retorne APENAS um objeto JSON com uma única chave 'trava_lingua' contendo o texto, SEM nenhum texto adicional antes ou depois.\n" +
                "Formato: {\"trava_lingua\": \"O rato roeu a roupa do rei de Roma.\"}\n" +
                "NÃO adicione explicações, comentários ou markdown.\n\n" +
                "Gere um trava-língua agora:",
                idade, faixaEtaria, descricaoDificuldade
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
    private List<String> extrairTravaLingua(String response) {
        List<String> travaLinguaList = new ArrayList<>();
        try {
            // Limpar a resposta
            response = response.trim();

            if (response.startsWith("```json")) {
                int startIndex = response.indexOf("{");
                int endIndex = response.lastIndexOf("}");
                if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                    response = response.substring(startIndex, endIndex + 1);
                }
            } else if (response.startsWith("```")) {
                response = response.substring(3);
            }
            if (response.endsWith("```")) {
                response = response.substring(0, response.length() - 3);
            }
            response = response.trim();

            // Tentar parsear como JSON
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            if (jsonObject.has("trava_lingua")) {
                String travaLingua = jsonObject.get("trava_lingua").getAsString();
                if (!travaLingua.isEmpty()) {
                    travaLinguaList.add(travaLingua);
                }
            }

        } catch (Exception e) {
            // Fallback: se o JSON falhar, assuma que a resposta é o trava-língua puro
            System.err.println("Erro ao parsear JSON, tratando a resposta como texto puro: " + e.getMessage());
            String cleanedResponse = response.replaceAll("[\"'{}]", "").replace("trava_lingua:", "").trim();
            if (!cleanedResponse.isEmpty()) {
                travaLinguaList.add(cleanedResponse);
            }
        }

        return travaLinguaList;
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