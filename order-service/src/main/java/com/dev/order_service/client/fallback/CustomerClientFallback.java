package com.dev.order_service.client.fallback;

import com.dev.order_service.client.CustomerClient;
import com.dev.order_service.dto.CustomerDTO;
import com.dev.order_service.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class CustomerClientFallback implements CustomerClient {

    @Override
    public CustomerDTO getCustomer(Long id) {
        throw new ServiceUnavailableException("Customer service is currently unavailable");
    }
}