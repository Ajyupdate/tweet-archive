package com.example.tweet_archive.service.AuditJobService;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import com.example.tweet_archive.model.AuditResult;
import com.example.tweet_archive.model.JobStatus;
import com.example.tweet_archive.model.Tweet;
import com.example.tweet_archive.service.ArchiveParserService.ArchiveParser;
import com.example.tweet_archive.service.AuditOrchestrator.AuditOrchestrator;
import com.example.tweet_archive.service.CsvExportService.CsvExportService;

public class AuditJobService {
    private static final Logger logger = LoggerFactory.getLogger(AuditJobService.class);

    private final ArchiveParser archiveParser;
    private final AuditOrchestrator auditOrchestrator;
    private final CsvExportService csvExportService;

    public AuditJobService(ArchiveParser archiveParser, AuditOrchestrator auditOrchestrator, CsvExportService csvExportService) {
        this.archiveParser = archiveParser;
        this.auditOrchestrator = auditOrchestrator;
        this.csvExportService = csvExportService;
    }

     // @Async means this method returns immediately to the controller.
    // The actual work runs on the "taskExecutor" thread pool defined
    // in AppConfig. Because of Spring's proxy mechanism, this MUST be
    // called from a different bean (AuditController) — calling it from
    // within this class would bypass the proxy and run synchronously.
    //
    // Return type is void because the controller doesn't need to await
    // a result — job state is tracked via JobStatus and polled via
    // GET /audit/status/{jobId}.
    //
    // The entire method is wrapped in try/catch. There is no caller
    // waiting on this thread to handle exceptions, so any uncaught
    // exception would silently disappear. We catch everything and
    // mark the job FAILED with the error message instead.

    @Async("taskExecutor")
    public void runJob(JobStatus job, Path archivePath){
        try {
            job.setStatus(JobStatus.Status.RUNNING);
            job.setStartedAt(LocalDateTime.now());
            logger.info("Job {} started for archive: {}", job.getJobId(), archivePath);

            //Step 2 - parse the archive
            List<Tweet> tweets = archiveParser.parseArchive(archivePath);
            job.setTotalTweets(tweets.size());
            logger.info("Job {} parsed {} tweets", job.getJobId(), tweets.size());

            // Step 3 — run the audit with a progress callback.
            // The lambda passed as onBatchComplete is called by
            // AuditOrchestrator after each batch with the cumulative
            // processed count. This updates processedTweets atomically
            // so status polling always shows current progress.
            List<AuditResult> results = auditOrchestrator.runAudit(
                tweets,
                processedCount -> {
                    job.setProcessedTweets(processedCount);
                    logger.debug("Job {} progress: {}/{}", job.getJobId(), processedCount, tweets.size());
                } );
            
            //Step 4 - export to CSV
            Path outpuPath = csvExportService.exportResults(results);
            job.setOutputPath(outpuPath.toString());
            logger.info("Job {} complete. Output: {}", job.getJobId(), outpuPath);

            //Step 5 - mark done
            job.setStatus(JobStatus.Status.DONE);
            job.setCompletedAt(LocalDateTime.now());

        }catch(Exception e){
            // Any failure in the pipeline lands here.
            // We never rethrow — there's no caller on this thread.
            logger.error("Job {} failed {}", job.getJobId(), e.getMessage(), e);
            job.setStatus(JobStatus.Status.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
        }
    }
}
