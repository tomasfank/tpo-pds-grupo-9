package com.riva.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.riva.model.cart.Carrito;

public interface CarritoRepository extends MongoRepository<Carrito, String> {

    Optional<Carrito> findByClienteId(String clienteId);
}
