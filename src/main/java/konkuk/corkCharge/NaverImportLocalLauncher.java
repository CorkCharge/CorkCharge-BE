package konkuk.corkCharge;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class NaverImportLocalLauncher {
    public static void main(String[] args) {
        System.out.println("start??");
        new SpringApplicationBuilder(CorkChargeApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("db-local", "common", "naver-import")
                .properties("debug=true") // 조건평가 로그
                .run(args);
    }
}
