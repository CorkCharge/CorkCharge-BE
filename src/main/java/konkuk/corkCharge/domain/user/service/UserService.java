package konkuk.corkCharge.domain.user.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.image.service.S3ImageService;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.dto.response.GetUserProfileResponse;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final S3ImageService s3ImageService;

    @Transactional
    public GetUserProfileResponse getUserProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        String imageUrl = imageRepository.findProfileImageByUser_UserId(userId)
                .map(Image::getImageUrl)
                .orElse(null);

        return new GetUserProfileResponse(
                user.getName(),
                user.getSocialId(),
                imageUrl
        );
    }

    @Transactional
    public void updateUserProfile(Long userId, String name, MultipartFile imageFile){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if(name != null){
            user.setName(name);
        }

        if(imageFile != null && !imageFile.isEmpty()){
                    String newImageUrl = s3ImageService.uploadImages(List.of(imageFile), ImageCategory.USER, ImageType.USER)
                            .get(0);

            imageRepository.findProfileImageByUser_UserId(userId)
                    .ifPresentOrElse(
                            existingImage-> {
                                existingImage.setImageUrl(newImageUrl);
                                existingImage.setType(ImageType.USER);
                                existingImage.setCategory(ImageCategory.USER);
                            },
                            () -> {
                                Image newImage = Image.builder()
                                        .user(user)
                                        .imageUrl(newImageUrl)
                                        .type(ImageType.USER)
                                        .category(ImageCategory.USER)
                                        .build();
                                imageRepository.save(newImage);
                            }
                            );
        }
    }


}
