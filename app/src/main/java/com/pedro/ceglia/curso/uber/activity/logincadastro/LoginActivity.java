package com.pedro.ceglia.curso.uber.activity.logincadastro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.pedro.ceglia.curso.uber.R;
import com.pedro.ceglia.curso.uber.config.ConfiguracoesFirebase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText etEmail, etSenha;
    private Button btlogar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        configurandoToolbar();
        configuracoesIniciais();
    }

    private void configuracoesIniciais(){
        etEmail = findViewById(R.id.etLoginEmail);
        etSenha = findViewById(R.id.etLoginSenha);
        btlogar = findViewById(R.id.btLoginLogar);
        progressBar = findViewById(R.id.progressBarLogin);

        auth = ConfiguracoesFirebase.getAuth();

        btlogar.setOnClickListener(v -> {
            verificandoCampos();
        });
    }

    private void verificandoCampos(){
        String email = etEmail.getText().toString();
        String senha = etSenha.getText().toString();

        if (!email.isEmpty()){
            if (!senha.isEmpty()){

                escondendoWidgets();
                logarConta(email, senha);

            } else {
                criandoToast("Digite sua senha!");
            }
        } else {
            criandoToast("Digite seu email!");
        }
    }

    private void logarConta(String email, String senha){
        auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener( task ->{
            if (task.isSuccessful()){
                finish();
                criandoToast("Usuario Logado Com Sucesso!");

            } else {
                exibindoWidgets();
                String excecao;
                try {
                    throw task.getException();
                }catch ( FirebaseAuthInvalidUserException e ) {
                    excecao = "Usuário não está cadastrado.";
                }catch ( FirebaseAuthInvalidCredentialsException e ){
                    excecao = "E-mail e senha não correspondem a um usuário cadastrado";
                }catch (Exception e){
                    excecao = "Erro ao Logar usuário: "  + e.getMessage();
                    e.printStackTrace();
                }
                criandoToast(excecao);
            }
        });
    }

    private void configurandoToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Uber");
        toolbar.setSubtitle("Login");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    private void escondendoWidgets(){
        progressBar.setVisibility(View.VISIBLE);

        etEmail.setVisibility(View.GONE);
        etSenha.setVisibility(View.GONE);
        btlogar.setVisibility(View.GONE);
    }

    private void exibindoWidgets(){
        progressBar.setVisibility(View.GONE);


        etEmail.setVisibility(View.VISIBLE);
        etSenha.setVisibility(View.VISIBLE);
        btlogar.setVisibility(View.VISIBLE);
    }


    private void criandoToast(String mensagem){
        Toast.makeText(LoginActivity.this, mensagem, Toast.LENGTH_SHORT).show();
    }
}