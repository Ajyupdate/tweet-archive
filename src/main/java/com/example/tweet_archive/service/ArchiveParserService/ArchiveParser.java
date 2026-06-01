package com.example.tweet_archive.service.ArchiveParserService;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.nio.charset.StandardCharsets;
import com.example.tweet_archive.exception.ArchiveParseException;
import com.example.tweet_archive.exception.ArchiveReadException;
import org.springframework.stereotype.Component;

import com.example.tweet_archive.model.Tweet;
import com.example.tweet_archive.model.TweetWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
@Component
public class ArchiveParser {

    private ObjectMapper objectMapper;
    public ArchiveParser(   ObjectMapper objectMapper){

        this.objectMapper = objectMapper;
    }

    public List<Tweet> parseArchive(Path filePath) {
        try {
            String raw = readRawContent(filePath);

            String cleanJson = stripJsWrapper(raw);

            return parseJson(cleanJson);
        }catch (ArchiveReadException | ArchiveParseException e) {
            throw e;
        }
         catch (Exception e) {
            throw new ArchiveParseException("Failed to parse archive", e);
        }
    }

    private String readRawContent(Path filePath) {
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new ArchiveReadException("File not found " + filePath, e);
        } catch (Exception e) {
            throw new ArchiveReadException("Failed to read archive content " + filePath, e);
        }
    }

    private String stripJsWrapper(String rawData) {
        if(rawData == null || rawData.isEmpty()){
            throw new ArchiveParseException("File content is empty or null");
        }

        int start = rawData.indexOf("[");
        int end = rawData.lastIndexOf("]");

        if(start == -1 || end == -1){
            throw new ArchiveParseException("Invalid format: Json array not found");
        }

        if(start > end){
            throw new ArchiveParseException("Invalid format ] appears before [");
        }

        String json = rawData.substring(start, end + 1).trim();
        if(json.isEmpty()){
            throw new ArchiveParseException("Data is empty");
        }
        return json;
    }

    private List<Tweet> parseJson(String jsonContent) {
        try {
            List<TweetWrapper> wrappers = objectMapper.readValue(jsonContent, new com.fasterxml.jackson.core.type.TypeReference<List<TweetWrapper>>(){});
            return wrappers.stream().map(TweetWrapper::getTweet).filter(Objects::nonNull).toList();
        } catch (java.io.IOException e) {
            throw new ArchiveParseException("Failed to parse twitter archive json", e);
        }
    }
}
