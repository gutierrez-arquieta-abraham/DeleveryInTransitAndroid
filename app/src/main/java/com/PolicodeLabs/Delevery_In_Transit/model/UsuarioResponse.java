package com.PolicodeLabs.Delevery_In_Transit.model;

public class UsuarioResponse {
    private Integer id;
    private String nombre;
    private String email;

    private String rol;    // "GESTOR" o "REPARTIDOR" (String)
    private Integer rolId; // <--- ¡ESTE ES EL QUE FALTABA! (1 o 2)

    private Integer idLicencia;

    // Getters
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }

    public String getRol() { return rol; }
    public Integer getRolId() { return rolId; } // <--- Necesario para el Login

    public Integer getIdLicencia() { return idLicencia; }
}