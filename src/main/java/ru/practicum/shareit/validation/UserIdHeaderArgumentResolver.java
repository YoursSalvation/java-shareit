package ru.practicum.shareit.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserIdHeaderArgumentResolver implements HandlerMethodArgumentResolver {

    private final String userIdHeader;

    public UserIdHeaderArgumentResolver(
            @Value("${shareit.api.auth.userheader}") String userIdHeader
    ) {
        this.userIdHeader = userIdHeader;
    }

    @Override
    public boolean supportsParameter(MethodParameter p) {
        return p.hasParameterAnnotation(UserIdHeader.class);
    }

    @Override
    public Object resolveArgument(MethodParameter p, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        String headerValue = webRequest.getHeader(userIdHeader);
        if (headerValue == null) throw new MissingRequestHeaderException(userIdHeader, p);
        try {
            return Long.parseLong(headerValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Header " + userIdHeader + " should be Long number");
        }
    }
}