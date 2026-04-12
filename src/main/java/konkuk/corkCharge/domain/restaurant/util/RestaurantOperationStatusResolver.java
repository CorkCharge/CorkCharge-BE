package konkuk.corkCharge.domain.restaurant.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RestaurantOperationStatusResolver {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public String resolve(String openingHours) {
        if (openingHours == null || openingHours.isBlank()) {
            return "영업종료";
        }

        Map<String, List<MinuteRange>> schedule = parseSchedule(openingHours);
        if (schedule.isEmpty()) {
            return "영업종료";
        }

        LocalDateTime now = LocalDateTime.now(KST);
        String today = toKorDay(now.getDayOfWeek());
        String yesterday = toKorDay(now.getDayOfWeek().minus(1));
        int currentMinute = now.getHour() * 60 + now.getMinute();

        List<MinuteRange> todayRanges = schedule.get(today);
        if (todayRanges != null && isOpenByTodaySchedule(currentMinute, todayRanges)) {
            return "영업중";
        }

        List<MinuteRange> yesterdayRanges = schedule.get(yesterday);
        if (yesterdayRanges != null && isOpenByYesterdaySchedule(currentMinute, yesterdayRanges)) {
            return "영업중";
        }

        return "영업종료";
    }

    private Map<String, List<MinuteRange>> parseSchedule(String openingHours) {
        Map<String, List<MinuteRange>> result = new HashMap<>();
        String[] daySchedules = openingHours.split(",");

        for (String daySchedule : daySchedules) {
            String[] dayAndTimes = daySchedule.split(":", 2);
            if (dayAndTimes.length != 2) {
                continue;
            }

            String day = dayAndTimes[0].trim();
            String[] timeSegments = dayAndTimes[1].trim().split("/");

            List<MinuteRange> ranges = new ArrayList<>();
            for (String segment : timeSegments) {
                String[] times = segment.trim().split("-", 2);
                if (times.length != 2) {
                    continue;
                }

                Integer start = parseMinute(times[0].trim());
                Integer end = parseMinute(times[1].trim());
                if (start == null || end == null) {
                    continue;
                }
                ranges.add(new MinuteRange(start, end));
            }

            if (!ranges.isEmpty()) {
                result.put(day, ranges);
            }
        }

        return result;
    }

    private Integer parseMinute(String timeText) {
        String[] parts = timeText.split(":");
        if (parts.length != 2) {
            return null;
        }

        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            if (hour == 24 && minute == 0) {
                return 24 * 60;
            }
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return null;
            }

            return hour * 60 + minute;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isOpenByTodaySchedule(int nowMinute, List<MinuteRange> ranges) {
        for (MinuteRange range : ranges) {
            if (range.is24Hours()) {
                return true;
            }
            if (range.isOvernight()) {
                if (nowMinute >= range.startMinute()) {
                    return true;
                }
            } else if (nowMinute >= range.startMinute() && nowMinute < range.endMinute()) {
                return true;
            }
        }
        return false;
    }

    private boolean isOpenByYesterdaySchedule(int nowMinute, List<MinuteRange> ranges) {
        for (MinuteRange range : ranges) {
            if (range.isOvernight() && nowMinute < range.endMinute()) {
                return true;
            }
        }
        return false;
    }

    private String toKorDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    private record MinuteRange(int startMinute, int endMinute) {
        private boolean isOvernight() {
            return endMinute < startMinute;
        }

        private boolean is24Hours() {
            return startMinute == endMinute;
        }
    }
}
