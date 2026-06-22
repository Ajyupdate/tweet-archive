package com.example.tweet_archive.model;

import java.util.List;

public record GeminiResponse(
    List<Candidate> candidates
){}