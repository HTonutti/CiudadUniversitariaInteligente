package com.holamundo.ciudaduniversitariainteligente;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static android.os.SystemClock.sleep;

public class MenuFragment extends Fragment {
    private MainActivity oMainActivity = null;
    private String comida;
    private String fecha;
    private String postre;
    private TextView fechaView;
    private TextView comidaView;
    private TextView postreView;
    private SwipeRefreshLayout swiper;

    public MenuFragment() {
        this.fecha = null;
        this.comida = null;
        this.postre = null;
        this.fechaView= null;
        this.comidaView= null;
        this.swiper= null;
        actualizarMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflo menu
        final View rootView = inflater.inflate(R.layout.menu, container, false);

//        SQLiteOpenHelper helper= new SQLiteOpenHelper(getContext(), "Menu", null, 1) {
//            @Override
//            public void onCreate(SQLiteDatabase sqLiteDatabase) {
//                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS Menu (fecha TEXT, comida TEXT)");
//            }
//            @Override
//            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
//                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS Menu (fecha TEXT, comida TEXT)");
//            }
//
//        };
//        SQLiteDatabase db= helper.getWritableDatabase();
//        db.execSQL("CREATE TABLE IF NOT EXISTS Menu (fecha TEXT, comida TEXT)"); //sino la primera vez tira error
//        Cursor c = db.rawQuery("SELECT fecha, comida FROM Menu",null);
//        c.moveToFirst();
//        db.close();
//
//        if(c.getCount() == 1) {
//            TextView fecha= rootView.findViewById(R.id.fecha_menu);
//            fecha.setText(c.getString(0));
//            TextView comida= rootView.findViewById(R.id.comida_menu);
//            comida.setText(c.getString(1));
//        }else {
//            TextView fecha= rootView.findViewById(R.id.fecha_menu);
//            fecha.setText("Error cargando datos.");
//            TextView comida= rootView.findViewById(R.id.comida_menu);
//            fecha.setText("c= "+Integer.toString(c.getCount()));
//        }
        fechaView= rootView.findViewById(R.id.fecha_menu);
        comidaView= rootView.findViewById(R.id.comida_menu);
        postreView= rootView.findViewById(R.id.postre_menu);

        if(this.fecha== null || this.comida==null || this.postre==null) {
            actualizarMenu();
        }else {
            actualizarViews();
        }

        swiper = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_menu);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                actualizarMenu();
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        fechaView= null;
        comidaView= null;
        postreView= null;
        swiper= null;
        super.onDestroyView();
    }

    //desactiva refresh si swiper no es null
    public void actualizarMenu(){
        //directamente con firebase
        //firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        //get all docs from collection noticias
        db.collection("comedor").document("menu").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()) {
                                //firestore
                                Map<String, Object> mapa = document.getData();
                                if(mapa.get("fecha")!=null) {
                                    try {
                                        fecha = dateToString((Date) mapa.get("fecha"));
                                    }catch (Exception e){
                                        fecha = mapa.get("fecha").toString();
                                    }
                                }
                                else fecha= null;
                                comida= (String) mapa.get("comida");
                                postre= (String) mapa.get("postre");
                                if(comidaView!=null && fechaView!=null && postreView!=null) actualizarViews();
                            }
                            if(swiper!=null) swiper.setRefreshing(false);
                            if(swiper!=null) Toast.makeText(getContext(), "Menú actualizado.", Toast.LENGTH_SHORT);
                        }else {
                            if(swiper!=null) swiper.setRefreshing(false);
                        }
                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if(swiper!=null) swiper.setRefreshing(false);
                if(swiper!=null) Toast.makeText(getContext(), "No se pudo actualizar el menú.", Toast.LENGTH_SHORT);
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                if(swiper!=null) swiper.setRefreshing(false);
                if(swiper!=null) Toast.makeText(getContext(), "No se pudo actualizar el menú.", Toast.LENGTH_SHORT);
            }
        });

    }

    public void actualizarViews(){
        if(fecha== null) fechaView.setText("No se pudo obtener la fecha.");
        else fechaView.setText(fecha);

        if(comida==null) comidaView.setText("No se pudo obtener información.");
        else comidaView.setText(comida);

        if(postre==null) postreView.setText("No se pudo obtener información.");
        else postreView.setText(postre);
    }

    public void setMainActivity(MainActivity oMA){
        this.oMainActivity = oMA;
    }

    private String dateToString(Date valorFecha){
        String salida="";
        Calendar calendar= Calendar.getInstance();
//        calendar.setTimeInMillis(valorFecha);
        calendar.setTime(valorFecha);
        //hora y minutos
        salida = salida + String.format("%02d",calendar.get(Calendar.HOUR_OF_DAY));
        salida=salida+":"+String.format("%02d",calendar.get(Calendar.MINUTE))+", ";
        //dia
        switch (calendar.get(Calendar.DAY_OF_WEEK)){
            case Calendar.SUNDAY:{ salida= salida +"Domingo "; break; }
            case Calendar.MONDAY:{ salida= salida +"Lunes "; break; }
            case Calendar.TUESDAY:{ salida= salida +"Martes "; break; }
            case Calendar.WEDNESDAY:{ salida= salida +"Miércoles "; break; }
            case Calendar.THURSDAY:{ salida= salida +"Jueves "; break; }
            case Calendar.FRIDAY:{ salida= salida +"Viernes "; break; }
            case Calendar.SATURDAY:{ salida= salida +"Sábado "; break; }
            default:
        }
        //numero de dia
        salida= salida + Integer.toString(calendar.get(Calendar.DAY_OF_MONTH))+" de ";
        //mes
        switch (calendar.get(Calendar.MONTH)){
            case Calendar.JANUARY:{ salida= salida +"Enero "; break; }
            case Calendar.FEBRUARY:{ salida= salida +"Febrero "; break; }
            case Calendar.MARCH:{ salida= salida +"Marzo "; break; }
            case Calendar.APRIL:{ salida= salida +"Abril "; break; }
            case Calendar.MAY:{ salida= salida +"Mayo "; break; }
            case Calendar.JUNE:{ salida= salida +"Junio "; break; }
            case Calendar.JULY:{ salida= salida +"Julio "; break; }
            case Calendar.AUGUST:{ salida= salida +"Agosto "; break; }
            case Calendar.SEPTEMBER:{ salida= salida +"Septiembre "; break; }
            case Calendar.OCTOBER:{ salida= salida +"Octubre "; break; }
            case Calendar.NOVEMBER:{ salida= salida +"Noviembre "; break; }
            case Calendar.DECEMBER:{ salida= salida +"Diciembre "; break; }
            default:
        }
        //año
        salida= salida + "de " + Integer.toString(calendar.get(Calendar.YEAR));
        return salida;
    }
}
