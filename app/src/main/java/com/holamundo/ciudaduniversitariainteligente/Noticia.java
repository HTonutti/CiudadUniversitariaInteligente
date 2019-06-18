package com.holamundo.ciudaduniversitariainteligente;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

public class Noticia {
    private String fecha;
    private String titulo;
    private String descrip;
    private String link;
    private int imagen;
    private Bitmap bitmapImage;

    //constructor
    public Noticia(String fecha, String titulo, String descrip, String link) {
        this.fecha = fecha;

        this.titulo = titulo;

        this.descrip = descrip;

        this.link = link;

        this.imagen = 0;

        this.bitmapImage= null;
    }

    //constructor
    public Noticia(String fecha, String titulo, String descrip, String link, Bitmap bitmapImage) {
        this.fecha = fecha;

        this.titulo = titulo;

        this.descrip = descrip;

        this.link = link;

        this.imagen = 0;

        this.bitmapImage= bitmapImage;
    }

    //getters y setters
    public Bitmap getBitmapImage() {
        return bitmapImage;
    }

    public void setBitmapImage(Bitmap bitmapImage) {
        this.bitmapImage = bitmapImage;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDescrip() {
        return descrip;
    }

    public void setDescrip(String descrip) {
        this.descrip = descrip;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getImagen() {
        return imagen;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }
}
