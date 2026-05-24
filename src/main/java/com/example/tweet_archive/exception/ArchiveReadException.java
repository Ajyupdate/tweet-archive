package com.example.tweet_archive.exception;

public class ArchiveReadException extends RuntimeException {
    public ArchiveReadException(String message){
        super(message);
    }

    public ArchiveReadException(String message, Throwable cause){
        super(message, cause);
    }
}
