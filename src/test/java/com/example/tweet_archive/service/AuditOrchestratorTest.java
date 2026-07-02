package com.example.tweet_archive.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.tweet_archive.config.AuditProperties;
import com.example.tweet_archive.model.AuditResult;
import com.example.tweet_archive.model.Tweet;
import com.example.tweet_archive.service.AuditOrchestrator.AuditOrchestrator;
import com.example.tweet_archive.service.GeminiService.GeminiService;

@ExtendWith(MockitoExtension.class)
class AuditOrchestratorTest {
    @Mock
    private GeminiService geminiService;

    private AuditOrchestrator auditOrchestrator;

    @BeforeEach
    void setUp(){
        AuditProperties properties = 
            new AuditProperties(
                List.of("offensive content"),
                10
            );
        
        auditOrchestrator = 
            new AuditOrchestrator(geminiService, properties);
    }

    @Test
    void shouldReturnEmptyListForEmptyInput(){
        List<AuditResult> results = auditOrchestrator.runAudit(
            List.of(), processes -> {}
        );
        assertTrue(results.isEmpty());
    }

    @Test
    void shouldProcessAllTweetsSuccessfully(){
        List<Tweet> tweets = createTweets(5);

        when(geminiService.evaluateTweet(any()))
            .thenAnswer(invocation -> {
                Tweet tweet = invocation.getArgument(0);

                AuditResult result = new AuditResult(
                    tweet.getId(),
                    tweet.getFullText(),
                    false,
                    "Safe"
                );

                return CompletableFuture.completedFuture(result);
            });

        List<AuditResult> results = 
            auditOrchestrator.runAudit(tweets, processed -> {});
        assertEquals(5, results.size());

        assertEquals("tweet-1", results.get(0).getTweetId());
    }

    @Test
    void shouldUseFallbackWhenOneEvaluationFails() {

        List<Tweet> tweets =
                createTweets(3);

        when(geminiService.evaluateTweet(any()))
                .thenAnswer(invocation -> {

                    Tweet tweet =
                            invocation.getArgument(0);

                    if ("tweet-2".equals(tweet.getId())) {

                        return CompletableFuture
                                .failedFuture(
                                        new RuntimeException(
                                                "429 Rate Limit"
                                        )
                                );
                    }

                    return CompletableFuture
                            .completedFuture(
                                    new AuditResult(
                                            tweet.getId(),
                                            tweet.getFullText(),
                                            false,
                                            "Safe"
                                    )
                            );
                });

        List<AuditResult> results =
                auditOrchestrator.runAudit(
                        tweets,
                        processed -> {}
                );

        assertEquals(3, results.size());

        AuditResult failed =
                results.get(1);

        assertFalse(
                failed.isShouldDelete()
        );

        assertTrue(
                failed.getReason()
                        .contains(
                                "Evaluation failed"
                        )
        );
    }
     @Test
    void shouldReportProgressAfterEachBatch() {

        List<Tweet> tweets =
                createTweets(23);

        List<Integer> progressUpdates =
                new ArrayList<>();

        when(geminiService.evaluateTweet(any()))
                .thenAnswer(invocation -> {

                    Tweet tweet =
                            invocation.getArgument(0);

                    return CompletableFuture
                            .completedFuture(
                                    new AuditResult(
                                            tweet.getId(),
                                            tweet.getFullText(),
                                            false,
                                            "Safe"
                                    )
                            );
                });

        auditOrchestrator.runAudit(
                tweets,
                progressUpdates::add
        );

        assertEquals(
                List.of(10, 20, 23),
                progressUpdates
        );
    }

    private List<Tweet> createTweets(
            int count
    ) {

        List<Tweet> tweets =
                new ArrayList<>();

        for (int i = 1; i <= count; i++) {

            Tweet tweet = new Tweet();

            tweet.setId("tweet-" + i);

            tweet.setFullText(
                    "Tweet " + i
            );

            tweets.add(tweet);
        }

        return tweets;
    }

}
