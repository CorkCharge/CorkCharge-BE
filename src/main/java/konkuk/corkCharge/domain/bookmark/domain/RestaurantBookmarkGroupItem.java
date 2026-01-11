package konkuk.corkCharge.domain.bookmark.domain;

import jakarta.persistence.*;
import konkuk.corkCharge.global.entity.BaseEntity;
import lombok.*;

@Entity
@Table(
        name = "restaurant_bookmark_group_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_bookmark",
                        columnNames = {"restaurant_bookmark_group_id", "bookmark_id"}
                )
        }
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantBookmarkGroupItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "restaurant_bookmark_group_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "restaurant_bookmark_group_id",
            nullable = false
    )
    private RestaurantBookmarkGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bookmark_id",
            nullable = false
    )
    private Bookmark bookmark;
}