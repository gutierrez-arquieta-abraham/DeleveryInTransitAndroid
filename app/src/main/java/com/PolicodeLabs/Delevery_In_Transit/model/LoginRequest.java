package com.PolicodeLabs.Delevery_In_Transit.model;

public class LoginRequest {
    private String email;
    private String contrasena;

    public LoginRequest(String email, String contrasena) {
        this.email = email;
        this.contrasena = contrasena;
    }
}