package com.example.tweet_archive.service.ArchiveParserService;

import java.util.List;
import java.util.Objects;

import org.hibernate.boot.archive.spi.ArchiveException;
import org.springframework.stereotype.Component;

import com.example.tweet_archive.model.Tweet;
import com.example.tweet_archive.model.TweetWrapper;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class ParseJson {
    public List<Tweet> parseJson(String jsonContent){
       try{
        ObjectMapper objectMapper = new ObjectMapper();
        List<TweetWrapper> wrappers = objectMapper.readValue(jsonContent, new TypeReference<List<TweetWrapper>>(){});
        return wrappers.stream()
            .map(TweetWrapper:: getTweet)
            .filter(Objects::nonNull)
            .toList();
       }
       catch(ArchiveException e){
        throw new ArchiveException("Failed to parse twitter aechive json", e);
       }
    }
}
