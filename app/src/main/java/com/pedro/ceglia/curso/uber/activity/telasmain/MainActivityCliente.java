package com.pedro.ceglia.curso.uber.activity.telasmain;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pedro.ceglia.curso.uber.R;
import com.pedro.ceglia.curso.uber.config.ConfiguracoesFirebase;
import com.pedro.ceglia.curso.uber.helper.Local;
import com.pedro.ceglia.curso.uber.helper.UsuarioFirebase;
import com.pedro.ceglia.curso.uber.model.Destino;
import com.pedro.ceglia.curso.uber.model.Requisicao;
import com.pedro.ceglia.curso.uber.model.Usuario;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivityCliente extends AppCompatActivity implements OnMapReadyCallback {

    // Sdk Maps For Android
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localizacaoAtual;
    private LatLng localizacaoDestino;
    private LatLng localizacaoPassageiro;
    private LatLng localizacaoMotorista;
    private Marker motoristaMarker;
    private Marker passageiroMarker;
    private Marker destinoMarker;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference requisicoesRef;
    private ValueEventListener valueEventListenerRequisicoes;
    private Usuario usuarioLogado;
    private Query query;

    // Widgets
    private EditText etLocalCliente, etLocalDestino;
    private LinearLayout lLets;
    private Button btMain;

    // Models
    // Models
    private boolean uberChamado = false;
    private boolean confirmadoEndInicial;
    private Requisicao requisicao;
    private Usuario passageiro;
    private Usuario motorista;
    private String statusRequisicao;
    private Destino destino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_cliente);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        configurandoToolbar();
        configuracoesIniciais();
        chamarUber();
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificaStatusRequisicao();
    }

    @Override
    protected void onStop() {
        super.onStop();
        query.removeEventListener(valueEventListenerRequisicoes);
    }

    private void configuracoesIniciais(){

        etLocalCliente = findViewById(R.id.etMeuLocal);
        etLocalDestino = findViewById(R.id.etDestino);
        lLets = findViewById(R.id.linearLayoutEdits);
        btMain = findViewById(R.id.btChamarUber);

        auth = ConfiguracoesFirebase.getAuth();
        DatabaseReference database = ConfiguracoesFirebase.getDatabaseReference();
        requisicoesRef = database.child("requisicoes");
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogadoAuth();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        recuperandoLocalizacaoDoUsuario();
    }

    private void recuperandoLocalizacaoDoUsuario(){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(@NonNull Location location) {

                // recuperar Latitude Longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localizacaoAtual = new LatLng(latitude, longitude);

                // Atualizar GeoFire
                //UsuarioFirebase.atualizarDadosLocalizacao(localizacaoAtual, getApplicationContext());

                // Limpando Marcadores
                mMap.clear();
                // Criando Marcador


                if (statusRequisicao != null && !statusRequisicao.isEmpty()){
                    if (statusRequisicao.equals(Requisicao.STATUS_VIAGEM) || statusRequisicao.equals(Requisicao.STATUS_FINALIZADA)){
                        locationManager.removeUpdates(locationListener);
                    }
                    alteraInterfaceStatusRequisicao(statusRequisicao);
                }else{
                    if (ActivityCompat.checkSelfPermission(MainActivityCliente.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                10000,
                                10,
                                locationListener
                        );
                    }
                    adicionandoMarcadorDoPassageiro(localizacaoAtual, usuarioLogado.getNome());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoAtual, 15));
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(MainActivityCliente.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }
    }

    private void chamarUber(){
        btMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uberChamado){
                    requisicao.setStatus(Requisicao.STATUS_CANCELADA);
                    requisicao.atualizarStatus();
                    uberChamado = false;
                } else {
                    String enderecoDestino = etLocalDestino.getText().toString();
                    String enderecoCliente = etLocalCliente.getText().toString();
                    if (!enderecoDestino.equals("") ) {
                        if (!enderecoCliente.equals("")){
                            Address addressDestinoInicial = recuperarEndereco(enderecoCliente);
                            Address addressDestinoFinal = recuperarEndereco(enderecoDestino);
                            if (addressDestinoInicial != null) {
                                if (addressDestinoFinal != null){

                                    // Verificando EnderecoInicial
                                    confirmadoEndInicial = false;

                                    Destino destino2 = new Destino();
                                    destino2.setCidade(addressDestinoInicial.getAdminArea());
                                    destino2.setBairro(addressDestinoInicial.getSubLocality());
                                    destino2.setCep(addressDestinoInicial.getPostalCode());
                                    destino2.setRua(addressDestinoInicial.getThoroughfare());
                                    destino2.setNumero(addressDestinoInicial.getFeatureName());
                                    destino2.setLatitude(addressDestinoInicial.getLatitude());
                                    destino2.setLongitude(addressDestinoInicial.getLongitude());

                                    StringBuilder mensagem = new StringBuilder();

                                    mensagem.append("Cidade : ").append(destino2.getCidade());
                                    mensagem.append("\nBairro : ").append(destino2.getBairro());
                                    mensagem.append("\nCEP : ").append(destino2.getCep());
                                    mensagem.append("\nRua : ").append(destino2.getRua());
                                    mensagem.append("\nNumero : ").append(destino2.getNumero());

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityCliente.this);
                                    builder.setTitle("Confirme seu endereço Inicial!");
                                    builder.setMessage(mensagem);
                                    builder.setPositiveButton("Confirmar", (dialog, which) -> {
                                        // Salvar Requisição
                                        confirmadoEndInicial = true;
                                        Log.i("mensagem", "Confirmação 1");


                                        Destino destinoFinal = new Destino();
                                        destinoFinal.setCidade(addressDestinoFinal.getAdminArea());
                                        destinoFinal.setBairro(addressDestinoFinal.getSubLocality());
                                        destinoFinal.setCep(addressDestinoFinal.getPostalCode());
                                        destinoFinal.setRua(addressDestinoFinal.getThoroughfare());
                                        destinoFinal.setNumero(addressDestinoFinal.getFeatureName());
                                        destinoFinal.setLatitude(addressDestinoFinal.getLatitude());
                                        destinoFinal.setLongitude(addressDestinoFinal.getLongitude());

                                        StringBuilder mensagem2 = new StringBuilder();

                                        mensagem2.append("Cidade : ").append(destinoFinal.getCidade());
                                        mensagem2.append("\nBairro : ").append(destinoFinal.getBairro());
                                        mensagem2.append("\nCEP : ").append(destinoFinal.getCep());
                                        mensagem2.append("\nRua : ").append(destinoFinal.getRua());
                                        mensagem2.append("\nNumero : ").append(destinoFinal.getNumero());

                                        AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivityCliente.this);
                                        builder2.setTitle("Confirme seu Destino!");
                                        builder2.setMessage(mensagem2);
                                        builder2.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                // Salvar Requisição
                                                salvandoRequisicao(destinoFinal, destino2);
                                                uberChamado = true;
                                                Log.i("mensagem", "Confirmação 2");

                                            }
                                        });

                                        builder2.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                        AlertDialog alertDialog2 = builder2.create();
                                        alertDialog2.show();

                                    });

                                    builder.setNegativeButton("Cancelar", (dialog, which) -> {

                                    });
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                } else {
                                    criandoToast("Não foi possivel localizar o Endereço do Destino");
                                }
                            } else {
                                criandoToast("Não foi possivel localizar o Endereço Inicial");
                            }
                        } else {
                            criandoToast("Digite algum Endereço Inicial!");
                        }
                    } else {
                        criandoToast("Digite algum Endereço de Destino !");
                    }

                    // FIM
                }
            }
        });
    }

    private Address recuperarEndereco(String endereco){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);
            if (listaEnderecos != null && listaEnderecos.size() > 0){
                return listaEnderecos.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @SuppressLint("SetTextI18n")
    private void salvandoRequisicao(Destino destino, Destino destinoInicial){

        Requisicao requisicao = new Requisicao();
        requisicao.setDestino(destino);


        Usuario usuarioPassageiro = UsuarioFirebase.getDadosUsuarioLogadoAuth();
        usuarioPassageiro.setLatitude(String.valueOf(destinoInicial.getLatitude()));
        usuarioPassageiro.setLongitude(String.valueOf(destinoInicial.getLongitude()));
        requisicao.setPassageiro(usuarioPassageiro);
        LatLng destinoInicialLatLgn = new LatLng( destinoInicial.getLatitude(), destinoInicial.getLongitude());
        UsuarioFirebase.atualizarDadosLocalizacao(destinoInicialLatLgn, getApplicationContext());
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar();

        lLets.setVisibility(View.GONE);
        btMain.setText("Aguardando...");
    }

    private void verificaStatusRequisicao(){

        query = requisicoesRef.orderByChild("passageiro/id")
                .equalTo(usuarioLogado.getId());
        valueEventListenerRequisicoes = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Requisicao> listaDeRequisicoes = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    listaDeRequisicoes.add(ds.getValue(Requisicao.class));
                }
                Collections.reverse(listaDeRequisicoes);
                if (listaDeRequisicoes != null && listaDeRequisicoes.size() > 0) {
                    requisicao = listaDeRequisicoes.get(0);
                    if (requisicao != null){
                        if (!requisicao.getStatus().equals(Requisicao.STATUS_ENCERRADA)){
                            statusRequisicao = requisicao.getStatus();
                            destino = requisicao.getDestino();
                            passageiro = requisicao.getPassageiro();
                            localizacaoDestino = new LatLng( destino.getLatitude(), destino.getLongitude());
                            localizacaoPassageiro = new LatLng(Double.parseDouble(passageiro.getLatitude()),
                                    Double.parseDouble(passageiro.getLongitude()));
                            if (requisicao.getMotorista() != null){
                                motorista = requisicao.getMotorista();
                                localizacaoMotorista = new LatLng(Double.parseDouble(motorista.getLatitude()),
                                        Double.parseDouble(motorista.getLongitude()));
                            }
                            alteraInterfaceStatusRequisicao(statusRequisicao);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void alteraInterfaceStatusRequisicao(String statusRequisicao){

        btMain.setEnabled(true);
        if (statusRequisicao != null && !statusRequisicao.isEmpty()){
            switch (statusRequisicao) {

                case Requisicao.STATUS_AGUARDANDO:
                    lLets.setVisibility(View.GONE);
                    btMain.setText("Aguardando Confirmcação do Motorista...");
                    uberChamado = true;
                    // Criando Marcador Passageiro
                    adicionandoMarcadorDoPassageiro(localizacaoPassageiro, passageiro.getNome());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoPassageiro, 15));
                    break;

                case Requisicao.STATUS_A_CAMINHO:
                    lLets.setVisibility(View.GONE);
                    btMain.setText("Motorista a Caminho do Passageiro");
                    btMain.setEnabled(false);
                    uberChamado = true;

                    // Adicionando Marcadores
                    adicionandoMarcadorDoPassageiro(localizacaoPassageiro, passageiro.getNome());
                    adicionandoMarcadorDoMotorista(localizacaoMotorista, motorista.getNome());
                    // Centralizando Marcadores
                    centralizandoMarcadores(localizacaoMotorista, localizacaoPassageiro);

                    break;
                case Requisicao.STATUS_VIAGEM:
                    lLets.setVisibility(View.GONE);
                    btMain.setText("Em Viagem");
                    btMain.setEnabled(false);
                    uberChamado = true;

                    adicionandoMarcadorDoMotorista(localizacaoMotorista, motorista.getNome());
                    adicionandoMarcadorDoDestino(localizacaoDestino);

                    centralizandoMarcadores(localizacaoMotorista, localizacaoDestino);

                    break;

                case Requisicao.STATUS_FINALIZADA:
                    lLets.setVisibility(View.GONE);
                    uberChamado = true;
                    btMain.setEnabled(false);
                    btMain.setText("Viagem Finalizada");
                    adicionandoMarcadorDoDestino(localizacaoDestino);

                    // Calcular Preço
                    float distancia = Local.calcularDistancia(localizacaoPassageiro, localizacaoDestino);
                    float valor = distancia * 4;
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    String valorSt = decimalFormat.format(valor);


                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Encerrado Viagem");
                    builder.setMessage("Corrida Finalizada - R$" + valorSt);
                    builder.setCancelable(false);
                    builder.setNegativeButton("Encerrar viagem", (dialog, which) -> {
                        requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
                        requisicao.atualizarStatus();

                        finish();
                        startActivity(new Intent(getIntent()));
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    break;

                case Requisicao.STATUS_CANCELADA :
                    criandoToast("Requisição foi cancelada pelo passageiro!");
                    finish();
                    startActivity(new Intent(getIntent()));
                    requisicao.deletar();
                    break;
            }
        } else {
            adicionandoMarcadorDoPassageiro(localizacaoPassageiro, "Seu Local");
        }
    }

    private void centralizandoMarcadores(LatLng marker1, LatLng marker2){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(marker1);
        builder.include(marker2);

        LatLngBounds bounds = builder.build();

        // Recuperando Largura do Dispositivo
        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacamentoInterno = (int) (largura * 0.20);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds,
                        largura, altura, espacamentoInterno
                ));
    }

    private void adicionandoMarcadorDoMotorista(LatLng localizacao, String nome){

        if (motoristaMarker != null)
            motoristaMarker.remove();

        motoristaMarker = mMap.addMarker(new MarkerOptions()
                .position(localizacao)
                .title(nome)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro)
                ));

    }

    private void adicionandoMarcadorDoPassageiro(LatLng localizacao, String nome){

        if (passageiroMarker != null)
            passageiroMarker.remove();

        passageiroMarker = mMap.addMarker(new MarkerOptions()
                .position(localizacao)
                .title(nome)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)
                ));

    }

    private void adicionandoMarcadorDoDestino(LatLng localizacao){

        if (passageiroMarker != null)
            passageiroMarker.remove();

        if (destinoMarker != null)
            destinoMarker.remove();

        destinoMarker = mMap.addMarker(new MarkerOptions()
                .position(localizacao)
                .title("Destino")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino)
                ));

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configurandoToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Uber");
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


    private void criandoToast(String mensagem){
        Toast.makeText(MainActivityCliente.this,
                mensagem,
                Toast.LENGTH_SHORT).show();
    }
}