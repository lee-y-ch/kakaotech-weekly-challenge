package com.community.community.auth;

import com.community.community.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;

    public CurrentUserIdArgumentResolver(AuthService authService) {
        this.authService = authService;
    }

    // Spring이 Controller 메서드 파라미터를 확인
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserId.class)
                && (parameter.getParameterType().equals(Integer.class)
                || parameter.getParameterType().equals(int.class));
    }

    // 실제로 값을 만들어 반환
    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

        // @CookieValue(required = false)와 같은 흐름을 유지
        // 없으면 null로 값을 유지하기 위해서 -> AuthService가 unauthorized 예외 처리 
        String accessToken = null;

        if (request != null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        return authService.getCurrentUserId(accessToken);
    }
}
