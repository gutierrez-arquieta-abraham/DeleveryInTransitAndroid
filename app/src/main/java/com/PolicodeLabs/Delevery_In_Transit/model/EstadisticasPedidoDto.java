package com.PolicodeLabs.Delevery_In_Transit.model;

public class EstadisticasPedidoDto {
    private long minutosTranscurridos;
    private double kilometrosRecorridos;

    public long getMinutosTranscurridos() { return minutosTranscurridos; }
    public void setMinutosTranscurridos(long minutosTranscurridos) { this.minutosTranscurridos = minutosTranscurridos; }
    public double getKilometrosRecorridos() { return kilometrosRecorridos; }
    public void setKilometrosRecorridos(double kilometrosRecorridos) { this.kilometrosRecorridos = kilometrosRecorridos; }
}