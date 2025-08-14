package com.post_hub.iam_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    /*WebConfig only for Local testing
    * Uncomment if needed to test locally */

    private static final long MAX_AGE_SECS = 3600;

    private final String[] allowedOrigins = new String[] {
            "http://localhost:8081"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods(CorsConfiguration.ALL)
                .allowedHeaders(CorsConfiguration.ALL)
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECS);
    }

}
