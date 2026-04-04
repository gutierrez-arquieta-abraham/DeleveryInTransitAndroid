package com.PolicodeLabs.Delevery_In_Transit.model; // Asegúrate que el paquete sea el correcto

import java.io.Serializable;

public class UbicacionDto implements Serializable {

    private Integer numOrd;
    private Double latitud;
    private Double longitud;

    // Este es el nuevo campo que matcheará con el backend
    private String estatus;

    // Constructor vacío (Necesario para Retrofit/Gson)
    public UbicacionDto() {
    }

    // Constructor completo (Opcional, por si lo usas tú manual)
    public UbicacionDto(Integer numOrd, Double latitud, Double longitud, String estatus) {
        this.numOrd = numOrd;
        this.latitud = latitud;
        this.longitud = longitud;
        this.estatus = estatus;
    }

    // --- GETTERS Y SETTERS MANUALES ---

    public Integer getNumOrd() {
        return numOrd;
    }

    public void setNumOrd(Integer numOrd) {
        this.numOrd = numOrd;
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

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    // ToString para depurar (ver en el Logcat qué llega)
    @Override
    public String toString() {
        return "UbicacionDto{" +
                "numOrd=" + numOrd +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", estatus='" + estatus + '\'' +
                '}';
    }
}