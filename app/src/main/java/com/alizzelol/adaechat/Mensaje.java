package com.alizzelol.adaechat;

import java.util.Date;

public class Mensaje {
    private String emisor;
    private String contenidoMensaje;
    private Date timestamp;

    public Mensaje() {
    }

    public Mensaje(String emisor, String contenidoMensaje, Date timestamp) {
        this.emisor = emisor;
        this.contenidoMensaje = contenidoMensaje;
        this.timestamp = timestamp;
    }

    // Getters y setters
    public String getEmisor() {
        return emisor;
    }

    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }

    public String getContenidoMensaje() {
        return contenidoMensaje;
    }

    public void setContenidoMensaje(String contenidoMensaje) {
        this.contenidoMensaje = contenidoMensaje;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}