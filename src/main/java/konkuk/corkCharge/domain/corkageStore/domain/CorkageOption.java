package konkuk.corkCharge.domain.corkageStore.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "corkage_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CorkageOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "corkage_option_id")
    private Long corkageOptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OptionType optionType;

    @Column(name = "etc_content", columnDefinition = "TEXT")
    private String etcContent;  // ETC인 경우에만 해당

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corkage_store_id", nullable = false)
    private CorkageStore corkageStore;
}