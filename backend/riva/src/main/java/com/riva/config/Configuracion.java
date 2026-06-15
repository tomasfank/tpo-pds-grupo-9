package com.riva.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Patron Singleton — centraliza los parametros generales del ecommerce (moneda,
 * tasa de IVA, costo de envio y umbral de envio gratis). El constructor es privado
 * y el unico acceso es {@link #obtenerInstancia()}, que crea el objeto la primera
 * vez y devuelve siempre la misma referencia, evitando configuraciones inconsistentes.
 *
 * <p>Es el Singleton clasico (GoF) del diagrama de clases: aunque Spring podria
 * proveer un bean, el TPO modela explicitamente la instancia estatica autocontenida.
 *
 * <p>La clase ofrece dos niveles de configuracion, ambos presentes en el UML:
 * <ul>
 *   <li><b>Parametros tipados</b> (moneda, tasaIva, costoEnvio, umbralEnvioGratis): los
 *       que el dominio consume hoy — el {@code TiendaFacade} calcula el envio con ellos y
 *       {@code GET /api/config} los expone al frontend.</li>
 *   <li><b>Almacen generico clave-valor</b> ({@code configData} +
 *       {@link #obtenerParametro(String)} / {@link #actualizarParametro(String, String)}):
 *       punto de extension del Singleton para sumar parametros de configuracion en runtime
 *       sin ampliar la API tipada (flags, textos, etc.). Su comportamiento esta cubierto por
 *       {@code ConfiguracionTest}. De forma deliberada no se expone un endpoint de escritura:
 *       editar la configuracion no es un caso de uso del alcance (CU-01..CU-24), por lo que
 *       el mutador queda disponible en el modelo pero sin superficie REST.</li>
 * </ul>
 */
public class Configuracion {

    private static Configuracion instancia;

    private String moneda;
    private double tasaIva;
    private double costoEnvio;
    private double umbralEnvioGratis;

    // Almacen generico de configuracion (ver javadoc de la clase): extension del Singleton
    // para parametros clave-valor accedidos via obtenerParametro/actualizarParametro.
    private final Map<String, String> configData;

    private Configuracion() {
        this.moneda = "USD";
        this.tasaIva = 0.21;
        this.costoEnvio = 9.99;
        this.umbralEnvioGratis = 100.0;
        this.configData = new HashMap<>();
    }

    public static synchronized Configuracion obtenerInstancia() {
        if (instancia == null) {
            instancia = new Configuracion();
        }
        return instancia;
    }

    // Lectura del almacen generico clave-valor (punto de extension del Singleton).
    public String obtenerParametro(String clave) {
        return configData.get(clave);
    }

    // Escritura del almacen generico. Mutador del Singleton, modelado en el UML y cubierto por
    // ConfiguracionTest; sin endpoint REST porque editar configuracion no es un caso de uso del TPO.
    public void actualizarParametro(String clave, String valor) {
        configData.put(clave, valor);
    }

    public String getMoneda() {
        return moneda;
    }

    public double getTasaIva() {
        return tasaIva;
    }

    public double getCostoEnvio() {
        return costoEnvio;
    }

    public double getUmbralEnvioGratis() {
        return umbralEnvioGratis;
    }
}
