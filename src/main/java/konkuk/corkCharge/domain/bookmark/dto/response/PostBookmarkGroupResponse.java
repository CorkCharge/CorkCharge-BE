package konkuk.corkCharge.domain.bookmark.dto.response;

public record PostBookmarkGroupResponse(
        Long groupId,
        String name,
        String icon,
        Integer displayOrder
) {
}