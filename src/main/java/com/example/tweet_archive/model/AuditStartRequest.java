package com.example.tweet_archive.model;

// Simple record to deserialize the POST /audit/start request body.
// Jackson maps the "archivePath" JSON field to this record component.
public record AuditStartRequest(String archivePath){} 