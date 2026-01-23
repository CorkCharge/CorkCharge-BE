package konkuk.corkCharge.domain.restaurant.repository;

import konkuk.corkCharge.domain.corkageStore.domain.CorkageType;

public interface MapPinProjection {
    Long getRestaurantId();
    String getRestaurantName();
    Double getLatitude();
    Double getLongitude();

    CorkageType getCorkageType();
    Integer getCorkagePrice();
    Integer getMinMultiPrice();
}
