package konkuk.corkCharge.domain.notification.entity;

import java.util.Arrays;

public enum NotificationType {
    EVENT,      // 이벤트
    NOTICE,     // 공지
    REQUEST,    // 해주세요
    INQUIRY;    // 문의

    public static boolean isValid(NotificationType type) {
        return type != null && Arrays.asList(values()).contains(type);
    }
}