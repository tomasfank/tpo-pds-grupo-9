package com.riva.model.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.riva.pattern.notification.CanalNotificacion;
import com.riva.pattern.payment.MetodoPago;
import com.riva.pattern.payment.ResultadoPago;
import com.riva.pattern.state.EstadoPedido;
import com.riva.pattern.state.EstadoPedidoFactory;
import com.riva.pattern.state.EstadoPendiente;
import com.riva.pattern.notification.SujetoObservable;

@Document(collection = "pedidos")
public class Pedido implements SujetoObservable {

    @Id
    private String id;

    @Indexed
    private String clienteId;

    private LocalDateTime fecha;
    private BigDecimal total;
    private List<ItemPedido> items = new ArrayList<>();
    private List<TransicionEstado> historialEstados = new ArrayList<>();
    private DireccionEnvio direccionEnvio;
    private String estadoNombre;
    private String metodoPagoNombre;

    @Transient
    private EstadoPedido estado;

    @Transient
    private MetodoPago metodoPago;

    @Transient
    private List<CanalNotificacion> observadores = new ArrayList<>();

    protected Pedido() {
        // requerido por Spring Data
    }

    public Pedido(String clienteId, List<ItemPedido> items, DireccionEnvio direccionEnvio) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("clienteId es obligatorio");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Pedido requiere al menos un item");
        }
        this.clienteId = clienteId;
        this.fecha = LocalDateTime.now();
        this.items = new ArrayList<>(items);
        this.direccionEnvio = direccionEnvio;
        this.total = calcularTotalDecimal();
        setEstado(new EstadoPendiente());
        registrarEstadoActual();
    }

    public void avanzarEstado() {
        estadoActual().avanzar(this);
        registrarEstadoActual();
        notificar();
    }

    public ResultadoPago pagar() {
        if (metodoPago == null) {
            throw new IllegalStateException("metodoPago es obligatorio");
        }
        if (!metodoPago.validarDatosPago()) {
            return new ResultadoPago(false, "Datos de pago invalidos");
        }
        return metodoPago.procesarPago(total.doubleValue());
    }

    public void cambiarMetodoPago(MetodoPago metodoPago) {
        if (metodoPago == null) {
            throw new IllegalArgumentException("metodoPago es obligatorio");
        }
        this.metodoPago = metodoPago;
    }

    public void registrarMetodoPago(String metodoPagoNombre) {
        if (metodoPagoNombre == null || metodoPagoNombre.isBlank()) {
            throw new IllegalArgumentException("metodoPagoNombre es obligatorio");
        }
        this.metodoPagoNombre = metodoPagoNombre;
    }

    @Override
    public void suscribir(CanalNotificacion canal) {
        if (canal == null) {
            throw new IllegalArgumentException("canal es obligatorio");
        }
        observadores.add(canal);
    }

    @Override
    public void desuscribir(CanalNotificacion canal) {
        observadores.remove(canal);
    }

    @Override
    public void notificar() {
        // CU-23 (flujo 6a): el fallo de un canal se registra y no interrumpe a los
        // demas ni revierte el estado del pedido.
        observadores.forEach(canal -> {
            try {
                canal.actualizar(this);
            } catch (RuntimeException ex) {
                System.err.printf(
                        "Fallo al notificar el pedido %s por un canal: %s%n",
                        id,
                        ex.getMessage()
                );
            }
        });
    }

    public void setEstado(EstadoPedido estado) {
        if (estado == null) {
            throw new IllegalArgumentException("estado es obligatorio");
        }
        this.estado = estado;
        this.estadoNombre = estado.nombre();
    }

    public void actualizarDireccionEnvio(DireccionEnvio direccionEnvio) {
        if (direccionEnvio == null) {
            throw new IllegalArgumentException("direccionEnvio es obligatoria");
        }
        this.direccionEnvio = direccionEnvio;
    }

    public boolean puedeAvanzarEstado() {
        return estadoActual().puedeAvanzar();
    }

    public String nombreEstadoActual() {
        return estadoActual().nombre();
    }

    public void reconstruirEstadoDesdePersistencia() {
        this.estado = EstadoPedidoFactory.desdeNombre(estadoNombre);
    }

    private EstadoPedido estadoActual() {
        if (estado == null) {
            reconstruirEstadoDesdePersistencia();
        }
        return estado;
    }

    private void registrarEstadoActual() {
        historialEstados.add(new TransicionEstado(LocalDateTime.now(), nombreEstadoActual()));
    }

    private BigDecimal calcularTotalDecimal() {
        return items.stream()
                .map(ItemPedido::subtotalDecimal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<ItemPedido> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<TransicionEstado> getHistorialEstados() {
        return Collections.unmodifiableList(historialEstados);
    }

    public DireccionEnvio getDireccionEnvio() {
        return direccionEnvio;
    }

    public EstadoPedido getEstado() {
        return estadoActual();
    }

    public String getEstadoNombre() {
        return estadoNombre;
    }

    public String getMetodoPagoNombre() {
        return metodoPagoNombre;
    }
}
