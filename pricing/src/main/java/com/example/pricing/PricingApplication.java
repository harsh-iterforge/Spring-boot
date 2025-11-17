package com.example.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
        "com.example.pricing.client",     // Feign clients
        "com.example.userservice.api"      // generated OpenAPI interfaces if needed
})
public class PricingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PricingApplication.class, args);
        System.out.println("🚀 Pricing Service Started with Feign Enabled...");
    }

}
