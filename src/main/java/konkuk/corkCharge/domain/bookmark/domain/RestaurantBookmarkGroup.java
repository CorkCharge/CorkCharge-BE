package konkuk.corkCharge.domain.bookmark.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.domain.user.domain.User;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.*;

@Entity
@Table(
        name = "restaurant_bookmark_group",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_group_name",
                        columnNames = {"user_id", "name"}
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantBookmarkGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_bookmark_group_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    // 그룹 정렬 순서 결정할 때 사용
    @Column(name = "display_order")
    private Integer displayOrder;

    // 그룹 아이콘
    @Column(name = "icon")
    private String icon;
}
