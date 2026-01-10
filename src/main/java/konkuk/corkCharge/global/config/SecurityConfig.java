package konkuk.corkCharge.global.config;

import konkuk.corkCharge.global.oauth.jwt.JwtAuthenticationFilter;
import konkuk.corkCharge.global.oauth.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final OAuthService oAuthService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
                                "/oauth/naver/login", "/oauth/reissue", "/actuator/**", "/oauth/test/user")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/restaurants",
                                "/restaurants/*",
                                "/restaurants/hot",
                                "/restaurants/search",
                                "/restaurants/filter",
                                "/restaurants/map",
                                "/reviews/corkageReview",
                                "/reviews/{restaurantId}",
                                "reviews/home",
                                "/tips",
                                "/tips/*")
                        .permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/corkages/filter",
                                "/restaurants/cluster/list",
                                "/tips",
                                "/restaurants/new",
                                "/restaurants/category",
                                "/restaurants/nearby",
                                "/restaurants/home")
                        .permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .anyRequest().authenticated()
                )
                .oauth2Login(o -> o.userInfoEndpoint(u -> u.userService(oAuthService)))
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
