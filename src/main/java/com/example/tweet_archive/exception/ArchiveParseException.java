package com.example.tweet_archive.exception;

public class ArchiveParseException extends RuntimeException {
    public ArchiveParseException(String message) {
        super(message);
    }

    public ArchiveParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
