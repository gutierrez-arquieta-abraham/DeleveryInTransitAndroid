package com.PolicodeLabs.Delevery_In_Transit.ui.crear_pedido;

import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearPedidoFragment extends Fragment {

    private EditText etDireccion, etDescripcion, etNombreCliente, etTelefonoCliente;
    private Button btnCrear;
    private int idLicencia = 1; // Tu negocio por defecto

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_crear_pedido, container, false);

        etDireccion = root.findViewById(R.id.etDireccion);
        etDescripcion = root.findViewById(R.id.etDescripcion);
        // Asegúrate de definir estos IDs en tu XML
        etNombreCliente = root.findViewById(R.id.etNombreCliente);
        etTelefonoCliente = root.findViewById(R.id.etTelefonoCliente);
        btnCrear = root.findViewById(R.id.btnCrearPedido);

        btnCrear.setOnClickListener(v -> procesarPedido());

        return root;
    }

    private void procesarPedido() {
        String direccion = etDireccion.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String nombreCliente = etNombreCliente.getText().toString().trim();
        String telefonoCliente = etTelefonoCliente.getText().toString().trim();

        if (direccion.isEmpty() || descripcion.isEmpty() || nombreCliente.isEmpty()) {
            Toast.makeText(getContext(), "Llena los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- MAGIA DEL GEOCODER ---
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        double latitudDestino = 0.0;
        double longitudDestino = 0.0;

        try {
            List<Address> direcciones = geocoder.getFromLocationName(direccion, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                Address ubicacion = direcciones.get(0);
                latitudDestino = ubicacion.getLatitude();
                longitudDestino = ubicacion.getLongitude();
            } else {
                Toast.makeText(getContext(), "No se encontraron coordenadas para esta dirección", Toast.LENGTH_LONG).show();
                return; // Detenemos el envío si la dirección no es válida
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al calcular la ubicación. Revisa tu conexión.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Crear el objeto con los datos (Asegúrate de que el constructor de PedidoRequest acepte estos nuevos parámetros)
        PedidoRequest nuevoPedido = new PedidoRequest(
                descripcion,
                direccion,
                idLicencia,
                nombreCliente,
                telefonoCliente,
                latitudDestino,
                longitudDestino
        );

        // 2. Enviar al Backend
        subirPedido(nuevoPedido);
    }

    private void subirPedido(PedidoRequest nuevoPedido) {
        RetrofitClient.getApiService().crearPedido(nuevoPedido).enqueue(new Callback<PedidoDto>() {
            @Override
            public void onResponse(Call<PedidoDto> call, Response<PedidoDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "¡Pedido creado exitosamente!", Toast.LENGTH_LONG).show();
                    etDireccion.setText("");
                    etDescripcion.setText("");
                    etNombreCliente.setText("");
                    etTelefonoCliente.setText("");
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