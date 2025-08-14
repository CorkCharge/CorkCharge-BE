package konkuk.corkCharge.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record GetUserProfileResponse (
    String name,
    String email,
    String image_url
)
{
}
