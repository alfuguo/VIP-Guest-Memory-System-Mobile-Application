package com.restaurant.vip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VipGuestMemorySystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(VipGuestMemorySystemApplication.class, args);
    }
}