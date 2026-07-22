package com.example.webstarter;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@ConfigurationPropertiesScan
public class WebStarterApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(WebStarterApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebStarterApplication.class);
    }

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }
}

