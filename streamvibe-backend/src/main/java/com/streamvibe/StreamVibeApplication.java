package com.streamvibe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class StreamVibeApplication {
    public static void main(String[] args) { SpringApplication.run(StreamVibeApplication.class, args);
    }
}

