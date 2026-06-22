package com.example.ms_envios;

import com.example.ms_envios.client.OrderClient;
import com.example.ms_envios.client.UserClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class MsShippingApplicationTests {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private OrderClient orderClient;

    @Test
    void contextLoads() {
        // Test que verifica que el contexto de Spring carga correctamente
    }
}