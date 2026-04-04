package com.PolicodeLabs.Delevery_In_Transit.ui.negocioconexion;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.databinding.FragmentConexionNegocioBinding;
import com.PolicodeLabs.Delevery_In_Transit.model.NegocioDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConexionNegocioFragment extends Fragment {

    private FragmentConexionNegocioBinding binding;
    private int idNegocioRecibido = -1; // Aquí guardaremos el ID que recibimos

    // --- NUEVO: Variable para el email (Temporalmente hardcoded, idealmente viene del Login) ---
    private String emailUsuario = "jefe@taqueria.com";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConexionNegocioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Intentamos recuperar ID por Bundle (si venimos de la pantalla anterior)
        if (getArguments() != null) {
            idNegocioRecibido = getArguments().getInt("ID_NEGOCIO", -1);
        }

        // 2. --- NUEVO: Carga automática por Email/RFC ---
        // Esto sobreescribirá el ID si encuentra el negocio en la base de datos
        cargarDatosDeMiNegocio();

        binding.btnGuardarCambios.setOnClickListener(v -> {
            if (validarCampos()) {
                enviarDatosAlServidor();
            }
        });
    }

    // --- NUEVO MÉTODO DE CARGA AUTOMÁTICA ---
    private void cargarDatosDeMiNegocio() {
        // Bloqueamos inputs y botón mientras carga
        binding.btnGuardarCambios.setEnabled(false);
        binding.btnGuardarCambios.setText("Buscando negocio...");

        RetrofitClient.getApiService().obtenerMiNegocio(emailUsuario).enqueue(new Callback<NegocioDto>() {
            @Override
            public void onResponse(Call<NegocioDto> call, Response<NegocioDto> response) {
                binding.btnGuardarCambios.setEnabled(true);
                binding.btnGuardarCambios.setText("Guardar cambios");

                if (response.isSuccessful() && response.body() != null) {
                    NegocioDto miNegocio = response.body();

                    // ¡ÉXITO! Ya tenemos el ID real desde la base de datos
                    idNegocioRecibido = miNegocio.getIdLicencia();

                    // Pre-llenamos los campos con lo que ya está en la BD
                    if (miNegocio.getNomEmp() != null) binding.etNombreNegocio.setText(miNegocio.getNomEmp());
                    if (miNegocio.getRfcEnc() != null) binding.etRFC.setText(miNegocio.getRfcEnc());
                    if (miNegocio.getDireccion() != null) binding.etDireccionNegocio.setText(miNegocio.getDireccion());
                    if (miNegocio.getZonaCobertura() != null) binding.etZonaCobertura.setText(String.valueOf(miNegocio.getZonaCobertura()));

                    Toast.makeText(getContext(), "Negocio vinculado: " + idNegocioRecibido, Toast.LENGTH_SHORT).show();
                } else {
                    // Si es 404, significa que aún no hay datos guardados para este usuario,
                    // pero esperamos que el idNegocioRecibido haya llegado por el Bundle anterior.
                    // Si ambos fallan, el usuario tendrá que asegurarse de que el flujo sea correcto.
                }
            }

            @Override
            public void onFailure(Call<NegocioDto> call, Throwable t) {
                binding.btnGuardarCambios.setEnabled(true);
                binding.btnGuardarCambios.setText("Guardar cambios");
                // No mostramos error invasivo aquí, quizás solo no tiene internet o no tiene negocio aún
            }
        });
    }

    // --- VALIDACIONES ---
    private boolean validarCampos() {
        boolean esValido = true;

        if (TextUtils.isEmpty(binding.etNombreNegocio.getText())) {
            binding.etNombreNegocio.setError("El nombre es obligatorio");
            esValido = false;
        }

        if (TextUtils.isEmpty(binding.etDireccionNegocio.getText())) {
            binding.etDireccionNegocio.setError("La dirección es obligatoria");
            esValido = false;
        }

        // Validación especial para Zona de Cobertura (Debe ser un número)
        String zonaTxt = binding.etZonaCobertura.getText().toString();
        if (TextUtils.isEmpty(zonaTxt)) {
            binding.etZonaCobertura.setError("Define tu zona en Km");
            esValido = false;
        } else {
            try {
                Integer.parseInt(zonaTxt);
            } catch (NumberFormatException e) {
                binding.etZonaCobertura.setError("Ingresa solo números enteros (ej: 15)");
                esValido = false;
            }
        }

        if (TextUtils.isEmpty(binding.etRFC.getText())) {
            binding.etRFC.setError("El RFC es obligatorio");
            esValido = false;
        }

        // Nota: Si tu backend no guarda password en esta tabla, esta validación es solo visual
        if (TextUtils.isEmpty(binding.etPassword.getText())) {
            binding.etPassword.setError("Confirma con tu contraseña");
            esValido = false;
        }

        return esValido;
    }

    // --- ENVÍO DE DATOS ---
    private void enviarDatosAlServidor() {
        if (idNegocioRecibido == -1) {
            Toast.makeText(getContext(), "Error: No se identificó el negocio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el DTO con los datos del formulario
        NegocioDto datosNegocio = new NegocioDto();
        datosNegocio.setNomEmp(binding.etNombreNegocio.getText().toString().trim());
        datosNegocio.setDireccion(binding.etDireccionNegocio.getText().toString().trim());

        // Convertimos el texto a Entero para enviarlo
        String zonaTexto = binding.etZonaCobertura.getText().toString().trim();
        datosNegocio.setZonaCobertura(Integer.parseInt(zonaTexto));

        datosNegocio.setRfcEnc(binding.etRFC.getText().toString().trim());

        // Deshabilitar botón visualmente
        binding.btnGuardarCambios.setEnabled(false);
        binding.btnGuardarCambios.setText("Guardando...");

        // Llamada a la API (PUT)
        RetrofitClient.getApiService().actualizarNegocio(idNegocioRecibido, datosNegocio).enqueue(new Callback<NegocioDto>() {
            @Override
            public void onResponse(Call<NegocioDto> call, Response<NegocioDto> response) {
                // Reactivar botón pase lo que pase
                binding.btnGuardarCambios.setEnabled(true);
                binding.btnGuardarCambios.setText("Guardar cambios");

                if (response.isSuccessful() && response.body() != null) {
                    NegocioDto respuesta = response.body();

                    Toast.makeText(getContext(), "¡Datos guardados!", Toast.LENGTH_SHORT).show();

                    // Navegar al último fragmento pasando el código de conexión generado
                    Bundle bundle = new Bundle();
                    bundle.putString("CODIGO_CONEXION", respuesta.getCodigoConexion());

                    NavController navController = Navigation.findNavController(binding.getRoot());
                    navController.navigate(R.id.action_datos_to_clave, bundle);

                } else {
                    Toast.makeText(getContext(), "Error al guardar. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NegocioDto> call, Throwable t) {
                binding.btnGuardarCambios.setEnabled(true);
                binding.btnGuardarCambios.setText("Guardar cambios");
                Toast.makeText(getContext(), "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}