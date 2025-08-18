package konkuk.corkCharge.global.config;

import konkuk.corkCharge.global.oauth.jwt.LoginArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private static final String DEPLOYED_FRONTEND_ORIGIN = "https://cork-charge-fe-deployment.vercel.app";
    private static final String HTTP_PREFIX = "http://";
    private static final String LOCAL_ADDRESS = "localhost";
    private static final String FRONTEND_PORT = "5173";

    private final LoginArgumentResolver loginArgumentResolver;

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

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginArgumentResolver);
    }
}
