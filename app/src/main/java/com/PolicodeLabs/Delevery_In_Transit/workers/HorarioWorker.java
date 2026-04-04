package com.PolicodeLabs.Delevery_In_Transit.workers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;

import java.util.Calendar;

public class HorarioWorker extends Worker{

    public HorarioWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // 1. Obtener día y hora actual en MINUTOS desde la medianoche
        Calendar calendar = Calendar.getInstance();
        int minutosActuales = (calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE);
        String diaSemana = obtenerDiaString(calendar.get(Calendar.DAY_OF_WEEK));

        // 2. Leer preferencias
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("HorariosPrefs", Context.MODE_PRIVATE);
        Integer idUsuario = getApplicationContext().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE).getInt("ID_USUARIO", -1);

        if (idUsuario == -1) return Result.success();

        // 3. Convertir las horas guardadas (ej. "08:00") a minutos
        int minInicio = convertirTextoAMinutos(prefs.getString(diaSemana + "_Inicio", ""));
        int minDescanso = convertirTextoAMinutos(prefs.getString(diaSemana + "_Descanso", ""));
        int minFin = convertirTextoAMinutos(prefs.getString(diaSemana + "_Fin", ""));

        // Si no hay horario configurado para hoy, no hacemos nada
        if (minInicio == -1 || minFin == -1) return Result.success();

        // 4. Lógica infalible de rangos
        String estatusCalculado = "FUERA_SERVICIO"; // Por defecto, si no está en turno

        if (minutosActuales >= minInicio && minutosActuales < minFin) {
            // Está dentro de su horario laboral
            estatusCalculado = "DISPONIBLE";

            // Si además tiene hora de descanso, asumimos que toma 60 minutos exactos de descanso
            if (minDescanso != -1) {
                int finDescanso = minDescanso + 60; // 1 hora de comida
                if (minutosActuales >= minDescanso && minutosActuales < finDescanso) {
                    estatusCalculado = "EN_DESCANSO";
                }
            }
        }

        // 5. Enviar al servidor
        actualizarEnServidor(idUsuario, estatusCalculado);

        return Result.success();
    }

    // Método utilitario para convertir texto "HH:mm" a entero
    private int convertirTextoAMinutos(String horaString) {
        if (horaString == null || horaString.isEmpty() || horaString.contains("Seleccionar")) {
            return -1;
        }
        try {
            // Asume que tu arreglo XML tiene valores como "08:00" o "14:30"
            String[] partes = horaString.split(":");
            int horas = Integer.parseInt(partes[0].trim());
            int minutos = Integer.parseInt(partes[1].trim());
            return (horas * 60) + minutos;
        } catch (Exception e) {
            Log.e("HorarioWorker", "Error al parsear hora: " + horaString);
            return -1;
        }
    }

    private void actualizarEnServidor(Integer idUsuario, String estatus) {
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
            case Calendar.FRIDAY: return "Viernes"; // ¡Agregué el viernes que faltaba!
            case Calendar.SATURDAY: return "Sabado";
            case Calendar.SUNDAY: return "Domingo";
            default: return "";
        }
    }
}