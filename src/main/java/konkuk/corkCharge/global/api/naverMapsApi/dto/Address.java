package konkuk.corkCharge.global.api.naverMapsApi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Address(
        @JsonProperty("x") String longitude,
        @JsonProperty("y") String latitude
) {}
