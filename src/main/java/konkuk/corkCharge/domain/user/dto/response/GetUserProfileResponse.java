package konkuk.corkCharge.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetUserProfileResponse {
    private String name;
    private String social_id;
    private String image_url;
}
