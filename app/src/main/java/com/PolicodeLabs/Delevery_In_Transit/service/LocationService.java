package com.PolicodeLabs.Delevery_In_Transit.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private int idRepartidor = -1;

    // CONFIGURACIÓN TURBO (10 Segundos)
    private static final long UPDATE_INTERVAL_MS = 10000;
    private static final long FASTEST_INTERVAL_MS = 5000;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("GPS_DEBUG", "🚀 Servicio Creado. Iniciando...");

        // 1. Obtener ID
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        idRepartidor = prefs.getInt("ID_USUARIO", -1);

        if (idRepartidor == -1) {
            Log.e("GPS_DEBUG", "❌ ERROR: No hay ID_USUARIO. Deteniendo servicio.");
            stopSelf();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 2. Definir qué hacer cuando llega una coordenada
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (android.location.Location location : locationResult.getLocations()) {
                    Log.d("GPS_DEBUG", "📍 Moviéndose: " + location.getLatitude() + ", " + location.getLongitude());
                    enviarAlServidor(location.getLatitude(), location.getLongitude());
                }
            }
        };
    }

    // Este método es VITAL para que el servicio sea "Pegajoso" (Sticky)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 1. Iniciar Notificación Permanente (OBLIGATORIO para que no muera)
        mostrarNotificacion();

        // 2. Iniciar el GPS
        iniciarRastreo();

        // 3. Decirle a Android: "Si me matas por falta de memoria, revíveme en cuanto puedas"
        return START_STICKY;
    }

    private void iniciarRastreo() {
        try {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
                    .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
                    .setWaitForAccurateLocation(false)
                    .build();

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                Log.d("GPS_DEBUG", "📡 Rastreo ACTIVO cada 10 seg.");
            } else {
                Log.e("GPS_DEBUG", "❌ Sin permisos de ubicación.");
            }
        } catch (Exception e) {
            Log.e("GPS_DEBUG", "❌ Error al iniciar rastreo: " + e.getMessage());
        }
    }

    private void enviarAlServidor(double lat, double lon) {
        // En tu método enviarAlServidor:
        RetrofitClient.getApiService().actualizarUbicacion(idRepartidor, lat, lon)
                .enqueue(new Callback<ResponseBody>() {  // <--- CAMBIA 'String' POR 'ResponseBody' AQUÍ

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { // <--- AQUÍ TAMBIÉN
                        if (response.isSuccessful()) {
                            Log.i("GPS", "OK");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) { // <--- Y AQUÍ TAMBIÉN
                        Log.e("GPS", "Error: " + t.getMessage());
                    }
                });
    }

    private void mostrarNotificacion() {
        String CHANNEL_ID = "gps_channel_turbo";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Rastreo Activo", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("QuickFleet en Ruta")
                .setContentText("Tu ubicación se actualiza en tiempo real")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true) // Evita que el usuario borre la notificación por error
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        Log.d("GPS_DEBUG", "🛑 Servicio detenido.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}