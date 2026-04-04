package com.PolicodeLabs.Delevery_In_Transit.model;

public class RegistroRequest {
    private String nombre;
    private String email;
    private String contrasena;
    private String rfc;
    private NegocioId negocio; // Objeto anidado

    // Constructor
    public RegistroRequest(String nombre, String email, String contrasena, String rfc, Integer idLicencia) {
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.rfc = rfc;
        // Aquí creamos el objeto anidado automáticamente
        this.negocio = new NegocioId(idLicencia);
    }

    // Clase interna para simular: "negocio": { "idLicencia": 1 }
    public static class NegocioId {
        private Integer idLicencia;
        public NegocioId(Integer idLicencia) { this.idLicencia = idLicencia; }
    }
}