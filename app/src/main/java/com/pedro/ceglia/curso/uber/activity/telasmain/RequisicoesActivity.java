package com.pedro.ceglia.curso.uber.activity.telasmain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pedro.ceglia.curso.uber.R;
import com.pedro.ceglia.curso.uber.adapter.AdapterRequisicoes;
import com.pedro.ceglia.curso.uber.config.ConfiguracoesFirebase;
import com.pedro.ceglia.curso.uber.helper.RecyclerItemClickListener;
import com.pedro.ceglia.curso.uber.helper.UsuarioFirebase;
import com.pedro.ceglia.curso.uber.model.Requisicao;
import com.pedro.ceglia.curso.uber.model.Usuario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequisicoesActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference database;
    private DatabaseReference requisicoesRef;
    private Usuario usuarioLogado;
    private ValueEventListener valueEventListener;

    // RecyclerView
    private RecyclerView rv;
    private AdapterRequisicoes adapter;
    private List<Requisicao> listaDeRequisicoes;

    // Widgets
    private TextView tvRequisicao;

    // MAPS SDK FOR ANDROID
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localizacaoAtual;

    //
    private boolean deuCerto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes);

        configuracoesIniciais();
        configurandoToolbar();

        // Recupera a localizaçao do motorista
        recuperarLocalizacaoMotorista();

        configurandoRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!verificandoStatusDaCorrida()){
            recuperandoRequisicoes();
        }

    }

    private void configuracoesIniciais(){
        auth = ConfiguracoesFirebase.getAuth();
        database = ConfiguracoesFirebase.getDatabaseReference();
        requisicoesRef = database.child("requisicoes");
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogadoAuth();
        rv = findViewById(R.id.rvRequisicoes);
        tvRequisicao = findViewById(R.id.tvRequisicoes);
        listaDeRequisicoes = new ArrayList<>();
    }
    private void configurandoToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Requisições");
        setSupportActionBar(toolbar);
    }

    private void configurandoRecyclerView(){
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        adapter = new AdapterRequisicoes(listaDeRequisicoes, this, usuarioLogado);
        rv.setAdapter(adapter);

    }


    // Verifica se existem requisições
    private void recuperandoRequisicoes(){

        listaDeRequisicoes.clear();

        Query query = requisicoesRef.orderByChild("status")
                .equalTo(Requisicao.STATUS_AGUARDANDO);
        valueEventListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() > 0){
                    tvRequisicao.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    for (DataSnapshot ds : snapshot.getChildren()){
                        listaDeRequisicoes.add(ds.getValue(Requisicao.class));
                    }
                    Collections.reverse(listaDeRequisicoes);
                    adapter.notifyDataSetChanged();

                } else {
                    tvRequisicao.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    // verifica a Localizacao do motorista para calcula a distancia do cliente
    private void recuperarLocalizacaoMotorista(){

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(@NonNull Location location) {

                // recuperar Latitude Longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                if (!String.valueOf(latitude).isEmpty() && !String.valueOf(longitude).isEmpty()){
                    // Define a localizaçao do usuario
                    usuarioLogado.setLatitude(String.valueOf(latitude));
                    usuarioLogado.setLongitude(String.valueOf(longitude));
                    localizacaoAtual = new LatLng(latitude, longitude);
                    // Atualiza o GeoFire
                    UsuarioFirebase.atualizarDadosLocalizacao(localizacaoAtual, RequisicoesActivity.this);
                    locationManager.removeUpdates(locationListener);
                    adapter.notifyDataSetChanged();
                    criandoToast();
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
            );
        }

        // Configura evento de Clique
        rv.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        rv,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Requisicao requisicaoEscolhida = listaDeRequisicoes.get(position);
                                abrindoTelaDeCorrida(requisicaoEscolhida, usuarioLogado, true);
                            }
                            @Override
                            public void onLongItemClick(View view, int position) { }
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { }
                        }
                )
        );
    }


    private boolean verificandoStatusDaCorrida(){
        deuCerto = false;
        Query requisicoesPesquisa = requisicoesRef.orderByChild("motorista/id").equalTo(usuarioLogado.getId());
        requisicoesPesquisa.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Requisicao requisicaoRecuperada = ds.getValue(Requisicao.class);
                    assert requisicaoRecuperada != null;
                    if (requisicaoRecuperada.getStatus().equals(Requisicao.STATUS_A_CAMINHO)
                            || requisicaoRecuperada.getStatus().equals( Requisicao.STATUS_VIAGEM)
                            || requisicaoRecuperada.getStatus().equals( Requisicao.STATUS_FINALIZADA)){
                        abrindoTelaDeCorrida(requisicaoRecuperada, requisicaoRecuperada.getMotorista(), false);
                        deuCerto = true;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        return deuCerto;
    }

    // Abri a Main Activity Motorista
    private void abrindoTelaDeCorrida(Requisicao recPar, Usuario userPar, boolean verificacao){
        Intent i = new Intent(RequisicoesActivity.this, MainActivityMotorista.class);
        i.putExtra("requisicao", recPar);
        i.putExtra("motorista", userPar);
        i.putExtra("verificacao", verificacao);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main_cliente, menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuSair) {
            auth.signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void criandoToast(){
        Toast.makeText(this, "Localização Recuperada",Toast.LENGTH_SHORT).show();
    }

}