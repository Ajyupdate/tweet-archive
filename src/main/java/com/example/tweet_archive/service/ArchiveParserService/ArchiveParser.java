package com.example.tweet_archive.service.ArchiveParserService;

import java.nio.file.Path;
import java.util.List;

import org.hibernate.boot.archive.spi.ArchiveException;
import org.springframework.stereotype.Component;

import com.example.tweet_archive.model.Tweet;

@Component
public class ArchiveParser {
    private ReadRawContent readRawContent;
    private StripJsWrapper stripJsWrapper;
    private ParseJson parseJson;

    public ArchiveParser(ReadRawContent readRawContent, StripJsWrapper stripJsWrapper, ParseJson parseJson){
        this.readRawContent = readRawContent;
        this.stripJsWrapper = stripJsWrapper;
        this.parseJson = parseJson;
    }

    public List<Tweet> parseArchive(Path filePath) {
        try {
            String raw = readRawContent.readRawContent(filePath);

            String cleanJson = stripJsWrapper.stripJsWrapper(raw);

            return parseJson.parseJson(cleanJson);
        } catch (Exception e) {
            throw new ArchiveException("Failed to parse archive", e);
        }
    }
}
