package live.xavi.text_to_video.services;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioUploadService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public MinioUploadService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String upload(String filePath) throws Exception {
        // Generate a unique object name
        String objectName = "video_" + UUID.randomUUID() + ".mp4";

        // Upload the file
        try (InputStream is = new FileInputStream(filePath)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, -1, 10485760)
                            .contentType("video/mp4")
                            .build()
            );
        }

        // Generate a presigned URL valid for 24 hours
        String presignedUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectName)
                        .expiry(24, TimeUnit.HOURS)
                        .build()
        );

        return presignedUrl; // Return this to the frontend
    }
}
