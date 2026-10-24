package com.pranathi.pka.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public AIService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String summarize(String content) {

    String prompt =
            """
            Summarize the following notebook.

            %s
            """
            .formatted(content);

    return callGemini(prompt);

}
private String callGemini(String prompt) {

    String requestBody = """
    {
      "contents": [
        {
          "parts": [
            {
              "text": "%s"
            }
          ]
        }
      ]
    }
    """.formatted(prompt.replace("\"", "\\\""));

    String geminiResponse = webClient.post()
            .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=" + apiKey)
            .header("Content-Type", "application/json")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block();

    try {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(geminiResponse);

        return root
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

    } catch (Exception e) {

        throw new RuntimeException("Failed to parse Gemini response", e);

    }

}
public String askNotebook(
        String notebookContent,
        String conversationHistory,
        String question
) {

    String prompt =
            """
You are an AI assistant for a Personal Knowledge Assistant.

Answer ONLY using the notebook below.

If the answer cannot be found in the notebook,
say:

"I couldn't find that information in this notebook."

----------------------------
Notebook
----------------------------

%s

----------------------------
Conversation
----------------------------

%s

----------------------------
User Question
----------------------------

%s

Give a clear, detailed answer.
"""
            .formatted(
                    notebookContent,
                    conversationHistory,
                    question
            );

    return callGemini(prompt);

}
}