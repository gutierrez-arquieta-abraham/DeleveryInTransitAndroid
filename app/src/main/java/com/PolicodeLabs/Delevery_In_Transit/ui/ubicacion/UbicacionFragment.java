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

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.databinding.FragmentUbicacionBinding;
import com.PolicodeLabs.Delevery_In_Transit.model.NegocioDto;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;
import com.PolicodeLabs.Delevery_In_Transit.model.UsuarioDto;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UbicacionFragment extends Fragment {

    private FragmentUbicacionBinding binding;

    // Variables del Mapa
    private MapView map;
    private Map<Integer, Marker> marcadoresActivos = new HashMap<>(); // Marcadores de pedidos
    private Marker marcadorNegocio = null; // <-- NUEVO: Marcador intocable para el local

    // Variables para actualización automática
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private int ID_LICENCIA = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());
        binding = FragmentUbicacionBinding.inflate(inflater, container, false);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        ID_LICENCIA = prefs.getInt("ID_LICENCIA", -1);
        Log.d("DEBUG_APP", "El ID_LICENCIA recuperado es: " + ID_LICENCIA); // <-- REVISA ESTO EN LOGCAT

        configurarMapa();

        // <-- NUEVO: Traer la ubicación del negocio UNA sola vez al inicio
        cargarUbicacionDelNegocio();

        return binding.getRoot();
    }

    private void configurarMapa() {
        map = binding.mapaOSM;
        map.setMultiTouchControls(true);

        // Centro temporal por si el negocio tarda en cargar o no tiene coordenadas
        GeoPoint startPoint = new GeoPoint(19.4326, -99.1332);
        map.getController().setZoom(13.0);
        map.getController().setCenter(startPoint);
    }

    // --- LÓGICA PARA EL PIN DEL NEGOCIO ---
    private void cargarUbicacionDelNegocio() {
        if (ID_LICENCIA == -1) return;

        Log.d("DEBUG_API", "➡️ Pidiendo datos del negocio ID: " + ID_LICENCIA);

        RetrofitClient.getApiService().getNegocioById(ID_LICENCIA).enqueue(new Callback<NegocioDto>() {
            @Override
            public void onResponse(Call<NegocioDto> call, Response<NegocioDto> response) {
                Log.d("DEBUG_API", "⬅️ getNegocioById respondió. ¿Éxito?: " + response.isSuccessful() + " | Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    NegocioDto negocio = response.body();
                    Log.d("DEBUG_API", "📍 Coordenadas recibidas: Lat=" + negocio.getLatitud() + ", Lon=" + negocio.getLongitud());

                    if (negocio.getLatitud() != null && negocio.getLongitud() != null
                            && negocio.getLatitud() != 0 && negocio.getLongitud() != 0) {

                        GeoPoint puntoNegocio = new GeoPoint(negocio.getLatitud(), negocio.getLongitud());
                        map.getController().setCenter(puntoNegocio);
                        map.getController().setZoom(16.0);

                        if (marcadorNegocio == null) {
                            marcadorNegocio = new Marker(map);
                            marcadorNegocio.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            Drawable icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_mylocation).mutate();
                            icon.setTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                            marcadorNegocio.setIcon(icon);
                            map.getOverlays().add(marcadorNegocio);
                        }

                        marcadorNegocio.setPosition(puntoNegocio);
                        marcadorNegocio.setTitle(negocio.getNomEmp());
                        map.invalidate();
                        Log.d("DEBUG_API", "✅ Pin del negocio dibujado en el mapa.");
                    } else {
                        Log.e("DEBUG_API", "❌ El negocio no tiene coordenadas válidas en la BD.");
                    }
                } else {
                    Log.e("DEBUG_API", "❌ Error del servidor en getNegocioById. Body nulo o error HTTP.");
                }
            }

            @Override
            public void onFailure(Call<NegocioDto> call, Throwable t) {
                Log.e("DEBUG_API", "🚨 Falla de red en getNegocioById: " + t.getMessage());
            }
        });
    }

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
        binding = null;
    }

    private void iniciarActualizacionAutomatica() {
        runnable = new Runnable() {
            @Override
            public void run() {
                obtenerDatosDelServidor();
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(runnable);
    }

    private void detenerActualizacion() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
    // --- LÓGICA PARA LOS CONTADORES (##) ---
    private void obtenerDatosDelServidor() {
        if (ID_LICENCIA == -1) return;

        Log.d("DEBUG_API", "➡️ Pidiendo repartidores para el negocio ID: " + ID_LICENCIA);
        RetrofitClient.getApiService().obtenerRepartidoresPorNegocio(ID_LICENCIA).enqueue(new Callback<List<UsuarioDto>>() {
            @Override
            public void onResponse(Call<List<UsuarioDto>> call, Response<List<UsuarioDto>> response) {
                Log.d("DEBUG_API", "⬅️ obtenerRepartidores respondió. ¿Éxito?: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DEBUG_API", "👥 Cantidad de repartidores recibidos: " + response.body().size());
                    actualizarContadores(response.body());
                }else {
                    try {
                        String errorServidor = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e("DEBUG_API", "❌ Error del servidor al pedir repartidores. Código HTTP: " + response.code() + ". Detalle: " + errorServidor);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onFailure(Call<List<UsuarioDto>> call, Throwable t) {
                Log.e("DEBUG_API", "🚨 Falla de red en obtenerRepartidores: " + t.getMessage());
            }
        });

        // (Aquí mantienes tu otra llamada getPedidosPorNegocio igual que antes)
    }

    private void actualizarMapa(List<PedidoDto> listaPedidos) {
        if (map == null) return;

        List<Integer> pedidosActivosActuales = new ArrayList<>();

        for (PedidoDto pedido : listaPedidos) {
            String estado = pedido.getEstadoReal() != null ? pedido.getEstadoReal().toUpperCase() : "";

            if (pedido.getLatitud() != null && pedido.getLongitud() != null
                    && pedido.getLatitud() != 0 && pedido.getLongitud() != 0
                    && (estado.equals("EN_CAMINO") || estado.equals("EN_CURSO") || estado.equals("ASIGNADO"))) {

                Integer idPedido = pedido.getNumOrd();
                pedidosActivosActuales.add(idPedido);
                GeoPoint punto = new GeoPoint(pedido.getLatitud(), pedido.getLongitud());

                if (marcadoresActivos.containsKey(idPedido)) {
                    Marker m = marcadoresActivos.get(idPedido);
                    m.setPosition(punto);
                    m.setSnippet("Repartidor: " + pedido.getNombreRepartidor());
                } else {
                    Marker m = new Marker(map);
                    m.setPosition(punto);
                    m.setTitle("Pedido #" + idPedido);
                    m.setSnippet("Repartidor: " + pedido.getNombreRepartidor());
                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    if(pedido.getIdRepartidor() != null) {
                        m.setIcon(obtenerIconoColoreado(pedido.getIdRepartidor()));
                    }

                    map.getOverlays().add(m);
                    marcadoresActivos.put(idPedido, m);
                }
            }
        }

        List<Integer> marcadoresAEliminar = new ArrayList<>();
        for (Integer idRegistrado : marcadoresActivos.keySet()) {
            if (!pedidosActivosActuales.contains(idRegistrado)) {
                Marker marcadorMuerto = marcadoresActivos.get(idRegistrado);
                map.getOverlays().remove(marcadorMuerto);
                marcadoresAEliminar.add(idRegistrado);
            }
        }

        for (Integer idParaBorrar : marcadoresAEliminar) {
            marcadoresActivos.remove(idParaBorrar);
        }

        map.invalidate();
    }

    private Drawable obtenerIconoColoreado(Integer id) {
        Drawable icono = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_mylocation).mutate();
        int colorHash = -16777216 + (id * 999999);
        icono.setTint(colorHash);
        return icono;
    }

    // --- NUEVA LÓGICA DE CONTADORES STRICTAMENTE FILTRADA ---
    private void actualizarContadores(List<UsuarioDto> listaRepartidores) {
        if (binding == null || listaRepartidores == null) return;

        int enServicioTotal = 0; // Disponible + Ocupado
        int disponibles = 0;
        int ocupados = 0; // Asumiremos que este es tu "En Entrega"
        int descanso = 0;
        int fueraServicio = 0;

        for (UsuarioDto u : listaRepartidores) {
            String estatus = u.getEstatus();
            if (estatus == null) estatus = "FUERA_SERVICIO";
            estatus = estatus.toUpperCase().trim();

            switch (estatus) {
                case "DISPONIBLE":
                    disponibles++;
                    enServicioTotal++;
                    break;
                case "OCUPADO":
                    ocupados++;
                    enServicioTotal++;
                    break;
                case "EN_DESCANSO":
                    descanso++;
                    break;
                case "FUERA_SERVICIO":
                default:
                    // Si viene un estado raro o nulo, por seguridad lo contamos como fuera de servicio
                    fueraServicio++;
                    break;
            }
        }

        binding.tvServicioCount.setText(String.valueOf(enServicioTotal));
        binding.tvFueraServicioCount.setText(String.valueOf(fueraServicio));
        binding.tvEnEntregaCount.setText(String.valueOf(ocupados));
        binding.tvDisponiblesCount.setText(String.valueOf(disponibles));
        binding.tvDescansoCount.setText(String.valueOf(descanso));
    }
}