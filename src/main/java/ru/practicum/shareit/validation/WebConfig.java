package ru.practicum.shareit.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.practicum.shareit.booking.api.BookingApiState;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserIdHeaderArgumentResolver userIdHeaderArgumentResolver;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(
                String.class,
                BookingApiState.class,
                value -> BookingApiState.valueOf(value.toUpperCase())
        );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userIdHeaderArgumentResolver);
    }

}