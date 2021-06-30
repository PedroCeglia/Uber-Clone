package com.pedro.ceglia.curso.uber.model;

import com.google.firebase.database.DatabaseReference;
import com.pedro.ceglia.curso.uber.config.ConfiguracoesFirebase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Requisicao implements Serializable {

    private Destino destino;
    private String idRequisicao;
    private Usuario passageiro;
    private Usuario motorista;
    private String status;

    public static final String STATUS_AGUARDANDO = "aguardando";
    public static final String STATUS_A_CAMINHO  = "acaminho";
    public static final String STATUS_VIAGEM = "viagem";
    public static final String STATUS_FINALIZADA = "finalizada";
    public static final String STATUS_ENCERRADA = "encerrada";
    public static final String STATUS_CANCELADA = "cancelada";


    public Requisicao() {
    }

    public void salvar(){
        DatabaseReference reference = ConfiguracoesFirebase.getDatabaseReference();
        DatabaseReference requisicoesRef = reference.child("requisicoes");

        // Configurando Id da requisição
        String idRequisicao = requisicoesRef.push().getKey();
        setIdRequisicao(idRequisicao);

        requisicoesRef
                .child(getIdRequisicao())
                .setValue(this);

    }
    public void deletar(){
        DatabaseReference reference = ConfiguracoesFirebase.getDatabaseReference();
        DatabaseReference requisicoesRef = reference.child("requisicoes");



        requisicoesRef
                .child(getIdRequisicao())
                .removeValue();

    }
    public void atualizarStatus(){
        DatabaseReference reference = ConfiguracoesFirebase.getDatabaseReference();
        DatabaseReference requisicoesRef = reference.child("requisicoes");
        DatabaseReference requisicaoRef = requisicoesRef.child(getIdRequisicao());

        HashMap objeto = new HashMap();
        objeto.put("status", getStatus());

        requisicaoRef.updateChildren(objeto);
    }

    public void atualizarMotoristaLocalizacao(Usuario motorista){
        DatabaseReference reference = ConfiguracoesFirebase.getDatabaseReference();
        DatabaseReference requisicoesRef = reference.child("requisicoes");
        DatabaseReference requisicaoRef = requisicoesRef
                .child(getIdRequisicao())
                .child("motorista");

        Map objeto = new HashMap();
        objeto.put("latitude", motorista.getLatitude());
        objeto.put("longitude", motorista.getLongitude());

        requisicaoRef.updateChildren(objeto);
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }

    public String getIdRequisicao() {
        return idRequisicao;
    }

    public void setIdRequisicao(String idRequisicao) {
        this.idRequisicao = idRequisicao;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
