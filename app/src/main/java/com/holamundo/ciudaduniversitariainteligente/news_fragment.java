package com.holamundo.ciudaduniversitariainteligente;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by Sebastian Fenoglio on 15/05/2019.
 */
public class news_fragment extends Fragment {
    private MainActivity oMainActivity = null;
    private ArrayList<Noticia> noticias;
    private ArrayList<String> ids;
    private SwipeRefreshLayout swiper;
    private ListView lv;
    private news_adapter adapter;
    private Noticia ultimaNoticia;

    public news_fragment() {
        noticias=new ArrayList<Noticia>();
        ids= new ArrayList<String>();
        swiper= null;
        lv= null;
        adapter= null;

        actualizarNoticias();
    }

    @Override
    public void onDestroyView() {
        swiper= null;
        lv= null;
        adapter= null;
        super.onDestroyView();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflo mi listView
        final View rootView = inflater.inflate(R.layout.news, container, false);

        // actualizo si no hay noticias
        if(noticias.size()==0){
            actualizarNoticias();
        }

        //array adapter
        adapter= new news_adapter(getActivity(), this.noticias, oMainActivity);
        lv= (ListView) rootView.findViewById(R.id.lista_news);
        lv.setAdapter(adapter);

        //lv.setOnClickListener();
        swiper = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_news);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                actualizarNoticias();
            }
        });

        return rootView;

    }



    public void actualizarNoticias(){
//    public SQLiteDatabase obtenerNoticias(){
//        SQLiteOpenHelper helper= new SQLiteOpenHelper(getActivity(), "Noticias", null, 1) {
//            @Override
//            public void onCreate(SQLiteDatabase sqLiteDatabase) {
//                cargarDatos(sqLiteDatabase);
//
//            }
//
//            @Override
//            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
//                cargarDatos(sqLiteDatabase);
//            }
//
//            public void cargarDatos(SQLiteDatabase sqLiteDatabase){
//                sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Noticias");
//                sqLiteDatabase.execSQL("CREATE TABLE Noticias (id TEXT, fecha TEXT, titulo TEXT, descripcion TEXT, link TEXT)");
//
//                for (int i = 0; i < 10; i++) {
//                    sqLiteDatabase.execSQL("INSERT INTO Noticias (id, fecha, titulo, descripcion, link) VALUES "
//                            + "('" + i +"', 'fecha" + Integer.toString(i) + "', 'titulo " + Integer.toString(i) + "', 'descripciondescripciondescripciondescripciondescripcion', 'link')");
//                }
//
//            }
//        };
//
//        return helper.getReadableDatabase();
        //firebase storage
        // Create a storage reference from our app
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        //firestore
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        //settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        //get all docs from collection noticias
        db.collection("noticias").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    //primero borro lo que hay
                    noticias.clear();
                    ids= new ArrayList<String>();


                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //firestore
                        Map<String, Object> mapa = document.getData();

                        //si son nulos se le pone string vacio
                        String fecha, titulo, descrip, link;
                        if(mapa.get("fecha")!=null) {
                            try {
                                fecha = dateToString((Date) mapa.get("fecha"));
                            }catch (Exception e){
                                fecha = mapa.get("fecha").toString();
                            }
                        }
                        else fecha= "";
                        if(mapa.get("titulo")!=null) titulo= (String) mapa.get("titulo");
                        else titulo= "";
                        if(mapa.get("descrip")!=null) descrip= (String) mapa.get("descrip");
                        else descrip= "";
                        if(mapa.get("link")!=null) link= (String) mapa.get("link");
                        else link= "";

                        //armar noticia sin imagen
                        final Noticia noti = new Noticia(fecha, titulo, descrip, link);
                        //agregar a arrays
                        final String id= document.getId();
                        ids.add(id);
                        noticias.add(noti);

                        //firebase storage para la imagen, teniendo cuidado que 'noticias' puede no contener mas a la noticia recien agregada por ser asincrono
                        if(mapa.get("imagen")!=null) {
                            StorageReference imageRef = storageRef.child((String) mapa.get("imagen"));
                            final long ONE = 1024 * 1024;
                            imageRef.getBytes(ONE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    noti.setBitmapImage(bm);

                                    //busco si está la noticia o no
                                    if (ids.contains(id)) {
                                        //agregar imagen
                                        int indice = ids.indexOf(id);
                                        noticias.set(indice, noti);

                                        if (adapter != null) adapter.notifyDataSetChanged();
                                    }
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            //nada
                                        }
                                    });
                        }
                    }

                    //agregar el ultimo
                    ultimaNoticia= new Noticia("","Más noticias","Para ver más noticias, ingresar al link de la página de la UNL.","https://www.unl.edu.ar/noticias/",null);
                    noticias.add(ultimaNoticia);

                    if(swiper!=null) swiper.setRefreshing(false);
                    if(swiper!=null) Toast.makeText(getContext(), "Novedades actualizadas.", Toast.LENGTH_SHORT);
                    if (adapter != null) adapter.notifyDataSetChanged();
                } else {
                    if(swiper!=null) {  swiper.setRefreshing(false);
                                        Toast.makeText(getContext(), "No se pudo actualizar las noticias.", Toast.LENGTH_SHORT);}
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if(swiper!=null) {  swiper.setRefreshing(false);
                    Toast.makeText(getContext(), "No se pudo actualizar las noticias.", Toast.LENGTH_SHORT);}
            }
        });
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
