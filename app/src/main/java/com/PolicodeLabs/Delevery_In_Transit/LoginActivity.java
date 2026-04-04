package com.PolicodeLabs.Delevery_In_Transit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.model.LoginRequest;
import com.PolicodeLabs.Delevery_In_Transit.model.UsuarioResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    TextView textViewNoAccount;
    Button buttonSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonSignIn = findViewById(R.id.buttonSignIn);
        textViewNoAccount = findViewById(R.id.textViewNoAccount);

        buttonSignIn.setOnClickListener(this);
        textViewNoAccount.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonSignIn) {
            iniciarSesion();
        } else if (v.getId() == R.id.textViewNoAccount) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        }
    }

    private void iniciarSesion() {
        EditText etEmail = findViewById(R.id.editTextEmail);
        EditText etPassword = findViewById(R.id.editTextPassword);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginData = new LoginRequest(email, password);

        RetrofitClient.getApiService().login(loginData).enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UsuarioResponse usuario = response.body();

                    SharedPreferences.Editor editor = getSharedPreferences("MisPreferencias", MODE_PRIVATE).edit();

                    // --- GUARDAR DATOS PRINCIPALES ---
                    editor.putInt("ID_USUARIO", usuario.getId());
                    editor.putString("NOMBRE_USUARIO", usuario.getNombre());
                    editor.putString("EMAIL_USUARIO", usuario.getEmail());
                    editor.putInt("ROL_USUARIO", usuario.getRolId()); // Buena práctica guardarlo

                    // --- BLINDAJE DEL ID LICENCIA ---
                    // Si el usuario ya tiene un negocio asignado, lo guardamos.
                    // Si es nuevo (null o 0), le ponemos -1 para saber que no tiene negocio.
                    if (usuario.getIdLicencia() != null && usuario.getIdLicencia() > 0) {
                        editor.putInt("ID_LICENCIA", usuario.getIdLicencia());
                    } else {
                        editor.putInt("ID_LICENCIA", -1);
                    }

                    editor.apply(); // Confirmar guardado

                    // Navegación
                    if (usuario.getRolId() == 1) {
                        // Pasamos el nombre al activity para que te salude
                        Intent intent = new Intent(LoginActivity.this, Main_Negocio.class);
                        intent.putExtra("NOMBRE_USUARIO", usuario.getNombre());
                        startActivity(intent);
                        finish();
                    } else if (usuario.getRolId() == 2) {
                        Intent intent = new Intent(LoginActivity.this, Main_Repartidor.class);
                        intent.putExtra("NOMBRE_USUARIO", usuario.getNombre());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Rol desconocido", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "Error de credenciales", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                Log.e("LoginError", "Error de red: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}