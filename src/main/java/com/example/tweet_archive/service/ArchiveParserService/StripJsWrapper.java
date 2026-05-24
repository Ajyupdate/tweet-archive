package com.example.tweet_archive.service.ArchiveParserService;

import org.springframework.stereotype.Component;

import com.example.tweet_archive.exception.ArchiveReadException;

@Component
public class StripJsWrapper {
    public String stripJsWrapper(String rawData){
        if(rawData == null || rawData.isEmpty()){
            throw new ArchiveReadException("File content is empty or null");
        }

        int start = rawData.indexOf("[");
        int end = rawData.indexOf("]");

        if(start == -1 || end == -1){
            throw new ArchiveReadException("Invalid format: Json array not found");
        }

        if(start > end){
            throw new ArchiveReadException("Invalid format ] appears before [");
        }

        String json = rawData.substring(start, end + 1).trim();
        if(json.isEmpty()){
            throw new ArchiveReadException("Data is empty");
        }
        return json;

    }
}
