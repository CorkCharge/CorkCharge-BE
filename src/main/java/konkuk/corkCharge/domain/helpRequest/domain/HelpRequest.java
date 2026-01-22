package konkuk.corkCharge.domain.helpRequest.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;
import konkuk.corkCharge.domain.corkageStore.domain.OptionType;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "help_request")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HelpRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "help_id")
    private Long helpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "corkage_type", length = 30)
    private CorkageType corkageType;

    @Column(name = "preferred_price", nullable = true)
    private Integer preferredPrice;

    // 기타 서비스 우선순위 (1순위, 2순위)
    @Enumerated(EnumType.STRING)
    @Column(name = "first_priority", length = 30)
    private OptionType firstPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "second_priority", length = 30)
    private OptionType secondPriority;

    // 추가 요청사항
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}