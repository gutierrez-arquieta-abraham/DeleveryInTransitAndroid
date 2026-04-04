package com.PolicodeLabs.Delevery_In_Transit.ui.negocioconexion;

import android.os.Bundle;
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
import com.PolicodeLabs.Delevery_In_Transit.databinding.FragmentConexionLicenciaBinding;
import com.PolicodeLabs.Delevery_In_Transit.model.NegocioDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConexionLicenciaFragment extends Fragment {

    private FragmentConexionLicenciaBinding binding;

    // TODO: Obtener dinámicamente del Login. Por ahora usamos el del Jefe Diego.
    private String emailUsuario = "jefe@taqueria.com";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConexionLicenciaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. INTENTO DE ACCESO RÁPIDO (AUTOMÁTICO)
        verificarEstadoDelUsuario();

        // 2. ACCESO MANUAL (BOTÓN)
        binding.btnRegistrarLicencia.setOnClickListener(v -> {
            String codigoIngresado = binding.etCodigoLicencia.getText().toString().trim();
            if (codigoIngresado.isEmpty()) {
                binding.etCodigoLicencia.setError("Escribe el código de licencia");
            } else {
                validarLicenciaManual(codigoIngresado);
            }
        });
    }

    // --- LÓGICA AUTOMÁTICA (BÚSQUEDA SILENCIOSA) ---
    private void verificarEstadoDelUsuario() {
        // Deshabilitamos botón mientras verifica para evitar conflictos
        binding.btnRegistrarLicencia.setEnabled(false);
        binding.btnRegistrarLicencia.setText("Cargando...");

        RetrofitClient.getApiService().obtenerMiNegocio(emailUsuario).enqueue(new Callback<NegocioDto>() {
            @Override
            public void onResponse(Call<NegocioDto> call, Response<NegocioDto> response) {
                // Restauramos UI por si el usuario se queda aquí
                if (binding != null) {
                    binding.btnRegistrarLicencia.setEnabled(true);
                    binding.btnRegistrarLicencia.setText("Registrar");
                }

                if (response.isSuccessful() && response.body() != null) {
                    NegocioDto negocio = response.body();
                    NavController navController = Navigation.findNavController(binding.getRoot());

                    // CASO A: Registro COMPLETO (Ya tiene código -NG) -> Ir directo a Clave
                    if (negocio.getCodigoConexion() != null && !negocio.getCodigoConexion().isEmpty()) {
                        Toast.makeText(getContext(), "Sesión recuperada: " + negocio.getNomEmp(), Toast.LENGTH_SHORT).show();

                        Bundle bundle = new Bundle();
                        bundle.putString("CODIGO_CONEXION", negocio.getCodigoConexion());

                        navController.navigate(R.id.action_licencia_to_clave, bundle);
                    }
                    // CASO B: Registro INCOMPLETO (Tiene ID pero faltan datos) -> Ir a Datos
                    else {
                        Bundle bundle = new Bundle();
                        bundle.putInt("ID_NEGOCIO", negocio.getIdLicencia());
                        navController.navigate(R.id.action_licencia_to_datos, bundle);
                    }
                }
                // CASO C: (404) Usuario nuevo -> Se queda aquí para ingresar licencia manualmente
            }

            @Override
            public void onFailure(Call<NegocioDto> call, Throwable t) {
                if (binding != null) {
                    binding.btnRegistrarLicencia.setEnabled(true);
                    binding.btnRegistrarLicencia.setText("Registrar");
                }
            }
        });
    }

    // --- LÓGICA MANUAL (POR SI NO TIENE NEGOCIO VINCULADO AÚN) ---
    private void validarLicenciaManual(String codigoString) {
        binding.btnRegistrarLicencia.setEnabled(false);
        binding.btnRegistrarLicencia.setText("Verificando...");

        // Usamos el endpoint que devuelve Integer (ID)
        RetrofitClient.getApiService().validarCodigoLicencia(codigoString).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (binding == null) return;

                binding.btnRegistrarLicencia.setEnabled(true);
                binding.btnRegistrarLicencia.setText("Registrar");

                if (response.isSuccessful() && response.body() != null) {
                    int idNegocio = response.body();

                    Toast.makeText(getContext(), "Licencia Válida", Toast.LENGTH_SHORT).show();

                    Bundle bundle = new Bundle();
                    bundle.putInt("ID_NEGOCIO", idNegocio);

                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.action_licencia_to_datos, bundle);

                } else {
                    binding.etCodigoLicencia.setError("Licencia no válida o no encontrada");
                    Toast.makeText(getContext(), "Verifica tu código DIT", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                if (binding != null) {
                    binding.btnRegistrarLicencia.setEnabled(true);
                    binding.btnRegistrarLicencia.setText("Registrar");
                    Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}