package konkuk.corkCharge.domain.corkageStore.service;

import konkuk.corkCharge.domain.corkageStore.domain.*;
import konkuk.corkCharge.domain.corkageStore.dto.request.MultiCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.PostAddCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.PostAdminCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.response.GetCorkageVerificationResponse;
import konkuk.corkCharge.domain.corkageStore.dto.response.PostAdminCorkageResponse;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.corkageStore.repository.MultiCorkageRepository;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.ownerRestaurant.domain.OwnerRestaurant;
import konkuk.corkCharge.domain.ownerRestaurant.repository.OwnerRestaurantRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.user.domain.Role;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class CorkageStoreService {

    private final RestaurantRepository restaurantRepository;
    private final CorkageStoreRepository corkageStoreRepository;
    private final MultiCorkageRepository multiCorkageRepository;
    private final UserRepository userRepository;
    private final OwnerRestaurantRepository ownerRestaurantRepository;
    private final ImageRepository imageRepository;

    @Transactional
    public void createCorkage(Long userId, PostAddCorkageRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (user.getRole() == Role.USER) {
            throw new CustomException(PERMISSION_DENIED);
        }

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        if (restaurant.isHasCorkage()) {
            throw new CustomException(ALREADY_REGISTERED_CORKAGE);
        }

        CorkageType corkageType = CorkageType.valueOf(request.corkageType());

        CorkageStore corkageStore = CorkageStore.builder()
                .restaurant(restaurant)
                .corkageType(corkageType)
                .corkagePrice(request.corkagePrice())
                .build();

        corkageStoreRepository.save(corkageStore);

        if (corkageType == CorkageType.MULTIPLE && request.multiCorkages() != null) {
            for (MultiCorkageRequest multiCorkage : request.multiCorkages()) {
                MultiCorkage entity = MultiCorkage.builder()
                        .liquorType(multiCorkage.liquorType())
                        .price(multiCorkage.price())
                        .corkageStore(corkageStore)
                        .build();

                multiCorkageRepository.save(entity);

                corkageStore.getMultiPrices().add(entity);
            }
        }

        if (request.optionTypes() != null) {
            List<OptionType> bitTypes = request.optionTypes().stream()
                    .map(OptionType::valueOf)
                    .toList();

            // optionBits 저장
            corkageStore.addOptionBits(bitTypes);

            // ETC 텍스트 저장 (옵션에 ETC 포함될 때만)
            if (bitTypes.contains(OptionType.ETC)) {
                corkageStore.updateEtcContent(request.etcContent());
            }
        }

        restaurant.setHasCorkage(true);
    }

    @Transactional(readOnly = true)
    public List<GetCorkageVerificationResponse> requestCorkage(Long userId){
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<OwnerRestaurant> mappings = ownerRestaurantRepository.findAllByUser_UserIdAndRestaurant_HasCorkageFalse(userId);

        if(mappings.isEmpty()){
            throw new CustomException(PERMISSION_DENIED);
        }

        return mappings.stream()
                .map(OwnerRestaurant::getRestaurant)
                .map(restaurant -> {
                    String thumbnailUrl = imageRepository
                            .findFirstByCategoryAndTypeIdAndType(
                                    ImageCategory.RESTAURANT,
                                    restaurant.getRestaurantId(),
                                    ImageType.MAIN
                            )
                            .map(Image::getImageUrl)
                            .orElse(null);

                    return new GetCorkageVerificationResponse(
                            restaurant.getRestaurantId(),
                            restaurant.getName(),
                            restaurant.getAddress(),
                            thumbnailUrl
                    );
                })
                .toList();
    }

    @Transactional
    public PostAdminCorkageResponse adminRequestCorkage(PostAdminCorkageRequest request, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if(user.getRole() != Role.ADMIN){
            throw new CustomException(PERMISSION_DENIED);
        }

        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow((() -> new CustomException(RESTAURANT_NOT_FOUND)));

        String thumbnailUrl = imageRepository
                .findFirstByCategoryAndTypeIdAndType(
                        ImageCategory.RESTAURANT,
                        restaurant.getRestaurantId(),
                        ImageType.MENU
                )
                .map(Image::getImageUrl)
                .orElse(null);

        return new PostAdminCorkageResponse(
                request.restaurantId(),
                request.name(),
                request.address(),
                thumbnailUrl
        );
    }
}