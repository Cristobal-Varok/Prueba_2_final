package com.example.ms_envios.service;

import com.example.ms_envios.client.OrderClient;
import com.example.ms_envios.client.UserClient;
import com.example.ms_envios.dto.OrderDTO;
import com.example.ms_envios.dto.CreateEnvioRequest;
import com.example.ms_envios.dto.response.EnvioResponseDTO;
import com.example.ms_envios.exception.custom.*;
import com.example.ms_envios.model.Envios;
import com.example.ms_envios.model.EnviosStatus;
import com.example.ms_envios.repository.EnviosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnviosServiceTest {

    @Mock
    private EnviosRepository enviosRepository;

    @Mock
    private OrderClient orderClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private EnviosService enviosService;

    private Envios envio;
    private CreateEnvioRequest request;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        request = new CreateEnvioRequest();
        request.setOrderId(1L);
        request.setAddress("Av. Siempre Viva 123");

        orderDTO = new OrderDTO();
        orderDTO.setId(1L);
        orderDTO.setClientId(101L);
        orderDTO.setPaymentStatus("PAID");
        orderDTO.setStatus("PAID");
        orderDTO.setTotalAmount(36.0);

        envio = Envios.builder()
                .shippingId(1L)
                .orderId(1L)
                .userId(101L)
                .address("Av. Siempre Viva 123")
                .status(EnviosStatus.PENDING)
                .trackingNumber("TRK12345678")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByIdOrThrow_ShouldReturnEnvio_WhenExists() {
        when(enviosRepository.findById(1L)).thenReturn(Optional.of(envio));

        Envios result = enviosService.findByIdOrThrow(1L);

        assertNotNull(result);
        assertEquals(1L, result.getShippingId());
        verify(enviosRepository).findById(1L);
    }

    @Test
    void findByIdOrThrow_ShouldThrowException_WhenNotFound() {
        when(enviosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ShippingNotFoundException.class, () -> enviosService.findByIdOrThrow(99L));
        verify(enviosRepository).findById(99L);
    }

    @Test
    void shippingExistsByOrder_ShouldReturnTrue_WhenExists() {
        when(enviosRepository.existsByOrderId(1L)).thenReturn(true);

        boolean result = enviosService.shippingExistsByOrder(1L);

        assertTrue(result);
        verify(enviosRepository).existsByOrderId(1L);
    }

    @Test
    void shippingExistsByOrder_ShouldReturnFalse_WhenNotExists() {
        when(enviosRepository.existsByOrderId(99L)).thenReturn(false);

        boolean result = enviosService.shippingExistsByOrder(99L);

        assertFalse(result);
        verify(enviosRepository).existsByOrderId(99L);
    }

    @Test
    void createShipping_ShouldCreateShipping_WhenValid() {
        when(userClient.userExists("javier")).thenReturn(true);
        when(orderClient.getOrderById(1L)).thenReturn(orderDTO);
        when(enviosRepository.existsByOrderId(1L)).thenReturn(false);
        when(enviosRepository.save(any(Envios.class))).thenReturn(envio);

        EnvioResponseDTO result = enviosService.createShipping("javier", request);

        assertNotNull(result);
        assertEquals(1L, result.getShippingId());
        assertEquals(1L, result.getOrderId());
        assertEquals(EnviosStatus.PENDING, result.getStatus());
        verify(userClient).userExists("javier");
        verify(orderClient).getOrderById(1L);
        verify(enviosRepository).save(any(Envios.class));
    }

    @Test
    void createShipping_ShouldThrowException_WhenUserNotExists() {
        when(userClient.userExists("javier")).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> enviosService.createShipping("javier", request));
        verify(enviosRepository, never()).save(any());
    }

    @Test
    void createShipping_ShouldThrowException_WhenOrderNotFound() {
        when(userClient.userExists("javier")).thenReturn(true);
        when(orderClient.getOrderById(1L)).thenThrow(new RuntimeException("Orden no encontrada"));

        assertThrows(OrderNotFoundException.class, () -> enviosService.createShipping("javier", request));
        verify(enviosRepository, never()).save(any());
    }

    @Test
    void createShipping_ShouldThrowException_WhenOrderNotPaid() {
        orderDTO.setPaymentStatus("PENDING");

        when(userClient.userExists("javier")).thenReturn(true);
        when(orderClient.getOrderById(1L)).thenReturn(orderDTO);

        assertThrows(OrderNotPaidException.class, () -> enviosService.createShipping("javier", request));
        verify(enviosRepository, never()).save(any());
    }

    @Test
    void createShipping_ShouldThrowException_WhenShippingAlreadyExists() {
        when(userClient.userExists("javier")).thenReturn(true);
        when(orderClient.getOrderById(1L)).thenReturn(orderDTO);
        when(enviosRepository.existsByOrderId(1L)).thenReturn(true);

        assertThrows(ShippingAlreadyExistsException.class, () -> enviosService.createShipping("javier", request));
        verify(enviosRepository, never()).save(any());
    }

    @Test
    void updateStatus_ShouldUpdateToShipped_WhenValid() {
        Envios envioPendiente = Envios.builder()
                .shippingId(1L)
                .status(EnviosStatus.PENDING)
                .build();

        Envios envioActualizado = Envios.builder()
                .shippingId(1L)
                .status(EnviosStatus.SHIPPED)
                .shippedAt(LocalDateTime.now())
                .estimatedDelivery(LocalDateTime.now().plusDays(5))
                .build();

        when(enviosRepository.findById(1L)).thenReturn(Optional.of(envioPendiente));
        when(enviosRepository.save(any(Envios.class))).thenReturn(envioActualizado);

        EnvioResponseDTO result = enviosService.updateStatus(1L, EnviosStatus.SHIPPED);

        assertNotNull(result);
        assertEquals(EnviosStatus.SHIPPED, result.getStatus());
        assertNotNull(result.getShippedAt());
        assertNotNull(result.getEstimatedDelivery());
        verify(enviosRepository).save(any(Envios.class));
    }

    @Test
    void updateStatus_ShouldUpdateToDelivered_WhenValid() {
        Envios envioEnviado = Envios.builder()
                .shippingId(1L)
                .status(EnviosStatus.SHIPPED)
                .build();

        Envios envioActualizado = Envios.builder()
                .shippingId(1L)
                .status(EnviosStatus.DELIVERED)
                .deliveredAt(LocalDateTime.now())
                .build();

        when(enviosRepository.findById(1L)).thenReturn(Optional.of(envioEnviado));
        when(enviosRepository.save(any(Envios.class))).thenReturn(envioActualizado);

        EnvioResponseDTO result = enviosService.updateStatus(1L, EnviosStatus.DELIVERED);

        assertNotNull(result);
        assertEquals(EnviosStatus.DELIVERED, result.getStatus());
        assertNotNull(result.getDeliveredAt());
        verify(enviosRepository).save(any(Envios.class));
    }

    @Test
    void updateStatus_ShouldThrowException_WhenNotFound() {
        when(enviosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ShippingNotFoundException.class, () -> enviosService.updateStatus(99L, EnviosStatus.SHIPPED));
        verify(enviosRepository, never()).save(any());
    }

    @Test
    void getShipping_ShouldReturnEnvio_WhenExists() {
        when(enviosRepository.findById(1L)).thenReturn(Optional.of(envio));

        EnvioResponseDTO result = enviosService.getShipping(1L);

        assertNotNull(result);
        assertEquals(1L, result.getShippingId());
        verify(enviosRepository).findById(1L);
    }

    @Test
    void getShipping_ShouldThrowException_WhenNotFound() {
        when(enviosRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ShippingNotFoundException.class, () -> enviosService.getShipping(99L));
        verify(enviosRepository).findById(99L);
    }

    @Test
    void getByOrder_ShouldReturnList() {
        when(enviosRepository.findByOrderId(1L)).thenReturn(List.of(envio));

        List<EnvioResponseDTO> result = enviosService.getByOrder(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(enviosRepository).findByOrderId(1L);
    }

    @Test
    void getByOrder_ShouldReturnEmptyList_WhenNoShippings() {
        when(enviosRepository.findByOrderId(99L)).thenReturn(List.of());

        List<EnvioResponseDTO> result = enviosService.getByOrder(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(enviosRepository).findByOrderId(99L);
    }

    @Test
    void getByUser_ShouldReturnList() {
        when(enviosRepository.findByUserId(101L)).thenReturn(List.of(envio));

        List<EnvioResponseDTO> result = enviosService.getByUser(101L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(enviosRepository).findByUserId(101L);
    }

    @Test
    void getByStatus_ShouldReturnList() {
        when(enviosRepository.findByStatus(EnviosStatus.PENDING)).thenReturn(List.of(envio));

        List<EnvioResponseDTO> result = enviosService.getByStatus(EnviosStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(enviosRepository).findByStatus(EnviosStatus.PENDING);
    }
}