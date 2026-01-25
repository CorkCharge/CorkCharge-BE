package konkuk.corkCharge.domain.corkageStore.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "option_bits")
    private Integer optionBits = 0;

    @Column(name = "etc_content", columnDefinition = "TEXT")
    private String etcContent;

    public void updateEtcContent(String etcContent) {
        this.etcContent = etcContent;
    }

    public void addOptionBits(List<OptionType> optionTypes) {
        for (OptionType type : optionTypes) {
            this.optionBits |= (1 << type.ordinal());
        }
    }

    @Column(name = "pairing", columnDefinition = "TEXT")
    private String pairing;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Builder
    public CorkageStore(Restaurant restaurant, CorkageType corkageType, Integer corkagePrice){
        this.restaurant = restaurant;
        this.corkageType = corkageType;
        this.corkagePrice = corkagePrice;
    }

    public void updateCorkageType(CorkageType corkageType) {
        this.corkageType = corkageType;
    }

    public void updateCorkagePrice(Integer corkagePrice) {
        this.corkagePrice = corkagePrice;
    }

    public void resetOptionBits() {
        this.optionBits = 0;
    }

    public void clearEtcContent() {
        this.etcContent = null;
    }

}