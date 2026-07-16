package com.example.tweet_archive.controller;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.tweet_archive.service.AuditJobService.AuditJobService;

import com.example.tweet_archive.model.AuditStartRequest;
import com.example.tweet_archive.model.JobStatus;
import com.example.tweet_archive.registry.JobRegistry;
@RestController
@RequestMapping("/audit")
public class AuditController {
    private final AuditJobService auditJobService;
    private final JobRegistry jobRegistry;

    public AuditController(
        AuditJobService auditJobService,
        JobRegistry jobRegistry
    ){
        this.auditJobService = auditJobService;
        this.jobRegistry = jobRegistry;
    }

    // POST /audit/start
    //
    // Returns 202 Accepted immediately — the audit runs in the background.
    // The response body contains only the jobId so the client knows what
    // to poll. We do NOT return the full JobStatus here because it would
    // show PENDING for every field, which is misleading.
    //
    // Sequence:
    // 1. Create job with a new UUID and PENDING status
    // 2. Register it so GET /audit/status can find it
    // 3. Hand off to AuditJobService — this call returns immediately
    //    because runJob() is @Async
    // 4. Return 202 with jobId

    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startAudit(
        @RequestBody AuditStartRequest request
    ){
        String jobId = UUID.randomUUID().toString();
        JobStatus job = new JobStatus(jobId);

        jobRegistry.register(job);
        auditJobService.runJob(job, Path.of(request.archivePath()));

        return ResponseEntity
            .accepted()
            .body(Map.of("jobId", jobId));
    }

     // GET /audit/status/{jobId}
    //
    // Returns the full JobStatus as JSON so the caller can see:
    // - current status (PENDING / RUNNING / DONE / FAILED)
    // - progress (processedTweets / totalTweets)
    // - outputPath once complete
    // - errorMessage if failed
    //
    // Returns 404 if the jobId was never registered (unknown job).

    @GetMapping("/status/{jobId}")
    public ResponseEntity<JobStatus> getStatus(
        @PathVariable String jobId
    ){
        return jobRegistry.findById(jobId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
