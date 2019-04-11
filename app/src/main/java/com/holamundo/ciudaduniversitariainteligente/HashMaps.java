package com.holamundo.ciudaduniversitariainteligente;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lautaro on 26/2/2017.
 * Modificated by Hernan Tonutti on 22/03/2019
 *
 * CAMBIOS
 *  Agregado hash del plano de FCM
 *
 **/
public class HashMaps {
    private Map<String, LatLngBounds> hashMapBounds = new HashMap<>();  //hashMap con el nombre del edificio y los limites del mismo
    private Map<String, Integer> hashMapID = new HashMap<>(); //hashMap con el nombre del edificio y el plano del mismo

    public HashMaps(){
        //Edificio 0 - FICH/FCBC
        hashMapBounds.put("ed0_0", new LatLngBounds(new LatLng(-31.640064, -60.673090), new LatLng(-31.639671, -60.671973)));
        hashMapBounds.put("ed0_1", new LatLngBounds(new LatLng(-31.640064, -60.673090), new LatLng(-31.639671, -60.671973)));
        hashMapBounds.put("ed0_2", new LatLngBounds(new LatLng(-31.640064, -60.673090), new LatLng(-31.639671, -60.671973)));
        hashMapBounds.put("ed0_3", new LatLngBounds(new LatLng(-31.640064, -60.673090), new LatLng(-31.639671, -60.671973)));
        hashMapBounds.put("ed4_0", new LatLngBounds(new LatLng(-31.639896, -60.671735), new LatLng(-31.639576, -60.670991)));
        hashMapBounds.put("ed4_1", new LatLngBounds(new LatLng(-31.639896, -60.671735), new LatLng(-31.639576, -60.670991)));

        //Edificio - FADU/FHUC/ISM
        hashMapBounds.put("ed5_0", new LatLngBounds(new LatLng(-31.640346, -60.673949), new LatLng(-31.639980, -60.673311)));
        hashMapBounds.put("ed5_1", new LatLngBounds(new LatLng(-31.640346, -60.673949), new LatLng(-31.639980, -60.673311)));
        hashMapBounds.put("ed5_2", new LatLngBounds(new LatLng(-31.640346, -60.673949), new LatLng(-31.639980, -60.673311)));
        hashMapBounds.put("ed5_3", new LatLngBounds(new LatLng(-31.640346, -60.673949), new LatLng(-31.639980, -60.673311)));
        hashMapBounds.put("ed5_4", new LatLngBounds(new LatLng(-31.640346, -60.673949), new LatLng(-31.639980, -60.673311)));

        //Edificio 1 - FCM
        hashMapBounds.put("ed1_0", new LatLngBounds(new LatLng(-31.639872, -60.670817), new LatLng(-31.639313, -60.670216)));

        //Edificio - Aulario
        hashMapBounds.put("ed6_0", new LatLngBounds(new LatLng(-31.640131, -60.674323), new LatLng(-31.639922, -60.674045)));
        hashMapBounds.put("ed6_1", new LatLngBounds(new LatLng(-31.640131, -60.674323), new LatLng(-31.639922, -60.674045)));
        hashMapBounds.put("ed6_2", new LatLngBounds(new LatLng(-31.640131, -60.674323), new LatLng(-31.639922, -60.674045)));
        hashMapBounds.put("ed6_3", new LatLngBounds(new LatLng(-31.640131, -60.674323), new LatLng(-31.639922, -60.674045)));
        hashMapBounds.put("ed6_4", new LatLngBounds(new LatLng(-31.640131, -60.674323), new LatLng(-31.639922, -60.674045)));
        hashMapBounds.put("ed6_5", new LatLngBounds(new LatLng(-31.640131, -60.674323), new LatLng(-31.639922, -60.674045)));


        //--------------------------------------------------------------------------------------------------------------------//

        hashMapID.put("ed0_0", R.drawable.ed2_0);
        hashMapID.put("ed0_1", R.drawable.ed2_1);
        hashMapID.put("ed0_2", R.drawable.ed2_2);
        hashMapID.put("ed0_3", R.drawable.ed2_3);
        hashMapID.put("ed1_0", R.drawable.ed3_0);
        hashMapID.put("ed4_0", R.drawable.ed4_0);
        hashMapID.put("ed4_1", R.drawable.ed4_1);
        hashMapID.put("ed5_0", R.drawable.ed5_0);
        hashMapID.put("ed5_1", R.drawable.ed5_1);
        hashMapID.put("ed5_2", R.drawable.ed5_2);
        hashMapID.put("ed5_3", R.drawable.ed5_3);
        hashMapID.put("ed5_4", R.drawable.ed5_4);
        hashMapID.put("ed6_0", R.drawable.ed6_0);
        hashMapID.put("ed6_1", R.drawable.ed6_1);
        hashMapID.put("ed6_2", R.drawable.ed6_2);
        hashMapID.put("ed6_3", R.drawable.ed6_3);
        hashMapID.put("ed6_4", R.drawable.ed6_4);
        hashMapID.put("ed6_5", R.drawable.ed6_5);

        //--------------------------------------------------------------------------------------------------------------------//
    }

    public Map<String, LatLngBounds> getHashMapsBound(){return hashMapBounds;}
    public Map<String, Integer> getHashMapID(){return hashMapID;}
}
