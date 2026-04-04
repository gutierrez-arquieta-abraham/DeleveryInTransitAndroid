package com.PolicodeLabs.Delevery_In_Transit.ui.crear_pedido;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.annotation.Nullable;
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

    // <-- ¡Adiós al 1 quemado! Inicia en vacío por seguridad
    private int idLicencia = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_crear_pedido, container, false);

        etDireccion = root.findViewById(R.id.etDireccion);
        etDescripcion = root.findViewById(R.id.etDescripcion);
        etNombreCliente = root.findViewById(R.id.etNombreCliente);
        etTelefonoCliente = root.findViewById(R.id.etTelefonoCliente);
        btnCrear = root.findViewById(R.id.btnCrearPedido);

        btnCrear.setOnClickListener(v -> procesarPedido());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- LECTURA DE LA MEMORIA (SharedPreferences) ---
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        idLicencia = prefs.getInt("ID_LICENCIA", -1);
    }

    private void procesarPedido() {
        // Validación de seguridad: Asegurarnos de que el gestor tiene una licencia válida
        if (idLicencia == -1) {
            Toast.makeText(getContext(), "Error: No tienes un negocio asignado para crear pedidos.", Toast.LENGTH_LONG).show();
            return;
        }

        String direccion = etDireccion.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String nombreCliente = etNombreCliente.getText().toString().trim();
        String telefonoCliente = etTelefonoCliente.getText().toString().trim();

        if (direccion.isEmpty() || descripcion.isEmpty() || nombreCliente.isEmpty()) {
            Toast.makeText(getContext(), "Llena los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bloqueamos el botón temporalmente
        btnCrear.setEnabled(false);
        btnCrear.setText("Calculando ubicación...");

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
                // Restauramos el botón si falló
                btnCrear.setEnabled(true);
                btnCrear.setText("Crear Pedido");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al calcular la ubicación. Revisa tu conexión.", Toast.LENGTH_SHORT).show();
            btnCrear.setEnabled(true);
            btnCrear.setText("Crear Pedido");
            return;
        }

        PedidoRequest nuevoPedido = new PedidoRequest(
                descripcion,
                direccion,
                idLicencia,
                nombreCliente,
                telefonoCliente,
                latitudDestino,
                longitudDestino
        );

        subirPedido(nuevoPedido);
    }

    private void subirPedido(PedidoRequest nuevoPedido) {
        btnCrear.setText("Subiendo al servidor...");

        RetrofitClient.getApiService().crearPedido(nuevoPedido).enqueue(new Callback<PedidoDto>() {
            @Override
            public void onResponse(Call<PedidoDto> call, Response<PedidoDto> response) {
                // Restauramos el botón
                btnCrear.setEnabled(true);
                btnCrear.setText("Crear Pedido");

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
                // Restauramos el botón
                btnCrear.setEnabled(true);
                btnCrear.setText("Crear Pedido");
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}