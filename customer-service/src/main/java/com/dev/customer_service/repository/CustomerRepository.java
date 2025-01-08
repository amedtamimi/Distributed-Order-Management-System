package com.dev.customer_service.repository;

import com.dev.customer_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Customer> findByActiveTrue();

    @Query("SELECT c FROM Customer c WHERE c.firstName LIKE %:keyword% OR c.lastName LIKE %:keyword% OR c.email LIKE %:keyword%")
    List<Customer> searchCustomers(@Param("keyword") String keyword);
}