package com.PolicodeLabs.Delevery_In_Transit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;

import java.util.List;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<PedidoDto> listaPedidos;
    private final OnItemClickListener listener; // <-- NUEVO: El "oído" del adaptador

    // Interfaz para comunicar el clic al Fragmento
    public interface OnItemClickListener {
        void onItemClick(PedidoDto pedido);
    }

    // Constructor actualizado
    public PedidosAdapter(List<PedidoDto> listaPedidos, OnItemClickListener listener) {
        this.listaPedidos = listaPedidos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false); // Tu tarjeta XML
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        PedidoDto pedido = listaPedidos.get(position);

        holder.tvId.setText("Pedido #" + pedido.getNumOrd());
        holder.tvDescripcion.setText(pedido.getDescripcion());
        holder.tvEstado.setText(pedido.getEstadoReal());

        // --- NUEVO: Detectar el clic en la tarjeta ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(pedido);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaPedidos != null ? listaPedidos.size() : 0;
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvDescripcion, tvEstado;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvIdPedido);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvEstado = itemView.findViewById(R.id.tvEstado);
        }
    }
}