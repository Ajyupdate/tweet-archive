package com.example.tweet_archive.service.AuditOrchestrator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.example.tweet_archive.config.AuditProperties;
import com.example.tweet_archive.model.AuditResult;
import com.example.tweet_archive.model.Tweet;
import com.example.tweet_archive.service.GeminiService.GeminiService;

import jakarta.annotation.PostConstruct;

@Service
public class AuditOrchestrator {
    private final GeminiService geminiService;
    private final AuditProperties auditProperties;

    public AuditOrchestrator(
        GeminiService geminiService,
        AuditProperties auditProperties
    ){
        this.geminiService = geminiService;
        this.auditProperties = auditProperties;
    }
    @PostConstruct
    public void validateBatchSize(){
        if(auditProperties.batchSize() <= 0){
            throw new IllegalStateException(
                "audit.batch-size must be greater than 0"
            );
        }
    }
    public List<AuditResult> runAudit(
        List<Tweet> tweets,
        Consumer<Integer> onBatchComplete
    ){
        if (tweets == null || tweets.isEmpty()){
            return List.of();
        }
        List<AuditResult> allResults = new ArrayList<>();

        int processedCount = 0;
        
        int batchSize = auditProperties.batchSize();

        for(int start = 0; start < tweets.size(); start += batchSize){
            int end = Math.min(start + batchSize, tweets.size());
            List<Tweet>batch = tweets.subList(start, end);

            List<CompletableFuture<AuditResult>> 
                futures = 
                batch.stream()
                    .map(tweet -> geminiService 
                                    .evaluateTweet(tweet)
                                    .exceptionally(ex ->
                                        new AuditResult(
                                            tweet.getId(),
                                            tweet.getFullText(),
                                            false,
                                            "Evaluation failed: " +
                                                (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage())
)
                                    )
                    ).toList();
            
            CompletableFuture
                .allOf(futures.toArray(
                    new CompletableFuture[futures.size()]
                ))
                .join();
            
            List<AuditResult> batchResults = 
                futures.stream()
                    .map(CompletableFuture:: join)
                    .toList();
            
            allResults.addAll(batchResults);
            processedCount += batch.size();
            onBatchComplete.accept(processedCount);

        }

        return allResults;
    }
}
