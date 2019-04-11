package com.holamundo.ciudaduniversitariainteligente;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by Lautaro on 29/11/2016.
 * Modificated by Hernan Tonutti on 01/04/2019
 *
 * CAMBIOS
 *  Cambiada forma de ver si el mapa esta creado en map activity (ahora no pide un atributo al main que deberia ser privado)
 */
public class MyLocationListener implements LocationListener {

    //mainActivity es el mapFragment
    private MapsFragment mainActivity;

    public void setMainActivity(MapsFragment mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onLocationChanged(Location loc) {
        mainActivity.setLat(loc.getLatitude());
        mainActivity.setLon(loc.getLongitude());

        /*Utilizo esto para actualizar mi marcador en el mapa
        Si el mapa no está creado, llamo a onMapReady
        Sino, a actualizaPosicion que solamente mueve el marcador
        */
        if(mainActivity.getEstadoMapa() == false) {
            mainActivity.activaMapa();
        }
        else{
            mainActivity.actualizaPosicion();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Este mŽtodo se ejecuta cuando el GPS es desactivado
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Este mŽtodo se ejecuta cuando el GPS es activado

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Este mŽtodo se ejecuta cada vez que se detecta un cambio en el
        // status del proveedor de localizaci—n (GPS)
        // Los diferentes Status son:
        // OUT_OF_SERVICE -> Si el proveedor esta fuera de servicio
        // TEMPORARILY_UNAVAILABLE -> Temp˜ralmente no disponible pero se
        // espera que este disponible en breve
        // AVAILABLE -> Disponible
    }
}
