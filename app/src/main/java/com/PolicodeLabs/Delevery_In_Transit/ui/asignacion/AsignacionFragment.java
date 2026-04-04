package com.PolicodeLabs.Delevery_In_Transit.ui.asignacion;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.adapters.PedidosAdapter;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;
import com.PolicodeLabs.Delevery_In_Transit.model.UsuarioResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AsignacionFragment extends Fragment {

    private RecyclerView recyclerView;
    private PedidosAdapter adapter;
    private int idLicencia = 1; // ID por defecto (o obténlo del Intent si prefieres)

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Usamos el layout que tiene el RecyclerView (no el de los Spinners)
        View root = inflater.inflate(R.layout.fragment_asignacion, container, false);

        // Vinculamos el RecyclerView (Asegúrate que en tu XML el ID sea recyclerPedidosAsignacion)
        recyclerView = root.findViewById(R.id.recyclerPedidosAsignacion);

        // Si recyclerView es null, es porque tu XML todavía tiene Spinners. ¡Cámbialo!
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            cargarPedidosPendientes();
        }

        return root;
    }

    private void cargarPedidosPendientes() {
        RetrofitClient.getApiService().getPedidosPorNegocio(idLicencia).enqueue(new Callback<List<PedidoDto>>() {
            @Override
            public void onResponse(Call<List<PedidoDto>> call, Response<List<PedidoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PedidoDto> todos = response.body();
                    List<PedidoDto> pendientes = new ArrayList<>();

                    for (PedidoDto p : todos) {
                        if ("PENDIENTE".equalsIgnoreCase(p.getEstadoReal()) || "PENDIENTE".equalsIgnoreCase(p.getEstadoReal())) {
                            pendientes.add(p);
                        }
                    }

                    adapter = new PedidosAdapter(pendientes, new PedidosAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(PedidoDto pedido) {
                            mostrarElegirRepartidor(pedido);
                        }
                    });
                    recyclerView.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<PedidoDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarElegirRepartidor(PedidoDto pedido) {
        RetrofitClient.getApiService().getRepartidoresPorNegocio(idLicencia).enqueue(new Callback<List<UsuarioResponse>>() {
            @Override
            public void onResponse(Call<List<UsuarioResponse>> call, Response<List<UsuarioResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UsuarioResponse> repartidores = response.body();
                    String[] nombres = new String[repartidores.size()];
                    for (int i = 0; i < repartidores.size(); i++) {
                        nombres[i] = repartidores.get(i).getNombre();
                    }

                    new AlertDialog.Builder(getContext())
                            .setTitle("Asignar Pedido #" + pedido.getNumOrd())
                            .setItems(nombres, (dialog, which) -> {
                                int idRepartidor = repartidores.get(which).getId();
                                ejecutarAsignacion(pedido.getNumOrd(), idRepartidor);
                            })
                            .show();
                }
            }
            @Override public void onFailure(Call<List<UsuarioResponse>> call, Throwable t) {}
        });
    }

    private void ejecutarAsignacion(int numOrd, int idRepartidor) {
        RetrofitClient.getApiService().asignarPedido(numOrd, idRepartidor).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Asignado!", Toast.LENGTH_SHORT).show();
                    cargarPedidosPendientes(); // Recargar lista
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}