package com.devlens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DevLens Backend Application Entry Point
 * 
 * Provides RESTful API backend for AI-powered code analysis, project management,
 * and developer assistance tailored for programming students.
 */
@SpringBootApplication
public class DevlensBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevlensBackendApplication.class, args);
    }
}
