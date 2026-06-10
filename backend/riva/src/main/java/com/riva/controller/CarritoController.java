package com.riva.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riva.dto.AgregarItemCarritoRequest;
import com.riva.dto.CarritoResponse;
import com.riva.dto.ModificarCantidadCarritoRequest;
import com.riva.service.CarritoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
public class CarritoController {

    private static final String DEFAULT_CLIENTE_ID = "cliente-demo";

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public CarritoResponse get(@RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId) {
        return CarritoResponse.from(carritoService.obtenerCarrito(clienteId));
    }

    @PostMapping("/items")
    public CarritoResponse agregarItem(
            @RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId,
            @Valid @RequestBody AgregarItemCarritoRequest request
    ) {
        return CarritoResponse.from(carritoService.agregarItem(
                clienteId,
                request.productId(),
                request.variantId(),
                request.cantidad()
        ));
    }

    @PatchMapping("/items/{itemId}")
    public CarritoResponse modificarCantidad(
            @RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId,
            @PathVariable String itemId,
            @Valid @RequestBody ModificarCantidadCarritoRequest request
    ) {
        return CarritoResponse.from(carritoService.modificarCantidad(clienteId, itemId, request.cantidad()));
    }

    @DeleteMapping("/items/{itemId}")
    public CarritoResponse eliminarItem(
            @RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId,
            @PathVariable String itemId
    ) {
        return CarritoResponse.from(carritoService.eliminarItem(clienteId, itemId));
    }

    @DeleteMapping("/items")
    public CarritoResponse vaciar(
            @RequestHeader(name = "X-Cliente-Id", defaultValue = DEFAULT_CLIENTE_ID) String clienteId
    ) {
        return CarritoResponse.from(carritoService.vaciar(clienteId));
    }
}
