package com.community.community.config;

import com.community.community.auth.CurrentUserIdArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;
    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

    public WebConfig(
            @Value("${file.upload-dir}") String uploadDir,
            CurrentUserIdArgumentResolver currentUserIdArgumentResolver
    ) {
        this.uploadDir = uploadDir;
        this.currentUserIdArgumentResolver = currentUserIdArgumentResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Path.of(uploadDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath + "/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "http://leo-community.xyz",
                        "https://leo-community.xyz"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
    }
}