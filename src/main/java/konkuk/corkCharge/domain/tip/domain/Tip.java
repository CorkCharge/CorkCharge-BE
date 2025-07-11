package konkuk.corkCharge.domain.tip.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tip")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tip extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tip_id")
    private Long tipId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_category", nullable = false)
    private TipCategory tipCategory;
}
