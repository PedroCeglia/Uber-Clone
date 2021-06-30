package com.pedro.ceglia.curso.uber.helper;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

public class Local {
    public static float calcularDistancia(LatLng latLngInicia, LatLng latLngFinal){

        Location localI = new Location("Local Inicial");
        localI.setLatitude(latLngInicia.latitude);
        localI.setLongitude(latLngInicia.longitude);

        Location localF = new Location("Local Inicial");
        localF.setLatitude(latLngFinal.latitude);
        localF.setLongitude(latLngFinal.longitude);

        float distancia = localI.distanceTo(localF) / 1000; // Em Metros /1000 = em Km
        return distancia;
    }

    public static String formatarDistancia(float distancia){

        String distanciaFormatada;
        if (distancia < 1){
            distancia = distancia * 1000; // em Metros
            distanciaFormatada = Math.round(distancia) + "M ";
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            distanciaFormatada = decimalFormat.format(distancia) + "KM ";
        }

        return distanciaFormatada;
    }

}
