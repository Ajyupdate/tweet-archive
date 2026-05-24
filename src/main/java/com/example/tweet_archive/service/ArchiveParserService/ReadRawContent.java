package com.example.tweet_archive.service.ArchiveParserService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.example.tweet_archive.exception.ArchiveReadException;

@Component
public class ReadRawContent {
    public String readRawContent(Path filePath){
        try{
            return Files.readString(filePath, StandardCharsets.UTF_8);
        }catch(NoSuchFileException e){
            throw new ArchiveReadException("File not found: " + filePath, e);
        }catch(IOException e){
            throw new ArchiveReadException("Error reading file: " + filePath, e);
        }
    }
}
