package com.example.tweet_archive.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
public class JobStatus {
    public enum Status{
        
         PENDING, RUNNING, DONE, FAILED
    }
    private final String jobId;

    //volatile ensures changes made by the async thread are
    // immediately visible to the HTTP thread reading status

    private volatile Status status;
    private volatile int totalTweets;
    private volatile String outputPath;
    private volatile String errorMessage;
    private volatile LocalDateTime startedAt;
    private volatile LocalDateTime completedAt;

    //AtomicInteger so the batch callback can increment this
    //from the async thread without a race condition

    private final AtomicInteger processedTweets = new AtomicInteger(0);

    public JobStatus(String jobId) {
        this.jobId = jobId;
        this.status = Status.PENDING;
    }

    // -- Getters --

    public String getJobId() {
        return jobId;
    }

    public Status getStatus() {
        return status;
    }

    public int getTotalTweets() {
        return totalTweets;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public int getProcessedTweets() {
        return processedTweets.get();
    }

    //-- Setters used by AuditJobService to transition state ---

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setTotalTweets(int totalTweets) {
        this.totalTweets = totalTweets;
    }

    //Called from the AuditOrchestrator batch callback.
    //Uses set() rather than incrementAndGet() because the 
    //Orchestrator passes the cumulative count directly.

    public void setProcessedTweets(int count) {
        this.processedTweets.set(count);
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

}
