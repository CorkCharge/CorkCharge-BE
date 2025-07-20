package konkuk.corkCharge.domain.image.service;


import com.amazonaws.services.s3.model.PutObjectRequest;

import java.net.URL;

public interface AmazonS3Service {

    void putObject(PutObjectRequest request);
    URL getUrl(String bucketName, String fileName);
}
