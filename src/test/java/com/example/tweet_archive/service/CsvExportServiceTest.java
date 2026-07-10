package com.example.tweet_archive.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.tweet_archive.config.AuditProperties;
import com.example.tweet_archive.model.AuditResult;
import com.example.tweet_archive.service.CsvExportService.CsvExportService;

class CsvExportServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldExportResultsSuccessfully() 
        throws Exception{
            Path output = tempDir.resolve("audit-results.csv");
            AuditProperties properties = new AuditProperties(List.of("offensive"), 10, output.toString());
            CsvExportService service = new CsvExportService(properties);
            List<AuditResult> results = List.of(new AuditResult("1", "tweet one", true, "offensive"),
                                                new AuditResult("2", "tweet two", false, "safe"),
                                                new AuditResult("3", "tweet three", false, "neutral"));

            Path file = service.exportResults(results);
            List<String> lines = Files.readAllLines(file);
            assertEquals(4, lines.size());
            assertEquals("tweetId, tweetUrl, shouldDelete, reason", lines.get(0));                                    
        }
    
    @Test
    void shouldEscapeCommas()
        throws Exception{
            Path output = tempDir.resolve("comma.csv");
            AuditProperties properties = new AuditProperties(List.of(), 10, output.toString());
            CsvExportService service = new CsvExportService(properties);
                       List<AuditResult> results = List.of(new AuditResult("1", "text", true, "bad, controversial content"),
                                                            new AuditResult("2", "text", false, "safe"));


            Path file = service.exportResults(results);
            String row = Files.readAllLines(file).get(1);
            assertTrue(
                row.contains(
                        "\"bad, controversial content\""
                )
            );

        }
    
    @Test
    void shouldWriteOnlyHeaderForEmptyResults()
        throws Exception{
            Path output = tempDir.resolve("empty.csv");
            AuditProperties properties = new AuditProperties(List.of(), 10, output.toString());
            CsvExportService service = new CsvExportService(properties);
            Path file = service.exportResults(List.of());
            List<String> lines = Files.readAllLines(file);
            assertEquals(1, lines.size());
            assertEquals("tweetId,tweetUrl,shouldDelete,reason",
            lines.get(0));
        }
}
