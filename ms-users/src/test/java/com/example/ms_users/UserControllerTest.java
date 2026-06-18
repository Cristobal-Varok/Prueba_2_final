package com.example.ms_users;

import com.example.ms_users.controller.AdminController;
import com.example.ms_users.controller.AuthController;
import com.example.ms_users.controller.UserController;
import com.example.ms_users.security.jwt.JwtService;
import com.example.ms_users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private AuthController authController;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUserProfileEndpoint() {
        Authentication authentication = mock(Authentication.class);

        ResponseEntity<?> response = userController.getMyProfile(authentication);
        assertNotNull(response);
        assertNotNull(response.getStatusCode());
    }

    @Test
    void testUserLoginEndpoint() {

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("username", "admin");
        loginBody.put("password", "secret123");

        ResponseEntity<?> response = authController.login(loginBody);
        assertNotNull(response);
        assertNotNull(response.getStatusCode());
    }

    @Test
    void testAdminDashboardEndpoint() {
        ResponseEntity<?> response = adminController.getAllUsers();
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}