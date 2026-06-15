package com.riva.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.riva.config.Configuracion;
import com.riva.dto.ConfigResponse;

// Expone los parametros generales del ecommerce (patron Singleton: Configuracion).
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @GetMapping
    public ConfigResponse obtener() {
        return ConfigResponse.from(Configuracion.obtenerInstancia());
    }
}
