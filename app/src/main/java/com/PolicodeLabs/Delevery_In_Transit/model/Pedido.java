package com.PolicodeLabs.Delevery_In_Transit.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Pedido implements Serializable {

    // 2. ¡ESTA ETIQUETA ES LA SOLUCIÓN! 👇
    // Le dice a Android: "Cuando llegue 'numOrd', guárdalo aquí"
    @SerializedName("numOrd")
    private Integer id;

    private String descripcion;
    @SerializedName("destino")
    private String direccionEntrega;
    private String estatus;
    private String nombreNegocio;

    // --- CAMPOS NUEVOS ---
    private String nombreCliente;
    private String fechaEntrega;
    private String horaEntrega;
    @SerializedName("minutosTranscurridos")
    private Double minutosTranscurridos;

    @SerializedName("kilometrosRecorridos")
    private Double kilometrosRecorridos;

    public Pedido() {}

    // --- GETTERS Y SETTERS ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    // Alias para compatibilidad
    public Integer getNumOrd() { return id; }
    public void setNumOrd(Integer numOrd) { this.id = numOrd; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public String getNombreNegocio() { return nombreNegocio; }
    public void setNombreNegocio(String nombreNegocio) { this.nombreNegocio = nombreNegocio; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(String fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public String getHoraEntrega() { return horaEntrega; }
    public void setHoraEntrega(String horaEntrega) { this.horaEntrega = horaEntrega; }

    public Double getMinutosTranscurridos() { return minutosTranscurridos; }
    public void setMinutosTranscurridos(Double minutosTranscurridos) { this.minutosTranscurridos = minutosTranscurridos; }

    public Double getKilometrosRecorridos() { return kilometrosRecorridos; }
    public void setKilometrosRecorridos(Double kilometrosRecorridos) { this.kilometrosRecorridos = kilometrosRecorridos; }
}