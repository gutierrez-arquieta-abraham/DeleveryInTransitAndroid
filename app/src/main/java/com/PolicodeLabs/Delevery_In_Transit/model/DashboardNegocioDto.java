package com.PolicodeLabs.Delevery_In_Transit.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class DashboardNegocioDto implements Serializable {

    @SerializedName("promedioTiempoEntrega")
    private Double promedioTiempoEntrega;

    @SerializedName("totalKilometrosRecorridos")
    private Double totalKilometrosRecorridos;

    @SerializedName("totalPedidosEntregados")
    private Integer totalPedidosEntregados;

    @SerializedName("historialReciente")
    private List<PedidoDto> historialReciente;

    // --- GETTERS ---
    public Double getPromedioTiempoEntrega() { return promedioTiempoEntrega; }
    public Double getTotalKilometrosRecorridos() { return totalKilometrosRecorridos; }
    public Integer getTotalPedidosEntregados() { return totalPedidosEntregados; }
    public List<PedidoDto> getHistorialReciente() { return historialReciente; }
}