package com.dev.order_service.client.fallback;

import com.dev.order_service.client.CustomerClient;
import com.dev.order_service.dto.CustomerDTO;
import com.dev.order_service.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomerClientFallback implements CustomerClient {

    private static final Logger logger = LoggerFactory.getLogger(CustomerClientFallback.class);

    @Value("${client.customer-service.url}")
    private String customerServiceUrl;

    @Override
    public CustomerDTO getCustomer(Long id) {
        String requestUrl = customerServiceUrl + "/api/customers/" + id;
        logger.error("Fallback invoked for getCustomer method. URL: {}", requestUrl);
        logger.warn("Failed to fetch customer details for customer ID: {}", id);
        String errorMessage = "Customer service is currently unavailable";
        logger.error("Throwing exception: {}", errorMessage);
        throw new ServiceUnavailableException(errorMessage);
    }
}
