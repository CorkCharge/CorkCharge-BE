package konkuk.corkCharge.global.openApi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestaurantRowDto {
    private String BPLCNM;       // 사업장명
    private String RDNWHLADDR;   // 도로명주소
    private String RDNPOSTNO;    // 도로명우편번호
    private String SITETEL;      // 전화번호
    private String LAT;          // 위도
    private String LNG;          // 경도
    private String DTLSTATENM;   // 상세영업상태명
}
