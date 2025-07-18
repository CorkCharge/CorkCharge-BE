package konkuk.corkCharge.domain.user.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.dto.request.PutUserProfileRequest;
import konkuk.corkCharge.domain.user.dto.response.GetUserProfileResponse;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static konkuk.corkCharge.domain.image.domain.ImageType.USER;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

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
            String newImageUrl = s3Uploader.upload(imageFile);

            imageRepository.findProfileImageByUser_UserId(userId)
                    .ifPresentOrElse(
                            existingImage-> {
                                s3Uploader.delete(existingImage.getImageUrl());

                                existingImage.setImageUrl(newImageUrl);
                            },
                            () -> {
                                Image newImage = new Image(user, newImageUrl, USER);
                            }
                            );
        }
    }


}
