package com.riva.model.pedido;

public class DireccionEnvio {

    private String calle;
    private String numero;
    private String ciudad;
    private String provincia;
    private String codigoPostal;

    protected DireccionEnvio() {
        // requerido por Spring Data
    }

    public DireccionEnvio(String calle, String numero, String ciudad, String provincia, String codigoPostal) {
        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
        this.provincia = provincia;
        this.codigoPostal = codigoPostal;
    }

    public String getCalle() {
        return calle;
    }

    public String getNumero() {
        return numero;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }
}
