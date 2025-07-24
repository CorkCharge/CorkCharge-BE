package konkuk.corkCharge.domain.corkageStore.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "multi_corkage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MultiCorkage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "multi_corkage_id")
    private Long multiCorkageId;

    @Column(name = "liquor_type", length = 100)
    private String liquorType;

    @Column(name = "price")
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corkage_store_id", nullable = false)
    private CorkageStore corkageStore;
}