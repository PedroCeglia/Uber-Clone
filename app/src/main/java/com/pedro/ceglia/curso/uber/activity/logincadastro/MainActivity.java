package com.pedro.ceglia.curso.uber.activity.logincadastro;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pedro.ceglia.curso.uber.R;
import com.pedro.ceglia.curso.uber.activity.telasmain.MainActivityCliente;
import com.pedro.ceglia.curso.uber.activity.telasmain.MainActivityMotorista;
import com.pedro.ceglia.curso.uber.config.ConfiguracoesFirebase;
import com.pedro.ceglia.curso.uber.model.Usuario;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configuracoesIniciais();
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificandoSeAUsuarioLogado();
    }

    private void configuracoesIniciais(){

        auth = ConfiguracoesFirebase.getAuth();
        database = ConfiguracoesFirebase.getDatabaseReference();

        Button btCadastro = findViewById(R.id.btInicialCadastrar);
        Button btLogin = findViewById(R.id.btInicialLogar);

        btLogin.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        });

        btCadastro.setOnClickListener(v ->{
            Intent i = new Intent(MainActivity.this, CadastroActivity.class);
            startActivity(i);
        });
    }

    private void verificandoSeAUsuarioLogado(){
        if (auth.getCurrentUser() != null) {
            // Abrir tela principal
            String id = auth.getCurrentUser().getUid();
            verificandoNoFirebase(id);
        }
    }

    private void verificandoNoFirebase(String id){
        DatabaseReference usuariosRef = database.child("usuarios").child(id);
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Usuario usuario = snapshot.getValue(Usuario.class);
                if (usuario != null){
                    Intent i;
                    if (usuario.getTipo().equals("M")){
                        // Abrir Activity para Motorista
                        i = new Intent(MainActivity.this, MainActivityMotorista.class);
                    } else {
                        // Abrir Activity  para Cliente
                        i = new Intent(MainActivity.this, MainActivityCliente.class);
                    }
                    startActivity(i);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}