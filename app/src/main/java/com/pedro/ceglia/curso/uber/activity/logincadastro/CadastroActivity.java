package com.pedro.ceglia.curso.uber.activity.logincadastro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.pedro.ceglia.curso.uber.R;
import com.pedro.ceglia.curso.uber.config.ConfiguracoesFirebase;
import com.pedro.ceglia.curso.uber.helper.UsuarioFirebase;
import com.pedro.ceglia.curso.uber.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etNome, etEmail, etSenha;
    private Button btCadastrar;
    private LinearLayout lLSw;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch sw;
    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        configurandoToolbar();
        configuracoesIniciais();
    }

    private void configuracoesIniciais(){
        etNome = findViewById(R.id.etCadastroNome);
        etEmail = findViewById(R.id.etCadastroEmail);
        etSenha = findViewById(R.id.etCadastroSenha);
        btCadastrar = findViewById(R.id.btCadastroCadastrar);
        sw = findViewById(R.id.swCadastro);
        lLSw = findViewById(R.id.lLSwCadastro);
        progressBar = findViewById(R.id.progressBarCadastro);

        auth = ConfiguracoesFirebase.getAuth();

        btCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificandoCampos();
            }
        });
    }

    private void verificandoCampos(){
        String nome = etNome.getText().toString();
        String email = etEmail.getText().toString();
        String senha = etSenha.getText().toString();

        if (!nome.isEmpty()){
            if (!email.isEmpty()){
                if (!senha.isEmpty()){

                    escondendoWidgets();
                    criandoUsuario(nome, email, senha);

                } else {
                   criandoToast("Digite uma senha!");
                }
            } else {
                criandoToast("Digite seu Email!");
            }
        } else{
            criandoToast("Digite seu nome!");
        }

    }

    private void criandoUsuario(String nome, String email, String senha){
        auth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                UsuarioFirebase.atualizarNomeUsuario(nome);
                Usuario usuario = new Usuario();
                if (sw.isChecked()){
                    usuario.setTipo("C");
                } else {
                    usuario.setTipo("M");
                }
                usuario.setEmail(email);
                usuario.setSenha(senha);
                usuario.setId(UsuarioFirebase.getIdentificadorUsuario());
                usuario.salvar();
                finish();

            } else {

                String excecao ;
                try {
                    throw task.getException();
                }catch ( FirebaseAuthWeakPasswordException e){
                    excecao = "Digite uma senha mais forte!";
                    etSenha.setText("");
                }catch ( FirebaseAuthInvalidCredentialsException e){
                    excecao= "Por favor, digite um e-mail válido";
                    etEmail.setText("");
                }catch ( FirebaseAuthUserCollisionException e){
                    excecao = "Este conta já foi cadastrada";
                    etEmail.setText("");
                }catch (Exception e){
                    excecao = "Erro ao cadastrar usuário: "  + e.getMessage();
                    e.printStackTrace();
                }
                if (excecao != null && !excecao.isEmpty())
                    criandoToast(excecao);

                exibindoWidgets();
            }
        });
    }


    private void configurandoToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Uber");
        toolbar.setSubtitle("Cadastro");
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

        etNome.setVisibility(View.GONE);
        etEmail.setVisibility(View.GONE);
        etSenha.setVisibility(View.GONE);
        lLSw.setVisibility(View.GONE);
        btCadastrar.setVisibility(View.GONE);
    }

    private void exibindoWidgets(){
        progressBar.setVisibility(View.GONE);

        etNome.setVisibility(View.VISIBLE);
        etEmail.setVisibility(View.VISIBLE);
        etSenha.setVisibility(View.VISIBLE);
        lLSw.setVisibility(View.VISIBLE);
        btCadastrar.setVisibility(View.VISIBLE);
    }

    @SuppressLint("ShowToast")
    private void criandoToast(String mensagem){
        Toast.makeText(CadastroActivity.this,
                mensagem,
                Toast.LENGTH_SHORT);
    }

}