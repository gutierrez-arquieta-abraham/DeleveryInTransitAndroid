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

import com.PolicodeLabs.Delevery_In_Transit.databinding.ActivityMainNegocioBinding;
import com.google.android.material.navigation.NavigationView;

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
                R.id.nav_conexion_negocio, R.id.nav_ubicacion, R.id.nav_rutas, R.id.nav_asignacion, R.id.nav_crear_pedido,R.id.nav_conexion_clave)
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
}