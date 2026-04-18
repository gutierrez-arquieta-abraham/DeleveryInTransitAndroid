package com.PolicodeLabs.Delevery_In_Transit.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.api.RetrofitClient;
import com.PolicodeLabs.Delevery_In_Transit.model.DashboardNegocioDto;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private TextView tvPromedioTiempo, tvTotalKm;
    private BarChart graficaTiempos;
    private int ID_LICENCIA = -1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Asegúrate de que este sea el nombre correcto de tu XML
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvPromedioTiempo = root.findViewById(R.id.tvPromedioTiempo);
        tvTotalKm = root.findViewById(R.id.tvTotalKm);
        graficaTiempos = root.findViewById(R.id.graficaTiempos);

        SharedPreferences prefs = requireActivity().getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);
        ID_LICENCIA = prefs.getInt("ID_LICENCIA", -1);

        configurarGrafica();
        obtenerDatosEstadisticos();

        return root;
    }

    // Le damos formato a la gráfica para que se vea elegante y profesional
    private void configurarGrafica() {
        graficaTiempos.getDescription().setEnabled(false); // Quitamos el texto feo de abajo
        graficaTiempos.getLegend().setEnabled(false); // Quitamos la leyenda redundante
        graficaTiempos.setDrawGridBackground(false);
        graficaTiempos.setDrawBorders(false);

        // Configuramos el eje X (Abajo)
        XAxis xAxis = graficaTiempos.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false); // Quitamos la cuadrícula
        xAxis.setGranularity(1f); // Que salte de 1 en 1

        // Configuramos los ejes Y (Izquierda y Derecha)
        graficaTiempos.getAxisLeft().setDrawGridLines(true);
        graficaTiempos.getAxisRight().setEnabled(false); // Apagamos el eje derecho para que no estorbe
    }

    private void obtenerDatosEstadisticos() {
        if (ID_LICENCIA == -1) {
            Log.e("DASHBOARD", "Error: ID_LICENCIA es -1. No se puede pedir información al servidor.");
            return;
        }

        Log.d("DASHBOARD", "➡️ Pidiendo estadísticas para el negocio ID: " + ID_LICENCIA);

        RetrofitClient.getApiService().obtenerDashboardNegocio(ID_LICENCIA).enqueue(new Callback<DashboardNegocioDto>() {
            @Override
            public void onResponse(Call<DashboardNegocioDto> call, Response<DashboardNegocioDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DASHBOARD", "✅ Datos recibidos correctamente");
                    DashboardNegocioDto dashboard = response.body();

                    // 1. PINTAMOS LOS KPIs MATEMÁTICOS
                    tvPromedioTiempo.setText(dashboard.getPromedioTiempoEntrega() + " min");
                    tvTotalKm.setText(dashboard.getTotalKilometrosRecorridos() + " km");

                    // 2. PINTAMOS LA GRÁFICA
                    dibujarGrafica(dashboard.getHistorialReciente());
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Desconocido";
                        Log.e("DASHBOARD", "❌ Error del servidor. Código HTTP: " + response.code() + ". Detalle: " + error);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<DashboardNegocioDto> call, Throwable t) {
                Log.e("DASHBOARD", "🚨 Falla de red: " + t.getMessage());
            }
        });
    }

    private void dibujarGrafica(List<PedidoDto> historial) {
        if (historial == null || historial.isEmpty()) {
            graficaTiempos.clear();
            return;
        }

        List<BarEntry> entradas = new ArrayList<>();

        // Iteramos los pedidos para extraer sus tiempos
        for (int i = 0; i < historial.size(); i++) {
            PedidoDto pedido = historial.get(i);

            // Si el tiempo es nulo, lo ponemos en 0 por seguridad
            float tiempo = pedido.getMinutosTranscurridos() != null ? pedido.getMinutosTranscurridos().floatValue() : 0f;

            // X = Número de Pedido (0, 1, 2...), Y = Tiempo en minutos
            entradas.add(new BarEntry((float) i, tiempo));
        }

        BarDataSet dataSet = new BarDataSet(entradas, "Tiempo de Entrega");
        dataSet.setColor(Color.parseColor("#009688")); // Verde azulado elegante
        dataSet.setValueTextSize(10f); // Tamaño del número arriba de la barra
        dataSet.setValueTextColor(Color.DKGRAY);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f); // Grosor de las barras

        graficaTiempos.setData(barData);
        graficaTiempos.animateY(1000); // Animación al abrir la pantalla (1 segundo)
        graficaTiempos.invalidate(); // Refrescamos el lienzo
    }
}