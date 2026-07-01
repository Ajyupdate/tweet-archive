package com.example.tweet_archive.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import com.example.tweet_archive.exception.ArchiveParseException;
import com.example.tweet_archive.model.Tweet;
import com.example.tweet_archive.service.ArchiveParserService.ArchiveParser;

@SpringBootTest
class ArchiveParserServiceTest {
    @Autowired
    private ArchiveParser archiveParser;

    @Test
    void shouldParseArchiveSUccessfully() throws Exception {
        ClassPathResource resource = new ClassPathResource("valid-tweet.js");

        Path path = resource.getFile().toPath();

        List<Tweet> tweets = archiveParser.parseArchive(path);

        assertEquals(2, tweets.size());

        Tweet firsTweet = tweets.get(0);

        assertEquals("1", firsTweet.getId());
        assertEquals("Hello Twitter", firsTweet.getFullText());
    }

    @Test
    void shouldThrowExceptionWhenWrapperInvalid() throws Exception{
        ClassPathResource resource = new ClassPathResource("malformed-wrapper.js");

        Path path = resource.getFile().toPath();

        assertThrows(ArchiveParseException.class, () -> archiveParser.parseArchive(path));
    }

    @Test
    void shouldThrowExceptionWhenInvalidJson() throws Exception{
        ClassPathResource resource = new ClassPathResource("invalid-json.js");

        Path path = resource.getFile().toPath(); 

        assertThrows(ArchiveParseException.class, () -> archiveParser.parseArchive(path));
    }

}