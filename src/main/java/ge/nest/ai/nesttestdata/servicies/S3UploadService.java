package ge.nest.ai.nesttestdata.servicies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Service
@Slf4j
public class S3UploadService {

    private final S3Client s3Client;
    private final String bucketName;

    @Autowired
    public S3UploadService(S3Client s3Client, @Value("${aws.s3.bucketName:my-bucket-nest-core}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String uploadFile(File file) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File does not exist or is not a regular file: " + file.getAbsolutePath());
        }

        String originalFileName = file.getName();
        String contentType = Files.probeContentType(file.toPath()); // Best effort to get content type
        if (contentType == null) {
            contentType = "application/octet-stream"; // Default if unknown
        }
        long fileSize = file.length();

        String s3Key = generateUniqueKey(originalFileName);

        try (FileInputStream fis = new FileInputStream(file)) {
            PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength(fileSize);

            // if (makePublicRead) {
            //    putObjectRequestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            // }

            s3Client.putObject(putObjectRequestBuilder.build(),
                    RequestBody.fromInputStream(fis, fileSize));

            log.info("File {} uploaded successfully to S3 bucket {} with key {}",
                    originalFileName, bucketName, s3Key);

            return getObjectUrl(s3Key);

        } catch (S3Exception e) {
            log.error("S3Exception while uploading File {}: {}", originalFileName, e.getMessage(), e);
            throw new IOException("Failed to upload File to S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    private String generateUniqueKey(String originalFileName) {
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + fileExtension;
    }

    public String getObjectUrl(String objectKey) {
        // This basic URL construction assumes public read access or presigned URL usage.
        // For private buckets, you'd typically generate a presigned URL.
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, s3Client.serviceClientConfiguration().region().id(), objectKey);
        // Or, more robustly if you have AWS Utilities:
        // return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(objectKey)).toExternalForm();
    }
}
