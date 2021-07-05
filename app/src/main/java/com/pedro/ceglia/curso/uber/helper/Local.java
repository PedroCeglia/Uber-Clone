package com.pedro.ceglia.curso.uber.helper;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

public class Local {


    public static float calcularDistancia(LatLng latLngInicia, LatLng latLngFinal){
        Location localI = new Location("Local Inicial");
        localI.setLatitude(latLngInicia.latitude);
        localI.setLongitude(-43.2052141);
        Log.i("latitude M", String.valueOf(latLngInicia.latitude));
        Log.i("longitude M", String.valueOf(latLngInicia.longitude));

        Location localF = new Location("Local Final");
        localF.setLatitude(-22.9612867);
        localF.setLongitude(-43.2052141);
        Log.i("latitude C", String.valueOf(latLngFinal.latitude));
        Log.i("longitude C", String.valueOf(latLngFinal.longitude));

        Log.i("Distancia em KM", String.valueOf(localI.distanceTo(localF) / 1000));
        return localI.distanceTo(localF) / 1000;
    }

    public static String formatarDistancia(float distancia){

        if (distancia < 1){
            distancia = distancia * 1000; // em Metros
             return Math.round(distancia) + " M ";
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            return decimalFormat.format(distancia) + " KM ";
        }
    }

}
