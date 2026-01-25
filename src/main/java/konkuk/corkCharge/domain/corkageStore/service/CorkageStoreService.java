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
import konkuk.corkCharge.domain.ownerRestaurant.repository.OwnerRestaurantRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.domain.restaurant.service.RestaurantSummaryService;
import konkuk.corkCharge.domain.user.domain.Role;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.domain.user.repository.UserRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static konkuk.corkCharge.domain.corkageStore.domain.OptionType.ETC;
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
    private final RestaurantSummaryService restaurantSummaryService;

    @Transactional
    public void createOrUpdateCorkage(Long userId, PostAddCorkageRequest req) {
        if (req == null || req.restaurantId() == null) {
            throw new CustomException(BAD_REQUEST);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        boolean isMyRestaurant = ownerRestaurantRepository.existsByUser_UserIdAndRestaurant_RestaurantId(userId, req.restaurantId());
        if (!isMyRestaurant) {
            throw new CustomException(PERMISSION_DENIED);
        }

        if (user.getRole() != Role.OWNER) {
            throw new CustomException(PERMISSION_DENIED);
        }

        Restaurant restaurant = restaurantRepository.findById(req.restaurantId())
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));


        // 콜키지 타입
        CorkageType newType = CorkageType.valueOf(req.corkageType());

        // 콜키지 옵션
        List<OptionType> optionTypes = (req.optionTypes() == null) ? List.of() : req.optionTypes().stream()
                .map(opt -> OptionType.valueOf(opt))
                .toList();

        CorkageStore corkageStore =
                corkageStoreRepository.findByRestaurant_RestaurantId(req.restaurantId())
                        .orElse(null);

        boolean isNew = false;

        if (corkageStore == null) {
            // 콜키지 매장이 아닌 경우
            corkageStore = CorkageStore.builder()
                    .restaurant(restaurant)
                    .corkageType(newType)
                    .corkagePrice(normalizePrice(newType, req.corkagePrice()))
                    .build();

            corkageStoreRepository.save(corkageStore);
            isNew = true;
        }

        if (!isNew) {
            applyTypeAndPrice(corkageStore, newType, req);
        }

        applyMultiCorkages(corkageStore, newType, req.multiCorkages());

        applyOptions(corkageStore, optionTypes, req.etcContent());
        corkageStoreRepository.saveAndFlush(corkageStore);

        if (!restaurant.isHasCorkage()) {
            restaurant.setHasCorkage(true);
        }

        restaurantSummaryService.evictSummary(req.restaurantId());
    }

    private Integer normalizePrice(CorkageType type, Integer corkagePrice) {
        if (type == CorkageType.FREE || type == CorkageType.MULTIPLE)
            return 0;

        return (corkagePrice == null) ? 0 : corkagePrice;
    }

    private void applyTypeAndPrice(CorkageStore corkageStore, CorkageType newType, PostAddCorkageRequest request) {
        corkageStore.updateCorkageType(newType);

        Integer price = normalizePrice(newType, request.corkagePrice());
        corkageStore.updateCorkagePrice(price);
    }

    private void applyMultiCorkages(CorkageStore corkageStore, CorkageType newType, List<MultiCorkageRequest> multiReqs) {

        Long corkageStoreId = corkageStore.getCorkageStoreId();
        if (corkageStoreId == null) {
            throw new CustomException(BAD_REQUEST);
        }

        if (newType != CorkageType.MULTIPLE) {
            multiCorkageRepository.deleteAllByCorkageStoreId(corkageStoreId);
            return;
        }

        multiCorkageRepository.deleteAllByCorkageStoreId(corkageStoreId);

        for (MultiCorkageRequest r : multiReqs) {
            String liquorType = r.liquorType();

            int price = r.price();

            MultiCorkage entity = MultiCorkage.builder()
                    .liquorType(liquorType)
                    .price(price)
                    .corkageStore(corkageStore)
                    .build();

            multiCorkageRepository.save(entity);
        }
    }

    private void applyOptions(CorkageStore corkageStore, List<OptionType> optionTypes, String etcContent) {
        corkageStore.resetOptionBits();

        if (optionTypes == null || optionTypes.isEmpty()) {
            corkageStore.clearEtcContent();
            return;
        }

        corkageStore.addOptionBits(optionTypes);

        if (optionTypes.contains(ETC)) {
            corkageStore.updateEtcContent(etcContent);
        } else {
            corkageStore.clearEtcContent();
        }
    }

    @Transactional(readOnly = true)
    public List<GetCorkageVerificationResponse> requestCorkage(Long userId){
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        List<Long> restaurantIds = ownerRestaurantRepository.findRestaurantIdsByUserId(userId);

        if (restaurantIds.isEmpty()) {
            return List.of();
        }

        List<RestaurantSummary> summaries = restaurantSummaryService.getSummariesInOrder(restaurantIds);

        return summaries.stream()
                .map(summary -> new GetCorkageVerificationResponse(
                        summary.getRestaurantId(),
                        summary.getName(),
                        summary.getAddress(),
                        summary.getMainImageUrl()
                ))
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