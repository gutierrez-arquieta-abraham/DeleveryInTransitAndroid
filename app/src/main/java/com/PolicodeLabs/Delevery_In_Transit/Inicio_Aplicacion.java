package com.PolicodeLabs.Delevery_In_Transit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Inicio_Aplicacion extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_aplicacion);

        LinearLayout layout = findViewById(R.id.inicio_app);
        layout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.inicio_app){
            Intent intento = new Intent(Inicio_Aplicacion.this, MainActivity.class);
            startActivity(intento);
            finish();
        }
    }
}