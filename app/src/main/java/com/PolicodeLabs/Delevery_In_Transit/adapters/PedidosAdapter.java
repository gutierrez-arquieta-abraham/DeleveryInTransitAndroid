package com.PolicodeLabs.Delevery_In_Transit.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.model.PedidoDto;
import java.util.List;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<PedidoDto> listaPedidos;
    private OnItemClickListener listener; // Para los clics de asignación

    public interface OnItemClickListener {
        void onItemClick(PedidoDto pedido);
    }

    // Constructor que permite clics (para AsignacionFragment)
    public PedidosAdapter(List<PedidoDto> listaPedidos, OnItemClickListener listener) {
        this.listaPedidos = listaPedidos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        PedidoDto pedido = listaPedidos.get(position);

        holder.tvNumOrd.setText("Pedido #" + pedido.getNumOrd());
        holder.tvEstado.setText(pedido.getEstadoReal());
        holder.tvDestino.setText("Destino: " + pedido.getDestino());
        holder.tvDescripcion.setText("Detalle: " + pedido.getDescripcion());

        String nombreCliente = pedido.getNombreCliente() != null ? pedido.getNombreCliente() : "Cliente Desconocido";
        holder.tvCliente.setText("Cliente: " + nombreCliente);

        // Si hacen clic en TODA la tarjeta (Asignación)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(pedido);
        });

        // Si hacen clic en el BOTÓN DE TELÉFONO
        if(holder.btnLlamarCliente != null) {
            holder.btnLlamarCliente.setOnClickListener(v -> {
                String telefono = pedido.getTelefonoCliente();
                if (telefono != null && !telefono.trim().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + telefono));
                    v.getContext().startActivity(intent);
                } else {
                    Toast.makeText(v.getContext(), "Sin número de contacto", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() { return listaPedidos != null ? listaPedidos.size() : 0; }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumOrd, tvEstado, tvDestino, tvDescripcion, tvCliente;
        ImageButton btnLlamarCliente;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Usamos los IDs de item_pedido.xml
            tvNumOrd = itemView.findViewById(R.id.tvNumOrd);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvDestino = itemView.findViewById(R.id.tvDestino);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvCliente = itemView.findViewById(R.id.tvCliente);
            btnLlamarCliente = itemView.findViewById(R.id.btnLlamarCliente);
        }
    }
}