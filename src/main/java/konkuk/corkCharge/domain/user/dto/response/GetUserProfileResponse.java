package konkuk.corkCharge.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record GetUserProfileResponse (
    String name,
    String social_id,
    String image_url
)
{
}
