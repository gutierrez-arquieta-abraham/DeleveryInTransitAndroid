package com.PolicodeLabs.Delevery_In_Transit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;

import java.util.List;

public class PedidoRealizadoAdapter extends RecyclerView.Adapter<PedidoRealizadoAdapter.PedidoRealizadoViewHolder> {

    private List<PedidoDto> listaPedidos;
    private OnEstadisticasClickListener listener;

    // Interfaz para comunicar el clic al Fragmento
    public interface OnEstadisticasClickListener {
        void onVerEstadisticasClick(int numOrd);
    }

    public PedidoRealizadoAdapter(List<PedidoDto> listaPedidos, OnEstadisticasClickListener listener) {
        this.listaPedidos = listaPedidos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PedidoRealizadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido_realizado, parent, false);
        return new PedidoRealizadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoRealizadoViewHolder holder, int position) {
        PedidoDto pedido = listaPedidos.get(position);

        holder.tvNumOrd.setText("Pedido #" + pedido.getNumOrd());

        // Manejo del Nombre del Cliente (Desde el nuevo DTO)
        String nombreCliente = pedido.getNombreCliente() != null ? pedido.getNombreCliente() : "Cliente Desconocido";

        // Manejo de la fecha de entrega (Spring Boot devuelve algo como "2026-04-04T14:30:00")
        // Manejo de la fecha de entrega y la hora
        String fechaSucia = pedido.getFechaHoraEntrega();
        String fechaLimpia = "--/--/----";

        if (fechaSucia != null && fechaSucia.contains("T")) {
            String[] partes = fechaSucia.split("T");
            String fecha = partes[0];
            String hora = partes[1];

            // Si la hora trae milisegundos (ej. 14:30:00.000), se los quitamos
            if (hora.contains(".")) {
                hora = hora.substring(0, hora.indexOf("."));
            }

            fechaLimpia = fecha + " a las " + hora;
        }

        holder.tvClienteFecha.setText(nombreCliente + " - Entregado: " + fechaLimpia);

        // Destino
        holder.tvDestino.setText("Destino: " + pedido.getDestino());

        // Evento del botón de Estadísticas
        holder.btnEstadisticas.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVerEstadisticasClick(pedido.getNumOrd());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaPedidos != null ? listaPedidos.size() : 0;
    }

    public void updateData(List<PedidoDto> nuevaLista) {
        this.listaPedidos = nuevaLista;
        notifyDataSetChanged();
    }

    // --- ViewHolder Interno ---
    public static class PedidoRealizadoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumOrd, tvClienteFecha, tvDestino;
        Button btnEstadisticas;

        public PedidoRealizadoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumOrd = itemView.findViewById(R.id.tvNumOrdHistorial);
            tvClienteFecha = itemView.findViewById(R.id.tvClienteFecha);
            tvDestino = itemView.findViewById(R.id.tvDestinoHistorial);
            btnEstadisticas = itemView.findViewById(R.id.btnVerEstadisticas);
        }
    }
}