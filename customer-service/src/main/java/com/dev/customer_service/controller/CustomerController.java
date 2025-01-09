package com.dev.customer_service.controller;

import com.dev.customer_service.dto.CustomerDTO;
import com.dev.customer_service.service.CustomerService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;

    @GetMapping("/{id}")
    @RateLimiter(name = "customerService")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) {
        logger.info("Fetching customer with ID: {}", id);
        CustomerDTO customer = customerService.getCustomer(id);
        logger.info("Fetched customer: {}", customer);
        return ResponseEntity.ok(customer);
    }

    @GetMapping
    @RateLimiter(name = "customerService")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        logger.info("Fetching all customers");
        List<CustomerDTO> customers = customerService.getAllCustomers();
        logger.info("Fetched {} customers", customers.size());
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    @RateLimiter(name = "customerService")
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        logger.info("Creating new customer: {}", customerDTO);
        CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);
        logger.info("Created customer: {}", createdCustomer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RateLimiter(name = "customerService")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO customerDTO) {
        logger.info("Updating customer with ID: {}, Data: {}", id, customerDTO);
        CustomerDTO updatedCustomer = customerService.updateCustomer(id, customerDTO);
        logger.info("Updated customer: {}", updatedCustomer);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    @RateLimiter(name = "customerService")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        logger.info("Deleting customer with ID: {}", id);
        customerService.deleteCustomer(id);
        logger.info("Deleted customer with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @RateLimiter(name = "customerService")
    public ResponseEntity<List<CustomerDTO>> searchCustomers(@RequestParam String keyword) {
        logger.info("Searching customers with keyword: {}", keyword);
        List<CustomerDTO> customers = customerService.searchCustomers(keyword);
        logger.info("Found {} customers for keyword: {}", customers.size(), keyword);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/active")
    @RateLimiter(name = "customerService")
    public ResponseEntity<List<CustomerDTO>> getActiveCustomers() {
        logger.info("Fetching active customers");
        List<CustomerDTO> activeCustomers = customerService.getActiveCustomers();
        logger.info("Fetched {} active customers", activeCustomers.size());
        return ResponseEntity.ok(activeCustomers);
    }
}
