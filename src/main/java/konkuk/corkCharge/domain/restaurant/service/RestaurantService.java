package konkuk.corkCharge.domain.restaurant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import konkuk.corkCharge.domain.corkageStore.domain.CorkageStore;
import konkuk.corkCharge.domain.corkageStore.domain.MultiCorkage;
import konkuk.corkCharge.domain.corkageStore.repository.CorkageStoreRepository;
import konkuk.corkCharge.domain.image.domain.Image;
import konkuk.corkCharge.domain.image.domain.ImageCategory;
import konkuk.corkCharge.domain.image.domain.ImageType;
import konkuk.corkCharge.domain.image.repository.ImageRepository;
import konkuk.corkCharge.domain.image.service.AmazonS3Service;
import konkuk.corkCharge.domain.restaurant.domain.Restaurant;
import konkuk.corkCharge.domain.restaurant.dto.request.GetFilterRequest;
import konkuk.corkCharge.domain.restaurant.dto.response.*;
import konkuk.corkCharge.domain.restaurant.repository.RestaurantRepository;
import konkuk.corkCharge.global.api.naverMapsApi.NaverGeocodingClient;
import konkuk.corkCharge.global.api.naverMapsApi.dto.Address;
import konkuk.corkCharge.global.api.naverMapsApi.dto.NaverMapsResponse;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;
import static org.springframework.util.StringUtils.truncate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final CorkageStoreRepository corkageStoreRepository;
    private final ImageRepository imageRepository;
    private final NaverGeocodingClient naverGeocodingClient;

    private final AmazonS3Service s3Service;

    private final ObjectMapper om = new ObjectMapper();
    private final RestTemplate rt = new RestTemplate();

    @Value("${naver.client-id}") private String naverClientId;
    @Value("${naver.client-secret}") private String naverClientSecret;


    @Transactional(readOnly = true)
    public List<GetRestaurantListResponse> getCorkageRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findByHasCorkageTrue();

        if (restaurants.isEmpty()) {
            throw new CustomException(CORKAGE_RESTAURANT_NOT_FOUND);
        }

        return restaurants.stream()
                .map(GetRestaurantListResponse::from)
                .toList();
    }

    public GetRestaurantDetailResponse getRestaurantDetail(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(RESTAURANT_NOT_FOUND));

        return GetRestaurantDetailResponse.from(restaurant);
    }

    @Transactional
    public List<GetSearchRestaurantResponse> searchRestaurants(String keyword) {
        List<Restaurant> matchedRestaurants = restaurantRepository.findByNameContaining(keyword);

        return matchedRestaurants.stream()
                .map(GetSearchRestaurantResponse::from)
                .toList();
    }

    @Transactional
    public List<GetHotRestaurantResponse> getHotRestaurants() {
        List<Restaurant> hotRestaurants = restaurantRepository.findByBookmarkCountGreaterThanEqual(5);

        return hotRestaurants.stream()
                .map(GetHotRestaurantResponse::from)
                .toList();
    }

    @Transactional
    public List<?> filterRestaurants(GetFilterRequest request) {
        List<Restaurant> matchedRestaurants = filterByAddress(request.sido(), request.sigungu(), request.dongList());

        return switch (request.type()) {
            case "hot" -> matchedRestaurants.stream()
                    .filter(r -> r.getBookmarkCount() >= 5)
                    .map(GetHotRestaurantResponse::from)
                    .toList();

            case "map" -> matchedRestaurants.stream()
                    .filter(Restaurant::isHasCorkage)
                    .map(GetSearchRestaurantResponse::from)
                    .toList();

            default -> throw new CustomException(NOT_EXIT_TYPE);
        };

    }

    private List<Restaurant> filterByAddress(String sido, String sigungu, List<String> dongList) {
        if (sido == null || sido.isBlank()) {
            throw new CustomException(SIDO_REQUIRED);
        }

        List<Restaurant> matchedRestaurants = restaurantRepository.findByAddressContaining(sido);

        if (sigungu != null && !sigungu.isBlank()) {
            matchedRestaurants = matchedRestaurants.stream()
                    .filter(r -> r.getAddress().contains(sigungu))
                    .toList();
        }

        if (dongList != null && !dongList.isEmpty()) {
            matchedRestaurants = matchedRestaurants.stream()
                    .filter(r -> dongList.stream().anyMatch(d -> r.getAddress().contains(d)))
                    .toList();
        }

        return matchedRestaurants;
    }

    @Transactional
    public List<?> GetMapCluster(String level, double latMin, double latMax, double lonMin, double lonMax) {
        List<Restaurant> restaurants = restaurantRepository.findByHasCorkageTrue();

        if (restaurants.isEmpty()) {
            throw new CustomException(CORKAGE_RESTAURANT_NOT_FOUND);
        }

        // 위도/경도가 없는 매장의 경우 추가
        restaurants.forEach(restaurant -> {
            if (restaurant.getLatitude() == 0 || restaurant.getLongitude() == 0) {
                NaverMapsResponse response = naverGeocodingClient.getCoordinatesByAddress(restaurant.getAddress());
                if (!response.addresses().isEmpty()) {
                    Address address = response.addresses().get(0);
                    restaurant.updateCoordinates(
                            Double.parseDouble(address.latitude()),
                            Double.parseDouble(address.longitude())
                    );
                }
            }
        });

        List<Restaurant> filtered = restaurants.stream()
                .filter(r -> r.getLatitude() >= latMin && r.getLatitude() <= latMax)
                .filter(r -> r.getLongitude() >= lonMin && r.getLongitude() <= lonMax)
                .toList();

        return switch (level) {
            case "restaurant" -> filtered.stream()
                    .map(GetMapRestaurantResponse::from)
                    .toList();

            case "dong", "sigungu", "sido" -> filtered.stream()
                    .map(GetMapClusterResponse::from)
                    .toList();

            default -> throw new CustomException(BAD_REQUEST);
        };
    }

    @Transactional(readOnly = true)
    public List<GetClusterListResponse> getClusterList(List<Long> restaurantIds) {
        List<Restaurant> restaurants = restaurantRepository.findAllById(restaurantIds);

        return restaurants.stream()
                .sorted((r1, r2) -> {
                    int price1 = getComparableCorkagePrice(r1);
                    int price2 = getComparableCorkagePrice(r2);
                    return Integer.compare(price1, price2);
                })
                .map(GetClusterListResponse::from)
                .toList();
    }

    private int getComparableCorkagePrice(Restaurant r) {
        CorkageStore cs = r.getCorkageStore();

        return switch (cs.getCorkageType()) {
            case FREE -> 0;
            case MULTIPLE -> cs.getMultiPrices().stream()
                    .mapToInt(MultiCorkage::getPrice)
                    .min().orElse(Integer.MAX_VALUE);
            default -> cs.getCorkagePrice() != null ? cs.getCorkagePrice() : Integer.MAX_VALUE;
        };
    }

    // 변경됨: 네이버 웹검색(모바일)만 사용해 매칭 수행
    // 변경됨: 통합검색 대신 네이버지도 SPA API로 placeId를 찾고,
//        m.place 상세 페이지를 직접 크롤링해 우편번호 매칭
    @Transactional
    public long importFromNaver() {
        // 1) corkage_store에 데이터가 하나라도 있으면 스킵
        long csCount = corkageStoreRepository.count();
        if (csCount > 0L) {
            log.info("[naver-import] skip: corkage_store already has {} rows", csCount);
            return 0L;
        }

        log.info("[naver-import] start (corkage_store empty) — crawling...");
        List<Restaurant> targets = restaurantRepository.findAll();
        long updated = 0L;

        for (Restaurant r : targets) {
            try {
                String name   = r.getName();
                String postal = normalizePostal(r.getRoadZipCode());

                if (isBlank(name) || isBlank(postal)) {
                    log.warn("[naver-import] skip (missing name/postal) id={} name='{}' postal={}",
                            r.getRestaurantId(), name, postal);
                    continue;
                }

                // 2) 네이버 지도 SPA 내부 API에서 placeId 수집 → m.place 상세 URL들
                List<String> placePages = findPlacePagesByMapApi(name, postal);
                boolean matched = false;

                if (placePages.isEmpty()) {
                    log.info("[naver-import]  -> no candidates from map-api. id={} name='{}'", r.getRestaurantId(), name);
                }

                // 3) 후보 상세 페이지를 순회하며 우편번호 비교
                for (String placeUrl : placePages) {
                    NaverPlaceDetail d = scrapePlaceDetail(placeUrl);
                    String candPostal  = normalizePostal(d.postalCode);

                    log.info("[naver-import] compare id={} name='{}' crawled_zip={} expected_zip={} url={}",
                            r.getRestaurantId(), name, candPostal, postal, placeUrl);

                    if (postal.equals(candPostal)) {
                        log.info("[naver-import]  MATCH by postal. id={} name='{}'", r.getRestaurantId(), name);
                        boolean changed = patchRestaurantFromNaver(r, /*candidate not used*/ null, d);
                        if (changed) updated++;
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    log.info("[naver-import]  -> no candidate matched zip. id={} name='{}'",
                            r.getRestaurantId(), name);
                    log.warn("[naver-import] no match by postal for id={} name='{}'",
                            r.getRestaurantId(), name);
                }

                // 과도한 호출 방지
                try { Thread.sleep(150); } catch (InterruptedException ignored) {}

            } catch (Exception e) {
                log.error("[naver-import] error id={} name='{}' : {}", r.getRestaurantId(), r.getName(), e.toString());
            }
        }

        log.info("[naver-import] done. updated={}", updated);
        return updated;
    }

    private NaverPlaceDetail scrapePlaceDetailFromEntry(String placeId) {
        String entryUrl = "https://map.naver.com/p/entry/place/" + placeId;
        log.info("[web-crawl] entry url={}", entryUrl);

        try (Playwright pw = Playwright.create();
             Browser browser = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
             BrowserContext ctx = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900))) {

            Page page = ctx.newPage();
            page.navigate(entryUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

            // entryIframe 찾기
            page.locator("iframe#entryIframe").waitFor(new Locator.WaitForOptions().setTimeout(15000));
            Frame entry = page.frames().stream()
                    .filter(f -> f.url() != null && f.url().contains("pcmap.place.naver.com/place/"))
                    .findFirst().orElseThrow(() -> new IllegalStateException("entryIframe not found"));

            // (1) 우편번호
            String postal = null;
            Locator zipLabel = entry.locator("span.place_blind:has-text(\"우편번호\")").first();
            if (zipLabel.count() > 0) {
                String around = zipLabel.locator("xpath=..").innerText();
                postal = findFirstFiveDigit(around);
            }
            if (postal == null) {
                postal = findFirstFiveDigit(entry.locator("body").innerText());
            }

            // (2) 대표사진
            String photoSrc = null;
            Locator heroImg = entry.locator("img[id^='business_'][alt='업체']").first();
            if (heroImg.count() > 0) {
                photoSrc = tryExtractOriginalFromProxy(heroImg.getAttribute("src"));
            } else {
                Locator anyImg = entry.locator("img[alt*='사진'], img[alt='업체']").first();
                if (anyImg.count() > 0) photoSrc = tryExtractOriginalFromProxy(anyImg.getAttribute("src"));
            }

            // (3) 영업시간 – 형식 불문, 관련 텍스트만 모아 정리
            String openingHoursText = extractOpeningHoursFlexible(entry);

            NaverPlaceDetail d = new NaverPlaceDetail();
            d.placeId = placeId;
            d.postalCode = postal;
            d.heroImageUrl = photoSrc;
            d.openingHoursText = openingHoursText;
            return d;
        }
    }

    // 2) 어떤 UI든 영업시간 관련 텍스트만 뽑아 한 줄로 정리
    private String extractOpeningHoursFlexible(Frame entry) {
        // 접혀있으면 펼치기 시도
        Locator btn = entry.locator("a[role='button']:has-text(\"영업\")").first();
        if (btn.count() > 0) {
            String expanded = Optional.ofNullable(btn.getAttribute("aria-expanded")).orElse("true");
            if ("false".equalsIgnoreCase(expanded)) {
                try { btn.click(); entry.waitForTimeout(200); } catch (Exception ignore) {}
            }
            String blk = btn.innerText();
            String cleaned = postProcessBusinessHours(blk);
            if (cleaned != null && !cleaned.isBlank()) return cleaned;
        }

        // 다른 컨테이너 후보들
        String[] sels = {
                "div:has-text(\"영업시간\")", "section:has-text(\"영업시간\")",
                "div:has-text(\"연중무휴\")",  "div:has-text(\"24시간\")"
        };
        for (String s : sels) {
            Locator l = entry.locator(s).first();
            if (l.count() > 0) {
                String cleaned = postProcessBusinessHours(l.innerText());
                if (cleaned != null && !cleaned.isBlank()) return cleaned;
            }
        }

        // 최후 수단: 본문 전체에서 영업시간 관련 줄만 필터링
        return postProcessBusinessHours(entry.locator("body").innerText());
    }

// ====== 헬퍼 ======

    private String findFirstFiveDigit(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("\\b(\\d{5})\\b").matcher(s);
        return m.find() ? m.group(1) : null;
    }

    private String tryExtractOriginalFromProxy(String url) {
        if (url == null) return null;
        try {
            int q = url.indexOf('?');
            if (q < 0) return url;
            for (String p : url.substring(q + 1).split("&")) {
                int eq = p.indexOf('=');
                if (eq < 0) continue;
                String k = URLDecoder.decode(p.substring(0, eq), StandardCharsets.UTF_8);
                if ("src".equalsIgnoreCase(k)) {
                    String v = URLDecoder.decode(p.substring(eq + 1), StandardCharsets.UTF_8);
                    return URLDecoder.decode(v, StandardCharsets.UTF_8);
                }
            }
        } catch (Exception ignore) {}
        return url;
    }

    /** 줄 단위로 걸러서 '영업/연중무휴/24시간/휴무/브레이크타임/라스트오더 + 시간대'만 남기고 " / "로 연결 */
    private String postProcessBusinessHours(String raw) {
        if (raw == null) return null;
        String norm = raw.replace('\u00A0', ' ')
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\r?\\n", "\n");

        Pattern timeRange = Pattern.compile("\\b(\\d{1,2}:\\d{2})\\s*[-~]\\s*(\\d{1,2}:\\d{2})\\b");
        Pattern dayWord   = Pattern.compile("(월|화|수|목|금|토|일|매일|평일|주말|공휴일)");
        String[] kw = { "영업", "연중무휴", "24시간", "브레이크타임", "라스트오더", "휴무", "재료소진", "마감" };

        LinkedHashSet<String> keep = new LinkedHashSet<>();
        for (String line : norm.split("\n")) {
            String s = line.trim();
            if (s.isEmpty()) continue;
            boolean hasTime = timeRange.matcher(s).find();
            boolean hasDay  = dayWord.matcher(s).find();
            boolean hasKw   = Arrays.stream(kw).anyMatch(s::contains);
            boolean looksClock = s.matches(".*\\b\\d{1,2}:\\d{2}\\b.*");

            if (hasTime || (hasDay && looksClock) || hasKw) {
                s = s.replaceAll("\\s*[-~]\\s*", "-")
                        .replaceAll("^[·•▶▷⮕>\\-]+\\s*", "")
                        .replaceAll(" +", " ");
                keep.add(s);
            }
        }
        if (keep.isEmpty()) return null;

        List<String> uniq = new ArrayList<>();
        for (String s : keep) {
            if (uniq.stream().noneMatch(x -> x.equalsIgnoreCase(s))) uniq.add(s);
        }
        return String.join(" / ", uniq);
    }

    // ===== 내부 DTO =====
    private static class NaverPlaceDetail {
        String placeId;
        String postalCode;
        String heroImageUrl;
        String openingHoursText;
    }

    // 공백 체크
    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // 우편번호 정규화(숫자 5자리만 추출)
    private String normalizePostal(String s) {
        if (s == null) return null;
        Matcher m = Pattern.compile("(\\d{5})").matcher(s);
        return m.find() ? m.group(1) : s.trim();
    }

    // importFromNaver() 가 기대하는 시그니처용 프록시
    private NaverPlaceDetail scrapePlaceDetail(String placeUrl) {
        // /entry/place/{id} 에서 placeId만 뽑아 실제 iframe 크롤러로 위임
        if (placeUrl == null) return null;
        String id = null;
        int idx = placeUrl.indexOf("/entry/place/");
        if (idx >= 0) {
            String rest = placeUrl.substring(idx + "/entry/place/".length());
            int q = rest.indexOf('?');
            id = q >= 0 ? rest.substring(0, q) : rest;
        }
        if (id == null || id.isEmpty()) {
            log.warn("[web-crawl] cannot parse placeId from url={}", placeUrl);
            return null;
        }
        return scrapePlaceDetailFromEntry(id);
    }

    /**
     * 네이버 지도(PC)에서 '가게명'으로만 검색하고, searchIframe 내 목록의 entry 링크들만 추출
     * - 절대 '네이버 통합검색'을 쓰지 않음
     * - 상위 5개까지만 수집
     */
    private List<String> findPlacePagesByMapApi(String name, String postal) {
        List<String> out = new ArrayList<>();
        if (isBlank(name)) return out;

        log.info("[web-search] open map and query name='{}'", name);

        try (Playwright pw = Playwright.create();
             Browser browser = pw.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
             BrowserContext ctx = browser.newContext(new Browser.NewContextOptions().setViewportSize(1440, 900))) {

            Page page = ctx.newPage();
            page.navigate("https://map.naver.com/p/",
                    new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

            // 검색 입력 & 트리거
            Locator input = page.locator("input.input_search");
            input.waitFor(new Locator.WaitForOptions().setTimeout(15000));
            input.fill(name);
            page.locator("button.button_search").click();

            // 검색결과 iframe 로딩 (※ Java는 frameByName 없음 → frameLocator 사용)
            page.locator("iframe#searchIframe").waitFor(new Locator.WaitForOptions().setTimeout(15000));
            FrameLocator searchFL = page.frameLocator("iframe#searchIframe");

            // entry 이동 anchor 수집
            Locator anchors = searchFL.locator("a[href*='/entry/place/']");
            anchors.first().waitFor(new Locator.WaitForOptions().setTimeout(15000));

            int n = Math.min(anchors.count(), 5);
            for (int i = 0; i < n; i++) {
                String href = anchors.nth(i).getAttribute("href");
                if (href == null) continue;
                if (!href.startsWith("http")) {
                    href = "https://map.naver.com" + (href.startsWith("/") ? href : "/" + href);
                }
                out.add(href);
            }

            log.info("[web-search] extracted place links (canon) count={}", out.size());
        } catch (Exception e) {
            log.warn("[web-search] failed: {}", e.toString());
        }

        return out;
    }


    /**
     * 네이버에서 얻은 상세정보를 우리 Restaurant에 반영
     * 현재 스키마를 모르는 상태라, 일단 로깅만 하고 업데이트는 필요하면 여기에 추가.
     * (이미지 업로드/저장은 Image, AmazonS3Service 규격에 맞춰 별도로 구현)
     */
    private boolean patchRestaurantFromNaver(Restaurant r, Object unused, NaverPlaceDetail d) {
        if (d == null) return false;

        log.info("[naver-import] apply detail id={} name='{}' postal={} heroImage={} hours={}",
                r.getRestaurantId(), r.getName(), d.postalCode, d.heroImageUrl,
                truncate(d.openingHoursText, 120));

        // TODO: 필드가 있다면 아래에 실제 업데이트 추가
        // 예시:
        // r.updateBusinessHoursText(d.openingHoursText);
        // if (d.heroImageUrl != null) saveHeroImage(r, d.heroImageUrl);

        return false; // 실제 변경 시 true 리턴하도록 바꾸세요.
    }

}

