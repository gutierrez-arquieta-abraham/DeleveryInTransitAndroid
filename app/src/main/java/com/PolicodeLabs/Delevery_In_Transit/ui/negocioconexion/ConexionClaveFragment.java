package com.PolicodeLabs.Delevery_In_Transit.ui.negocioconexion;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.PolicodeLabs.Delevery_In_Transit.databinding.FragmentConexionClaveBinding;

public class ConexionClaveFragment extends Fragment {

    private FragmentConexionClaveBinding binding;
    private String claveGenerada = "PENDIENTE";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConexionClaveBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. RECUPERAR EL CÓDIGO GENERADO QUE NOS PASÓ EL FRAGMENTO ANTERIOR
        if (getArguments() != null) {
            claveGenerada = getArguments().getString("CODIGO_CONEXION", "ERROR");
            binding.tvCodigoGenerado.setText(claveGenerada);
        }

        // Configurar botones
        binding.btnCopiarCodigo.setOnClickListener(v -> copiarAlPortapapeles(claveGenerada));
        binding.btnCompartirCodigo.setOnClickListener(v -> compartirCodigo(claveGenerada));
    }

    private void copiarAlPortapapeles(String texto) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Código QuickFleet", texto);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Copiado: " + texto, Toast.LENGTH_SHORT).show();
    }

    private void compartirCodigo(String texto) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Unete a mi equipo en QuickFleet con este código: " + texto);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Compartir código vía...");
        startActivity(shareIntent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}