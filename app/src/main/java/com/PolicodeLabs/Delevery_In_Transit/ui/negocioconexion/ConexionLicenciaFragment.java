package com.PolicodeLabs.Delevery_In_Transit.ui.negocioconexion;

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
    private String emailUsuarioReal;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConexionLicenciaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- LECTURA DEL CORREO REAL ---
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        emailUsuarioReal = prefs.getString("EMAIL_USUARIO", "");

        if (emailUsuarioReal.isEmpty()) {
            Toast.makeText(getContext(), "Error: No se encontró el correo del usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        verificarEstadoDelUsuario();

        binding.btnRegistrarLicencia.setOnClickListener(v -> {
            String codigoIngresado = binding.etCodigoLicencia.getText().toString().trim();
            if (codigoIngresado.isEmpty()) {
                binding.etCodigoLicencia.setError("Escribe el código de licencia");
            } else {
                validarLicenciaManual(codigoIngresado);
            }
        });
    }

    private void verificarEstadoDelUsuario() {
        binding.btnRegistrarLicencia.setEnabled(false);
        binding.btnRegistrarLicencia.setText("Cargando...");

        RetrofitClient.getApiService().obtenerMiNegocio(emailUsuarioReal).enqueue(new Callback<NegocioDto>() {
            @Override
            public void onResponse(Call<NegocioDto> call, Response<NegocioDto> response) {
                if (binding != null) {
                    binding.btnRegistrarLicencia.setEnabled(true);
                    binding.btnRegistrarLicencia.setText("Registrar");
                }

                if (response.isSuccessful() && response.body() != null) {
                    NegocioDto negocio = response.body();
                    NavController navController = Navigation.findNavController(binding.getRoot());

                    // 👇 EL FIX: Guardamos el ID real en memoria 👇
                    requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
                            .edit()
                            .putInt("ID_LICENCIA", negocio.getIdLicencia())
                            .apply();

                    // CASO A: Registro COMPLETO
                    if (negocio.getCodigoConexion() != null && !negocio.getCodigoConexion().isEmpty()) {
                        Toast.makeText(getContext(), "Sesión recuperada: " + negocio.getNomEmp(), Toast.LENGTH_SHORT).show();
                        Bundle bundle = new Bundle();
                        bundle.putString("CODIGO_CONEXION", negocio.getCodigoConexion());
                        navController.navigate(R.id.action_licencia_to_clave, bundle);
                    }
                    // CASO B: Registro INCOMPLETO
                    else {
                        Bundle bundle = new Bundle();
                        bundle.putInt("ID_NEGOCIO", negocio.getIdLicencia());
                        navController.navigate(R.id.action_licencia_to_datos, bundle);
                    }
                }
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

    private void validarLicenciaManual(String codigoString) {
        binding.btnRegistrarLicencia.setEnabled(false);
        binding.btnRegistrarLicencia.setText("Verificando...");

        RetrofitClient.getApiService().validarCodigoLicencia(codigoString).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (binding == null) return;

                binding.btnRegistrarLicencia.setEnabled(true);
                binding.btnRegistrarLicencia.setText("Registrar");

                if (response.isSuccessful() && response.body() != null) {
                    int idNegocio = response.body();

                    // 👇 EL FIX: Guardamos el ID real en memoria también aquí 👇
                    requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
                            .edit()
                            .putInt("ID_LICENCIA", idNegocio)
                            .apply();

                    Toast.makeText(getContext(), "Licencia Válida", Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putInt("ID_NEGOCIO", idNegocio);
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.action_licencia_to_datos, bundle);
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