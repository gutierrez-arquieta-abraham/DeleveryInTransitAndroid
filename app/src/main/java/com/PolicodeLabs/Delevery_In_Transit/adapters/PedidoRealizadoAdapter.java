package com.PolicodeLabs.Delevery_In_Transit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.PolicodeLabs.Delevery_In_Transit.R;
import com.PolicodeLabs.Delevery_In_Transit.model.Pedido;

import java.util.List;

public class PedidoRealizadoAdapter extends RecyclerView.Adapter<PedidoRealizadoAdapter.PedidoRealizadoViewHolder> {

    private List<Pedido> listaPedidos;

    public PedidoRealizadoAdapter(List<Pedido> listaPedidos) {
        this.listaPedidos = listaPedidos;
    }

    // --- ViewHolder Interno ---
    public static class PedidoRealizadoViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserDate, tvAddress, tvDescription;

        public PedidoRealizadoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referencias a los IDs de item_pedido_realizado.xml
            tvUserDate = itemView.findViewById(R.id.tvCompletedUserDate);
            tvAddress = itemView.findViewById(R.id.tvCompletedAddress);
            tvDescription = itemView.findViewById(R.id.tvCompletedDescription);
        }
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
        Pedido pedido = listaPedidos.get(position);

        // 1. COMBINAR NOMBRE Y FECHA (Ya que el XML solo tiene un campo para ambos)
        // Antes: getUsuarioFecha() (No existe)
        // Ahora: Unimos Cliente + Fecha manualmente
        String cliente = pedido.getNombreCliente() != null ? pedido.getNombreCliente() : "Pedido";
        String fecha = pedido.getFechaEntrega() != null ? pedido.getFechaEntrega() : "--/--/--";
        String hora = pedido.getHoraEntrega() != null ? pedido.getHoraEntrega() : "";
        String textoCombinado = cliente + " - " + fecha + " de " + hora + " hrs";

        holder.tvUserDate.setText(textoCombinado);

        // 2. DIRECCIÓN
        // Antes: getDireccion() -> Ahora: getDireccionEntrega()
        String direccion = pedido.getDireccionEntrega();
        holder.tvAddress.setText(direccion != null ? direccion : "Dirección no disponible");

        // 3. DESCRIPCIÓN (Este no cambió, pero validamos nulos por si acaso)
        holder.tvDescription.setText(pedido.getDescripcion() != null ? pedido.getDescripcion() : "Sin descripción");
    }

    @Override
    public int getItemCount() {
        return listaPedidos != null ? listaPedidos.size() : 0;
    }

    public void updateData(List<Pedido> nuevaLista) {
        this.listaPedidos = nuevaLista;
        notifyDataSetChanged();
    }
}