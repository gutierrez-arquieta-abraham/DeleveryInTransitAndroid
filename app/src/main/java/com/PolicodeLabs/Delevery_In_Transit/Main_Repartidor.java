package com.PolicodeLabs.Delevery_In_Transit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.RecyclerView;

import com.PolicodeLabs.Delevery_In_Transit.databinding.ActivityMainRepartidorBinding;
import com.PolicodeLabs.Delevery_In_Transit.service.LocationService;
import com.google.android.material.navigation.NavigationView;

public class Main_Repartidor extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainRepartidorBinding binding;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private int idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainRepartidorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMainRepartidor.toolbar);

        drawerLayout = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_conexion, R.id.nav_pedidos, R.id.nav_servicio)
                .setOpenableLayout(drawerLayout)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_repartidor);

        // Configurar ActionBar
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // --- HEADER (NOMBRE DE USUARIO) ---
        View headerView = navigationView.getHeaderView(0);
        TextView tvNombre = headerView.findViewById(R.id.tvNombreUsuario);
        String nombreRecibido = getIntent().getStringExtra("NOMBRE_USUARIO");

        if (nombreRecibido != null) {
            tvNombre.setText("Bienvenido " + nombreRecibido);
        } else {
            tvNombre.setText("Bienvenido Repartidor");
        }

        // --- MANEJO MANUAL DEL MENÚ (AQUÍ ESTÁ LA MAGIA DEL LOGOUT) ---
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_Salir) {
                    // 1. 🛑 MATAR AL SERVICIO (Detener GPS)
                    Intent intentService = new Intent(Main_Repartidor.this, LocationService.class);
                    stopService(intentService);

                    // 2. 🧹 BORRAR MEMORIA (Olvidar ID anterior)
                    SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear(); // ¡Borrón y cuenta nueva!
                    editor.apply();

                    // 3. 🚪 IR AL LOGIN
                    Intent intent = new Intent(Main_Repartidor.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else {
                    // Navegación normal para los otros items
                    boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                    if (handled) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    return handled;
                }
            }
        });

        // --- PERMISOS Y ARRANQUE DEL SERVICIO ---
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            iniciarServicioGPS();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main__repartidor, menu);
        return true;
    }

    private void iniciarServicioGPS() {
        Intent intent = new Intent(this, LocationService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarServicioGPS();
        } else {
            Log.e("GPS_DEBUG", "❌ EL SISTEMA O EL USUARIO DENEGÓ EL PERMISO DE UBICACIÓN.");
        }
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
}