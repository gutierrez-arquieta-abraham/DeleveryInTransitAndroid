package com.PolicodeLabs.Delevery_In_Transit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView load;

    // CORRECCIÓN: Usar Handler con Looper.getMainLooper() para evitar la deprecación.
    private Handler handler = new Handler(Looper.getMainLooper());

    private int progress = 0;
    private int dotState = 0;
    private final int DURACION_TOTAL = 2000; // 4 segundos aprox
    private final int INTERVALO_PROGRESS = 40; // 100 pasos (4000 / 40)
    private final int INTERVALO_TEXTO = 250; // medio segundo para cambiar los puntos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.myProgressBar);
        load = findViewById(R.id.load);

        // Inicia las animaciones
        animarTexto();
        animarProgreso();
    }

    /** 🔵 Cambia el texto "Cargando" con puntos animados **/
    private void animarTexto() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dotState = (dotState + 1) % 4;
                String puntos = "";
                for (int i = 0; i < dotState; i++) {
                    puntos += " .";
                }
                load.setText("Cargando" + puntos);

                // Repite cada medio segundo
                handler.postDelayed(this, INTERVALO_TEXTO);
            }
        }, INTERVALO_TEXTO);
    }

    /** 🟢 Aumenta el progreso poco a poco y lanza LoginActivity **/
    private void animarProgreso() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progress++;
                progressBar.setProgress(progress);

                if (progress < 100) {
                    handler.postDelayed(this, INTERVALO_PROGRESS);
                } else {
                    // Espera un momento antes de cambiar de Activity
                    handler.postDelayed(() -> {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }, 300);
                }
            }
        }, INTERVALO_PROGRESS);
    }
}