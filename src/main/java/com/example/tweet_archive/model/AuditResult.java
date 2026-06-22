package com.example.tweet_archive.model;

public class AuditResult {
    private String tweetId;

    private String fullText;

    private boolean shouldDelete;

    private String reason;

    public AuditResult() {}

    public AuditResult(
        String tweetId,
        String fullText,
        boolean shouldDelete,
        String reason
    ){
        this.tweetId = tweetId;
        this.fullText = fullText;
        this.shouldDelete = shouldDelete;
        this.reason = reason;
    }
    public String getTweetId(){
        return tweetId;
    }
    public String getFullText(){
        return fullText;
    }
    public boolean isShouldDelete(){
        return shouldDelete;
    }
    public String getReason(){
        return reason;
    }

    public void setTweetId(String id){
        this.tweetId = id;
    }

    public void setFullText(String fullText){
        this.fullText = fullText;
    }

    public void setShouldDelete(boolean shouldDelete){
        this.shouldDelete = shouldDelete;
    }

    public void setReason(String reason){
        this.reason = reason;
    }
}
