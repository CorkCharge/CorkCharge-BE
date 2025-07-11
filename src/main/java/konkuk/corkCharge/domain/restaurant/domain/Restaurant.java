package konkuk.corkCharge.domain.restaurant.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "phone", length = 100)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "biz_type")
    private BizType bizType;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "bookmark_count")
    private Integer bookmarkCount;

    @Column(name = "has_corkage")
    private Boolean hasCorkage;
}
