package com.example.ms_envios.dto.request;

import com.example.ms_envios.dto.CreateEnvioRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateEnvioRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void createEnvioRequest_ShouldBeValid_WhenAllFieldsCorrect() {
        CreateEnvioRequest request = new CreateEnvioRequest();
        request.setOrderId(1L);
        request.setAddress("Av. Siempre Viva 123");

        Set<ConstraintViolation<CreateEnvioRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void createEnvioRequest_ShouldBeInvalid_WhenOrderIdNull() {
        CreateEnvioRequest request = new CreateEnvioRequest();
        request.setOrderId(null);
        request.setAddress("Av. Siempre Viva 123");

        Set<ConstraintViolation<CreateEnvioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals("El orderId es obligatorio", violations.iterator().next().getMessage());
    }

    @Test
    void createEnvioRequest_ShouldBeInvalid_WhenAddressBlank() {
        CreateEnvioRequest request = new CreateEnvioRequest();
        request.setOrderId(1L);
        request.setAddress("");

        Set<ConstraintViolation<CreateEnvioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals("La dirección de envío es obligatoria", violations.iterator().next().getMessage());
    }

    @Test
    void createEnvioRequest_ShouldBeInvalid_WhenAddressNull() {
        CreateEnvioRequest request = new CreateEnvioRequest();
        request.setOrderId(1L);
        request.setAddress(null);

        Set<ConstraintViolation<CreateEnvioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals("La dirección de envío es obligatoria", violations.iterator().next().getMessage());
    }
}