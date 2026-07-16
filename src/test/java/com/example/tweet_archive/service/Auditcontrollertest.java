package com.example.tweet_archive.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.tweet_archive.model.JobStatus;
import com.example.tweet_archive.registry.JobRegistry;
import com.example.tweet_archive.service.AuditJobService.AuditJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;
class Auditcontrollertest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditJobService auditJobService;

    @MockitoBean
    private JobRegistry jobRegistry;

    @Test
    void postAuditStart_returns202WithJobId() throws Exception {
        //AuditJobService.runjob is void and @Async - we just verify
        //the controller doesn't blow up calling it
        doNothing().when(auditJobService).runJob(any(), any());

         mockMvc.perform(post("/audit/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"archivePath\": \"/some/path/tweet.js\"}"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.jobId").exists())
            .andExpect(jsonPath("$.jobId").isString());
    }

    @Test
    void getAuditStatus_returnsJobStatus_whenJobExists() throws Exception{
        // Pre-register a known job so the registry mock can return it
        String knownJobId = "test-job-123";
        JobStatus job = new JobStatus(knownJobId);
        job.setStatus(JobStatus.Status.RUNNING);
        job.setTotalTweets(100);
        job.setProcessedTweets(40);

        org.mockito.Mockito.when(jobRegistry.findById(knownJobId))
            .thenReturn(java.util.Optional.of(job));
        mockMvc.perform(get("/audit/status/" + knownJobId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.jobId").value(knownJobId))
            .andExpect(jsonPath("$.status").value("RUNNING"))
            .andExpect(jsonPath("$.totalTweets").value(100))
            .andExpect(jsonPath("$.processedTweets").value(40));    
    }

    @Test
    void getAuditStatus_returns404_whenJobNotFound() throws Exception{
        org.mockito.Mockito.when(jobRegistry.findById("unknown-id"))
            .thenReturn(java.util.Optional.empty());
        
            mockMvc.perform(get("/audit/status/unknown-id"))
                .andExpect(status().isNotFound());
    }
}
