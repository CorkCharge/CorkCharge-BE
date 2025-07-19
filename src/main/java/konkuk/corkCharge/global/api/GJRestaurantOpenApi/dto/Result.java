package konkuk.corkCharge.global.api.GJRestaurantOpenApi.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Result {
    private String CODE;
    private String MESSAGE;
}
