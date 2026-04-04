package com.PolicodeLabs.Delevery_In_Transit.workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.transform.Result;

public class HorarioWorker extends Worker{

    public HorarioWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 1. Obtener día y hora actual
        Calendar calendar = Calendar.getInstance();
        String horaActual = new SimpleDateFormat("H:mm", Locale.getDefault()).format(new Date());

        // Obtener día de la semana (Lunes=2, Domingo=1 en Calendar de Java, ajusta según tu lógica)
        String diaSemana = obtenerDiaString(calendar.get(Calendar.DAY_OF_WEEK));

        // 2. Leer horarios guardados
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("HorariosPrefs", Context.MODE_PRIVATE);
        Integer idUsuario = getApplicationContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE).getInt("ID_USUARIO", -1);

        if (idUsuario == -1) return Result.success();

        // Claves ejemplo: "Lunes_Inicio", "Lunes_Descanso", "Lunes_Fin"
        String horaInicio = prefs.getString(diaSemana + "_Inicio", "");
        String horaDescanso = prefs.getString(diaSemana + "_Descanso", "");
        String horaFin = prefs.getString(diaSemana + "_Fin", "");

        // 3. Comparar y actuar
        if (horaActual.equals(horaInicio)) {
            actualizarEnServidor(idUsuario, "DISPONIBLE");
        }
        else if (horaActual.equals(horaFin)) {
            actualizarEnServidor(idUsuario, "FUERA_SERVICIO");
        }
        else if (horaActual.equals(horaDescanso)) {
            // Entrar a descanso
            actualizarEnServidor(idUsuario, "EN_DESCANSO");
            // Nota: Para salir del descanso automáticamente después de 1 hora,
            // necesitaríamos una lógica más compleja de "hora actual >= hora descanso + 1".
            // Por ahora, el usuario puede regresar manualmente o esperar al fin de turno.
        }

        return Result.success();
    }

    private void actualizarEnServidor(Integer idUsuario, String estatus) {
        // Llamada síncrona (execute) porque estamos en un hilo de fondo
        try {
            RetrofitClient.getApiService().actualizarEstatus(idUsuario, estatus).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String obtenerDiaString(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "Lunes";
            case Calendar.TUESDAY: return "Martes";
            case Calendar.WEDNESDAY: return "Miercoles";
            case Calendar.THURSDAY: return "Jueves";
            case Calendar.FRIDAY: return "Viernes"; // Falta en tu XML, ojo
            case Calendar.SATURDAY: return "Sabado";
            case Calendar.SUNDAY: return "Domingo";
            default: return "";
        }
    }
}