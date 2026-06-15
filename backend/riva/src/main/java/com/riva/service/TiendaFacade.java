package com.riva.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.riva.config.Configuracion;
import com.riva.dto.CheckoutResponse;
import com.riva.dto.ProcesarPagoRequest;
import com.riva.dto.ProcesarPagoResponse;
import com.riva.model.pedido.DireccionEnvio;
import com.riva.model.pedido.Pedido;

/**
 * Patron Facade — punto de entrada unico para confirmar una compra. Encapsula la
 * secuencia que de otro modo el cliente deberia orquestar a mano: verificar el
 * carrito, crear el {@link Pedido}, procesar el pago (Strategy) avanzando el estado
 * (State) y disparar las notificaciones (Observer). Devuelve un {@link CheckoutResponse}
 * sin exponer los subsistemas internos.
 */
@Service
public class TiendaFacade {

    private final PedidoService pedidoService;

    public TiendaFacade(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public CheckoutResponse confirmarCompra(String clienteId, ProcesarPagoRequest pago,
                                            DireccionEnvio direccionEnvio) {
        // 1) Verificar carrito + crear el pedido en estado Pendiente (State inicial).
        Pedido pedido = pedidoService.crearDesdeCarrito(clienteId, direccionEnvio);
        // 2) Procesar el pago (Strategy) -> transicion a Pagado (State) -> notificar (Observer).
        ProcesarPagoResponse resultado = pedidoService.procesarPago(clienteId, pedido.getId(), pago);

        BigDecimal total = resultado.pedido().total();
        boolean envioGratis = calcularEnvioGratis(total);
        BigDecimal costoEnvio = envioGratis
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(Configuracion.obtenerInstancia().getCostoEnvio());

        return new CheckoutResponse(
                resultado.exito(),
                resultado.mensaje(),
                resultado.pedido(),
                costoEnvio,
                envioGratis
        );
    }

    // Consulta el Singleton de configuracion para el umbral de envio gratis.
    private boolean calcularEnvioGratis(BigDecimal total) {
        double umbral = Configuracion.obtenerInstancia().getUmbralEnvioGratis();
        return total != null && total.doubleValue() >= umbral;
    }
}
