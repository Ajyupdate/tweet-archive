package com.example.tweet_archive.service.GeminiService;

// import com.example.tweet_archive.config.AppConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.tweet_archive.config.AuditProperties;
import com.example.tweet_archive.config.GeminiProperties;
import com.example.tweet_archive.exception.GeminiApiException;
import com.example.tweet_archive.model.AuditResult;
import com.example.tweet_archive.model.Content;
import com.example.tweet_archive.model.GeminiRequest;
import com.example.tweet_archive.model.GeminiResponse;
import com.example.tweet_archive.model.Part;
import com.example.tweet_archive.model.Tweet;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class GeminiService {
    // private final AppConfig appConfig;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final GeminiProperties geminiProperties;
    private final AuditProperties auditProperties;
    
    public GeminiService(
        RestClient restClient,
        ObjectMapper objectMapper,
        GeminiProperties geminiProperties,
        AuditProperties auditProperties
        //  AppConfig appConfig
    ){
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.geminiProperties = geminiProperties;
        this.auditProperties = auditProperties;
        // this.appConfig = appConfig;
    }

    @PostConstruct
    public void validateApiKey(){
        if (
            geminiProperties.apiKey() == null || geminiProperties.apiKey().isBlank()
        ){
            throw new IllegalStateException(
                "Gemini API key missing"
            );
        }
    }

    @Async 
    public CompletableFuture<AuditResult>
    evaluateTweet(Tweet tweet){
        try{
            String prompt = buildPrompt(tweet);

            GeminiResponse response = callGemini(prompt);
            AuditResult result = mapResponse(response, tweet);
            
            return CompletableFuture.completedFuture(result);
        }catch (Exception e){
            throw new GeminiApiException("Gemini evaluation failed", e);
        }
    }

    private String buildPrompt(Tweet tweet){
        String criteria = String.join(
            ", ",
                    auditProperties.criteria()
          );

        return """
                You are auditing a Twitter archive.

                Criteria:
                "%s"

                Tweet: "%s"

                Respond ONLY with JSON in this exact format:

                {
                "shouldDelete": true or false,
                "reason": "brief explanation"
                }
                """
                .formatted(
                    criteria,
                    tweet.getFullText()
                );
    }

    private GeminiResponse callGemini(
        String prompt
    ){
        GeminiRequest request = 
            new GeminiRequest(
                List.of(
                    new Content(
                        List.of(
                            new Part(prompt)
                        )
                    )
                )
            );
        
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + geminiProperties.model()
                    + ":generateContent?key="
                    + geminiProperties.apiKey();

        return restClient.post()
            .uri(url)
            .body(request)
            .retrieve()
            .body(GeminiResponse.class);            
    }

    private AuditResult mapResponse(
        GeminiResponse response,
        Tweet tweet
    ){
        if(
            response == null || 
            response.candidates() == null || 
            response.candidates().isEmpty()
        ){
            return new AuditResult(
                tweet.getId(),
                tweet.getFullText(),
                false,
                "Gemini response blocked or empty"
            );
        }

        String json =
            response.candidates()
                .get(0)
                .content()
                .parts()
                .get(0)
                .text();
        
        json = stripMarkdownFence(json);

        try{
            AuditResult result = objectMapper.readValue(json, AuditResult.class);

            result.setTweetId(tweet.getId());
            result.setFullText(tweet.getFullText());

            return result;
        }catch(Exception e){
            throw new GeminiApiException("Invalid Gemini Json response", e);
        }


    }
    private String stripMarkdownFence(
        String text
    ){
        String trimmed = text.trim();
        if(trimmed.startsWith("```")){
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*\\s*", "");
            trimmed = trimmed.replaceFirst("```\\s*$", "");
        }
        return trimmed.trim();
    }
}
