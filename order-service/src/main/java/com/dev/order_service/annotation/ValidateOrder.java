package com.dev.order_service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateOrder {
    // Add validation flags
    boolean validateCustomer() default true;
    boolean validateProducts() default true;
    boolean validateStock() default true;
}