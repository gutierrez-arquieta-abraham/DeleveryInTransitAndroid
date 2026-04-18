package com.PolicodeLabs.Delevery_In_Transit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.databinding.ActivityMainNegocioBinding;
import com.PolicodeLabs.Delevery_In_Transit.model.NegocioDto;
import com.google.android.material.navigation.NavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Main_Negocio extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainNegocioBinding binding;
    private NavController navController;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainNegocioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMainNegocio.toolbar);

        // --- Código del FAB eliminado ---

        drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // --- IDs actualizados para el menú de Negocio ---
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_conexion_negocio, R.id.nav_ubicacion, R.id.nav_rutas, R.id.nav_asignacion, R.id.nav_crear_pedido,R.id.nav_conexion_clave, R.id.nav_estadisticas, R.id.nav_Salir)
                .setOpenableLayout(drawerLayout)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_negocio);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // --- Manejo manual del menú para incluir "Salir" ---
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_Salir) { // Asegúrate que este ID exista en tu menú
                    // Acción para Salir
                    Intent intent = new Intent(Main_Negocio.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else {
                    // Manejo automático para los demás ítems
                    boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                    if (handled) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    return handled;
                }
            }
        });
        // 1. Obtenemos la vista del encabezado
        View headerView = navigationView.getHeaderView(0);

// 2. Buscamos el TextView (Asegúrate que el ID coincida con tu XML)
        TextView tvNombre = headerView.findViewById(R.id.tvNombreUsuario);

// 3. Recuperamos el nombre que nos mandó el Login
        String nombreRecibido = getIntent().getStringExtra("NOMBRE_USUARIO");

// 4. Lo pintamos
        if (nombreRecibido != null) {
            tvNombre.setText("Bienvenido " + nombreRecibido);
        } else {
            tvNombre.setText("Bienvenido Gestor");
        }
        // --- Fin del manejo manual ---
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú correcto (el de 3 puntos, si tienes uno)
        getMenuInflater().inflate(R.menu.main__negocio, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    public void convertirDireccionACoordenadasYGuardar(NegocioDto negocioCompleto, int idLicencia) {

        // 1. Extraemos la dirección del DTO y le damos contexto
        String busqueda = negocioCompleto.getDireccion() + ", México";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        new Thread(() -> {
            try {
                List<Address> resultados = geocoder.getFromLocationName(busqueda, 1);

                runOnUiThread(() -> {
                    if (resultados != null && !resultados.isEmpty()) {
                        Address ubicacionReal = resultados.get(0);

                        // 2. Inyectamos las coordenadas matemáticas al DTO
                        negocioCompleto.setLatitud(ubicacionReal.getLatitude());
                        negocioCompleto.setLongitud(ubicacionReal.getLongitude());

                        Log.d("GEOCODER", "📍 Coordenadas listas. Guardando en Spring Boot...");

                        // 3. LLAMADA A RETROFIT USANDO TU ApiService
                        RetrofitClient.getApiService().actualizarNegocio(idLicencia, negocioCompleto).enqueue(new Callback<NegocioDto>() {
                            @Override
                            public void onResponse(Call<NegocioDto> call, Response<NegocioDto> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(Main_Negocio.this, "✅ Datos y ubicación guardados correctamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Main_Negocio.this, "❌ Error del servidor al guardar", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<NegocioDto> call, Throwable t) {
                                Toast.makeText(Main_Negocio.this, "🚨 Falla de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(Main_Negocio.this, "❌ No pudimos ubicar esa dirección en el mapa. Sé más específico.", Toast.LENGTH_LONG).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Main_Negocio.this, "🚨 Error de red al buscar la dirección.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}