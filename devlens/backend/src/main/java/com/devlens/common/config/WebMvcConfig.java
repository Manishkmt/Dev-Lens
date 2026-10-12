package com.devlens.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Serves the frontend static files (HTML, CSS, JS, assets) directly through Spring Boot.
 * Maps requests to the devlens/frontend directory and forwards root "/" to login.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Dynamically resolve frontend directory whether running from devlens or devlens/backend
        File frontendDir = new File("frontend");
        if (!frontendDir.exists()) {
            frontendDir = new File("../frontend");
        }

        String frontendLocation = frontendDir.toURI().toString();

        registry.addResourceHandler("/**")
                .addResourceLocations(frontendLocation, "classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/pages/login.html");
    }
}
