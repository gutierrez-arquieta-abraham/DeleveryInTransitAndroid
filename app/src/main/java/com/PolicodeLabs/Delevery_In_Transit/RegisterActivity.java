package com.PolicodeLabs.Delevery_In_Transit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.model.RegistroRequest;
import com.PolicodeLabs.Delevery_In_Transit.model.UsuarioResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    // Declaramos las variables que coinciden con tu XML
    private EditText etNombre, etRfc, etEmail, etPass, etConfirmPass;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Asegúrate que tu XML se llame así

        // 1. Vincular con los IDs de tu XML
        etNombre = findViewById(R.id.editTextFirstName);
        etRfc = findViewById(R.id.editTextRFC);
        etEmail = findViewById(R.id.editTextEmail);
        etPass = findViewById(R.id.editTextPassword);
        etConfirmPass = findViewById(R.id.editTextConfirmPassword);
        btnRegister = findViewById(R.id.buttonRegister);

        // 2. Listener del botón
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarYRegistrar();
            }
        });
    }

    private void validarYRegistrar() {
        // Obtener los textos limpios (sin espacios extra)
        String nombre = etNombre.getText().toString().trim();
        String rfc = etRfc.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPass.getText().toString().trim();
        String confirmPassword = etConfirmPass.getText().toString().trim();

        // --- VALIDACIONES ---

        // 1. Que nada esté vacío
        if (nombre.isEmpty() || rfc.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor llene todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. ¡AQUÍ ESTÁ LO QUE PEDISTE! Verificar coincidencia
        if (!password.equals(confirmPassword)) {
            etConfirmPass.setError("Las contraseñas no coinciden");
            Toast.makeText(this, "Las contraseñas no son iguales", Toast.LENGTH_SHORT).show();
            return; // Detenemos la ejecución aquí
        }

        // 3. Preparar datos para la API
        // Asumimos idLicencia = 1 (Tu negocio por defecto)
        int idLicencia = 1;

        RegistroRequest request = new RegistroRequest(nombre, email, password, rfc, idLicencia);

        // 4. Llamar a Retrofit
        Toast.makeText(this, "Registrando...", Toast.LENGTH_SHORT).show();

        RetrofitClient.getApiService().registrarRepartidor(request).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ÉXITO
                    Toast.makeText(RegisterActivity.this, "¡Registro Exitoso!", Toast.LENGTH_LONG).show();

                    // Ir al Login
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // ERROR (Probablemente correo o RFC duplicado)
                    Toast.makeText(RegisterActivity.this, "Error: El correo o RFC ya existen", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}