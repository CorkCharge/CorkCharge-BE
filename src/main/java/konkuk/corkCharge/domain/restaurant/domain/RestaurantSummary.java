package konkuk.corkCharge.domain.restaurant.domain;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSummary implements Serializable {

    private Long restaurantId;
    private String name;

    // 기본 정보
    private String address;
    private String phone;
    private String representMenu;
    private String openingHours;

    // 이미지(대표 1장)
    private String mainImageUrl;
    private String menuImageUrl;
    private String pairingImageUrl;

    // 이미지(여러장)
    private List<String> mainImages;
    private List<String> menuImages;
    private List<String> pairingImages;

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
