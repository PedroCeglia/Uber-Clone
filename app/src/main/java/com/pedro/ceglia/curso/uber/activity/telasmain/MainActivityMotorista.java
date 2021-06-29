package com.pedro.ceglia.curso.uber.activity.telasmain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.pedro.ceglia.curso.uber.R;
import com.pedro.ceglia.curso.uber.config.ConfiguracoesFirebase;

public class MainActivityMotorista extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_motorista);

        configurandoToolbar();
        configuracoesIniciais();
    }

    private void configuracoesIniciais(){
        auth = ConfiguracoesFirebase.getAuth();
    }

    private void configurandoToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Uber");
        toolbar.setSubtitle("Requisições");
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_cliente, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menuSair) {
            auth.signOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}