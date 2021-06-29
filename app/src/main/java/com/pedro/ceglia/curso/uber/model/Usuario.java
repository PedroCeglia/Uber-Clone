package com.pedro.ceglia.curso.uber.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.pedro.ceglia.curso.uber.config.ConfiguracoesFirebase;

public class Usuario {

    private String nome;
    private String email;
    private String senha;
    private String id;
    private String tipo;

    public Usuario() {
    }

    public void salvar(){
        DatabaseReference database = ConfiguracoesFirebase.getDatabaseReference();
        String tt = getTipo();
        DatabaseReference usuariosRef;
        DatabaseReference usuariosRef2 = database.child("usuarios").child(getId());
        if (tt.equals("M")){
            usuariosRef = database.child("motoristas_usuarios").child(getId());
        } else {
            usuariosRef = database.child("usuarios_clientes").child(getId());
        }
        usuariosRef.setValue(this);
        usuariosRef2.setValue(this);

    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
