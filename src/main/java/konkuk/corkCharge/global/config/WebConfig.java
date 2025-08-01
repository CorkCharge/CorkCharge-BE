package konkuk.corkCharge.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private static final String DEPLOYED_FRONTEND_ORIGIN = "https://cork-charge-fe-deployment.vercel.app";
    private static final String HTTP_PREFIX = "http://";
    private static final String LOCAL_ADDRESS = "localhost";
    private static final String FRONTEND_PORT = "5173";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedOrigins(
                        DEPLOYED_FRONTEND_ORIGIN,
                        HTTP_PREFIX + LOCAL_ADDRESS + ":" + FRONTEND_PORT
                );
    }
}
