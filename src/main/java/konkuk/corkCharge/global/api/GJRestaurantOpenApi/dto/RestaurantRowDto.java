package konkuk.corkCharge.global.api.GJRestaurantOpenApi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantRowDto {
    @JsonProperty("BPLCNM")         // 사업장명
    private String BPLCNM;

    @JsonProperty("DTLSTATENM")     // 상세영업상태명
    private String DTLSTATENM;

    @JsonProperty("RDNWHLADDR")     // 도로명주소
    private String RDNWHLADDR;

    @JsonProperty("RDNPOSTNO")      // 도로명우편번호
    private String RDNPOSTNO;

    @JsonProperty("SITETEL")        // 전화번호
    private String SITETEL;

    @JsonProperty("LAT")            // 위도
    private String LAT;

    @JsonProperty("LNG")            // 경도
    private String LNG;
}
