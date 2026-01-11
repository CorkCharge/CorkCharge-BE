package konkuk.corkCharge.domain.bookmark.repository;

import konkuk.corkCharge.domain.bookmark.domain.BookmarkGroupVisibility;

public interface GroupRestaurantPinProjection {
    Long getGroupId();

    Integer getDisplayOrder();

    String getName();

    String getColor();

    BookmarkGroupVisibility getVisibility();

    Long getRestaurantId();

    Double getLatitude();

    Double getLongitude();
}
