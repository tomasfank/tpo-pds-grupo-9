package com.riva.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.riva.model.pedido.Pedido;

public interface PedidoRepository extends MongoRepository<Pedido, String> {

    List<Pedido> findByClienteIdOrderByFechaDesc(String clienteId);
}
