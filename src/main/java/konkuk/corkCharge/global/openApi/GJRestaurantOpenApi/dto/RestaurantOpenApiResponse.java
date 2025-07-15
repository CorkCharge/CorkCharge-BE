package konkuk.corkCharge.global.openApi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantOpenApiResponse {
    private InnerResponse LOCALDATA_020301_GJ;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InnerResponse {
        private List<RestaurantRowDto> row;
    }
}
