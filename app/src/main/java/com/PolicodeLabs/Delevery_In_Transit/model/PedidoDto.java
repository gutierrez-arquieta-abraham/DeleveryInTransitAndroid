package com.PolicodeLabs.Delevery_In_Transit.model;

public class PedidoDto {
    private Integer numOrd;
    private String descripcion;
    private String destino;
    private String estadoReal; // EL NUEVO NOMBRE OFICIAL

    // En Android recibimos las fechas como String desde el JSON
    private String fechaHoraCreacion;
    private String fechaHoraRecogida;
    private String fechaHoraEntrega;

    private Double latitud;
    private Double longitud;

    private Integer idLicencia;
    private String nombreNegocio;

    private Integer idRepartidor;
    private String nombreRepartidor;

    private String nombreCliente;
    private String telefonoCliente;
    private Double latitudDestino;
    private Double longitudDestino;

    public Integer getNumOrd() {
        return numOrd;
    }

    public void setNumOrd(Integer numOrd) {
        this.numOrd = numOrd;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getEstadoReal() {
        return estadoReal;
    }

    public void setEstadoReal(String estadoReal) {
        this.estadoReal = estadoReal;
    }

    public String getFechaHoraCreacion() {
        return fechaHoraCreacion;
    }

    public void setFechaHoraCreacion(String fechaHoraCreacion) {
        this.fechaHoraCreacion = fechaHoraCreacion;
    }

    public String getFechaHoraRecogida() {
        return fechaHoraRecogida;
    }

    public void setFechaHoraRecogida(String fechaHoraRecogida) {
        this.fechaHoraRecogida = fechaHoraRecogida;
    }

    public String getFechaHoraEntrega() {
        return fechaHoraEntrega;
    }

    public void setFechaHoraEntrega(String fechaHoraEntrega) {
        this.fechaHoraEntrega = fechaHoraEntrega;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Integer getIdLicencia() {
        return idLicencia;
    }

    public void setIdLicencia(Integer idLicencia) {
        this.idLicencia = idLicencia;
    }

    public String getNombreNegocio() {
        return nombreNegocio;
    }

    public void setNombreNegocio(String nombreNegocio) {
        this.nombreNegocio = nombreNegocio;
    }

    public Integer getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(Integer idRepartidor) {
        this.idRepartidor = idRepartidor;
    }

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }

    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getTelefonoCliente() { return telefonoCliente; }
    public void setTelefonoCliente(String telefonoCliente) { this.telefonoCliente = telefonoCliente; }
    public Double getLatitudDestino() { return latitudDestino; }
    public void setLatitudDestino(Double latitudDestino) { this.latitudDestino = latitudDestino; }
    public Double getLongitudDestino() { return longitudDestino; }
    public void setLongitudDestino(Double longitudDestino) { this.longitudDestino = longitudDestino; }
}