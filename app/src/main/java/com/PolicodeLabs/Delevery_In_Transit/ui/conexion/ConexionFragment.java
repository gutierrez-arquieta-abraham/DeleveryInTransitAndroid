package com.PolicodeLabs.Delevery_In_Transit.ui.conexion;

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
import androidx.navigation.Navigation;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.databinding.FragmentConexionBinding;
import com.PolicodeLabs.Delevery_In_Transit.model.UsuarioDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConexionFragment extends Fragment {

    private FragmentConexionBinding binding;
    private Integer idUsuarioLogueado;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConexionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. RECUPERAR ID DEL REPARTIDOR (Guardado en LoginActivity)
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        idUsuarioLogueado = prefs.getInt("ID_USUARIO", -1);

        // Seguridad: Si no hay ID, bloquear
        if (idUsuarioLogueado == -1) {
            Toast.makeText(getContext(), "Error de sesión. Vuelve a entrar.", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. ESCUCHAR EL BOTÓN
        binding.buttonRegisterBusiness.setOnClickListener(v -> {
            String codigo = binding.editTextBusinessCode.getText().toString().trim();

            if (codigo.isEmpty()) {
                binding.editTextBusinessCode.setError("Escribe el código DIT");
                return;
            }
            conectarConNegocio(codigo);
        });
    }

    private void conectarConNegocio(String codigo) {
        binding.buttonRegisterBusiness.setEnabled(false);
        binding.buttonRegisterBusiness.setText("Conectando...");

        // 3. LLAMADA AL SERVIDOR
        RetrofitClient.getApiService().unirseAEquipo(idUsuarioLogueado, codigo).enqueue(new Callback<UsuarioDto>() {
            @Override
            public void onResponse(Call<UsuarioDto> call, Response<UsuarioDto> response) {
                // Verificar que la vista siga viva
                if (binding == null) return;

                binding.buttonRegisterBusiness.setEnabled(true);
                binding.buttonRegisterBusiness.setText("Regístrate");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "¡Conexión Exitosa!", Toast.LENGTH_LONG).show();

                    // 4. GUARDAR EN MEMORIA QUE YA TIENE JEFE (Para no pedirlo de nuevo)
                    requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("YA_TIENE_EQUIPO", true)
                            .apply();

                    // 5. NAVEGAR AL MAPA DE PEDIDOS
                    // Esta es la acción que corregimos en el XML
                    Navigation.findNavController(binding.getRoot())
                            .navigate(R.id.action_conexion_to_pedidos);

                } else {
                    Toast.makeText(getContext(), "Código incorrecto o no existe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UsuarioDto> call, Throwable t) {
                if (binding != null) {
                    binding.buttonRegisterBusiness.setEnabled(true);
                    binding.buttonRegisterBusiness.setText("Regístrate");
                }
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}