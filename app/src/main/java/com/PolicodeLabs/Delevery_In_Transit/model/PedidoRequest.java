package com.PolicodeLabs.Delevery_In_Transit.model;

public class PedidoRequest {
    private String descripcion;
    private String destino;
    private NegocioId negocio;

    // Los nuevos campos para el mapa y contacto
    private String nombreCliente;
    private String telefonoCliente;
    private Double latitudDestino;
    private Double longitudDestino;

    public PedidoRequest(String descripcion, String destino, Integer idLicencia,
                         String nombreCliente, String telefonoCliente,
                         Double latitudDestino, Double longitudDestino) {
        this.descripcion = descripcion;
        this.destino = destino;
        this.negocio = new NegocioId(idLicencia);
        this.nombreCliente = nombreCliente;
        this.telefonoCliente = telefonoCliente;
        this.latitudDestino = latitudDestino;
        this.longitudDestino = longitudDestino;
    }

    public static class NegocioId {
        private Integer idLicencia;
        public NegocioId(Integer idLicencia) { this.idLicencia = idLicencia; }
    }
}