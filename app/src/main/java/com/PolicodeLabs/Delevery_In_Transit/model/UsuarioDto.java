package com.PolicodeLabs.Delevery_In_Transit.model;

import java.io.Serializable;

public class UsuarioDto implements Serializable {

    private Integer id;
    private String nombre;
    private String email;
    private String rfc;
    private String rol;     // "GESTOR", "REPARTIDOR"
    private String estatus; // "DISPONIBLE", "OCUPADO", "DESCANSO", "FUERA_SERVICIO"

    private Double latitudActual;
    private Double longitudActual;

    // ID del negocio al que pertenece
    private Integer idLicencia;

    public UsuarioDto() {}

    // --- GETTERS Y SETTERS ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    // ¡ESTE ES EL IMPORTANTE PARA LOS CONTADORES!
    public String getEstatus() { return estatus; }
    public void setEstatus(String estatus) { this.estatus = estatus; }

    public Double getLatitudActual() { return latitudActual; }
    public void setLatitudActual(Double latitudActual) { this.latitudActual = latitudActual; }

    public Double getLongitudActual() { return longitudActual; }
    public void setLongitudActual(Double longitudActual) { this.longitudActual = longitudActual; }

    public Integer getIdLicencia() { return idLicencia; }
    public void setIdLicencia(Integer idLicencia) { this.idLicencia = idLicencia; }
}