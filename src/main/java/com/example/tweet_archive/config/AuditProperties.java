package com.example.tweet_archive.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "audit")
public record AuditProperties(
    List<String> criteria,
    int batchSize,
    String outputPath
){}
