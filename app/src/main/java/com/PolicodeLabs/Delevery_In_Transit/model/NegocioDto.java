package com.PolicodeLabs.Delevery_In_Transit.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NegocioDto implements Serializable {

    // Usamos @SerializedName para asegurarnos que coincida EXACTAMENTE con el JSON de Spring Boot

    @SerializedName("idLicencia")
    private Integer idLicencia;

    @SerializedName("nomEmp")
    private String nomEmp;

    @SerializedName("rfcEnc")
    private String rfcEnc;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("zonaCobertura")
    private Integer zonaCobertura;

    @SerializedName("codigoLicencia")
    private String codigoLicencia;

    @SerializedName("codigoConexion")
    private String codigoConexion;

    // --- CONSTRUCTORES ---
    public NegocioDto() {
    }

    // --- GETTERS Y SETTERS ---
    public Integer getIdLicencia() {
        return idLicencia;
    }

    public void setIdLicencia(Integer idLicencia) {
        this.idLicencia = idLicencia;
    }

    public String getNomEmp() {
        return nomEmp;
    }

    public void setNomEmp(String nomEmp) {
        this.nomEmp = nomEmp;
    }

    public String getRfcEnc() {
        return rfcEnc;
    }

    public void setRfcEnc(String rfcEnc) {
        this.rfcEnc = rfcEnc;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getZonaCobertura() {
        return zonaCobertura;
    }

    public void setZonaCobertura(Integer zonaCobertura) {
        this.zonaCobertura = zonaCobertura;
    }

    public String getCodigoLicencia() {
        return codigoLicencia;
    }

    public void setCodigoLicencia(String codigoLicencia) {
        this.codigoLicencia = codigoLicencia;
    }

    public String getCodigoConexion() {
        return codigoConexion;
    }

    public void setCodigoConexion(String codigoConexion) {
        this.codigoConexion = codigoConexion;
    }
}