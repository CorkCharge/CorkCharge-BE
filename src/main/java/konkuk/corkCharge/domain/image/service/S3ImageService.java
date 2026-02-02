package konkuk.corkCharge.domain.image.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.FAILED_DELETE_IMAGE;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.FAILED_UPLOAD;

@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final AmazonS3Service s3Service;

    @Value("${cloud.aws.s3.bucket}")
    private String BUCKET_NAME;

    @Value("${cloud.aws.s3.base-url}")
    private String BASE_URL;

    public List<String> uploadImages(List<MultipartFile> files, ImageCategory catecory, ImageType imageType) {
        String dir = getDirectory(catecory, imageType);
        List<String> uploadedUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String fullPath = dir + "/" + fileName;

            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());

                PutObjectRequest request = new PutObjectRequest(
                        BUCKET_NAME,
                        fullPath,
                        file.getInputStream(),
                        metadata
                );

                s3Service.putObject(request);
                uploadedUrls.add(BASE_URL + "/" + fullPath);


            } catch (IOException e) {
                throw new CustomException(FAILED_UPLOAD);
            }
        }
        return uploadedUrls;
    }

    public void deleteImage(String imageUrl){
        if(imageUrl == null || imageUrl.isEmpty()) return;

        try{
            String key = imageUrl.substring(imageUrl.indexOf(".amazonaws.com/") + ".amazonaws.com/".length());
            s3Service.deleteObject(BUCKET_NAME, key);
        } catch(Exception e){
            throw  new CustomException(FAILED_DELETE_IMAGE);
        }
    }

    private String getDirectory(ImageCategory category, ImageType imageType) {
        return switch (category) {
            case RESTAURANT -> {
                if (imageType == null) {
                    throw new IllegalArgumentException("RESTAURANT category requires non-null ImageType.");
                }
                yield "restaurant/" + imageType.name().toLowerCase();
            }
            case REVIEW -> "review";
            case TIP -> "tip";
            case CORKAGE -> "corkage";
            case USER -> "user";
            case NOTIFICATION -> "notification";
        };
    }

}
