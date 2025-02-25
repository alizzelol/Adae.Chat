package com.alizzelol.adaechat;

import java.util.Date;

public class Mensaje {
    private String emisor;
    private String receptor;
    private String contenidoMensaje;
    private Date timestamp;

    public Mensaje() {
        // Constructor vacío requerido por Firestore
    }

    public Mensaje(String emisor, String receptor, String contenidoMensaje, Date timestamp) {
        this.emisor = emisor;
        this.receptor = receptor;
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

    public String getReceptor() {
        return receptor;
    }

    public void setReceptor(String receptor) {
        this.receptor = receptor;
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
