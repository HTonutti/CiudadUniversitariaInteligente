package com.holamundo.ciudaduniversitariainteligente;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Vector;

/**
 * Created by Lautaro on 08/12/2016.
 * Modificated by Hernan Tonutti on 10/04/2019
 *
 * CAMBIOS
 *  Agregado boton y funcionalidad para borrar todas las busquedas del historial
 *  Agregada funcionalidad para borrar una busqueda al hacer un click largo en esta
 *          Definicion del listener
 *          Agregado cartel para confirmar la supresion del item del listview seleccionado
 */
public class ultimasBusquedas extends Fragment {

    private BaseDatos CUdb = null;
    private MainActivity oMainActivity = null;
    private Button b_borrarBusqueda;
    private int PosicionItemABorrar;
    private ArrayAdapter adapter;
    private SQLiteDatabase db;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflo mi listView
        final View rootView = inflater.inflate(R.layout.ultimas_busquedas, container, false);

        //Abro la base de datos
        CUdb = new BaseDatos(getActivity(),"DBBusquedas", null, 1);

        //Obtengo lo que hay en la BD
        db = CUdb.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT nombre, edificio FROM Busquedas",null);
        c.moveToFirst();
        db.close();

        //Creo el adapter, si hay busquedas registradas
        //Unicamente si hay busquedas defino los listener para el listview y el boton
        if(c.getCount() > 0) {
            Vector<String> array = new Vector<String>();
            do {
                array.add(c.getString(0) + ", " + c.getString(1));
            } while (c.moveToNext());

            adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, array);

            final ListView listView = rootView.findViewById(R.id.listView);

            //ItemClickListener para los elementos del listView, para hacer una busqueda desde aquí
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String itemClick = listView.getItemAtPosition(i).toString();
                    String Nombre = itemClick.substring(0, (itemClick.indexOf(',')));
                    String Edificio = itemClick.substring((itemClick.indexOf(',') + 2));
                    oMainActivity.mostrarBusqueda(Edificio, Nombre);

                }
            });

            //Genera el mensaje a mostrar cuando se desea borrar una busqueda individual
            final AlertDialog.Builder avisoBorrar = new AlertDialog.Builder(this.getContext());
            avisoBorrar.setMessage("¿ Desea borrar la búsqueda seleccionada ?");
            avisoBorrar.setCancelable(false);
            avisoBorrar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface avisoBorrar, int id) {
                    //Tomo el item de la posicion que define onLongItemClick, obtengo el nombre y edificio que representa
                    String itemClick = listView.getItemAtPosition(PosicionItemABorrar).toString();
                    String Nombre = itemClick.substring(0, (itemClick.indexOf(',')));
                    String Edificio = itemClick.substring((itemClick.indexOf(',') + 2));
                    //Primero borro desde la base de datos, luego elimino el item del listview y notifico para que refresque la vista
                    db=CUdb.getWritableDatabase();
                    db.execSQL("DELETE FROM Busquedas WHERE nombre='"+Nombre+"' AND edificio='"+Edificio+"'");
                    db.close();
                    adapter.remove(adapter.getItem(PosicionItemABorrar));
                    adapter.notifyDataSetChanged();
                    //Si era el ultimo elemento agrego la frase de que no hay busquedas
                    if(adapter.getCount()==0){
                        Vector<String> array = new Vector<String>();
                        array.add("Aun no se registran busquedas");
                        adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, array);
                        listView.setAdapter(adapter);
                    }

                }
            });
            avisoBorrar.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface avisoBorrar, int id) {
                    avisoBorrar.cancel();
                }
            });

            //En caso de click largo que defina la posicion del item presionado y muestre el mensaje
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    PosicionItemABorrar=position;
                    avisoBorrar.show();
                    return true;
                }
            });

            //Defino listener para boton de borrar todas las busquedas
            b_borrarBusqueda = rootView.findViewById(R.id.b_borraBusquedas);
            b_borrarBusqueda.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Primero borro desde la base de datos, luego creo un adapter vacio y lo seteo al listview
                    db=CUdb.getWritableDatabase();
                    db.execSQL("DELETE FROM Busquedas");
                    db.close();
                    Vector<String> array = new Vector<String>();
                    array.add("Aun no se registran busquedas");
                    adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, array);
                    listView.setAdapter(adapter);
                }
            });
            //Añado el adapter
            listView.setAdapter(adapter);
        }
        //Si todavia no se han hecho busquedas
        else{
            Vector<String> array = new Vector<String>();
            array.add("Aun no se registran busquedas");
            ListAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, array);
            final ListView listView = rootView.findViewById(R.id.listView);
            listView.setAdapter(adapter);
        }

        return rootView;

    }

    public void setMainActivity(MainActivity oMA){
        this.oMainActivity = oMA;
    }

}
