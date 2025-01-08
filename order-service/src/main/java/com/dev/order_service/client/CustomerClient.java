package com.dev.order_service.client;

import com.dev.order_service.client.fallback.CustomerClientFallback;
import com.dev.order_service.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "customer-service",
        url = "${client.customer-service.url}",
        fallback = CustomerClientFallback.class
)
public interface CustomerClient {

    @GetMapping("/api/customers/{id}")
    CustomerDTO getCustomer(@PathVariable("id") Long id);
}