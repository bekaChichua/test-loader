package ge.nest.ai.nesttestdata.servicies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FolderUploadService {

    private final S3UploadService s3UploadService;

    @Autowired
    public FolderUploadService(S3UploadService s3UploadService) {
        this.s3UploadService = s3UploadService;
    }

    public List<String> uploadAllFilesInFolder(String folderPath) {
        File folder = new File(folderPath);
        List<String> uploadedFileUrls = new ArrayList<>();

        if (!folder.exists() || !folder.isDirectory()) {
            log.error("Provided path is not a valid directory: {}", folderPath);
            throw new IllegalArgumentException("Invalid folder path: " + folderPath);
        }

        log.info("Starting upload for files in folder: {}", folderPath);
        File[] files = folder.listFiles();

        if (files == null || files.length == 0) {
            log.info("No files found in folder: {}", folderPath);
            return uploadedFileUrls;
        }

        for (File file : files) {
            if (file.isFile()) { // Only upload actual files, not sub-directories
                try {
                    log.info("Uploading file: {}", file.getName());
                    String fileUrl = s3UploadService.uploadFile(file);
                    uploadedFileUrls.add(fileUrl);
                    log.info("Successfully uploaded {} to S3. URL: {}", file.getName(), fileUrl);
                } catch (IOException e) {
                    log.error("Failed to upload file {}: {}", file.getName(), e.getMessage(), e);
                    // Decide how to handle partial failures: continue, stop, collect errors?
                    // For now, we log and continue.
                } catch (IllegalArgumentException e) {
                    log.error("Skipping invalid file {}: {}", file.getName(), e.getMessage());
                }
            }
        }
        log.info("Finished uploading files from folder: {}. Total uploaded: {}", folderPath, uploadedFileUrls.size());
        return uploadedFileUrls;
    }
}
