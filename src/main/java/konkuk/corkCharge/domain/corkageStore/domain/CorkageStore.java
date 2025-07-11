package konkuk.corkCharge.domain.corkageStore.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "corkage_store")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorkageStore extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "corkage_store_id")
    private Long corkageStoreId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(name = "corkage_price", length = 100)
    private String corkagePrice;

    @Column(name = "additional_options", columnDefinition = "TEXT")
    private String additionalOptions;

    @Column(name = "pairing", columnDefinition = "TEXT")
    private String pairing;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
