package ge.nest.ai.nesttestdata.servicies;


import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.net.URI;

@Service
public class S3UploaderClientService {

    private final RestClient restClient;

    public S3UploaderClientService(RestClient.Builder restClientBuilder) {
        // You might configure the RestClient builder further if needed
        this.restClient = restClientBuilder.build();
    }

    /**
     * Uploads a local file to S3 using a pre-signed URL.
     *
     * @param presignedUrlInfo    DTO containing the presigned URL and expected method.
     * @param localFile           The local file to upload.
     * @param expectedContentType The Content-Type that was used when generating the presigned URL.
     *                            The S3 presigned URL *requires* this to match.
     * @throws IOException If file reading or HTTP request fails.
     */
    public void uploadFileWithPresignedUrl(PresignedUrlInfo presignedUrlInfo, File localFile, String expectedContentType) throws IOException {
        if (!localFile.exists() || !localFile.isFile()) {
            throw new IOException("Local file does not exist or is not a file: " + localFile.getAbsolutePath());
        }
        if (!"PUT".equalsIgnoreCase(presignedUrlInfo.getMethod())) {
            throw new IllegalArgumentException("Presigned URL method must be PUT for this uploader. Received: " + presignedUrlInfo.getMethod());
        }

        URI uri = URI.create(presignedUrlInfo.getPresignedUrl());
        long contentLength = localFile.length();

        // Prepare the request body from the file
        FileSystemResource fileResource = new FileSystemResource(localFile);

        // --- Using RestClient ---
        try {
            ResponseEntity<Void> response = restClient.put()
                    .uri(uri)
                    .contentType(MediaType.parseMediaType(expectedContentType)) // MUST match Content-Type used for presigning
                    .contentLength(contentLength) // Good practice, sometimes required by S3 or presigned URL constraints
                    .body(fileResource)
                    .retrieve()
                    .toBodilessEntity(); // We don't expect a body from S3 on successful PUT

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("File uploaded successfully to S3 via presigned URL: " + localFile.getName());
                System.out.println("S3 Object Key: " + presignedUrlInfo.getObjectKey());
            } else {
                // S3 often returns XML error messages in the body for failures
                // For simplicity, just printing status. In production, parse the error.
                throw new IOException("Failed to upload file to S3. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // Handle RestClientException or other exceptions
            throw new IOException("Error during S3 upload with presigned URL: " + e.getMessage(), e);
        }
    }

    // Example DTO to represent the info received from Service A
    // This should match the structure Service A sends
    @AllArgsConstructor
    public static class PresignedUrlInfo {
        private String presignedUrl;
        private String objectKey;
        private String method; // e.g., "PUT"
        // Potentially add expectedContentType here if Service A sends it explicitly

        // Getters and Setters
        public String getPresignedUrl() {
            return presignedUrl;
        }

        public void setPresignedUrl(String presignedUrl) {
            this.presignedUrl = presignedUrl;
        }

        public String getObjectKey() {
            return objectKey;
        }

        public void setObjectKey(String objectKey) {
            this.objectKey = objectKey;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

}
