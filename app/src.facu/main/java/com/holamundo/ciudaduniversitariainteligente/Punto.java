package com.holamundo.ciudaduniversitariainteligente;

import java.util.Vector;

/**
 * Created by Lautaro on 28/11/2016.
 */
public class Punto implements Comparable<Punto> {
    private double Latitud;
    private double Longitud;
    private String Edificio;
    private Vector<Punto> Vecinos;
    private int Piso;
    private String Nombre;
    private Integer Imagen;
    private Integer id;

    private Punto Padre;
    public float costo;


    public Punto(Integer id, double Lat, double Lon, String oEdificio, int piso, String oNombre, Integer oImg){
        this.Latitud = Lat;
        this.Longitud = Lon;
        this.Edificio = oEdificio;
        this.Piso = piso;
        this.Nombre = oNombre;
        Vecinos = new Vector<>();
        this.costo = 0;
        Padre = null;
        if(oImg == -1){
            oImg = null;
        }
        else{
            this.Imagen = oImg;
        }
        this.Imagen = oImg;
        this.id = id;
    }

    public void addVecino(Punto P){
        Vecinos.add(P);
    }

    //Setters y getters
    public double getLatitud(){return Latitud;}
    public double getLongitud(){return Longitud;}
    public String getEdificio(){return Edificio;}
    public Integer getPiso(){return Piso;}
    public String getNombre(){return Nombre;}
    public int cantVecinos(){return Vecinos.size();}
    public Punto getVecino(int i){return Vecinos.elementAt(i);}
    public Integer getImagen() {return Imagen;}
    public Integer getId(){return id;}
    public void setPadre (Punto P){this.Padre = P;}
    public Punto getPadre(){return this.Padre;}


    //Funcion de comparacion para el heap de armaCamino
    @Override
    public int compareTo(Punto punto) {
        int c = 0;
        if(this.equals(punto)){
        }
        else{
            if (this.costo > punto.costo){
                c = 1;
            }
            else{
                c = -1;
            }
        }
        return c;
    }

}
