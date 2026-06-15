package com.riva.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.riva.model.pedido.Pedido;

public interface PedidoRepository extends MongoRepository<Pedido, String> {

    List<Pedido> findByClienteIdOrderByFechaDesc(String clienteId);

    // CU-23 — el administrador lista todos los pedidos para avanzar su estado.
    List<Pedido> findAllByOrderByFechaDesc();
}
