package com.PolicodeLabs.Delevery_In_Transit.ui.pedidos;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.PolicodeLabs.Delevery_In_Transit.adapters.PedidoRealizadoAdapter;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.databinding.FragmentPedidosBinding;
import com.PolicodeLabs.Delevery_In_Transit.model.Pedido;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;
import com.PolicodeLabs.Delevery_In_Transit.service.LocationService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PedidosFragment extends Fragment implements View.OnClickListener {

    private FragmentPedidosBinding binding;
    private PedidoRealizadoAdapter completedAdapter;
    private ArrayList<Pedido> listaPedidosRealizados = new ArrayList<>();
    private PedidoDto pedidoActualEnCurso = null;
    private Integer idRepartidor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPedidosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI Inicial
        binding.cardCurrentOrder.setVisibility(View.GONE);
        binding.layoutCurrentOrderDetails.setVisibility(View.GONE);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        idRepartidor = prefs.getInt("ID_USUARIO", -1);

        // Listeners para las tarjetas (implementados en onClick abajo)
        binding.cardCurrentOrder.setOnClickListener(this);
        binding.cardCompletedOrders.setOnClickListener(this);

        // --- LÓGICA DEL BOTÓN ---
        binding.btnMarcarEntregado.setOnClickListener(v -> {
            if (pedidoActualEnCurso != null && pedidoActualEnCurso.getNumOrd() != null) {
                marcarComoEntregado(pedidoActualEnCurso.getNumOrd());
            } else {
                Toast.makeText(getContext(), "⛔ Error: Pedido nulo", Toast.LENGTH_LONG).show();
            }
        });

        binding.tvCurrentOrderAddress.setOnClickListener(v -> {
            if (pedidoActualEnCurso != null) abrirGoogleMaps(pedidoActualEnCurso.getDestino());
        });

        binding.tvCurrentOrderAddress.setOnClickListener(v -> {
            if (pedidoActualEnCurso != null) abrirGoogleMaps(pedidoActualEnCurso.getDestino());
        });

        // Configuración Recycler
        binding.recyclerViewCompletedOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        completedAdapter = new PedidoRealizadoAdapter(listaPedidosRealizados);
        binding.recyclerViewCompletedOrders.setAdapter(completedAdapter);

        // Cargar Datos
        if (idRepartidor != -1) {
            cargarPedidoActivo();
            cargarHistorial();
        }

        iniciarServicioGPS();
    }

    private void iniciarServicioGPS() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent serviceIntent = new Intent(requireContext(), LocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(serviceIntent);
            } else {
                requireContext().startService(serviceIntent);
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    private void cargarPedidoActivo() {
        RetrofitClient.getApiService().obtenerMisPedidos(idRepartidor).enqueue(new Callback<List<PedidoDto>>() {
            @Override
            public void onResponse(Call<List<PedidoDto>> call, Response<List<PedidoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    procesarActivos(response.body());
                } else {
                    actualizarVistaPedidoActual(false);
                }
            }
            @Override
            public void onFailure(Call<List<PedidoDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error cargando activos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procesarActivos(List<PedidoDto> pedidos) {
        pedidoActualEnCurso = null;
        boolean hayEnCurso = false;

        for (PedidoDto p : pedidos) {
            // CORRECCIÓN: Usar getEstadoReal() en lugar de getEstatus()
            String est = p.getEstadoReal() != null ? p.getEstadoReal().toUpperCase() : "";
            if (est.equals("EN_CURSO") || est.equals("EN_CAMINO") || est.equals("ASIGNADO")) {
                llenarPedidoActual(p);
                hayEnCurso = true;
                break;
            }
        }
        actualizarVistaPedidoActual(hayEnCurso);
    }

    private void cargarHistorial() {
        RetrofitClient.getApiService().obtenerHistorial(idRepartidor).enqueue(new Callback<List<PedidoDto>>() {
            @Override
            public void onResponse(Call<List<PedidoDto>> call, Response<List<PedidoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PedidoDto> listaDtos = response.body();
                    listaPedidosRealizados.clear();

                    for (PedidoDto dto : listaDtos) {
                        Pedido p = new Pedido();
                        p.setId(dto.getNumOrd());
                        p.setDescripcion(dto.getDescripcion());

                        // ✅ CAMBIO AQUÍ: Usamos la dirección real en lugar de coordenadas
                        p.setDireccionEntrega(dto.getDestino());

                        p.setEstatus(dto.getEstadoReal());
                        p.setNombreNegocio(dto.getNombreNegocio());
                        p.setFechaEntrega(dto.getFechaHoraEntrega());
                        p.setHoraEntrega(dto.getFechaHoraEntrega());
                        p.setNombreCliente(dto.getNombreNegocio());

                        listaPedidosRealizados.add(p);
                    }

                    completedAdapter.notifyDataSetChanged();

                    // Mostrar lista automáticamente si hay datos
                    if (!listaPedidosRealizados.isEmpty()) {
                        binding.recyclerViewCompletedOrders.setVisibility(View.VISIBLE);
                    } else {
                        binding.recyclerViewCompletedOrders.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<PedidoDto>> call, Throwable t) {
                // Log.e("HISTORIAL", "Error: " + t.getMessage());
            }
        });
    }

    private void actualizarVistaPedidoActual(boolean hayPedido) {
        if (hayPedido) {
            binding.cardCurrentOrder.setVisibility(View.VISIBLE);
            binding.layoutCurrentOrderDetails.setVisibility(View.VISIBLE);
        } else {
            binding.cardCurrentOrder.setVisibility(View.GONE);
        }
    }

    // CORRECCIÓN: El parámetro ahora es PedidoDto
    private void llenarPedidoActual(PedidoDto p) {
        this.pedidoActualEnCurso = p;
        // CORRECCIÓN: Usar getDestino()
        binding.tvCurrentOrderAddress.setText(p.getDestino());
        String desc = p.getDescripcion() != null ? p.getDescripcion() : "Sin descripción";
        binding.tvCurrentOrderDescription.setText(desc);
        binding.tvCurrentOrderStatus.setText("En Camino");

        binding.btnMarcarEntregado.setEnabled(true);
        binding.btnMarcarEntregado.setText("Marcar como Entregado");
    }

    private void marcarComoEntregado(Integer idPedido) {
        binding.btnMarcarEntregado.setEnabled(false);
        binding.btnMarcarEntregado.setText("Finalizando...");

        RetrofitClient.getApiService().actualizarEstadoPedido(idPedido, "ENTREGADO").enqueue(new Callback<PedidoDto>() {
            @Override
            public void onResponse(Call<PedidoDto> call, Response<PedidoDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "✅ ¡Entrega registrada!", Toast.LENGTH_LONG).show();
                    cargarPedidoActivo();
                    cargarHistorial();
                } else {
                    binding.btnMarcarEntregado.setEnabled(true);
                    binding.btnMarcarEntregado.setText("Marcar como Entregado");
                    Toast.makeText(getContext(), "Error del servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PedidoDto> call, Throwable t) {
                binding.btnMarcarEntregado.setEnabled(true);
                binding.btnMarcarEntregado.setText("Marcar como Entregado");
                Toast.makeText(getContext(), "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirGoogleMaps(String direccion) {
        if (direccion == null || direccion.isEmpty()) return;
        try {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(direccion));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        } catch (Exception e) {}
    }

    // ✅ CORRECCIÓN FINAL: El onClick SOLO maneja las tarjetas
    @Override
    public void onClick(View v) {
        if (v.getId() == binding.cardCurrentOrder.getId()) {
            toggleVisibility(binding.layoutCurrentOrderDetails);
        } else if (v.getId() == binding.cardCompletedOrders.getId()) {
            toggleVisibility(binding.recyclerViewCompletedOrders);
        }
    }

    private void toggleVisibility(View view) {
        view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
}