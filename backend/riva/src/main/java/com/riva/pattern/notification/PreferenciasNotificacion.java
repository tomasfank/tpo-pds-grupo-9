package com.riva.pattern.notification;

public class PreferenciasNotificacion {

    private boolean email;
    private boolean sms;
    private boolean push;

    public PreferenciasNotificacion(boolean email, boolean sms, boolean push) {
        this.email = email;
        this.sms = sms;
        this.push = push;
    }

    public boolean isEmail() {
        return email;
    }

    public boolean isSms() {
        return sms;
    }

    public boolean isPush() {
        return push;
    }

    public void configurarEmail(boolean habilitado) {
        this.email = habilitado;
    }

    public void configurarSms(boolean habilitado) {
        this.sms = habilitado;
    }

    public void configurarPush(boolean habilitado) {
        this.push = habilitado;
    }

}
