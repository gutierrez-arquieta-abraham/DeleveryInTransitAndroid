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
    private ArrayAdapter<CharSequence> adapter; // Adaptador global

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentServicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Recuperar ID del Repartidor
        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        idUsuario = prefs.getInt("ID_USUARIO", -1);

        // 2. Inicializar los 18 Spinners de golpe
        inicializarTodosLosSpinners();

        // 3. Cargar los horarios guardados para todos los días
        cargarHorariosGuardados();

        // 4. Listeners de Botones Manuales
        binding.buttonAvailable.setOnClickListener(this);
        binding.buttonOnBreak.setOnClickListener(this);
        binding.buttonOutOfService.setOnClickListener(this);

        // 5. INICIAR EL ROBOT AUTOMÁTICO (Solo una vez)
        iniciarWorker();
    }

    private void inicializarTodosLosSpinners() {
        // Creamos el adaptador UNA sola vez con el diseño negro
        adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.lista_horarios,
                R.layout.spinner_item_black);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Arreglo con los 18 Spinners (Lunes a Domingo, omitiendo el Viernes que no está en tu XML)
        Spinner[] todosLosSpinners = {
                binding.spinnerLunesInicio, binding.spinnerMartesInicio, binding.spinnerMiercolesInicio,
                binding.spinnerJuevesInicio, binding.spinnerViernesInicio, binding.spinnerSabadoInicio, binding.spinnerDomingoInicio,

                binding.spinnerLunesDescanso, binding.spinnerMartesDescanso, binding.spinnerMiercolesDescanso,
                binding.spinnerJuevesDescanso, binding.spinnerViernesDescanso, binding.spinnerSabadoDescanso, binding.spinnerDomingoDescanso,

                binding.spinnerLunesFin, binding.spinnerMartesFin, binding.spinnerMiercolesFin,
                binding.spinnerJuevesFin, binding.spinnerViernesFin, binding.spinnerSabadoFin, binding.spinnerDomingoFin
        };

        // Asignamos el adaptador a todos rápidamente
        for (Spinner spinner : todosLosSpinners) {
            spinner.setAdapter(adapter);
        }
    }

    private void cargarHorariosGuardados() {
        // Lunes
        seleccionarValorGuardado(binding.spinnerLunesInicio, "Lunes_Inicio");
        seleccionarValorGuardado(binding.spinnerLunesDescanso, "Lunes_Descanso");
        seleccionarValorGuardado(binding.spinnerLunesFin, "Lunes_Fin");
        // Martes
        seleccionarValorGuardado(binding.spinnerMartesInicio, "Martes_Inicio");
        seleccionarValorGuardado(binding.spinnerMartesDescanso, "Martes_Descanso");
        seleccionarValorGuardado(binding.spinnerMartesFin, "Martes_Fin");
        // Miércoles
        seleccionarValorGuardado(binding.spinnerMiercolesInicio, "Miercoles_Inicio");
        seleccionarValorGuardado(binding.spinnerMiercolesDescanso, "Miercoles_Descanso");
        seleccionarValorGuardado(binding.spinnerMiercolesFin, "Miercoles_Fin");
        // Jueves
        seleccionarValorGuardado(binding.spinnerJuevesInicio, "Jueves_Inicio");
        seleccionarValorGuardado(binding.spinnerJuevesDescanso, "Jueves_Descanso");
        seleccionarValorGuardado(binding.spinnerJuevesFin, "Jueves_Fin");
        // Viernes
        seleccionarValorGuardado(binding.spinnerViernesInicio, "Viernes_Inicio");
        seleccionarValorGuardado(binding.spinnerViernesDescanso, "Viernes_Descanso");
        seleccionarValorGuardado(binding.spinnerViernesFin, "Viernes_Fin");
        // Sábado
        seleccionarValorGuardado(binding.spinnerSabadoInicio, "Sabado_Inicio");
        seleccionarValorGuardado(binding.spinnerSabadoDescanso, "Sabado_Descanso");
        seleccionarValorGuardado(binding.spinnerSabadoFin, "Sabado_Fin");
        // Domingo
        seleccionarValorGuardado(binding.spinnerDomingoInicio, "Domingo_Inicio");
        seleccionarValorGuardado(binding.spinnerDomingoDescanso, "Domingo_Descanso");
        seleccionarValorGuardado(binding.spinnerDomingoFin, "Domingo_Fin");
    }

    private void seleccionarValorGuardado(Spinner spinner, String clavePref) {
        SharedPreferences horariosPrefs = requireActivity().getSharedPreferences("HorariosPrefs", Context.MODE_PRIVATE);
        String valorGuardado = horariosPrefs.getString(clavePref, "Seleccionar");

        if (adapter != null) {
            int spinnerPosition = adapter.getPosition(valorGuardado);
            if (spinnerPosition >= 0) {
                spinner.setSelection(spinnerPosition);
            }
        }
    }

    // Guardar TODO cuando el usuario sale de la pantalla
    @Override
    public void onPause() {
        super.onPause();
        guardarHorarios();
    }

    private void guardarHorarios() {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences("HorariosPrefs", Context.MODE_PRIVATE).edit();

        // Lunes
        editor.putString("Lunes_Inicio", binding.spinnerLunesInicio.getSelectedItem().toString());
        editor.putString("Lunes_Descanso", binding.spinnerLunesDescanso.getSelectedItem().toString());
        editor.putString("Lunes_Fin", binding.spinnerLunesFin.getSelectedItem().toString());
        // Martes
        editor.putString("Martes_Inicio", binding.spinnerMartesInicio.getSelectedItem().toString());
        editor.putString("Martes_Descanso", binding.spinnerMartesDescanso.getSelectedItem().toString());
        editor.putString("Martes_Fin", binding.spinnerMartesFin.getSelectedItem().toString());
        // Miércoles
        editor.putString("Miercoles_Inicio", binding.spinnerMiercolesInicio.getSelectedItem().toString());
        editor.putString("Miercoles_Descanso", binding.spinnerMiercolesDescanso.getSelectedItem().toString());
        editor.putString("Miercoles_Fin", binding.spinnerMiercolesFin.getSelectedItem().toString());
        // Jueves
        editor.putString("Jueves_Inicio", binding.spinnerJuevesInicio.getSelectedItem().toString());
        editor.putString("Jueves_Descanso", binding.spinnerJuevesDescanso.getSelectedItem().toString());
        editor.putString("Jueves_Fin", binding.spinnerJuevesFin.getSelectedItem().toString());
        // Viernes
        editor.putString("Viernes_Inicio", binding.spinnerViernesInicio.getSelectedItem().toString());
        editor.putString("Viernes_Descanso", binding.spinnerViernesDescanso.getSelectedItem().toString());
        editor.putString("Viernes_Fin", binding.spinnerViernesFin.getSelectedItem().toString());
        // Sábado
        editor.putString("Sabado_Inicio", binding.spinnerSabadoInicio.getSelectedItem().toString());
        editor.putString("Sabado_Descanso", binding.spinnerSabadoDescanso.getSelectedItem().toString());
        editor.putString("Sabado_Fin", binding.spinnerSabadoFin.getSelectedItem().toString());
        // Domingo
        editor.putString("Domingo_Inicio", binding.spinnerDomingoInicio.getSelectedItem().toString());
        editor.putString("Domingo_Descanso", binding.spinnerDomingoDescanso.getSelectedItem().toString());
        editor.putString("Domingo_Fin", binding.spinnerDomingoFin.getSelectedItem().toString());

        editor.apply();
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
                else {
                    Toast.makeText(getContext(), "Error en el servidor", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<UsuarioDto> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cambiarColorBotones(View activo) {
        binding.buttonAvailable.setBackgroundTintList(requireContext().getColorStateList(android.R.color.white));
        binding.buttonOnBreak.setBackgroundTintList(requireContext().getColorStateList(android.R.color.white));
        binding.buttonOutOfService.setBackgroundTintList(requireContext().getColorStateList(android.R.color.white));

        activo.setBackgroundTintList(requireContext().getColorStateList(android.R.color.holo_green_light));
    }

    private void iniciarWorker() {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(HorarioWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(requireContext()).enqueue(request);
    }
}