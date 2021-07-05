package com.pedro.ceglia.curso.uber.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.pedro.ceglia.curso.uber.R;
import com.pedro.ceglia.curso.uber.helper.Local;
import com.pedro.ceglia.curso.uber.model.Requisicao;
import com.pedro.ceglia.curso.uber.model.Usuario;

import java.util.List;

public class AdapterRequisicoes extends RecyclerView.Adapter<AdapterRequisicoes.MyViewHolderRequisicoes> {

    private final List<Requisicao> listaDeRequisicao;
    private final Context c;
    private final Usuario motorista;

    public AdapterRequisicoes(List<Requisicao> listaDeRequisicao, Context c, Usuario motorista) {
        this.listaDeRequisicao = listaDeRequisicao;
        this.c = c;
        this.motorista = motorista;
    }

    @NonNull
    @Override
    public MyViewHolderRequisicoes onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_requisicoes_layout, parent, false);
        return new MyViewHolderRequisicoes(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolderRequisicoes holder, int position) {
        Requisicao rec = listaDeRequisicao.get(position);
        Usuario passageiro = rec.getPassageiro();

        holder.tvNomePassageiro.setText(passageiro.getNome());

        LatLng latLngPassageiro = new LatLng(
                Double.parseDouble(passageiro.getLatitude()),
                Double.parseDouble(passageiro.getLongitude())
        );

        if (motorista.getLatitude() != null && !motorista.getLatitude().isEmpty()){
            if (motorista.getLongitude() != null && !motorista.getLongitude().isEmpty()) {
                LatLng latLngMotorista = new LatLng(
                        Double.parseDouble(motorista.getLatitude()),
                        Double.parseDouble(motorista.getLongitude())
                );

                float distancia = Local.calcularDistancia(latLngMotorista, latLngPassageiro);
                String distanciaSt = Local.formatarDistancia(distancia);

                holder.tvDistancia.setText( String.valueOf(distanciaSt) + "-  aproximadamente");

            }
        }
    }

    @Override
    public int getItemCount() {
        return listaDeRequisicao.size();
    }

    public static class MyViewHolderRequisicoes extends RecyclerView.ViewHolder{

        private final TextView tvNomePassageiro;
        private final TextView tvDistancia;

        public MyViewHolderRequisicoes(@NonNull View itemView) {
            super(itemView);

            tvDistancia = itemView.findViewById(R.id.tvDistancia);
            tvNomePassageiro = itemView.findViewById(R.id.tvNomeUsuario);
        }
    }
}
