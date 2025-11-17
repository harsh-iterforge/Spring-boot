// src/main/java/com/example/pricing/client/ProductClient.java
package com.example.pricing.client;

import com.example.pricing.config.FeignLoggingConfig;
import com.example.userservice.model.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.userservice.api.ApiApi;

@FeignClient(name = "product-service", url = "${product.service.url}", configuration = com.example.pricing.config.FeignLoggingConfig.class)
public interface ProductClient extends  ApiApi {

//    @GetMapping("/api/products/{id}")
//    ResponseEntity<ProductResponse> getById(@PathVariable("id") Long id);
@Override
@GetMapping(value = "/api/products/{id}", produces = "application/json")
ResponseEntity<ProductResponse> getById(@PathVariable("id") Long id);

}
