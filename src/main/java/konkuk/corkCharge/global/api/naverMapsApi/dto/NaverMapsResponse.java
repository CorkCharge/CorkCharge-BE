package konkuk.corkCharge.global.api.naverMapsApi.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NaverMapsResponse(
        @JsonProperty("addresses") List<Address> addresses
) { }
