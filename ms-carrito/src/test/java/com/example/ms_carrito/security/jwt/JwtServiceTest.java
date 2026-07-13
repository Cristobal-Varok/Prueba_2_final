package com.example.ms_carrito.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "una_clave_secreta_larga_y_segura_de_al_menos_32_bytes";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
    }

    private String generarToken(String username, String role, long minutosValidez) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        Date now = new Date();
        Date expiration = new Date(now.getTime() + minutosValidez * 60 * 1000);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    @Test
    void deberiaExtraerUsernameDeTokenValido() {
        String token = generarToken("testuser", "USER", 10);
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    void deberiaExtraerRoleDeTokenValido() {
        String token = generarToken("testuser", "ADMIN", 10);
        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    @Test
    void deberiaValidarTokenNoExpirado() {
        String token = generarToken("testuser", "USER", 10);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void deberiaInvalidarTokenExpirado() {
        String token = generarToken("testuser", "USER", -10);
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void deberiaRetornarNullAlExtraerUsernameDeTokenMalformado() {
        assertNull(jwtService.extractUsername("token-invalido"));
    }

    @Test
    void deberiaRetornarNullAlExtraerRoleDeTokenMalformado() {
        assertNull(jwtService.extractRole("token-invalido"));
    }

    @Test
    void deberiaInvalidarTokenMalformado() {
        assertFalse(jwtService.isTokenValid("token-invalido"));
    }
}