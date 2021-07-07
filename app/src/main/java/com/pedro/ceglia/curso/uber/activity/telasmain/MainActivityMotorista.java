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
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.pedro.ceglia.curso.uber.R;
import com.pedro.ceglia.curso.uber.config.ConfiguracoesFirebase;
import com.pedro.ceglia.curso.uber.helper.Local;
import com.pedro.ceglia.curso.uber.helper.UsuarioFirebase;
import com.pedro.ceglia.curso.uber.model.Requisicao;
import com.pedro.ceglia.curso.uber.model.Usuario;

import java.text.DecimalFormat;

public class MainActivityMotorista extends AppCompatActivity implements OnMapReadyCallback {

    // Sdk Maps For Android
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localizacaoDestino;
    private LatLng localizacaoAtual;
    private LatLng localizacaoPassageiro;
    private LatLng localizacaoMotorista;
    private Marker motoristaMarker;
    private Marker passageiroMarker;
    private Marker destinoMarker;

    // Widgets
    private Button btAcessarCorrida;
    private FloatingActionButton fab;

    // Firebase
    private DatabaseReference database;
    private DatabaseReference requisicoes;
    private ValueEventListener valueEventListener;

    // Models
    private Usuario motorista;
    private Usuario passageiro;
    private Requisicao requisicao;
    private boolean permitindoMudancanoMapa;
    private boolean permitindoOnSupportNavigate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_motorista);

        configuracoesIniciais();
        configurandoToolbar();
        recuperandoBundles();


        // onClick
        aceitarCorrida();
        onClickFab();

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        permitindoMudancanoMapa = true;
        recuperarLocalizacaoUsuario();
        verificaStatusDaRequisicao();
    }

    private void configuracoesIniciais(){
        btAcessarCorrida = findViewById(R.id.btAceitarCorrida);
        fab = findViewById(R.id.fabDestinoGoogleMaps);
        database = ConfiguracoesFirebase.getDatabaseReference();

        // Inicializando Mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivityMotorista.this);
    }

    private void configurandoToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar_principal);
        toolbar.setTitle("Uber");
        toolbar.setSubtitle("Requisições");
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (permitindoOnSupportNavigate){
            finish();
        } else {
            criandoToast("Você não pode acessar outras corridas enquanto esta em uma!");
        }
        return false;
    }

    private void recuperandoBundles(){
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            motorista = (Usuario) bundle.getSerializable("motorista");
            permitindoOnSupportNavigate = bundle.getBoolean("verificacao");
            requisicao = (Requisicao) bundle.getSerializable("requisicao");
            if (requisicao != null){
                // recuperar Latitude Longitude
                double latitudePassageiro = Double.parseDouble(requisicao.getPassageiro().getLatitude());
                double longitudePassageiro = Double.parseDouble(requisicao.getPassageiro().getLongitude());

                localizacaoPassageiro = new LatLng(latitudePassageiro, longitudePassageiro);
                localizacaoDestino = new LatLng(requisicao.getDestino().getLatitude(),requisicao.getDestino().getLongitude());

                passageiro = requisicao.getPassageiro();

                if (requisicao.getStatus().equals(Requisicao.STATUS_A_CAMINHO) || requisicao.getStatus().equals(Requisicao.STATUS_VIAGEM)){
                    localizacaoMotorista = new LatLng(
                            Double.parseDouble(motorista.getLatitude()),
                            Double.parseDouble(motorista.getLongitude()));
                }
            }
        }
    }


    private void recuperarLocalizacaoUsuario(){

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(@NonNull Location location) {

                if (!requisicao.getStatus().equals(Requisicao.STATUS_AGUARDANDO)){
                    // recuperar Latitude Longitude
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    motorista = UsuarioFirebase.getDadosUsuarioLogadoAuth();
                    motorista.setLongitude(String.valueOf(longitude));
                    motorista.setLatitude(String.valueOf(latitude));
                    localizacaoAtual = new LatLng(latitude, longitude);


                    // Criando Marcadores
                    adicionandoMarcadorDoMotorista(localizacaoAtual, motorista.getNome());
                    UsuarioFirebase.atualizarDadosLocalizacao(localizacaoAtual, MainActivityMotorista.this);

                    requisicao.atualizarMotoristaLocalizacao(motorista);

                    if (permitindoMudancanoMapa){
                        centralizandoMarcadores(localizacaoAtual, localizacaoPassageiro);
                        permitindoMudancanoMapa = false;
                    }
                    /*if (requisicao.getStatus().equals(Requisicao.STATUS_A_CAMINHO)){
                        centralizandoMarcadores(localizacaoAtual, localizacaoPassageiro);
                    }

                     */


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
    }

    private void aceitarCorrida(){
        btAcessarCorrida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requisicao.setMotorista(motorista);
                localizacaoMotorista = new LatLng(
                        Double.parseDouble(motorista.getLatitude()),
                        Double.parseDouble(motorista.getLongitude()));
                requisicao.salvarMotorista(motorista);
                requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);
                requisicao.atualizarStatus();
                btAcessarCorrida.setOnClickListener(null);
            }
        });
    }


    private void verificaStatusDaRequisicao(){

        requisicoes = database.child("requisicoes").child(requisicao.getIdRequisicao());
        valueEventListener = requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requisicao = snapshot.getValue(Requisicao.class);
                switch(requisicao.getStatus()){

                    case Requisicao.STATUS_AGUARDANDO:
                        requisicaoAceitandoCorrida();
                        break;

                    case Requisicao.STATUS_A_CAMINHO :
                        requisicaoACaminho();
                        break;

                    case Requisicao.STATUS_VIAGEM :
                        requisicaoViagem();
                        permitindoMudancanoMapa = false;
                        break;

                    case Requisicao.STATUS_FINALIZADA :
                        requisicaoFinalizada();
                        permitindoMudancanoMapa = false;
                        break;

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void requisicaoAceitandoCorrida(){

        btAcessarCorrida.setText("Aceitar Corrida");
        fab.setVisibility(View.VISIBLE);


        adicionandoMarcadorDoDestino(localizacaoDestino, "Destino");
        adicionandoMarcadorDoPassageiro(localizacaoPassageiro, requisicao.getPassageiro().getNome());

        centralizandoMarcadores(localizacaoDestino, localizacaoPassageiro);


    }

    private void requisicaoACaminho(){

        btAcessarCorrida.setText("A Caminho Do Passageiro");
        fab.setVisibility(View.VISIBLE);

        if (destinoMarker!= null)
        destinoMarker.remove();

        adicionandoMarcadorDoMotorista(localizacaoMotorista, motorista.getNome());
        adicionandoMarcadorDoPassageiro(localizacaoPassageiro, requisicao.getPassageiro().getNome());

        centralizandoMarcadores(localizacaoMotorista, localizacaoPassageiro);

        // Iniciar monitoramento motorista/pasageiro
        iniciarMonitoramentoCorrida(motorista, localizacaoPassageiro);
    }

    private void requisicaoViagem(){

        btAcessarCorrida.setText("A Caminho Do Destino");
        fab.setVisibility(View.VISIBLE);

        // Exibir Marcadores
        if (localizacaoAtual != null){
            adicionandoMarcadorDoMotorista(localizacaoAtual, motorista.getNome());
        }else {
            adicionandoMarcadorDoMotorista(localizacaoMotorista, motorista.getNome());
        }

        adicionandoMarcadorDoDestino(localizacaoDestino, "Destino");

        // Centralizar Marcadores
        if (localizacaoAtual != null){
            centralizandoMarcadores(localizacaoAtual, localizacaoDestino);
        }else {
            centralizandoMarcadores(localizacaoMotorista, localizacaoDestino);
        }
        // Iniciar monitoramento motorista/pasageiro
        iniciarMonitoramentoCorrida(motorista, localizacaoDestino);
    }

    private void requisicaoFinalizada(){

        fab.setVisibility(View.GONE);
        permitindoOnSupportNavigate = false;
        if (passageiroMarker != null)
            passageiroMarker.remove();

        if (destinoMarker != null)
            destinoMarker.remove();

        adicionandoMarcadorDoDestino(localizacaoDestino, "Destino");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoDestino, 15));


        btAcessarCorrida.setText("Corrida Finalizada - R$" + requisicao.getPreco() );

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Encerrado Viagem");
        builder.setMessage("Corrida Finalizada - R$" + requisicao.getPreco());
        builder.setCancelable(false);
        builder.setNegativeButton("Encerrar viagem", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
                requisicao.atualizarStatus();

                finish();

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void iniciarMonitoramentoCorrida(Usuario m, LatLng localDestino){

        // Iniciar GeoFire
        DatabaseReference localUsuario = ConfiguracoesFirebase.getDatabaseReference()
                .child("local_usuario");
        GeoFire geoFire = new GeoFire(localUsuario);

        // Adicionar círculo no passageiro
        Circle circulo = mMap.addCircle(
                new CircleOptions()
                        .center(localDestino)
                        .radius(50)// em metros
                        .fillColor(Color.argb(90,255,153,0))
                        .strokeColor(Color.argb(190,255,152,0))
        );

        // ff
        GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(localDestino.latitude, localDestino.longitude),
                0.05 //  em km
        );

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                // Caso exista um marcador dentro da area desejada
                // esse método sera retornado
                Log.i("Chaves", key + "," + m.getId());
                if(key.equals(m.getId())){
                    criandoToast("Chegou ao Destino");

                    String status = requisicao.getStatus();

                    if (status != null && !status.isEmpty()){

                        switch (status){
                            case Requisicao.STATUS_A_CAMINHO :

                                // Altera a UI
                                btAcessarCorrida.setText("Iniciar Corrida");
                                btAcessarCorrida.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Alterar status da requisção
                                        requisicao.setStatus(Requisicao.STATUS_VIAGEM);
                                        requisicao.atualizarStatus();

                                        //Remove Listener
                                        geoQuery.removeAllListeners();
                                        circulo.remove();
                                        btAcessarCorrida.setText("Em Viagem");
                                        btAcessarCorrida.setOnClickListener(null);
                                    }
                                });
                                break;

                            case Requisicao.STATUS_VIAGEM :

                                // Altera a UI
                                btAcessarCorrida.setText("Finalizar Corrida");
                                btAcessarCorrida.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Alterar status da requisção
                                        requisicao.setStatus(Requisicao.STATUS_FINALIZADA);
                                        requisicao.atualizarStatus();

                                        //Remove Listener
                                        geoQuery.removeAllListeners();
                                        circulo.remove();
                                        btAcessarCorrida.setText("Viagem Finalizada");
                                        btAcessarCorrida.setOnClickListener(null);
                                    }
                                });

                                break;

                            case Requisicao.STATUS_CANCELADA :
                                criandoToast("Requisição foi cancelada pelo passageiro!");
                                finish();
                                break;

                        }
                    }
                }
            }

            @Override
            public void onKeyExited(String key) {
                // Esse método é retornado
                // quando um marcador sai da area desejada
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                // Esse método é retornado
                // quando os marcadores se movimentam porem dentro da area desejada
            }

            @Override
            public void onGeoQueryReady() {
                // esse método é retornado
                // quando todos os recursos são carregados

            }
            @Override
            public void onGeoQueryError(DatabaseError error) {
                // Esse método é chamado quando ocorre um erro
            }
        });
    }

    private void centralizandoMarcadores(LatLng marker1, LatLng marker2){

        if (mMap != null){
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
        if (mMap != null) {
            if (passageiroMarker != null)
                passageiroMarker.remove();

            passageiroMarker = mMap.addMarker(new MarkerOptions()
                    .position(localizacao)
                    .title(nome)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario)
                    ));
        }

    }

    private void adicionandoMarcadorDoDestino(LatLng localizacao, String nome){
        if (mMap != null) {
            if (passageiroMarker != null)
                passageiroMarker.remove();

            if (destinoMarker != null)
                destinoMarker.remove();

            if (localizacao != null) {
                destinoMarker = mMap.addMarker(new MarkerOptions()
                        .position(localizacao)
                        .title(nome)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino)
                        ));
            }
        }

    }

    private void onClickFab(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = requisicao.getStatus();
                if (!status.isEmpty()){

                    String latFab = "";
                    String lngFab = "";

                    switch ( status ){
                        case Requisicao.STATUS_A_CAMINHO :
                            latFab = String.valueOf(localizacaoPassageiro.latitude);
                            lngFab = String.valueOf(localizacaoPassageiro.longitude);
                            break;

                        case Requisicao.STATUS_VIAGEM :
                            latFab = String.valueOf(localizacaoDestino.latitude);
                            lngFab = String.valueOf(localizacaoDestino.longitude);
                            break;

                    }

                    // Abrir rota
                    String latLongFab = latFab + "," + lngFab;
                    Uri uri = Uri.parse("google.navigation:q="+ latLongFab +"&mode=d");
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    i.setPackage("com.google.android.apps.maps");
                    startActivity(i);

                }
            }
        });
    }

    private void criandoToast(String texto){
        Toast.makeText(this,texto,Toast.LENGTH_SHORT).show();
    }



}