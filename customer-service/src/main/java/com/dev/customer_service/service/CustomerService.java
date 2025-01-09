package com.dev.customer_service.service;

import com.dev.customer_service.dto.CustomerDTO;
import com.dev.customer_service.entity.Customer;
import com.dev.customer_service.exception.CustomerNotFoundException;
import com.dev.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    @Cacheable(value = "customers", key = "#id", unless = "#result == null")
    public CustomerDTO getCustomer(Long id) {
        logger.info("Fetching customer with ID: {}", id);
        CustomerDTO customer = convertToDTO(
                customerRepository.findById(id)
                        .orElseThrow(() -> new CustomerNotFoundException(id))
        );
        logger.info("Fetched customer: {}", customer);
        return customer;
    }

    @Cacheable(value = "customers", unless = "#result.isEmpty()")
    public List<CustomerDTO> getAllCustomers() {
        logger.info("Fetching all customers");
        List<CustomerDTO> customers = customerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("Fetched {} customers", customers.size());
        return customers;
    }

    @CachePut(value = "customers", key = "#result.id")
    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        logger.info("Creating customer with email: {}", customerDTO.getEmail());
        if (customerRepository.existsByEmail(customerDTO.getEmail())) {
            logger.error("Email already exists: {}", customerDTO.getEmail());
            throw new IllegalArgumentException("Email already exists: " + customerDTO.getEmail());
        }
        Customer customer = convertToEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer created with ID: {}", savedCustomer.getId());
        return convertToDTO(savedCustomer);
    }

    @CachePut(value = "customers", key = "#id")
    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        logger.info("Updating customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        updateCustomerFromDTO(customer, customerDTO);
        Customer updatedCustomer = customerRepository.save(customer);
        logger.info("Customer updated: {}", updatedCustomer);
        return convertToDTO(updatedCustomer);
    }

    @CacheEvict(value = "customers", key = "#id")
    @Transactional
    public void deleteCustomer(Long id) {
        logger.info("Deleting customer with ID: {}", id);
        if (!customerRepository.existsById(id)) {
            logger.error("Customer with ID {} not found", id);
            throw new CustomerNotFoundException(id);
        }
        customerRepository.deleteById(id);
        logger.info("Customer with ID {} deleted", id);
    }

    @Cacheable(value = "customers", key = "'search:' + #keyword", unless = "#result.isEmpty()")
    public List<CustomerDTO> searchCustomers(String keyword) {
        logger.info("Searching customers with keyword: {}", keyword);
        List<CustomerDTO> customers = customerRepository.searchCustomers(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("Found {} customers with keyword: {}", customers.size(), keyword);
        return customers;
    }

    @Cacheable(value = "customers", key = "'active'", unless = "#result.isEmpty()")
    public List<CustomerDTO> getActiveCustomers() {
        logger.info("Fetching active customers");
        List<CustomerDTO> activeCustomers = customerRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("Fetched {} active customers", activeCustomers.size());
        return activeCustomers;
    }

    private CustomerDTO convertToDTO(Customer customer) {
        logger.debug("Converting Customer entity to DTO: {}", customer);
        return CustomerDTO.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .active(customer.isActive())
                .build();
    }

    private Customer convertToEntity(CustomerDTO dto) {
        logger.debug("Converting DTO to Customer entity: {}", dto);
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setActive(dto.getActive());
        return customer;
    }

    private void updateCustomerFromDTO(Customer customer, CustomerDTO dto) {
        logger.debug("Updating Customer entity from DTO: {}", dto);
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setActive(dto.getActive());
    }
}

