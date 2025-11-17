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

    @Column(name = "min_corkage_price")
    private Integer minCorkagePrice;

    @Column(name = "max_corkage_price")
    private Integer maxCorkagePrice;

    @Column(name = "corkage_price", length = 100)   // 병당, 인당, 테이블당 가격
    private Integer corkagePrice;

    @OneToMany(mappedBy = "corkageStore", cascade = CascadeType.REMOVE)  // 다중 콜키지
    private Set<MultiCorkage> multiPrices = new HashSet<>();

    @OneToMany(mappedBy = "corkageStore", cascade = CascadeType.REMOVE)
    private List<CorkageOption> corkageOptions = new ArrayList<>();

    @Column(name = "option_bits")
    private Integer optionBits = 0; // Todo 따로 값 설정해주는 로직 있어야 함.

    public void addOptionBit(OptionType type) {
        this.optionBits |= (1 << type.ordinal());
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

    // MULTIPLE 외에는 min/max를 공통적으로 세팅
    private void initializeSinglePriceMinMax() {
        if (this.corkageType == CorkageType.PER_BOTTLE ||
                this.corkageType == CorkageType.PER_PERSON ||
                this.corkageType == CorkageType.PER_TABLE) {

            this.minCorkagePrice = this.corkagePrice;
            this.maxCorkagePrice = this.corkagePrice;

        } else if (this.corkageType == CorkageType.FREE) {
            this.minCorkagePrice = null;
            this.maxCorkagePrice = null;
        }
    }

    // MULTIPLE일 경우 multiPrices 기반으로 min/max 계산
    public void recalcMinMaxFromMulti() {
        if (this.multiPrices.isEmpty()) {
            this.minCorkagePrice = null;
            this.maxCorkagePrice = null;
            return;
        }

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (MultiCorkage mc : multiPrices) {
            min = Math.min(min, mc.getPrice());
            max = Math.max(max, mc.getPrice());
        }

        this.minCorkagePrice = min;
        this.maxCorkagePrice = max;
    }


    public void addMultiPrice(MultiCorkage price) {
        this.multiPrices.add(price);
        if (this.corkageType == CorkageType.MULTIPLE) {
            recalcMinMaxFromMulti();
        }
    }

    public void addAdditionalOption(CorkageOption option) {
        this.corkageOptions.add(option);
    }


    @Builder
    public CorkageStore(Restaurant restaurant, CorkageType corkageType, Integer corkagePrice){
        this.restaurant = restaurant;
        this.corkageType = corkageType;
        this.corkagePrice = corkagePrice;

        // min/max 초기화 로직
        if (corkageType == CorkageType.MULTIPLE) {
            // multiPrices는 store 생성 후 따로 계산 로직에서 계산됨
            this.minCorkagePrice = null;
            this.maxCorkagePrice = null;
        } else {
            initializeSinglePriceMinMax();
        }
    }
}