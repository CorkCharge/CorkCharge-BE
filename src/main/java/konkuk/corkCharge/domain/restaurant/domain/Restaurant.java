package konkuk.corkCharge.domain.restaurant.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "restaurant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_id")
    private Long restaurantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "road_zip_code", length = 10)
    private String roadZipCode; // 도로명주소

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // 공간 인덱스를 위한 POINT 컬럼 추가
    @Column(
            name = "location",
            columnDefinition = "POINT SRID 4326 NOT NULL",
            insertable = false,
            updatable = false
    )
    private Point location;

    @Column(name = "phone", length = 100)
    private String phone;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "bookmark_count")
    private Integer bookmarkCount = 0;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "has_corkage")
    private boolean hasCorkage;

    @Column(name = "represent_menu", length = 255)
    private String representMenu;

    @Column(name = "opening_hours", columnDefinition = "TEXT")
    private String openingHours;

    @Column(name = "raw_category", length = 100)
    private String rawCategory;

    @Builder
    public Restaurant(String name, String address, String roadZipCode, String phone,
                      Double latitude, Double longitude,
                      double rating, int bookmarkCount, boolean hasCorkage) {
        this.name = name;
        this.address = address;
        this.roadZipCode = roadZipCode;
        this.phone = phone;
        this.latitude = latitude != null ? latitude : 0.0;
        this.longitude = longitude != null ? longitude : 0.0;
        this.rating = rating;
        this.bookmarkCount = bookmarkCount;
        this.hasCorkage = hasCorkage;
    }

    public int getReviewCount() {
        return reviewCount != null ? reviewCount : 0;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public void updateCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateRating(double rating) {
        this.rating = rating;
    }

    public void setHasCorkage(boolean hasCorkage) {
        this.hasCorkage = hasCorkage;
    }

    public void setBookmarkCount(int bookmarkCount){
        this.bookmarkCount = bookmarkCount;
    }

}
