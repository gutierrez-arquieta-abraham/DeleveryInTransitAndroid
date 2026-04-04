package com.PolicodeLabs.Delevery_In_Transit.ui.ubicacion;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.databinding.FragmentUbicacionBinding;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;
import com.PolicodeLabs.Delevery_In_Transit.model.UsuarioDto;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UbicacionFragment extends Fragment {

    // Binding para acceder al XML
    private FragmentUbicacionBinding binding;

    // Variables del Mapa
    private MapView map;
    private Map<Integer, Marker> marcadoresActivos = new HashMap<>(); // Para rastrear marcadores por ID de Pedido

    // Variables para actualización automática (Loop)
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private int ID_LICENCIA = -1; // ID del Negocio (Gestor)

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. Configuración de OSMDroid (Importante para que cargue el mapa)
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // 2. Inflar el layout con Binding
        binding = FragmentUbicacionBinding.inflate(inflater, container, false);

        // 3. Obtener ID del Negocio (Guardado en Login)
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        ID_LICENCIA = prefs.getInt("ID_LICENCIA", -1);

        // 4. Configurar el Mapa Inicial
        configurarMapa();

        return binding.getRoot();
    }

    private void configurarMapa() {
        map = binding.mapaOSM; // ID del XML
        map.setMultiTouchControls(true);

        // Punto inicial (CDMX por defecto)
        GeoPoint startPoint = new GeoPoint(19.4326, -99.1332);
        map.getController().setZoom(13.0);
        map.getController().setCenter(startPoint);
    }

    // --- CICLO DE VIDA (Iniciar/Detener actualizaciones) ---

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        iniciarActualizacionAutomatica();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        detenerActualizacion();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Evitar fugas de memoria
    }

    // --- LÓGICA DE ACTUALIZACIÓN ---

    private void iniciarActualizacionAutomatica() {
        runnable = new Runnable() {
            @Override
            public void run() {
                obtenerDatosDelServidor();
                handler.postDelayed(this, 5000); // Repetir cada 5 segundos
            }
        };
        handler.post(runnable);
    }

    private void detenerActualizacion() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void obtenerDatosDelServidor() {
        if (ID_LICENCIA == -1) return;

        // LLAMADA 1: Obtener Pedidos (Para pintar marcadores en el mapa)
        // Asegúrate que este método exista en tu ApiService: getPedidosPorNegocio(id)
        RetrofitClient.getApiService().getPedidosPorNegocio(ID_LICENCIA).enqueue(new Callback<List<PedidoDto>>() {
            @Override
            public void onResponse(Call<List<PedidoDto>> call, Response<List<PedidoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    actualizarMapa(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<PedidoDto>> call, Throwable t) {
                Log.e("MAPA", "Error al obtener pedidos: " + t.getMessage());
            }
        });

        // LLAMADA 2: Obtener Repartidores (Para los contadores de abajo)
        // Asegúrate que este método exista: obtenerRepartidoresPorNegocio(id)
        RetrofitClient.getApiService().obtenerRepartidoresPorNegocio(ID_LICENCIA).enqueue(new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    actualizarContadores(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                Log.e("MAPA", "Error al obtener repartidores: " + t.getMessage());
            }
        });
    }

    // --- LÓGICA DEL MAPA (Marcadores) ---

    private void actualizarMapa(List<PedidoDto> listaPedidos) {
        if (map == null) return;

        for (PedidoDto pedido : listaPedidos) {
            // Solo nos interesan pedidos activos con coordenadas válidas
            if (pedido.getLatitud() != null && pedido.getLongitud() != null
                    && pedido.getLatitud() != 0 && pedido.getLongitud() != 0
                    && (pedido.getEstadoReal().equals("EN_CAMINO") || pedido.getEstadoReal().equals("EN_CURSO"))) {

                GeoPoint punto = new GeoPoint(pedido.getLatitud(), pedido.getLongitud());
                Integer idPedido = pedido.getNumOrd();

                if (marcadoresActivos.containsKey(idPedido)) {
                    // Si ya existe, solo movemos el icono
                    Marker m = marcadoresActivos.get(idPedido);
                    m.setPosition(punto);
                    m.setSnippet("Repartidor: " + pedido.getNombreRepartidor());
                } else {
                    // Si es nuevo, lo creamos
                    Marker m = new Marker(map);
                    m.setPosition(punto);
                    m.setTitle("Pedido #" + idPedido);
                    m.setSnippet("Repartidor: " + pedido.getNombreRepartidor());
                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    // Icono personalizado (opcional, usa uno por defecto si falla)
                    // m.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_moto));

                    // Colorear según ID repartidor (Tu lógica de colores)
                    if(pedido.getIdRepartidor() != null) {
                        m.setIcon(obtenerIconoColoreado(pedido.getIdRepartidor()));
                    }

                    map.getOverlays().add(m);
                    marcadoresActivos.put(idPedido, m);
                }
            }
        }
        map.invalidate(); // Refrescar visualmente
    }

    private Drawable obtenerIconoColoreado(Integer id) {
        // Usa un icono base que tengas en drawable, o el default de Android
        Drawable icono = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_mylocation).mutate();
        int colorHash = -16777216 + (id * 999999); // Genera color pseudo-aleatorio
        icono.setTint(colorHash);
        return icono;
    }

    // --- LÓGICA DE CONTADORES (Panel inferior) ---

    private void actualizarContadores(List<UsuarioDto> listaRepartidores) {
        if (binding == null || listaRepartidores == null) return;

        int enServicio = 0;
        int descanso = 0;
        int fueraServicio = 0;
        int entregando = 0;
        int disponibles = 0;

        for (UsuarioDto u : listaRepartidores) {
            String estatus = u.getEstatus();
            if (estatus == null) estatus = "FUERA_SERVICIO";
            estatus = estatus.toUpperCase();

            // GRUPO TRABAJANDO
            if (estatus.equals("DISPONIBLE") || estatus.equals("OCUPADO")
                    || estatus.equals("EN_CAMINO") || estatus.equals("EN_CURSO")) {

                enServicio++;

                if (estatus.equals("DISPONIBLE")) {
                    disponibles++;
                } else {
                    entregando++;
                }
            } else {
                // GRUPO NO TRABAJANDO
                fueraServicio++;
                if (estatus.equals("EN_DESCANSO")) {
                    descanso++;
                }
            }
        }

        // Asignar a los TextViews usando los IDs de tu XML
        binding.tvServicioCount.setText(String.valueOf(enServicio));
        binding.tvFueraServicioCount.setText(String.valueOf(fueraServicio));
        binding.tvEnEntregaCount.setText(String.valueOf(entregando));
        binding.tvDisponiblesCount.setText(String.valueOf(disponibles));
        binding.tvDescansoCount.setText(String.valueOf(descanso));
    }
}