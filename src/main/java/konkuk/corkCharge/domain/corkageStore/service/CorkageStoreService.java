package konkuk.corkCharge.domain.corkageStore.service;

import konkuk.corkCharge.domain.corkageStore.domain.*;
import konkuk.corkCharge.domain.corkageStore.dto.request.GetCorkageFilterRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.MultiCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.PostAddCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.PostAdminCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.response.GetCorkageVerificationResponse;
import konkuk.corkCharge.domain.corkageStore.dto.response.PostAdminCorkageResponse;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageOptionRepository;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.corkageStore.repository.MultiCorkageRepository;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.ownerRestaurant.domain.OwnerRestaurant;
import konkuk.corkCharge.domain.ownerRestaurant.repository.OwnerRestaurantRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.response.GetSearchRestaurantResponse;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.user.domain.Role;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.corkCharge.domain.image.domain.ImageType.MENU;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Service
@RequiredArgsConstructor
public class CorkageStoreService {

    private final RestaurantRepository restaurantRepository;
    private final CorkageStoreRepository corkageStoreRepository;
    private final MultiCorkageRepository multiCorkageRepository;
    private final CorkageOptionRepository corkageOptionRepository;
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
                corkageStore.addMultiPrice(entity);   // addMultiPrice 내부에서 recalcMinMax 호출
            }
        }

        if (request.optionTypes() != null) {

            List<OptionType> bitTypes = request.optionTypes().stream()
                    .map(OptionType::valueOf)
                    .toList();

            // 비트 OR 연산으로 optionBits 저장
            corkageStore.addOptionBits(bitTypes);

            // 기존 엔티티는 그대로 저장 (ETC 텍스트 포함)
            // Todo 이거 엔티티 삭제하거나 수정하면 이 부분 리팩토링해야 함
            for (String option : request.optionTypes()) {
                OptionType optionType = OptionType.valueOf(option);
                String etcContent = (optionType == OptionType.ETC) ? request.etcContent() : null;

                CorkageOption entity = CorkageOption.builder()
                        .optionType(optionType)
                        .etcContent(etcContent)
                        .corkageStore(corkageStore)
                        .build();

                corkageOptionRepository.save(entity);
            }
        }

        restaurant.setHasCorkage(true);
    }

    @Transactional
    public List<GetSearchRestaurantResponse> filterCorkageStores(GetCorkageFilterRequest request) {
        List<CorkageStore> corkageStores = corkageStoreRepository.findAllForRestaurant();

        return corkageStores.stream()
                .filter(store -> {      // 평점 필터링
                    Double rating = store.getRestaurant().getRating();
                    return rating != null && rating >= request.minScore() && rating <= request.maxScore();
                })
                .filter(store -> {      // 콜키지 타입 별 가격 필터링
                    CorkageType type = store.getCorkageType();

                    if (!request.corkageTypes().contains(type.name()))
                        return false;

                    Integer price = store.getCorkagePrice();

                    return switch (type) {
                        case PER_BOTTLE ->
                                price != null && price >= request.minBottlePrice() && price <= request.maxBottlePrice();
                        case PER_PERSON ->
                                price != null && price >= request.minPersonPrice() && price <= request.maxPersonPrice();
                        case PER_TABLE ->
                                price != null && price >= request.minTablePrice() && price <= request.maxTablePrice();
                        case MULTIPLE, FREE -> true;  // 가격 필터링 없이 다중 콜키지 여부로 필터링 적용
                    };
                })
                .filter(store -> { // 콜키지 옵션 필터링
                    if (request.optionTypes().isEmpty())
                        return true;

                    // 비트마스크로 변환
                    int filterMask = 0;
                    for (String opt : request.optionTypes()) {
                        OptionType type = OptionType.valueOf(opt);
                        filterMask |= (1 << type.ordinal());
                    }

                    // optionBits와 AND 연산으로 옵션 포함 여부 판단
                    return (store.getOptionBits() & filterMask) != 0;
                })
                .map(store -> GetSearchRestaurantResponse.from(store.getRestaurant()))
                .toList();
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