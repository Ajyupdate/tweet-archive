package com.example.tweet_archive.registry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.tweet_archive.model.JobStatus;


//ConcurrentHashMap is safe for concurrent reads and writes without
//explicit synchronization - important since async threads write job
//stat while Http threads read it for status polling
@Component
public class JobRegistry {
    private final ConcurrentHashMap<String, JobStatus> jobs = new ConcurrentHashMap<>();

    public void register(JobStatus job){
        jobs.put(job.getJobId(), job);
    }

    public Optional<JobStatus> findById(String jobId){
        return Optional.ofNullable(jobs.get(jobId));
    }

}
