package konkuk.corkCharge.global.api.GJRestaurantOpenApi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantOpenApiResponse {

    @JsonProperty("LOCALDATA_072404_GJ")
    private InnerResponse localData;
}
