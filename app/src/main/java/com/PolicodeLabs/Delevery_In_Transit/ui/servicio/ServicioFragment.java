package com.PolicodeLabs.Delevery_In_Transit.ui.servicio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.databinding.FragmentServicioBinding;
import com.PolicodeLabs.Delevery_In_Transit.model.UsuarioDto;
import com.PolicodeLabs.Delevery_In_Transit.workers.HorarioWorker;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServicioFragment extends Fragment implements View.OnClickListener {

    private FragmentServicioBinding binding;
    private Integer idUsuario;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentServicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Recuperar ID
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        idUsuario = prefs.getInt("ID_USUARIO", -1);

        // 2. Configurar Spinners (Solo pongo Lunes como ejemplo, repite para los demás)
        configurarSpinner(binding.spinnerLunesInicio, "Lunes_Inicio");
        configurarSpinner(binding.spinnerLunesDescanso, "Lunes_Descanso");
        configurarSpinner(binding.spinnerLunesFin, "Lunes_Fin");
        // ... Repite para Martes, Miércoles, etc.

        // 3. Listeners de Botones Manuales
        binding.buttonAvailable.setOnClickListener(this);
        binding.buttonOnBreak.setOnClickListener(this);
        binding.buttonOutOfService.setOnClickListener(this);

        // 4. INICIAR EL ROBOT AUTOMÁTICO (Solo una vez)
        iniciarWorker();
    }

    private void configurarSpinner(Spinner spinner, String clavePref) {
        // CAMBIO: Usamos createFromResource para leer el XML
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.lista_horarios, // <--- Aquí llamamos a la lista nueva
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Recuperar valor guardado
        SharedPreferences horariosPrefs = requireActivity().getSharedPreferences("HorariosPrefs", Context.MODE_PRIVATE);
        String valorGuardado = horariosPrefs.getString(clavePref, "Seleccionar");

        // Buscar posición
        int spinnerPosition = adapter.getPosition(valorGuardado);
        if (spinnerPosition >= 0) {
            spinner.setSelection(spinnerPosition);
        }
    }

    // Método para guardar todo cuando el usuario sale de la pantalla
    @Override
    public void onPause() {
        super.onPause();
        guardarHorarios();
    }

    private void guardarHorarios() {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences("HorariosPrefs", Context.MODE_PRIVATE).edit();

        // Guardar Lunes
        editor.putString("Lunes_Inicio", binding.spinnerLunesInicio.getSelectedItem().toString());
        editor.putString("Lunes_Descanso", binding.spinnerLunesDescanso.getSelectedItem().toString());
        editor.putString("Lunes_Fin", binding.spinnerLunesFin.getSelectedItem().toString());

        // ... Repite para los demás días ...

        editor.apply();
        // Toast.makeText(getContext(), "Horarios guardados", Toast.LENGTH_SHORT).show();
    }

    // --- LÓGICA BOTONES MANUALES ---
    @Override
    public void onClick(View v) {
        String nuevoEstado = "";

        if (v.getId() == binding.buttonAvailable.getId()) {
            nuevoEstado = "DISPONIBLE";
            cambiarColorBotones(binding.buttonAvailable);
        } else if (v.getId() == binding.buttonOnBreak.getId()) {
            nuevoEstado = "EN_DESCANSO";
            cambiarColorBotones(binding.buttonOnBreak);
        } else if (v.getId() == binding.buttonOutOfService.getId()) {
            nuevoEstado = "FUERA_SERVICIO";
            cambiarColorBotones(binding.buttonOutOfService);
        }

        cambiarEstatusEnServidor(nuevoEstado);
    }

    private void cambiarEstatusEnServidor(String estado) {
        if (idUsuario == -1) return;

        RetrofitClient.getApiService().actualizarEstatus(idUsuario, estado).enqueue(new Callback<UsuarioDto>() {
            @Override
            public void onResponse(Call<UsuarioDto> call, Response<UsuarioDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Estado actualizado: " + estado, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UsuarioDto> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cambiarColorBotones(View activo) {
        // Resetear todos a blanco
        binding.buttonAvailable.setBackgroundTintList(requireContext().getColorStateList(android.R.color.white));
        binding.buttonOnBreak.setBackgroundTintList(requireContext().getColorStateList(android.R.color.white));
        binding.buttonOutOfService.setBackgroundTintList(requireContext().getColorStateList(android.R.color.white));

        // Poner el activo en verde o color distintivo
        activo.setBackgroundTintList(requireContext().getColorStateList(android.R.color.holo_green_light));
    }

    private void iniciarWorker() {
        // Ejecutar cada 15 minutos (mínimo permitido por Android)
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(HorarioWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(requireContext()).enqueue(request);
    }
}