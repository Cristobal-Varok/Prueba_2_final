package com.example.ms_descuentos.service;

import com.example.ms_descuentos.client.UserClient;
import com.example.ms_descuentos.dto.request.CreateCouponRequest;
import com.example.ms_descuentos.dto.request.ValidateCouponRequest;
import com.example.ms_descuentos.dto.response.DescuentosResponseDTO;
import com.example.ms_descuentos.dto.response.DescuentosResult;
import com.example.ms_descuentos.exception.custom.*;
import com.example.ms_descuentos.model.Descuentos;
import com.example.ms_descuentos.model.DescuentosType;
import com.example.ms_descuentos.repository.DescuentosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DescuentosService {

    private final DescuentosRepository descuentosRepository;
    private final UserClient userClient;

    public Descuentos findByIdOrThrow(Long id) {
        log.debug("Buscando cupón por ID: {}", id);
        return descuentosRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cupón no encontrado - ID: {}", id);
                    return new CouponNotFoundException("Cupón no encontrado con ID: " + id);
                });
    }

    public Descuentos findByCodeOrThrow(String code) {
        log.debug("Buscando cupón por código: {}", code);
        return descuentosRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("Cupón no encontrado - Código: {}", code);
                    return new CouponNotFoundException("Cupón no encontrado con código: " + code);
                });
    }

    // Crear cupón general (sin usuario específico)
    @Transactional
    public DescuentosResponseDTO createCoupon(CreateCouponRequest request) {
        log.info("Creando cupón con código: {}", request.getCode());

        // Verificar si ya existe
        if (descuentosRepository.findByCode(request.getCode().toUpperCase()).isPresent()) {
            throw new CouponNotFoundException("Ya existe un cupón con el código: " + request.getCode());
        }

        Descuentos descuentos = new Descuentos();
        descuentos.setCode(request.getCode().toUpperCase());
        descuentos.setDescription(request.getDescription());
        descuentos.setDiscountType(request.getDiscountType());
        descuentos.setDiscountValue(request.getDiscountValue());
        descuentos.setValidFrom(request.getValidFrom());
        descuentos.setValidUntil(request.getValidUntil());
        descuentos.setMaxUses(request.getMaxUses() == 0 ? null : request.getMaxUses());
        descuentos.setCurrentUses(0);
        descuentos.setMinPurchaseAmount(request.getMinPurchaseAmount());
        descuentos.setActive(request.getActive() != null ? request.getActive() : true);
        descuentos.setApplicableProductIds(request.getApplicableProductIds());

        Descuentos saved = descuentosRepository.save(descuentos);
        log.info("Cupón creado con id: {}", saved.getDiscountId());
        return mapToResponseDTO(saved);
    }

    // Crear cupón para un usuario específico (solo ADMIN)
    @Transactional
    public DescuentosResponseDTO createCouponForUser(String username, CreateCouponRequest request) {
        log.info("Creando cupón para usuario: {} con código: {}", username, request.getCode());

        // Verificar que el usuario existe
        if (!userClient.userExists(username)) {
            log.warn("Usuario no existe al crear cupón: {}", username);
            throw new UserNotFoundException("Usuario no existe: " + username);
        }

        // Verificar si ya existe
        if (descuentosRepository.findByCode(request.getCode().toUpperCase()).isPresent()) {
            throw new CouponNotFoundException("Ya existe un cupón con el código: " + request.getCode());
        }

        Descuentos descuentos = new Descuentos();
        descuentos.setCode(request.getCode().toUpperCase() + "_" + username);
        descuentos.setDescription(request.getDescription() + " (Usuario: " + username + ")");
        descuentos.setDiscountType(request.getDiscountType());
        descuentos.setDiscountValue(request.getDiscountValue());
        descuentos.setValidFrom(request.getValidFrom());
        descuentos.setValidUntil(request.getValidUntil());
        descuentos.setMaxUses(1); // Cupón de un solo uso por usuario
        descuentos.setCurrentUses(0);
        descuentos.setMinPurchaseAmount(request.getMinPurchaseAmount());
        descuentos.setActive(request.getActive() != null ? request.getActive() : true);
        descuentos.setApplicableProductIds(request.getApplicableProductIds());

        Descuentos saved = descuentosRepository.save(descuentos);
        log.info("Cupón creado para usuario {} con id: {}", username, saved.getDiscountId());
        return mapToResponseDTO(saved);
    }

    public List<DescuentosResponseDTO> listAll() {
        log.debug("Listando todos los cupones");
        return descuentosRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<DescuentosResponseDTO> listActiveCoupons() {
        log.debug("Listando cupones activos");
        LocalDateTime now = LocalDateTime.now();
        return descuentosRepository.findByActiveTrueAndValidFromBeforeAndValidUntilAfter(now, now).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public DescuentosResponseDTO getCouponByCode(String code) {
        log.debug("Buscando cupón por código: {}", code);
        Descuentos descuentos = findByCodeOrThrow(code);
        return mapToResponseDTO(descuentos);
    }

    public boolean couponExists(String code) {
        log.debug("Verificando existencia de cupón: {}", code);
        boolean exists = descuentosRepository.findByCode(code.toUpperCase()).isPresent();
        log.debug("Cupón {} - Existe: {}", code, exists);
        return exists;
    }

    @Transactional
    public DescuentosResult validateCoupon(ValidateCouponRequest request) {
        String code = request.getCode().toUpperCase();
        log.info("Validando cupón: {} con total carrito: {}", code, request.getCartTotal());

        Descuentos coupon = descuentosRepository.findByCode(code).orElse(null);

        if (coupon == null) {
            log.warn("Cupón inválido: {} no existe", code);
            return new DescuentosResult(false, 0.0, "Cupón no válido", code);
        }

        if (!coupon.getActive()) {
            log.warn("Cupón {} desactivado", code);
            return new DescuentosResult(false, 0.0, "Cupón desactivado", code);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            log.warn("Cupón {} fuera de fecha vigencia", code);
            return new DescuentosResult(false, 0.0, "Cupón fuera de fecha de vigencia", code);
        }

        if (coupon.getMaxUses() != null && coupon.getCurrentUses() >= coupon.getMaxUses()) {
            log.warn("Cupón {} ha alcanzado su límite de usos", code);
            return new DescuentosResult(false, 0.0, "Cupón ya alcanzó su límite de usos", code);
        }

        if (request.getCartTotal() < coupon.getMinPurchaseAmount()) {
            log.debug("Monto mínimo no alcanzado para cupón {}, total: {}", code, request.getCartTotal());
            return new DescuentosResult(false, 0.0,
                    "Monto mínimo de compra: $" + coupon.getMinPurchaseAmount(), code);
        }

        Double discountAmount = 0.0;
        if (coupon.getDiscountType() == DescuentosType.PERCENTAGE) {
            discountAmount = request.getCartTotal() * (coupon.getDiscountValue() / 100.0);
        } else {
            discountAmount = Math.min(coupon.getDiscountValue(), request.getCartTotal());
        }
        discountAmount = Math.round(discountAmount * 100.0) / 100.0;

        log.info("Cupón {} aplicado correctamente, descuento: ${}", code, discountAmount);
        return new DescuentosResult(true, discountAmount, "Cupón aplicado correctamente", code);
    }

    @Transactional
    public DescuentosResult useCoupon(String code, Double cartTotal) {
        log.info("Usando cupón: {} con total carrito: ${}", code, cartTotal);

        ValidateCouponRequest request = new ValidateCouponRequest();
        request.setCode(code);
        request.setCartTotal(cartTotal);

        DescuentosResult validation = validateCoupon(request);
        if (!validation.isValid()) {
            return validation;
        }

        Descuentos coupon = findByCodeOrThrow(code);
        coupon.setCurrentUses(coupon.getCurrentUses() + 1);
        descuentosRepository.save(coupon);

        log.info("Cupón {} usado, usos actuales: {}/{}", code, coupon.getCurrentUses(),
                coupon.getMaxUses() != null ? coupon.getMaxUses() : "∞");
        return validation;
    }

    @Transactional
    public void deactivateCoupon(Long id) {
        log.info("Desactivando cupón con id: {}", id);
        Descuentos coupon = findByIdOrThrow(id);
        coupon.setActive(false);
        descuentosRepository.save(coupon);
        log.info("Cupón {} desactivado", coupon.getCode());
    }

    @Transactional
    public void activateCoupon(Long id) {
        log.info("Activando cupón con id: {}", id);
        Descuentos coupon = findByIdOrThrow(id);
        coupon.setActive(true);
        descuentosRepository.save(coupon);
        log.info("Cupón {} activado", coupon.getCode());
    }

    public boolean existsById(Long id) {
        log.debug("Verificando existencia de cupón por ID: {}", id);
        boolean exists = descuentosRepository.existsById(id);
        log.debug("Cupón ID: {} - Existe: {}", id, exists);
        return exists;
    }

    private DescuentosResponseDTO mapToResponseDTO(Descuentos descuentos) {
        return DescuentosResponseDTO.builder()
                .discountId(descuentos.getDiscountId())
                .code(descuentos.getCode())
                .description(descuentos.getDescription())
                .discountType(descuentos.getDiscountType())
                .discountValue(descuentos.getDiscountValue())
                .validFrom(descuentos.getValidFrom())
                .validUntil(descuentos.getValidUntil())
                .maxUses(descuentos.getMaxUses())
                .currentUses(descuentos.getCurrentUses())
                .minPurchaseAmount(descuentos.getMinPurchaseAmount())
                .active(descuentos.getActive())
                .applicableProductIds(descuentos.getApplicableProductIds())
                .build();
    }
}