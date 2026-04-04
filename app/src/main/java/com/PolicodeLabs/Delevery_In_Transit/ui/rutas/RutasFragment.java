package com.PolicodeLabs.Delevery_In_Transit.ui.rutas;

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
import com.PolicodeLabs.Delevery_In_Transit.adapters.PedidoRealizadoAdapter;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.model.Pedido;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RutasFragment extends Fragment {

    private RecyclerView recyclerView;
    private PedidoRealizadoAdapter adapter; // 👈 Cambiado a PedidoRealizadoAdapter
    private List<PedidoDto> listaHistorial = new ArrayList<>(); // Lista para el adapter
    private int idLicencia = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rutas, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewRutasCompletadas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializamos el adapter correcto
        adapter = new PedidoRealizadoAdapter(listaHistorial, numOrd -> {
            Toast.makeText(getContext(), "Pronto: Estadísticas de #" + numOrd, Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        cargarHistorialReal();

        return root;
    }

    private void cargarHistorialReal() {
        RetrofitClient.getApiService().obtenerHistorialNegocio(idLicencia).enqueue(new Callback<List<PedidoDto>>() {
            @Override
            public void onResponse(Call<List<PedidoDto>> call, Response<List<PedidoDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaHistorial.clear();
                    listaHistorial.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<List<PedidoDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}