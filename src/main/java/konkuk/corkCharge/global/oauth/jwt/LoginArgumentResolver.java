package konkuk.corkCharge.global.oauth.jwt;

import konkuk.corkCharge.global.annotation.LoginUserId;
import konkuk.corkCharge.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static konkuk.corkCharge.global.response.status.BaseExceptionResponseStatus.*;

@Slf4j
@Component
public class LoginArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUserId.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        LoginUserId anno = parameter.getParameterAnnotation(LoginUserId.class);
        boolean required = anno.required();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            if (!required)
                return null;
            throw new CustomException(AUTH_REQUIRED);
        }
        if (!(auth instanceof JwtTokenAuthentication jwtAuth)) {
            throw new CustomException(INVALID_AUTH_TYPE);
        }
        Long userId = jwtAuth.getUserId();
        if (userId == null) {
            throw new CustomException(USER_NOT_FOUND);
        }
        return userId;

    }
}
