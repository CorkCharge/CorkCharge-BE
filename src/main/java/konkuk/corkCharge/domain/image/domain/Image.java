package konkuk.corkCharge.domain.image.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.review.domain.Review;
import konkuk.corkCharge.domain.tip.domain.Tip;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "image",
        indexes = {
                @Index(name = "idx_image_owner", columnList = "category,type_id")
        })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Image extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "restaurant_id")
//    private Restaurant restaurant;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "corkage_store_id")
//    private CorkageStore corkageStore;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "review_id")
//    private Review review;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "tip_id")
//    private Tip tip;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private ImageType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private ImageCategory category;

    @Column(name = "type_id", nullable = false)
    private Long typeId;
}
