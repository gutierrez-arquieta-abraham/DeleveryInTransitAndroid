package com.PolicodeLabs.Delevery_In_Transit.ui.crear_pedido; // O donde quieras ponerlo

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearPedidoFragment extends Fragment {

    private EditText etDireccion, etDescripcion;
    private Button btnCrear;
    private int idLicencia = 1; // Tu negocio por defecto

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_crear_pedido, container, false);

        etDireccion = root.findViewById(R.id.etDireccion);
        etDescripcion = root.findViewById(R.id.etDescripcion);
        btnCrear = root.findViewById(R.id.btnCrearPedido);

        btnCrear.setOnClickListener(v -> subirPedido());

        return root;
    }

    private void subirPedido() {
        String direccion = etDireccion.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        if (direccion.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(getContext(), "Llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Crear el objeto con los datos
        PedidoRequest nuevoPedido = new PedidoRequest(descripcion, direccion, idLicencia);

        // 2. Enviar al Backend
        RetrofitClient.getApiService().crearPedido(nuevoPedido).enqueue(new Callback<PedidoDto>() {
            @Override
            public void onResponse(Call<PedidoDto> call, Response<PedidoDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Pedido creado exitosamente!", Toast.LENGTH_LONG).show();
                    // Limpiar campos
                    etDireccion.setText("");
                    etDescripcion.setText("");
                    // Opcional: Navegar a la lista de pedidos
                } else {
                    Toast.makeText(getContext(), "Error al subir pedido", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PedidoDto> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}