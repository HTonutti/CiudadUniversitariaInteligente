package com.holamundo.ciudaduniversitariainteligente;


import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;

import java.util.Stack;
import java.util.Vector;

/**
 * Created by Lautaro
 * Modificated by Hernán Tonutti on 01/04/2019
 *
 * CAMBIOS
 *  Agregado boton toggle para cambiar el modo de seguimiento de la camara en el mapa
 *  Agregado boton para dirigir a la ciudad universitaria
 *  Agregada funcionalidad para que checkee el item del menu lateral segun el fragment actual
 *  Agregado de animaciones entre los cambios de fragment
 *  Cambio de ubicacion de boton para el codigo QR (de ser un boton en el mapa paso a ser un item en el menu lateral)
 *  Cambio en el modo de ver los pisos
 *          Quitado de esta funcion en menu superior derecho
 *          Agregado spinner inferior para elegir piso a ver
 *  Agregada funcionalidad para ver por capas
 *          Reemplazando donde se mostraban los pisos antes, en menu superior derecho
 *          Posibilidad de ver multiples capas a la vez
 *          Descheckeo de todas las capas al cambiar a otro fragment (contando si presiono de vuelta mapa completo)
 *  Agregada funcionalidad para esconder y mostrar los botones y spinner del mapa segun en que fragment se esta
 *  Agregada funcion para agrupar las instanciaciones y los listener de los botones
 *  Cambios de iconos de los items de la barra lateral
 *  Reordenamiento y limpieza general del codigo
 *
 * */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final String[] INITIAL_PERMS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int INITIAL_REQUEST = 1337;

    /* Atributos de la clase*/
    private ArmaCamino oArmaCamino = null;
    private MapsFragment mapsFragment = null;
    private FragmentManager fm = getSupportFragmentManager();
    private IntentIntegrator scanIntegrator = new IntentIntegrator(this);
    private ultimasBusquedas ultimasBusquedas = null;
    private Menu menu = null;
    private BaseDatos CUdb = null;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private FloatingActionButton b_cu = null;
    private ToggleButton b_seguir = null;
    private Spinner spinnerPisos;
    private MenuItem mi_edi;
    private MenuItem mi_aul;
    private MenuItem mi_lab;
    private MenuItem mi_can;
    private MenuItem mi_ban;
    private MenuItem mi_fot;
    private MenuItem mi_tal;
    private Stack<Integer> IdsMenu=new Stack<>();

    /*Funciones*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Instancio la base de datos
        CUdb = new BaseDatos(getApplicationContext(), "DBCUI", null, 1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }

        //Instancio los objetos para ArmaCamino y el MapFragment
        oArmaCamino = new ArmaCamino(this);
        mapsFragment = new MapsFragment();
        ultimasBusquedas = new ultimasBusquedas();
        ultimasBusquedas.setMainActivity(this);

        //Instancio los botones del mapa, el spinner y sus listener
        DefineListener();

        //Agrego Nodos a mi vector de nodos en oArmaCamino
        cargaNodos();

        //Cambio el fragment por defecto por mi mapFragment
        fm.beginTransaction()
                .setCustomAnimations(R.animator.enter, R.animator.exit)
                .replace(R.id.fragment_container, mapsFragment)
                .addToBackStack(null)
                .commit();

        //Pusheo el id del item del menu y lo selecciono del menu lateral
        IdsMenu.push(R.id.mapa_completo);
        navigationView.setCheckedItem(IdsMenu.lastElement());
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //Si esta en el fragment del mapa y presiona atras, que cierre la app
            if (IdsMenu.lastElement()==R.id.mapa_completo)
                finish();
            else {
                //Saca el ultimo fragment de la pila y selecciona el item del menu correspondiente al fragment que se va a mostrar
                boolean frag = fm.popBackStackImmediate();
                IdsMenu.pop();
                navigationView.setCheckedItem(IdsMenu.lastElement());
                if(IdsMenu.lastElement()==R.id.mapa_completo && frag){
                    //Si vuelvo al mapa que muestre nuevamente los complementos
                    verComplementos(true);
                }
            }
        }
    }

    //onCreate del menu de la esquina superior derecha
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflo el menu de la esquina superior derecha
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        mi_edi=menu.findItem(R.id.edificios);
        mi_aul=menu.findItem(R.id.aulas);
        mi_tal=menu.findItem(R.id.taller);
        mi_lab=menu.findItem(R.id.laboratorios);
        mi_can=menu.findItem(R.id.cantinas);
        mi_ban=menu.findItem(R.id.baños);
        mi_fot=menu.findItem(R.id.fotocopiadoras);
        return true;
    }


    //Listener para las opciones de capa, menu de la esquina superior derecha
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Seteo que no me lleve la camara a la ubicacion actual, para que no lo haga cada vez que hago click en un item
        mapsFragment.setCamaraUbiActual(false);

        //Invierto el estado del item clickeado
        item.setChecked(!item.isChecked());
        Vector<Punto> nodos = new Vector<>();

        //El mapa se limpio asi que me voy fijando que item del menu esta checkeado para agregar los nodos correspondientes al vector
        if (mi_edi.isChecked())
            nodos.addAll(oArmaCamino.verNodosPorNombre("Entrada",1));
        if (mi_aul.isChecked())
            nodos.addAll(oArmaCamino.verNodosPorNombre("Aula",0));
        if (mi_tal.isChecked())
            nodos.addAll(oArmaCamino.verNodosPorNombre("Taller",0));
        if (mi_lab.isChecked())
            nodos.addAll(oArmaCamino.verNodosPorNombre("Lab",1));
        if (mi_can.isChecked())
            nodos.addAll(oArmaCamino.verNodosPorNombre("Cantina",0));
        if (mi_ban.isChecked())
            nodos.addAll(oArmaCamino.verNodosPorNombre("Baño",0));
        if (mi_fot.isChecked())
            nodos.addAll(oArmaCamino.verNodosPorNombre("Fotocopiadora",0));
        mapsFragment.mostrarNodos(nodos);

        //Obtengo el fragmento actual, lo saco y lo agrego nuevamente para que se actualice. Sin esto los nodos no se muestran
        Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof MapsFragment){
            FragmentTransaction fragTransaction =   fm.beginTransaction();
            fragTransaction.detach(currentFragment);
            fragTransaction.attach(currentFragment);
            fragTransaction.commit();
        }

        return super.onOptionsItemSelected(item);
    }


    //Switch segun en que opcion del menu lateral desplegable se selecciona
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        mapsFragment.setCamaraUbiActual(false);
        switch (id) {
            case R.id.buscar: {
                verComplementos(false);
                if (!(fm.findFragmentById(R.id.fragment_container) instanceof Busqueda)) {
                    mapsFragment.limpiarMapa(true);
                    descheckeaItemsMenu();
                    Busqueda busqueda = new Busqueda();
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.pop_enter, R.animator.pop_exit)
                            .replace(R.id.fragment_container, busqueda)
                            .addToBackStack(null)
                            .commit();
                }
                break;
            }
            case R.id.mapa_completo: {
                verComplementos(true);
                mapsFragment.limpiarMapa(true);
                descheckeaItemsMenu();
                if (!(fm.findFragmentById(R.id.fragment_container) instanceof MapsFragment)) {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.pop_enter, R.animator.pop_exit)
                            .replace(R.id.fragment_container, mapsFragment)
                            .addToBackStack(null)
                            .commit();
                }
                break;
            }
            case R.id.ultimas: {
                mapsFragment.limpiarMapa(true);
                descheckeaItemsMenu();
                verComplementos(false);

                if (!(fm.findFragmentById(R.id.fragment_container) instanceof ultimasBusquedas)) {
                    fm.beginTransaction()
                            .setCustomAnimations(R.animator.enter, R.animator.exit, R.animator.pop_enter, R.animator.pop_exit)
                            .replace(R.id.fragment_container, ultimasBusquedas)
                            .addToBackStack(null)
                            .commit();
                }
                break;
            }
            case R.id.qr: {
                // Se procede con el proceso de scaneo
                scanIntegrator.initiateScan();
                break;
            }
            default: return false;
        }
        //Pusheo el id del ultimo item seleccionado del menu
        IdsMenu.push(id);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Defino las instancias de los botones, spinner y menu asi como los listener que se necesiten
    public void DefineListener(){
        //Instancio los botones para llevar a la ciudad universitaria y el toggle para el modo de seguimiento
        b_cu=findViewById(R.id.b_cu);
        final LatLng ciudad_universitaria = new LatLng(-31.640543,-60.672544);
        b_cu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lleva a la ciudad universitaria
                mapsFragment.moverCamara(ciudad_universitaria,17);
            }
        });

        b_seguir=findViewById(R.id.b_seguir);
        b_seguir.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            //Cambio el modo de seguimiento
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mapsFragment.setCamaraUbiActual(true);
                mapsFragment.modoSeguimiento(isChecked);
            }
        });

        //Defino el toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Ciudad Inteligente");
        setSupportActionBar(toolbar);

        //Instancio el layout de activity_main
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //Instancio la barra de navegacion lateral
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Defino el spinner y su adaptador
        spinnerPisos = findViewById(R.id.PisosSpinner);
        //Posibles opciones del spinner
        final String[] itemsSP = {"Planta Baja", "Piso 1", "Piso 2", "Piso 3", "Piso 4", "Piso 5"};
        ArrayAdapter<String> arraySB1 = new ArrayAdapter<String>(this, R.layout.spinner_layout, itemsSP);
        spinnerPisos.setAdapter(arraySB1);
        spinnerPisos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = spinnerPisos.getSelectedItem();
                //Esta en planta baja o no
                boolean plantabaja = item.toString().contains("Baja");


                //Si esta en modo polilinea cambio estas, sino los nodos
                if(plantabaja) {
                    mapsFragment.setPisoActual(0);
                    if (mapsFragment.modoPolilinea())
                        mapsFragment.cambiarPolilinea(0);
                    else
                        mapsFragment.cambiarNodos(0);
                }
                else{
                    //Calcula el nro de piso segun lo seleccionado
                    Integer NroPiso = Integer.parseInt(item.toString().substring(item.toString().indexOf(' ') + 1));
                    mapsFragment.setPisoActual(NroPiso);
                    if(mapsFragment.modoPolilinea())
                        mapsFragment.cambiarPolilinea(NroPiso);
                    else
                        mapsFragment.cambiarNodos(NroPiso);
                }
                mapsFragment.CargaOverlays();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    //Cambia el estado de visibilidad de los botones y el menu que se muestran en el mapa
    public void verComplementos(boolean mostrar){
        if(mostrar){
            b_cu.setVisibility(View.VISIBLE);
            b_seguir.setVisibility(View.VISIBLE);
            spinnerPisos.setVisibility(View.VISIBLE);
        }
        else{
            b_cu.setVisibility(View.INVISIBLE);
            b_seguir.setVisibility(View.INVISIBLE);
            spinnerPisos.setVisibility(View.INVISIBLE);
        }
        //Esta bugueado, lo esconde pero no lo vuelve a mostrar nunca
        menu.setGroupVisible(R.id.grupo_menu,mostrar);
    }


    public void descheckeaItemsMenu(){
        mi_edi.setChecked(false);
        mi_aul.setChecked(false);
        mi_tal.setChecked(false);
        mi_lab.setChecked(false);
        mi_can.setChecked(false);
        mi_ban.setChecked(false);
        mi_fot.setChecked(false);

    }

    /*
    Mostrar busqueda llama a las funciones del mapFragment que:
    -muestran un conjunto de puntos en el mapa
    -muestran una polilinea desde el punto mas cercano hasta el objetivo
    *setPuntoMasCercano setea en oArmaCamino el nodo mas cercano a la posición donde estoy parado
    Luego reemplazo el fragment de Busqueda por el de mapa
    */
    public void mostrarBusqueda(String Edificio, String Nombre) {

        //Que la camara no me lleve a la ubicacion actual mia
        mapsFragment.setCamaraUbiActual(false);
        //Seteo el piso en planta baja y la opcion en el spinner
        mapsFragment.setPisoActual(0);
        spinnerPisos.setSelection(0);
        //Muestro los botones del mapa de nuevo
        verComplementos(true);

        if (Edificio.equals("*")) {
            mapsFragment.mostrarNodos(oArmaCamino.nodosMapa(Nombre));
        } else {
            oArmaCamino.setPuntoMasCercano(mapsFragment.getPosicion(), mapsFragment.getPisoActual());
            mapsFragment.dibujaCamino(oArmaCamino.camino(Edificio, Nombre));
            String texto = "Su objetivo está en " + oArmaCamino.getPisoObjetivo();
            Toast.makeText(getApplicationContext(), texto, Toast.LENGTH_LONG).show();
        }
        fm.beginTransaction().replace(R.id.fragment_container, mapsFragment).addToBackStack(null).commit();
    }

    //Funcion que le pasa a oArmaCamino un edificio y devuelve un Vector con todas las aulas de ese edificio
    public Vector<Punto> verAulasPorEdificio(String Edificio) {
        return oArmaCamino.verAulasPorEdificio(Edificio);
    }


    //Funcion para crear nodos del mapa y sus conexiones
    private void cargaNodos() {
        Vector<Punto> puntos = new Vector<>();
        SQLiteDatabase db1 = CUdb.getReadableDatabase();
        Cursor c = db1.rawQuery("SELECT *  FROM Punto", null);
        c.moveToFirst();

        //Creo y agrego los nodos
        if (c.getCount() > 0) {
            do {
                Punto oPunto = new Punto(c.getInt(0), Double.parseDouble(c.getString(1)), Double.parseDouble(c.getString(2)), c.getString(3), c.getInt(4), c.getString(5), c.getInt(6));
                puntos.add(oPunto);
            } while (c.moveToNext());
        }

        //Genero las conexiones
        for (int i = 0; i < puntos.size(); i++) {
            Cursor d = db1.rawQuery("SELECT idHasta FROM Conexiones WHERE idDesde = " + puntos.elementAt(i).getId(), null);
            d.moveToFirst();
            if (d.getCount() > 0) {
                do {
                    puntos.elementAt(i).addVecino(puntos.elementAt(d.getInt(0)));
                } while (d.moveToNext());
            }
            oArmaCamino.addNodo(puntos.elementAt(i));
        }
        //Cierro DB
        puntos.clear();
        db1.close();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Se obtiene el resultado del proceso de scaneo y se parsea
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            //Quiere decir que se obtuvo resultado pro lo tanto:
            //Desplegamos en pantalla el contenido del código de barra scaneado
            String scanContent = scanningResult.getContents();
            mapsFragment.setLat(Double.parseDouble(scanContent.substring(0, (scanContent.indexOf(',')))));
            mapsFragment.setLon(Double.parseDouble(scanContent.substring((scanContent.indexOf(',')) + 1, scanContent.length() - 2)));
            mapsFragment.setPisoActual(Integer.parseInt(scanContent.substring(scanContent.length() - 1)));
            mapsFragment.actualizaPosicion();

        } else {
            //Quiere decir que NO se obtuvo resultado
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No se ha recibido datos del scaneo!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
