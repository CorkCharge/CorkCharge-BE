package konkuk.corkCharge.domain.restaurant.dto.mapper;

import konkuk.corkCharge.domain.corkageStore.domain.OptionType;
import konkuk.corkCharge.domain.restaurant.domain.RestaurantSummary;
import konkuk.corkCharge.domain.restaurant.dto.response.GetHomeRestaurantResponse;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static konkuk.corkCharge.domain.corkageStore.domain.OptionType.ETC;

@Component
public class HomeRestaurantResponseMapper {

    public GetHomeRestaurantResponse toResponse(
            RestaurantSummary summary,
            Double distanceKm,
            boolean scrap
    ) {
        List<String> corkageOptions = decodeOptions(summary.getOptionBits(), summary.getOptionEtcContent());

        return new GetHomeRestaurantResponse(
                summary.getRestaurantId(),
                summary.getName(),
                summary.getAddress(),
                summary.getAvgRating(),
                summary.getReviewCount() == null ? 0 : summary.getReviewCount(),
                summary.getCorkagePrice(),
                corkageOptions,
                distanceKm,
                summary.getMainImageUrl(),
                summary.getOpeningHours(),
                getOperationStatus(summary.getOpeningHours()),
                summary.getBookmarkCount(),
                scrap
        );
    }

    private List<String> decodeOptions(Integer optionBits, String etcContent) {
        if (optionBits == null || optionBits == 0)
            return List.of();

        int bits = optionBits;

        return Stream.of(OptionType.values())
                .filter(type -> (bits &  (1 << type.ordinal())) != 0)
                .map(type -> type == ETC ? etcContent : type.getLabel())
                .filter(opt -> opt != null && !opt.isBlank())
                .toList();
    }

    private String getOperationStatus(String openingHours) {
        if (openingHours == null || openingHours.isBlank()) {
            return "영업종료";
        }

        Map<String, List<MinuteRange>> schedule = parseSchedule(openingHours);
        if (schedule.isEmpty()) {
            return "영업종료";
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        String today = toKorDay(now.getDayOfWeek());
        String yesterday = toKorDay(now.getDayOfWeek().minus(1));
        int currentMinute = now.getHour() * 60 + now.getMinute();

        List<MinuteRange> todayRanges = schedule.get(today);
        if (todayRanges != null && isOpenByTodaySchedule(currentMinute, todayRanges)) {
            return "운영중";
        }

        List<MinuteRange> yesterdayRanges = schedule.get(yesterday);
        if (yesterdayRanges != null && isOpenByYesterdaySchedule(currentMinute, yesterdayRanges)) {
            return "운영중";
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
