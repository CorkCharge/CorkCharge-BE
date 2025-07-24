package konkuk.corkCharge.domain.corkageStore.service;

import konkuk.corkCharge.domain.corkageStore.domain.*;
import konkuk.corkCharge.domain.corkageStore.dto.request.MultiCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.dto.request.PostAddCorkageRequest;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageOptionRepository;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.corkageStore.repository.MultiCorkageRepository;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.ALREADY_REGISTERED_CORKAGE;
import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.RESTAURANT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CorkageStoreService {

    private final RestaurantRepository restaurantRepository;
    private final CorkageStoreRepository corkageStoreRepository;
    private final MultiCorkageRepository multiCorkageRepository;
    private final CorkageOptionRepository corkageOptionRepository;

    @Transactional
    public void createCorkage(PostAddCorkageRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.restaurantId())
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        if (restaurant.isHasCorkage()) {
            throw new CustomException(ALREADY_REGISTERED_CORKAGE);
        }

        CorkageType corkageType = CorkageType.valueOf(request.CorkageType());

        CorkageStore corkageStore = CorkageStore.builder()
                .restaurant(restaurant)
                .corkageType(corkageType)
                .corkagePrice(request.corkagePrice())
                .build();

        corkageStoreRepository.save(corkageStore);

        // 다중 콜키지인 경우
        if (corkageType == CorkageType.MULTIPLE && request.multiCorkages() != null) {
            for (MultiCorkageRequest multiCorkage : request.multiCorkages()) {
                MultiCorkage entity = MultiCorkage.builder()
                        .liquorType(multiCorkage.liquorType())
                        .price(multiCorkage.price())
                        .corkageStore(corkageStore)
                        .build();
                multiCorkageRepository.save(entity);
            }
        }

        // 옵션
        if (request.optionTypes() != null) {
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

}