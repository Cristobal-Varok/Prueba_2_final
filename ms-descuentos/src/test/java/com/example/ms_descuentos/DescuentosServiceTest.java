package com.example.ms_descuentos;

import com.example.ms_descuentos.client.UserClient;
import com.example.ms_descuentos.dto.request.CreateCouponRequest;
import com.example.ms_descuentos.dto.request.ValidateCouponRequest;
import com.example.ms_descuentos.dto.response.DescuentosResponseDTO;
import com.example.ms_descuentos.dto.response.DescuentosResult;
import com.example.ms_descuentos.exception.custom.CouponNotFoundException;
import com.example.ms_descuentos.exception.custom.UserNotFoundException;
import com.example.ms_descuentos.model.Descuentos;
import com.example.ms_descuentos.model.DescuentosType;
import com.example.ms_descuentos.repository.DescuentosRepository;
import com.example.ms_descuentos.service.DescuentosService;
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
class DescuentosServiceTest {

    @Mock
    private DescuentosRepository descuentosRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private DescuentosService descuentosService;

    // Helper

    private Descuentos buildCupon(boolean active, LocalDateTime from, LocalDateTime until) {
        Descuentos c = new Descuentos();
        c.setDiscountId(1L);
        c.setCode("VERANO2025");
        c.setDiscountType(DescuentosType.PERCENTAGE);
        c.setDiscountValue(10.0);
        c.setMinPurchaseAmount(0.0);
        c.setCurrentUses(0);
        c.setMaxUses(null);
        c.setActive(active);
        c.setValidFrom(from);
        c.setValidUntil(until);
        return c;
    }

    //listAll

    @Test
    void deberiaRetornarListaDeCupones() {
        Descuentos cupon = buildCupon(true, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        when(descuentosRepository.findAll()).thenReturn(List.of(cupon));

        List<DescuentosResponseDTO> resultado = descuentosService.listAll();

        assertEquals(1, resultado.size());
        assertEquals("VERANO2025", resultado.get(0).getCode());
        verify(descuentosRepository).findAll();
    }

    //getCouponByCode
    @Test
    void deberiaRetornarCuponPorCodigo() {
        Descuentos cupon = buildCupon(true, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        when(descuentosRepository.findByCode("VERANO2025")).thenReturn(Optional.of(cupon));

        DescuentosResponseDTO resultado = descuentosService.getCouponByCode("VERANO2025");

        assertNotNull(resultado);
        assertEquals("VERANO2025", resultado.getCode());
    }

    @Test
    void deberiaLanzarExcepcionCuandoCuponNoExiste() {
        when(descuentosRepository.findByCode("NOEXISTE")).thenReturn(Optional.empty());

        assertThrows(CouponNotFoundException.class, () ->
                descuentosService.getCouponByCode("NOEXISTE"));
    }

    //createCoupon

    @Test
    void deberiaCrearCuponCorrectamente() {
        CreateCouponRequest request = new CreateCouponRequest();
        request.setCode("VERANO2025");
        request.setDiscountType(DescuentosType.PERCENTAGE);
        request.setDiscountValue(10.0);
        request.setValidFrom(LocalDateTime.now().minusDays(1));
        request.setValidUntil(LocalDateTime.now().plusDays(10));
        request.setMaxUses(0);
        request.setMinPurchaseAmount(0.0);
        request.setActive(true);

        Descuentos saved = buildCupon(true, request.getValidFrom(), request.getValidUntil());

        when(descuentosRepository.findByCode("VERANO2025")).thenReturn(Optional.empty());
        when(descuentosRepository.save(any(Descuentos.class))).thenReturn(saved);

        DescuentosResponseDTO resultado = descuentosService.createCoupon(request);

        assertNotNull(resultado);
        assertEquals("VERANO2025", resultado.getCode());
        verify(descuentosRepository).save(any(Descuentos.class));
    }

    //createCouponForUser

    @Test
    void deberiaLanzarExcepcionCuandoUsuarioNoExisteAlCrearCupon() {
        CreateCouponRequest request = new CreateCouponRequest();
        request.setCode("VERANO2025");
        request.setDiscountType(DescuentosType.PERCENTAGE);
        request.setDiscountValue(10.0);
        request.setValidFrom(LocalDateTime.now().minusDays(1));
        request.setValidUntil(LocalDateTime.now().plusDays(10));

        when(userClient.userExists("noexiste")).thenReturn(false);

        assertThrows(UserNotFoundException.class, () ->
                descuentosService.createCouponForUser("noexiste", request));
    }

    //validateCoupon

    @Test
    void deberiaValidarCuponCorrectamente() {
        Descuentos cupon = buildCupon(true, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        when(descuentosRepository.findByCode("VERANO2025")).thenReturn(Optional.of(cupon));

        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("VERANO2025");
        request.setCartTotal(100.0);

        DescuentosResult resultado = descuentosService.validateCoupon(request);

        assertTrue(resultado.isValid());
        assertEquals(10.0, resultado.getDiscountAmount());
    }

    @Test
    void deberiaRetornarInvalidoCuandoCuponDesactivado() {
        Descuentos cupon = buildCupon(false, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        when(descuentosRepository.findByCode("VERANO2025")).thenReturn(Optional.of(cupon));

        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("VERANO2025");
        request.setCartTotal(100.0);

        DescuentosResult resultado = descuentosService.validateCoupon(request);

        assertFalse(resultado.isValid());
        assertEquals("Cupón desactivado", resultado.getMessage());
    }

    @Test
    void deberiaRetornarInvalidoCuandoCuponExpirado() {
        Descuentos cupon = buildCupon(true, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(1));
        when(descuentosRepository.findByCode("VERANO2025")).thenReturn(Optional.of(cupon));

        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode("VERANO2025");
        request.setCartTotal(100.0);

        DescuentosResult resultado = descuentosService.validateCoupon(request);

        assertFalse(resultado.isValid());
        assertEquals("Cupón fuera de fecha de vigencia", resultado.getMessage());
    }

    //deactivateCoupon

    @Test
    void deberiaDesactivarCupon() {
        Descuentos cupon = buildCupon(true, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        when(descuentosRepository.findById(1L)).thenReturn(Optional.of(cupon));
        when(descuentosRepository.save(any(Descuentos.class))).thenReturn(cupon);

        descuentosService.deactivateCoupon(1L);

        assertFalse(cupon.getActive());
        verify(descuentosRepository).save(cupon);
    }
}