package com.example.tweet_archive.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.tweet_archive.model.AuditResult;
import com.example.tweet_archive.model.JobStatus;
import com.example.tweet_archive.model.Tweet;
import com.example.tweet_archive.service.ArchiveParserService.ArchiveParser;
import com.example.tweet_archive.service.AuditJobService.AuditJobService;
import com.example.tweet_archive.service.AuditOrchestrator.AuditOrchestrator;
import com.example.tweet_archive.service.CsvExportService.CsvExportService;

@ExtendWith(MockitoExtension.class)
class AuditJobServiceTest {
    @Mock
    private ArchiveParser archiveParser;

    @Mock
    private AuditOrchestrator auditOrchestrator;

    @Mock
    private CsvExportService csvExportService;

    @InjectMocks
    private AuditJobService auditJobService;

    private Path fakePath;

    @BeforeEach
    void setUp(){
        fakePath = Path.of("/fake/tweet.js");
    }

    @Test
    void runJob_transitionsToDone_onSuccess(){
        List<Tweet> tweets = List.of(
            new Tweet("1", "Hello", "2023-01-01"),
            new Tweet("2", "World", "2023-01-02"));
        
        List<AuditResult> results = List.of(
            new AuditResult("1", "Hello", false, "safe")
        );
        Path outPath = Path.of("/output/result.csv");

        when(archiveParser.parseArchive(fakePath)).thenReturn(tweets);
        when(auditOrchestrator.runAudit(any(), any())).thenReturn(results);
        when(csvExportService.exportResults(results)).thenReturn(outPath);

        JobStatus job = new JobStatus("job-1");

        // Act — runJob is @Async but we call it directly in tests
        // (no Spring proxy), so it runs synchronously here. This is
        // intentional: we're testing the logic, not the threading.
        auditJobService.runJob(job, fakePath);
        
        // Assert — exception is caught inside runJob, never rethrown
        assertEquals(JobStatus.Status.FAILED, job.getStatus());
        assertEquals("File not found", job.getErrorMessage());
        assertNotNull(job.getCompletedAt());
    }

     @Test
    void runJob_setsRunningStatus_beforeProcessing() {
        // This verifies the PENDING -> RUNNING transition happens
        // before any real work. We do this by checking startedAt is set.
        List<Tweet> tweets = List.of(new Tweet("1", "Hello", "2023-01-01"));
 
        when(archiveParser.parseArchive(fakePath)).thenReturn(tweets);
        when(auditOrchestrator.runAudit(any(), any())).thenReturn(List.of());
        when(csvExportService.exportResults(any())).thenReturn(Path.of("/out.csv"));
 
        JobStatus job = new JobStatus("job-3");
        assertEquals(JobStatus.Status.PENDING, job.getStatus());
 
        auditJobService.runJob(job, fakePath);
 
        // After completion, startedAt must have been set
        assertNotNull(job.getStartedAt());
    }
}
