package com.example.gltour.gltour;

import android.app.AlertDialog;
import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import configuraciones.Conexion;

/*
* Esta clase permite establecer los filtros de busqueda para los sitios Turisticos
* */

public class Lugares extends Fragment {
    private RadioGroup rdgroup;
    private RadioButton radio_selec;
    private Button btnConsultar;
    private View vista;
    private String ArryCanton[][], ArryCat[][];
    private ProgressDialog pdialog;
    private Fragment llugares;
    private FragmentTransaction ft;

    /*
    * En el metodo OnCreateView del fragmento, a traves del boton btnConsultar, detectamos cual
    * radiobutton fue seleccionado para proceder a realizar la busqueda respectiva, obtenemos el id
    * del radiobutton seleccionado y si es el que deseamos procedemos a llamar a su respectivo Dialog.
    * */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        vista = inflater.inflate(R.layout.fragment_lugares, container, false);
        vista.setBackgroundColor(Color.WHITE);

        rdgroup = (RadioGroup)vista.findViewById(R.id.rdgroup);
        btnConsultar = (Button)vista.findViewById(R.id.btn_consultar);

        btnConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int item_selec = rdgroup.getCheckedRadioButtonId();

                if (item_selec == -1) {
                    Toast.makeText(getActivity(), getString(R.string.msjNoCantonSelec),
                            Toast.LENGTH_SHORT).show();
                } else {
                    if(Conexion.isConnect(vista.getContext())){
                        //Para Canton
                        if (item_selec == R.id.rdcanton) {
                            //Mostrar Dialog Donde Aparecen los Cantones
                            //dialogoCantones();
                            new taskCantones().execute();
                        }
                        //Tipo de Lugar
                        if (item_selec == R.id.rdtlugar) {
                            //Muestra Dialogo donde aparecen categorias
                            new taskCategorias().execute();
                        }
                        if (item_selec == R.id.rdnombre) {
                            //Mostrar Dialogo para busquedas por nombre
                            dialogoNombre();
                        }
                    }
                    else
                        Toast.makeText(getActivity(), getString(R.string.sinConexion), Toast.LENGTH_LONG).show();
                }
            }
        });
        return vista;
    }

    /*
     * En los tres funciones que se encuentran a continuacion, tienen la funcion de crear y manipular
     * los eventos relacionado con la creacion de Dialogs, lo cuales son utilizados para solicitar
     * informacion adicional al usuario. Primero creamos la estructura del Dialogo con la Clase Builder,
     * al final creamos un objeto de la Clase AlertDialog que recibe como parametro el el objeto builder
     * que contiene la estructura del Dialog, luego procedemos a mostrarlo con la funcion show.
     */

    /*TODO: Dialog Cantones
    * Este metodo privado tiene como funcion mostrar la lista de Cantones en lo cual al seleccionar
    * uno, y luego presionar el boton aceptar se enviara la informacion de dicho canton al
    * siguiente fragmento. Esta funcion recibe como paremtro un arreglo de cantones el cual es
    * proporcionado por una tarea asincrona que se detallara mas adelante. Adicionalmente al siguiente
    * fragmento pasamos como parametro la url de donde se va a extraer informacion del servicio web.
     */

    public void dialogoCantones(final String[] cantones){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.titleDlgCantones));
        builder.setSingleChoiceItems(cantones, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String id_canton="";
                ListView lw = ((AlertDialog)dialog).getListView();
                //Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                int item = lw.getCheckedItemPosition();
                if(item == -1)
                    Toast.makeText(getActivity(),"Debe Seleccionar una Opcion", Toast.LENGTH_SHORT).show();
                else{
                    String nombre_canton=lw.getAdapter().getItem(item).toString();
                    for(int j=0; j<cantones.length; j++){
                        if(ArryCanton[j][1].equals(nombre_canton)){
                            id_canton= ArryCanton[j][0];
                            break;
                        }
                    }
                    Bundle bdl = new Bundle();
                    bdl.putString("parametro", id_canton);
                    bdl.putString("url","/rest/sitios/canton/");

                    llugares = new ListaLugares();
                    llugares.setArguments(bdl);

                    ft = getFragmentManager().beginTransaction();
                    ft.addToBackStack(null);
                    ft.replace(R.id.mainContent, llugares);
                    //ft.addToBackStack("lugares");
                    ft.commit();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Nothing
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /*
    * TODO: Dialog Categorias
    * Este dialogo tiene la funcion de presentar todos las categorias de los sitios turisticos, los
    * cuales se encuentra en el arreglo categorias que recibe la funcion como parametro. Despues de
    * que el usuario selecciona una opcion, el id de este es enviado al siguiente fragmento asi como
    * la url de donde se va a extraer la informacion del servicio web.
    * */
    public void dialogoCategoria(final String[] categorias){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.titleDlgCategoria));
        builder.setSingleChoiceItems(categorias, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //accion a realizar cuando se selecciona una categoria
            }
        });
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String id_cat="";
                ListView lw = ((AlertDialog)dialog).getListView();
                int item = lw.getCheckedItemPosition();
                if(item == -1)
                    Toast.makeText(getActivity(),"Debe Seleccionar una Opcion", Toast.LENGTH_SHORT).show();
                else{
                    String nombre_cat = lw.getAdapter().getItem(item).toString();
                    for(int i=0; i<categorias.length; i++){
                        if(ArryCat[i][1].equals(nombre_cat)){
                            id_cat = ArryCat[i][0];
                            break;
                        }
                    }
                    Bundle bdlc = new Bundle();
                    bdlc.putString("parametro",id_cat);
                    bdlc.putString("url","/rest/sitios/categoria/");

                    llugares = new ListaLugares();
                    llugares.setArguments(bdlc);

                    ft = getFragmentManager().beginTransaction();
                    ft.addToBackStack(null);
                    ft.replace(R.id.mainContent, llugares);
                    ft.commit();
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Nothing
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * TODO: Dialog Nombre
     * Este dialogo solicita al usario el nombre del sitio turistico que desea localicar.
     * Lo que primero se realiza es inflar el layout llamado dialog_nombre donde se encuentra el
     * control editText donde el usuario escribira el nombre del lugar, luego ese layout es
     * proporcionado como la vista del Dialog. Obtenemos el texto ingresado y lo pasamos como
     * parametro al siguiente fragmento asi como la url.
     */
    public void dialogoNombre(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle(getString(R.string.titleDlgNombre));
        final View v =inflater.inflate(R.layout.dialog_bnombre, null);
        builder.setView(v)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText nombreSearch = (EditText) v.findViewById(R.id.edtNombre);
                        //Toast.makeText(getActivity(), "Escribio: "+nombreSearch.getText().toString(), Toast.LENGTH_SHORT).show();

                        Bundle bdl = new Bundle();
                        bdl.putString("parametro",nombreSearch.getText().toString());
                        bdl.putString("url","/rest/sitios/nombre/");

                        llugares = new ListaLugares();
                        llugares.setArguments(bdl);

                        ft = getFragmentManager().beginTransaction();
                        ft.addToBackStack(null);
                        ft.replace(R.id.mainContent, llugares);
                        ft.commit();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /*Funcion que se encarga de mostrar un ProgressDialog circular, el cual es reutilizado por las
    * tareas asincronas taskCantomes y taskCategorias, el mismo no podra ser cancelado por el
    * usuario.
     */
    public void chargeSpinner(){
        pdialog = new ProgressDialog(getActivity());
        pdialog.setMessage(getString(R.string.carga));
        pdialog.setCancelable(false);
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.show();
    }
    //Consulta al Servicio REST en php para obtener todos los cantones de la Base de Datos.

    /*
    * TODO: Tarea Asincrona para cargar los Cantones
    * Esta tarea asincrona cumple la funcion de hacer una peticion GET a la url
    * http://geloctour.com//rest/cantones/ el cual devuele un conjunto de objetos en json
    * correspondientes a los cantones de una provincia, estos cantones son leidos y almacenados en
    * el arreglo datosStr[], esto se realiza en el metodo doInBackground el cual es sobreescrito de
    * la clase padre, antes de eso mostramos el ProgressDialog Circular al llamar al metodo
    * chargerSpinner en el metodo sobreescrito onPreExecute. Luego de que la tarea es terminada es
    * pasado como parametro datosStr al metodo onPostExecute que a su vez lo pasa como parametro al
     * metodo dialogoCanontes. En este ultimo metodo tambien se de tiene el progressdialog porque ya
     * ha finalizado la tarea.
    * */
    public class taskCantones extends AsyncTask<Void, Void, String[]>{
        private String datosStr[];
        private int numObj;
        private JSONObject jsonOne;

        @Override
        protected void onPreExecute() {
            chargeSpinner();
        }

        @Override
        protected String[] doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet conexion = new HttpGet(configuraciones.Config.dominio+"/rest/cantones");
            conexion.setHeader("content-type","application/json");
            try{
                HttpResponse resp = httpClient.execute(conexion);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray json = new JSONArray(respStr);

                numObj = json.length();
                datosStr = new String[numObj];
                ArryCanton = new String[numObj][2];
                //Log.w("Cantidad de Objetos", "" + json.length());
                for(int i=0; i<numObj; i++){
                    jsonOne = json.getJSONObject(i);
                    datosStr[i] = jsonOne.getString("nombre");
                    ArryCanton[i][0] = jsonOne.getString("id");
                    ArryCanton[i][1] = jsonOne.getString("nombre");
                }
            }
            catch (Exception ex){
                Log.e("Login ERROR..",ex.toString());
            }
            return datosStr;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            //Ocultar Progress Dialog
            pdialog.dismiss();
            dialogoCantones(strings);
        }
    }

    /*
    * TODO: Tarea Asincrona para Cargar Todas las Categorias
    * Esta tare asincrona realiza el mismo proceso que la taskCantones, la unica diferencia es que
    * hace una peticion GET a la url http://geloctour.com//rest/categorias/, se lee el json devuelto
    * y se extrae las categorias, las cuales son pasadas como parametro el metodo onPostExecute, el
    * cual a su vez es utilzado para pasarlo como parametro a dialogCategoria.
    * */

     public class taskCategorias extends AsyncTask<Void, Void, String[]>{
        private String datosStr[];
        private int numObj;
        private JSONObject jsonOne;

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            chargeSpinner();;
        }

        @Override
        protected String[] doInBackground(Void... params) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet conexion = new HttpGet(configuraciones.Config.dominio+"/rest/categorias");
            conexion.setHeader("content-type","application/json");
            try{
                HttpResponse resp = httpClient.execute(conexion);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray json = new JSONArray(respStr);

                numObj = json.length();
                datosStr = new String[numObj];
                ArryCat = new String[numObj][2];
                for(int j=0; j<numObj; j++){
                    jsonOne = json.getJSONObject(j);
                    datosStr[j] = jsonOne.getString("nombre");

                    ArryCat[j][0]= jsonOne.getString("id");
                    ArryCat[j][1]= jsonOne.getString("nombre");

                }
            }
            catch (Exception ex){
                Log.e("Se produjo un error", ex.toString());
            }
            return datosStr;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            //super.onPostExecute(strings);
            pdialog.dismiss();
            dialogoCategoria(strings);
        }
    }
}
