package konkuk.corkCharge.domain.corkageStore.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CorkageType corkageType;

    @Column(name = "corkage_price", length = 100)   // 병당, 인당, 테이블당 가격
    private Integer corkagePrice;

    @OneToMany(mappedBy = "corkageStore", cascade = CascadeType.REMOVE)  // 다중 콜키지
    private Set<MultiCorkage> multiPrices = new HashSet<>();

    @OneToMany(mappedBy = "corkageStore", cascade = CascadeType.REMOVE)
    private List<CorkageOption> corkageOptions = new ArrayList<>();

    @Column(name = "pairing", columnDefinition = "TEXT")
    private String pairing;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public void addMultiPrice(MultiCorkage price) {
        this.multiPrices.add(price);
    }

    public void addAdditionalOption(CorkageOption option) {
        this.corkageOptions.add(option);
    }

    @Builder
    public CorkageStore(Restaurant restaurant, CorkageType corkageType, Integer corkagePrice){
        this.restaurant = restaurant;
        this.corkageType = corkageType;
        this.corkagePrice = corkagePrice;
    }

}