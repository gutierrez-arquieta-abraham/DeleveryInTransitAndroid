package com.PolicodeLabs.Delevery_In_Transit.ui.negocioconexion;

import android.content.Context;
import android.content.SharedPreferences;
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
    private int idNegocioRecibido = -1;
    private String emailUsuarioReal;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConexionNegocioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        emailUsuarioReal = prefs.getString("EMAIL_USUARIO", "");

        if (getArguments() != null) {
            idNegocioRecibido = getArguments().getInt("ID_NEGOCIO", -1);
        }

        if (!emailUsuarioReal.isEmpty()) {
            cargarDatosDeMiNegocio();
        } else {
            Toast.makeText(getContext(), "Precaución: No se pudo verificar tu correo", Toast.LENGTH_SHORT).show();
        }

        binding.btnGuardarCambios.setOnClickListener(v -> {
            if (validarCampos()) {
                enviarDatosAlServidor();
            }
        });
    }

    private void cargarDatosDeMiNegocio() {
        binding.btnGuardarCambios.setEnabled(false);
        binding.btnGuardarCambios.setText("Buscando negocio...");

        RetrofitClient.getApiService().obtenerMiNegocio(emailUsuarioReal).enqueue(new Callback<NegocioDto>() {
            @Override
            public void onResponse(Call<NegocioDto> call, Response<NegocioDto> response) {
                binding.btnGuardarCambios.setEnabled(true);
                binding.btnGuardarCambios.setText("Guardar cambios");

                if (response.isSuccessful() && response.body() != null) {
                    NegocioDto miNegocio = response.body();

                    idNegocioRecibido = miNegocio.getIdLicencia();

                    if (miNegocio.getNomEmp() != null) binding.etNombreNegocio.setText(miNegocio.getNomEmp());
                    if (miNegocio.getRfcEnc() != null) binding.etRFC.setText(miNegocio.getRfcEnc());
                    if (miNegocio.getDireccion() != null) binding.etDireccionNegocio.setText(miNegocio.getDireccion());
                    if (miNegocio.getZonaCobertura() != null) binding.etZonaCobertura.setText(String.valueOf(miNegocio.getZonaCobertura()));
                }
            }

            @Override
            public void onFailure(Call<NegocioDto> call, Throwable t) {
                binding.btnGuardarCambios.setEnabled(true);
                binding.btnGuardarCambios.setText("Guardar cambios");
            }
        });
    }

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

        if (TextUtils.isEmpty(binding.etPassword.getText())) {
            binding.etPassword.setError("Confirma con tu contraseña");
            esValido = false;
        }

        return esValido;
    }

    private void enviarDatosAlServidor() {
        if (idNegocioRecibido == -1) {
            Toast.makeText(getContext(), "Error: No se identificó el negocio", Toast.LENGTH_SHORT).show();
            return;
        }

        NegocioDto datosNegocio = new NegocioDto();
        datosNegocio.setNomEmp(binding.etNombreNegocio.getText().toString().trim());
        datosNegocio.setDireccion(binding.etDireccionNegocio.getText().toString().trim());

        String zonaTexto = binding.etZonaCobertura.getText().toString().trim();
        datosNegocio.setZonaCobertura(Integer.parseInt(zonaTexto));
        datosNegocio.setRfcEnc(binding.etRFC.getText().toString().trim());

        binding.btnGuardarCambios.setEnabled(false);
        binding.btnGuardarCambios.setText("Guardando...");

        RetrofitClient.getApiService().actualizarNegocio(idNegocioRecibido, datosNegocio).enqueue(new Callback<NegocioDto>() {
            @Override
            public void onResponse(Call<NegocioDto> call, Response<NegocioDto> response) {
                binding.btnGuardarCambios.setEnabled(true);
                binding.btnGuardarCambios.setText("Guardar cambios");

                if (response.isSuccessful() && response.body() != null) {
                    NegocioDto respuesta = response.body();

                    // 👇 EL FIX: Grabamos el ID en memoria para liberar el resto de la app 👇
                    requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
                            .edit()
                            .putInt("ID_LICENCIA", idNegocioRecibido)
                            .apply();

                    Toast.makeText(getContext(), "¡Datos guardados!", Toast.LENGTH_SHORT).show();

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