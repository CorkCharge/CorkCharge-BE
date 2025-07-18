package konkuk.corkCharge.domain.user.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.dto.response.UserProfileResponseDto;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public UserProfileResponseDto getUserProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BaseExceptionResponseStatus.USER_NOT_FOUND));

        String imageUrl = imageRepository.findFirstByUserId(userId)
                .map(Image::getImageUrl)
                .orElse(null);

        return new UserProfileResponseDto(
                user.getName(),
                user.getSocialId(),
                imageUrl
        );
    }

}
