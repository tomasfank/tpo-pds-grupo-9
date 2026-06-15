package com.riva.model.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.TypeAlias;

import com.riva.pattern.notification.CanalEmail;
import com.riva.pattern.notification.CanalNotificacion;
import com.riva.pattern.notification.CanalPush;
import com.riva.pattern.notification.CanalSMS;
import com.riva.pattern.notification.PreferenciasNotificacion;

@TypeAlias("cliente")
public class Cliente extends Usuario {

    private PreferenciasNotificacion preferencias;

    protected Cliente() {
        // requerido por Spring Data
    }

    public Cliente(String nombre, String apellido, String email, String passwordHash) {
        super(nombre, apellido, email, passwordHash);
        this.preferencias = new PreferenciasNotificacion(true, true, true);
    }

    @Override
    public Rol rol() {
        return Rol.CLIENTE;
    }

    public PreferenciasNotificacion getPreferencias() {
        if (preferencias == null) {
            // Defensa ante documentos persistidos antes de existir el campo.
            preferencias = new PreferenciasNotificacion(true, true, true);
        }
        return preferencias;
    }

    // CU-24 — el cliente activa/desactiva sus canales (patron Observer: la
    // suscripcion se deriva de estas preferencias en cada notificacion).
    public void configurarNotificaciones(boolean email, boolean sms, boolean push) {
        PreferenciasNotificacion prefs = getPreferencias();
        prefs.configurarEmail(email);
        prefs.configurarSms(sms);
        prefs.configurarPush(push);
    }

    // Observer — el Cliente conoce los canales a los que debe suscribirse segun
    // sus preferencias. Email usa el contacto real; SMS y Push son simulados
    // (RF-25) por lo que su destino se deriva de la cuenta.
    public List<CanalNotificacion> canalesNotificacionHabilitados() {
        PreferenciasNotificacion prefs = getPreferencias();
        List<CanalNotificacion> canales = new ArrayList<>();
        if (prefs.isEmail()) {
            canales.add(new CanalEmail(getEmail()));
        }
        if (prefs.isSms()) {
            canales.add(new CanalSMS("sim-sms:" + getEmail()));
        }
        if (prefs.isPush()) {
            canales.add(new CanalPush("sim-push:" + getEmail()));
        }
        return canales;
    }
}
