package konkuk.corkCharge.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@RequiredArgsConstructor
public class AmazonS3ServiceImpl implements AmazonS3Service{

    private final AmazonS3 amazonS3Client;

    @Override
    public void putObject(PutObjectRequest request) {
        amazonS3Client.putObject(request);
    }

    @Override
    public URL getUrl(String bucketName, String fileName) {
        return amazonS3Client.getUrl(bucketName, fileName);
    }

    @Override
    public void deleteObject(String bucketName, String key) {
        amazonS3Client.deleteObject(bucketName, key);
    }
}
