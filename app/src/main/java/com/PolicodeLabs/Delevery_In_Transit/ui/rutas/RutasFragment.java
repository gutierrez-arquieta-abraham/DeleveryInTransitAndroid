package com.PolicodeLabs.Delevery_In_Transit.ui.rutas;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.adapters.PedidoRealizadoAdapter;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RutasFragment extends Fragment {

    private RecyclerView recyclerView;
    private PedidoRealizadoAdapter adapter;
    private List<PedidoDto> listaHistorial = new ArrayList<>();

    // <-- Inicia en -1 por seguridad
    private int idLicencia = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rutas, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewRutasCompletadas);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PedidoRealizadoAdapter(listaHistorial, numOrd -> {
            Toast.makeText(getContext(), "Pronto: Estadísticas de #" + numOrd, Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- LECTURA DE LA MEMORIA (SharedPreferences) ---
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        idLicencia = prefs.getInt("ID_LICENCIA", -1);

        // Solo cargamos el historial si el gestor tiene una licencia válida
        if (idLicencia != -1) {
            cargarHistorialReal();
        } else {
            Toast.makeText(getContext(), "Error: No tienes un negocio asignado", Toast.LENGTH_LONG).show();
        }
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
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}