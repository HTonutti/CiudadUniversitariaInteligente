package com.holamundo.ciudaduniversitariainteligente;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Map;
import java.util.Vector;

/**
 * Created by Lautaro on 29/11/2016.
 * Modificated by Hernán Tonutti on 01/04/2019
 *
 * CAMBIOS
 *  Agregado de marcadores personalizados
 *  Cambios en modo que la camara siga la ubicacion actual
 *          Agregada funcionalidad para que lo haga o no segun lo defina un boton
 *          Antes continuamente seguia la camara (molesta a la hora de ver algo en el mapa sin estar en la ciudad universitaria)
 *  Agregada funcionalidad para que dibujaCamino lleve la camara al primer punto del camino y mostrarNodos a la ciudad universitaria
 *  Agregado de funcionalidades con los overlays
 *          Funciones para definir y cargar overlays
 *          Muestra de los overlays segun el piso
 *          Overlays se pueden ver sin la necesidad de que se haya buscado algo
 *  Agregado boton para ubicacion actual
 *  Cambios en ubicacion
 *          Si el gps no da una ubicacion mueve la camara hacia la ciudad universitaria
 *          Movimiento suave con cualquier cambio de camara
 *          Aprovechamiento del los requestUpdate y removeUpdate del location manager
 *  Agregado marcador que indica el corrimiento de pisos que hay en el cubo (por tener entrepiso cuenta todos los pisos como numero de piso + 1)
 *  Quitado del marcador de ubicacion actual personalizado, aprovechamiento del brindado por la api
 *  Quitado de funcion de cargar imagenes en el infoview personalizado (estaba en desuso)
 *  Creadas funciones de getEstadoMapa y activaMapa para brindar encapsulamiento, asi el atributo miMapa para a ser privado
 *  Reordenamiento y limpieza general del codigo
 *
 * Modificated by Sebastián Fenoglio on 27/05/2019
 *  Llamada a firstTimeMapReadyCallback() de MainActivity para que las notificaciones se ejecuten luego de que el mapa esté listo.
 * */
public class MapsFragment extends Fragment implements OnMapReadyCallback, SensorEventListener {

    private MainActivity oMainActivity = null;
    private boolean primeraVez=true;

    private GoogleMap miMapa = null;
    
    private boolean CamaraUbiActual = true;
    private LocationManager mlocManager;
    private MyLocationListener mlocListener;
    private int cantPisos = 0;
    private int pisoActual = 0;

    //Usado para los overlays, numeros de los edificios en el hashmap y el maximo de pisos que se dan en estos
    int[] nroedi = new int[]{0, 1, 4, 5, 6};
    int maxPisos = 6;

    private Vector<PolylineOptions> misPolilineas = new Vector<>(); //Vector de polilineas, cada elemento será una polilinea para un piso
    private Vector<MarkerOptions> marcadoresPiso = new Vector<>(); //Vector de marcadores por piso, dos marcadores por polilinea por piso
    //uno en cada extremo
    private Vector<MarkerOptions> misMarcadores = new Vector<>();  //Vector de nodos para mostrar nodos sueltos (baños, bares, oficinas, etc)
    //Opciones del marcador para advertir el corrimiento de pisos que sucede en el cubo
    private MarkerOptions marcadorCubo;

    //Overlays que se encuentran actualmente mostrados en el mapa
    private Vector<GroundOverlay> OverlaysCargados = new Vector<>();
    //Overlays definidos segun hashmap, el vector de afuera es cada edificio y el vector interno indica los distintos pisos de cada uno de estos
    private Vector<Vector<GroundOverlayOptions>> misOverlays = new Vector<>();

    private HashMaps miHashMaps = new HashMaps();
    //Hash de los ids de los edificios con las posiciones donde deberian ubicarse
    private Map<String, LatLngBounds> hashMapBounds = miHashMaps.getHashMapsBound();
    //Hash de los ids de los edificios con las imagenes de cada uno y sus pisos
    private Map<String, Integer> hashMapID = miHashMaps.getHashMapID();

    //Latitud y longitud de donde se situa la camara
    private double lat;
    private double lon;

    //Coordenadas de la ciudad universitaria
    private LatLng latlngCU=new LatLng(-31.640543,-60.672544);

    //Alto y ancho de los marcadores personalizados
    private int alto_marcadores=100;
    private int ancho_marcadores=100;

    private static View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException e) {

        }

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Marcador para advertir el corrimiento de pisos que sucede en el cubo
        marcadorCubo=new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_advertencia))
                .title("El cubo cuenta todos los pisos como: numero de piso + 1. Ya que entre planta baja y primer piso se encuentra un entrepiso")
                .position(new LatLng(-31.639873, -60.673998));


        //LocationManager
        mlocManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        mlocListener.setMainActivity(this);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return null;
        }

        //Agrego los overlays del primer piso de todos los edificios
        DefineOverlays();
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        miMapa = googleMap;

        miMapa.addMarker(marcadorCubo);

        //Esto es para armar el grafo, clickeando encima del overlay y viendo la lat y long del punto
//        miMapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//                Log.d("Prueba", latLng.latitude + ", " + latLng.longitude);
//            }
//        });

        //Hago mi propia InfoWindow, para poder mostrar una imagen del nodo cuando hago click en el y ver el lugar que está señalado
        miMapa.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getActivity().getLayoutInflater().inflate(R.layout.custom_infowindow, null);
                TextView titulo = v.findViewById(R.id.titulo);

                titulo.setText(marker.getTitle());
                return v;
            }
        });

        //Checkea por los permisos (generado automaticamente)
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //Activo la muestra de la localizacion junto con el boton de mi localizacion y el de zoom
        miMapa.setMyLocationEnabled(true);
        miMapa.getUiSettings().setMyLocationButtonEnabled(true);
        miMapa.getUiSettings().setMapToolbarEnabled(false);
//        miMapa.getUiSettings().setZoomControlsEnabled(true);

        //Actualiza y lleva a la ubicacion actual
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        mlocManager.requestSingleUpdate(criteria,mlocListener,null);
        //Espero un tiempo para que pueda refinir la posicion el gps
//        SystemClock.sleep(1000);
        mlocManager.removeUpdates(mlocListener);
        Location location = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //Si esta la ubicacion activa que guarde mi ubicacion actual, sino la ciudad universitaria
        if(location==null){
            this.lat=latlngCU.latitude;
            this.lon=latlngCU.longitude;
        }
        else {
            this.lat = location.getLatitude();
            this.lon = location.getLongitude();
        }
        //Si inicio por primera vez que me lleve a mi ubicacion actual, sino molesta cada cambio del fragment volviendo a la ubicacion actual
        if(CamaraUbiActual) {
            LatLng position = new LatLng(this.lat, this.lon);
            miMapa.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 18));
        }

        //Agrego los marcadores adicionales (Edificios, baños, bares,etc), si los hay
        for (int i = 0; i < misMarcadores.size(); i++) {
            String texto;
            if (pisoActual == 0) {
                texto = "Planta Baja";
            } else {
                texto = "Piso " + pisoActual;
            }
            if (misMarcadores.elementAt(i).getTitle().contains(texto)) {
                miMapa.addMarker(misMarcadores.elementAt(i));
            }
        }

        //Agrego polilinea si la hay
        if (misPolilineas.size() != 0) {
            miMapa.addPolyline(misPolilineas.elementAt(pisoActual));
            miMapa.addMarker(marcadoresPiso.elementAt(2 * pisoActual));
            miMapa.addMarker(marcadoresPiso.elementAt(2 * pisoActual + 1));
        }
        
        //Agrego los overlays
        CargaOverlays();

        //Mapa listo---> llamar notificaciones
        if(primeraVez) {
            oMainActivity.firstTimeMapReadyCallback();
            primeraVez=false;
        }
    }

    //Setters, getters y demas utilidades
    public void setLat(double l) {
        this.lat = l;
    }

    public void setLon(double l) {
        this.lon = l;
    }

    public double getLat() {
        return this.lat;
    }

    public double getLon() {
        return this.lon;
    }

    public int getCantPisos() {
        return cantPisos;
    }

    public void setPisoActual(int p) {
        this.pisoActual = p;
    }

    public int getPisoActual() {
        return pisoActual;
    }

    public boolean modoPolilinea() {
        return !misPolilineas.isEmpty();
    }

    public boolean getEstadoMapa() {if(miMapa==null) return false; else return true; }

    public void activaMapa() {onMapReady(miMapa);}

    public void moverCamara(LatLng lugar, int zoom) {
        if(zoom!=0) {
            CameraUpdate localizacion = CameraUpdateFactory.newLatLngZoom(lugar, zoom);
            miMapa.animateCamera(localizacion);
        }
        else {
            CameraUpdate localizacion = CameraUpdateFactory.newLatLng(lugar);
            miMapa.animateCamera(localizacion);
        }
    }

    public void setCamaraUbiActual(boolean LlevaAUbicacionActual){
            CamaraUbiActual=LlevaAUbicacionActual;
    }

    //Cambia entre seguir la posición actual actualizando cada unos metros de movimiento o que no siga
    public void modoSeguimiento(boolean seguir) {
        //Checkeo de permisos generados automaticamente
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        setCamaraUbiActual(true);

        //Defino si se actualiza constantemente la ubicacion o remuevo las actualizaciones
        if (seguir)
            mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 200, 1, mlocListener);
        else
            mlocManager.removeUpdates(mlocListener);
    }

    //Define los overlays del primer piso de todos los edificios
    public void DefineOverlays() {
        for (int i = 0; i < nroedi.length; i++) {
            Vector<GroundOverlayOptions> OverlaysPiso = new Vector<>();
            for (int j = 0; j < maxPisos; j++) {
                String ed = "ed" + nroedi[i] + "_" + j;
                if (hashMapBounds.containsKey(ed)) {
                    OverlaysPiso.add(new GroundOverlayOptions()
                            .positionFromBounds(hashMapBounds.get(ed))
                            .image(BitmapDescriptorFactory.fromResource(hashMapID.get(ed))));
                }

            }
            misOverlays.add(OverlaysPiso);
        }
    }

    //Carga los overlays segun el piso actual que se este actualmente
    public void CargaOverlays() {
        //Remuevo todos los overlays cargados previamente uno por uno y luego vacio el vector
        for(int i = 0; i< OverlaysCargados.size();i++)
            OverlaysCargados.elementAt(i).remove();
        OverlaysCargados.clear();

        //Agrego los overlays al mapa y los guardo en el vector
        if (misOverlays.size() != 0) {
            for (int i = 0; i < misOverlays.size(); i++) {
                if(misOverlays.elementAt(i).size()>pisoActual)
                    OverlaysCargados.add(miMapa.addGroundOverlay(misOverlays.elementAt(i).elementAt(pisoActual)));
            }
        }
    }

    //Dibuja los marcadores cambiando su logo segun lo que se pretende mostrar (edificios, aulas, bares, etc)
    public void SeteaMarcadores(Vector<MarkerOptions> Marcadores, Vector<Punto> Nodos, int Posicion, String texto) {
        int id;
        if (Nodos.elementAt(Posicion).getNombre().contains("Baños")) {
            id=R.mipmap.ic_banio;
        } else if (Nodos.elementAt(Posicion).getNombre().equals("Cantina")) {
            id=R.mipmap.ic_cantina;
        } else if (Nodos.elementAt(Posicion).getNombre().equals("Escalera")) {
            id=R.mipmap.ic_flechas;
        } else if (Nodos.elementAt(Posicion).getNombre().contains("Aula")) {
            id=R.mipmap.ic_aula;
        } else if (Nodos.elementAt(Posicion).getNombre().startsWith("Entrada")) {
            id=R.mipmap.ic_edificio;
        } else if (Nodos.elementAt(Posicion).getNombre().contains("Fotocopiadora")) {
            id=R.mipmap.ic_fotocopiadora;
        } else if (Nodos.elementAt(Posicion).getNombre().startsWith("Lab")) {
           id=R.mipmap.ic_laboratorio;
        } else if (Nodos.elementAt(Posicion).getNombre().contains("Taller")) {
            id=R.mipmap.ic_taller;
        }
        else {
            Marcadores.add(new MarkerOptions()
                    .position(new LatLng(Nodos.elementAt(Posicion).getLatitud(), Nodos.elementAt(Posicion).getLongitud()))
                    .title(Nodos.elementAt(Posicion).getNombre() + " - " + texto)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            return;
        }
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(id,null);
        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmapdraw.getBitmap(), ancho_marcadores, alto_marcadores, false);
        Marcadores.add(new MarkerOptions()
                .position(new LatLng(Nodos.elementAt(Posicion).getLatitud(), Nodos.elementAt(Posicion).getLongitud()))
                .title(Nodos.elementAt(Posicion).getNombre() + " - " + texto)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    //Limpio el mapa de polilineas, marcadores, etc
    public void limpiarMapa(boolean seteaPiso) {
        misPolilineas.clear();
        marcadoresPiso.clear();
        misMarcadores.clear();
        miMapa.clear();
        if(seteaPiso)
            setPisoActual(0);
        CargaOverlays();
        miMapa.addMarker(marcadorCubo);
    }

    //Actualizo mi posición si me moví. Quito mi marcador y lo pongo en donde corresponde
    @TargetApi(Build.VERSION_CODES.M)
    void actualizaPosicion() {
        LatLng position = new LatLng(this.lat, this.lon);
        if(CamaraUbiActual)
            moverCamara(position, 0);


        if (!misPolilineas.isEmpty() && pisoActual + 1 <= misPolilineas.size()) {
            cambiarPolilinea(pisoActual);
        } else if (pisoActual + 1 > misPolilineas.size() && !misPolilineas.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "Su objetivo está en un piso inferior", Toast.LENGTH_LONG).show();
        }
        if (!misMarcadores.isEmpty() && pisoActual + 1 <= misMarcadores.size()) {
            cambiarNodos(pisoActual);
        } else if (pisoActual + 1 > misMarcadores.size() && !misMarcadores.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "Su objetivo está en un piso inferior", Toast.LENGTH_LONG).show();
        }

        CargaOverlays();

    }

    //Obtengo mi latitud y longitud en un objeto LatLng
    public LatLng getPosicion() {
        return new LatLng(this.lat, this.lon);
    }

    //Recibo un vector de puntos y creo un polilinea con ellos
    public void dibujaCamino(Vector<Punto> path) {
        moverCamara(new LatLng(path.elementAt(0).getLatitud(),path.elementAt(0).getLongitud()),18);
        limpiarMapa(false);
        cantPisos = 0;

        //Veo cuantos pisos hay
        for (int i = 0; i < path.size(); i++) {
            if (path.elementAt(i).getPiso() > cantPisos) {
                cantPisos = path.elementAt(i).getPiso();
            }
        }

        //Creo las polilineas y overlays que voy a usar
        cantPisos = cantPisos + 1;
        for (int i = 0; i < cantPisos; i++) {
            PolylineOptions p = new PolylineOptions().width(5).color(Color.RED);
            misPolilineas.add(p);
        }

        //Agrego puntos a las polilineas segun piso e identifico por que pisos pasa mi polilinea
        for (int i = 0; i < path.size(); i++) {
            misPolilineas.elementAt(path.elementAt(i).getPiso()).add(new LatLng(path.elementAt(i).getLatitud(), path.elementAt(i).getLongitud()));
        }

        //Seteo el primer marcador
        SeteaMarcadores(marcadoresPiso,path,0,"");

        //Busco el resto de marcadores por piso que voy a tener
        for (int i = 1; i < path.size() - 1; i++) {
            if (path.elementAt(i).getPiso() != path.elementAt(i + 1).getPiso()) {
                SeteaMarcadores(marcadoresPiso,path,i,"");
                SeteaMarcadores(marcadoresPiso,path,i+1,"");
            }
        }

        //Seteo el ultimo marcador
        SeteaMarcadores(marcadoresPiso,path,path.size()-1,"");

    }

    //Recibo un conjunto de puntos y creo marcadores para todos ellos
    public void mostrarNodos(Vector<Punto> nodos) {
//        moverCamara(latlngCU,18);
        limpiarMapa(false);
        Vector<String> edificios = new Vector<>();
        cantPisos = 0;
        for (int i = 0; i < nodos.size(); i++) {
            String texto;
            if (nodos.elementAt(i).getPiso() == 0) {
                texto = "Planta Baja";
            } else {
                texto = "Piso " + nodos.elementAt(i).getPiso();
            }
            //Cuento la cantidad de pisos en donde encontre lo que busco
            if (nodos.elementAt(i).getPiso() > cantPisos) {
                cantPisos = nodos.elementAt(i).getPiso();
            }

            //Agrego los marcadores segun el tipo
            SeteaMarcadores(misMarcadores,nodos,i,texto);
        }
    }

    public void cambiarPolilinea(int piso) {
        miMapa.clear();
        miMapa.addMarker(marcadorCubo);
        //Si no hay polilineas en ese piso que no haga nada
        if(misPolilineas.size()>piso) {
            miMapa.addPolyline(misPolilineas.elementAt(piso));
            miMapa.addMarker(marcadoresPiso.elementAt(2 * piso));
            miMapa.addMarker(marcadoresPiso.elementAt(2 * piso + 1));
        }
    }

    //Funcion para actualizar los nodos según el piso que se quiera ver
    public void cambiarNodos(int piso) {
        miMapa.clear();
        miMapa.addMarker(marcadorCubo);
        for (int i = 0; i < misMarcadores.size(); i++) {
            if (piso == 0) {
                if (misMarcadores.elementAt(i).getTitle().contains("Planta Baja")) {
                    miMapa.addMarker(misMarcadores.elementAt(i));
                }
            } else {
                if (misMarcadores.elementAt(i).getTitle().contains("Piso " + piso)) {
                    miMapa.addMarker(misMarcadores.elementAt(i));
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setMainActivity(MainActivity oMA){
        this.oMainActivity = oMA;
    }
}
