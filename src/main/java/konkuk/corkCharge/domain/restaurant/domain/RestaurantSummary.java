package konkuk.corkCharge.domain.restaurant.domain;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
public class RestaurantSummary implements Serializable {

    private Long restaurantId;
    private String name;

    // 기본 정보
    private String address;
    private String phone;
    private String representMenu;
    private String openingHours;

    // 이미지
    private String mainImageUrl;
    private String menuImageUrl;
    private String pairingImageUrl; // corkage pairing 대표 이미지

    // 리뷰
    private Integer reviewCount;
    private Double avgRating;

    // 북마크
    private Integer bookmarkCount;

    // 콜키지 정보
    private boolean hasCorkage;
    private CorkageType corkageType;
    private String corkagePrice;
    private Integer optionBits;
    private String optionEtcContent;   // ETC 옵션 텍스트

    // 페어링 정보
    private String pairingAlcohol;
    private String pairingDescription;

    // 위치
    private Double latitude;
    private Double longitude;
}
