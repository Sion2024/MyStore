package com.softuni.finalexam.config;

import com.softuni.finalexam.security.SessionCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration

public class WebConfiguration implements WebMvcConfigurer {

    private final SessionCheckInterceptor interceptor;

    public WebConfiguration(SessionCheckInterceptor interceptor) {
        this.interceptor = interceptor;
    }


//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry
//                .addInterceptor(interceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/css/**", "/images/**");
//    }
}
