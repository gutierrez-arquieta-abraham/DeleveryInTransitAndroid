package com.PolicodeLabs.Delevery_In_Transit.model;

public class PedidoRequest {
    private String descripcion;
    private String destino;
    private NegocioId negocio; // Objeto anidado para el ID

    public PedidoRequest(String descripcion, String destino, Integer idLicencia) {
        this.descripcion = descripcion;
        this.destino = destino;
        this.negocio = new NegocioId(idLicencia);
    }

    // Clase interna para generar el JSON: "negocio": { "idLicencia": 1 }
    public static class NegocioId {
        private Integer idLicencia;
        public NegocioId(Integer idLicencia) { this.idLicencia = idLicencia; }
    }
}