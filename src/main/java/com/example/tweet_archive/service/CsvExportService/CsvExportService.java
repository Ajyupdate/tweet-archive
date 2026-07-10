package com.example.tweet_archive.service.CsvExportService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.tweet_archive.config.AuditProperties;
import com.example.tweet_archive.exception.CsvExportException;
import com.example.tweet_archive.model.AuditResult;

@Service
public class CsvExportService {
    private static final
     Logger logger = LoggerFactory.getLogger(CsvExportService.class);

    private final AuditProperties auditProperties;

    public CsvExportService(AuditProperties auditProperties){
        this.auditProperties = auditProperties;
    }

    public Path exportResults(
        List<AuditResult> results
    ){
        Path outpuPath = Path.of(auditProperties.outputPath());
        try{
            if (outpuPath.getParent() != null){
                Files.createDirectories(outpuPath.getParent());
            }

            try (
                BufferedWriter writer = Files.newBufferedWriter(outpuPath)
            ){
                writer.write("tweetId,tweetUrl,shouldDelete,reason");

                writer.newLine();

                if(results != null){
                    for(AuditResult result: results){
                        writer.write(buildCsvRow(result));

                        writer.newLine();
                    }
                }
                writer.flush();
            }
            logger.info("Successfully wrote {} rows to {}",
                results == null ? 0 : results.size(),
                outpuPath
            );
            return outpuPath;
        }catch (IOException e){
            throw new CsvExportException("Failed to export CSV file", e);
        }
    }

    private String buildCsvRow(AuditResult result){
        String tweetUrl = "https://twitter.com/i/web/status/"
                            + result.getTweetId();
        
        return String.join(
            ",",
                        escapeCsv(result.getTweetId()),
                        escapeCsv(tweetUrl),
                        String.valueOf(result.isShouldDelete()),
                        escapeCsv(result.getReason())
        );
    }

    private String escapeCsv(
        String value
    ){
        if(value == null){
            return "";
        }
        boolean requiresQuotes = 
                    value.contains(",")
                    || value.contains("\n")
                    || value.contains("\"");
        String escaped = value.replace("\"", "\"\"");

        

        return requiresQuotes ? "\"" + escaped + "\"" : escaped;
    }

}
