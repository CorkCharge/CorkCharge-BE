package konkuk.corkCharge.domain.bookmark.repository;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkGroupVisibility;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;

public interface GroupRestaurantPinProjection {
    Long getGroupId();

    Integer getDisplayOrder();

    String getName();

    String getColor();

    BookmarkGroupVisibility getVisibility();

    Long getRestaurantId();

    Double getLatitude();

    Double getLongitude();

    // corkage price 관련
    CorkageType getCorkageType();

    Integer getCorkagePrice();

    Integer getMinMultiPrice();

}
