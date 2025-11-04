package konkuk.corkCharge.domain.user.service;

import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.image.service.S3ImageService;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.review.dto.response.GetMyPageReviewResponse;
import konkuk.corkCharge.domain.review.repository.ReviewRepository;
import konkuk.corkCharge.domain.user.domain.Role;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.dto.response.GetMyPageResponse;
import konkuk.corkCharge.domain.user.dto.response.GetReviewResponse;
import konkuk.corkCharge.domain.user.dto.response.GetUserProfileResponse;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static konkuk.corkCharge.domain.image.domain.ImageCategory.*;
import static konkuk.corkCharge.domain.image.domain.ImageType.MAIN;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final S3ImageService s3ImageService;
    private final ReviewRepository reviewRepository;

    @Transactional
    public GetUserProfileResponse getUserProfile(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return new GetUserProfileResponse(
                user.getName(),
                user.getEmail()
        );
    }

    @Transactional
    public void updateUserProfile(Long userId, String name){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if(name != null){
            user.setName(name);
        }
    }

    @Transactional(readOnly = true)
    public List<GetReviewResponse> getUserReviews(Long userId){
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<Review> reviews = reviewRepository.findAllByUser_UserId(userId);

        return reviews.stream()
                .map(review -> new GetReviewResponse(
                        review.getReviewId(),
                        review.getRestaurant().getRestaurantId(),
                        userId,
                        review.getContent(),
                        review.getRating(),
                        imageRepository.findFirstByCategoryAndTypeIdOrderByCreatedAtAsc(REVIEW, review.getReviewId())
                                .map(Image::getImageUrl)
                                .orElse(null),
                        review.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public GetMyPageResponse getMyPage(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<Review> reviews = reviewRepository.findAllByUser_UserId(userId);

        List<GetMyPageReviewResponse> reviewDtos = reviews.stream()
                .map(review -> {
                    var restaurant = review.getRestaurant();
                    String thumbnailUrl = imageRepository
                            .findFirstByCategoryAndTypeIdOrderByCreatedAtAsc(REVIEW, review.getReviewId())
                            .map(Image::getImageUrl)
                            .orElse(null);

                    return new GetMyPageReviewResponse(
                            restaurant.getName(),
                            restaurant.getAddress(),
                            thumbnailUrl
                    );
                })
                .collect(Collectors.toList());

        return new GetMyPageResponse(
                user.getName(),
                user.getEmail(),
                reviewDtos
        );
    }

    @Transactional
    public Role updateUserRole(Long userId, Role role){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        user.setRole(role);
        return role;
    }

    @Transactional
    public void updateRegistration(Long userId, MultipartFile registrationImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (registrationImage == null || registrationImage.isEmpty()) {
            return;
        }

        String imageUrl = s3ImageService
                .uploadImages(List.of(registrationImage), USER, null)
                .get(0);


        imageRepository.findFirstByCategoryAndTypeId(USER, userId)
                .ifPresentOrElse(existing -> {
                    // 기존 S3 파일 삭제 후 URL 교체
                    s3ImageService.deleteImage(existing.getImageUrl());
                    existing.setImageUrl(imageUrl);
                    existing.setCategory(USER);
                    existing.setTypeId(userId);
                }, () -> {
                    Image newImage = Image.builder()
                            .category(USER)
                            .typeId(userId)
                            .imageUrl(imageUrl)
                            .build();
                    imageRepository.save(newImage);
                });
    }
}
