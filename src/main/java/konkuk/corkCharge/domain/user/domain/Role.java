package konkuk.corkCharge.domain.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("유저"),
    OWNER("사장님"),
    ADMIN("관리자")
    ;

    private final String roleName;
}
