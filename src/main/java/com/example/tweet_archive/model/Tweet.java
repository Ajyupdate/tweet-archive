package com.example.tweet_archive.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tweet {
    @NotNull
    @JsonProperty("id_str")
    private String id;

    @NotNull
    @JsonProperty("full_text")
    private String fullText;

    @NotNull
    @JsonProperty("created_at")
    private String createdAt;

    @NotNull 
    @JsonProperty("retweet_count")
    private String retweetCount;

    @NotNull
    @JsonProperty("favorite_count")
    private String favoriteCount;

    // @Override
    // public String toString() {
    //     return "Tweet{id=" + id + ", text=" + fullText + "}";
    // }

    public Tweet() {}

    public Tweet(String id, String fullText, String createdAt) {
        this.id = id;
        this.fullText = fullText;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getFullText() {
        return fullText;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setRetweetCount(String retweetCount) {
        this.retweetCount = retweetCount;
    }

    public void setFavoriteCount(String favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

}



