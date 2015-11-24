package com.example.gltour.gltour;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
* Esta clase tiene la funcion de mostrar todos los lugares turisticos dependiendo del parametro y la
* url especificada. Hace la gestion de realizar la tarea asincrona que devuelve el conjunto de objetos
* que se van a cargar en el Adaptador asociado al objeto ListView.
* */
public class ListaLugares extends Fragment {
    private String qbuscar, url;
    public View vistaLug;
    public LinearLayout linearLug;

    /*
    * En este metodo se infla el fragmento List_lugares, el cual contendra todos los lugares
    * turisticos dependiendo a los filtros de busqueda. El parametro qbuscar puede contener los
    * siguientes tres valores: id del canton, id de la categoria, nombre del sitio turistico.
    * El parametro url contiene la porcion de la url donde se realiza la consulta al servicio web.
    * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Obtengo Cantones
        vistaLug = inflater.inflate(R.layout.fragment_listlugares, container, false);
        linearLug = (LinearLayout)vistaLug.findViewById(R.id.linearLugares);

        vistaLug.setBackgroundColor(Color.WHITE);
        Bundle bparam_search = this.getArguments();
        qbuscar = bparam_search.getString("parametro");
        url = bparam_search.getString("url");

        final AdapterLugares adpt= new AdapterLugares(new ArrayList<LugaresFAdapter>(),getActivity());
        ListView lview = (ListView)vistaLug.findViewById(R.id.listViewLugar);

        lview.setAdapter(adpt);
        AsyncListLugares asynl = new AsyncListLugares(adpt);
        asynl.execute(configuraciones.Config.dominio+url+qbuscar);


        /*Para detectar cuando el usuario dio click en un item del ListView, llamamos al metodo
        * setOnItemClickListener que es el encargado de registrar este evento, se le envia como
        * parametro la interfaz OnItemClickListener en el cual sobreescribimos el metodo OnItemClick
        * para manejar la accion necesaria despues que el usuario dio clic en un item*/
        lview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LugaresFAdapter lu = adpt.getItem(position); //El metodo getItem me devuelve un AdapterView que contiene todos los elementos de un lugar turistico

                //List<LugaresFAdapter> arrlu = new ArrayList<LugaresFAdapter>();

                //Creamos Bundle para pasar parametros al siguiente fragmento
                String arryLugar[] = {lu.getId(), lu.getNombre(), lu.getDescripcion(),
                        lu.getImagen(), lu.getCanton(), lu.getCalificacion()};

                Bundle bdl = new Bundle();
                bdl.putStringArray("sitio",arryLugar);
                Log.w("AQUI PARAMETROS", arryLugar[3]);
                LugaresContainer lc = new LugaresContainer();
                lc.setArguments(bdl);

                FragmentTransaction transaction= getActivity().getSupportFragmentManager().beginTransaction();
                //transaction.add(R.id.listViewLugar, lc).commit();
               //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.addToBackStack(null);
                transaction.replace(R.id.mainContent, lc);
                transaction.commit();
            }
        });
        return vistaLug;
    }

    /*Tarea Asincrona para Cargar todos los lugares segun los parametros dados*/
    public class AsyncListLugares extends AsyncTask<String, Void, List<LugaresFAdapter>> {
        private ProgressDialog pdialog;
        private AdapterLugares adpt;
        private String id, name, desc, cant, img, calf;

        public AsyncListLugares(AdapterLugares adpt){
            this.adpt = adpt;
            pdialog = new ProgressDialog(getActivity());
        }
        /*
        * Es el primer metodo que se ejecuta y Carga un Progress Dialog Circular
        * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdialog.setMessage("Cargando Datos");
            pdialog.setCancelable(false);
            pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pdialog.show();
        }

        /*
        * Hacemos una peticion GET al servidor web a traves de la url que se recibe como parametro
        * En un ciclo FOR obtenemos uno a uno los objetos en JSON los cuales son agregados a una
        * lista llamada lugares en la cual contendra objetos del tipo LugaresFAdpter, estos objetos
        * son creados en la funcion createLugar que contruye y devuelve un objeto de ese tipo, esta
        * es llamada en cada iteracion del FOR.
        * */
        @Override
        protected List<LugaresFAdapter> doInBackground(String... params) {
            List<LugaresFAdapter> lugares = new ArrayList<>();
            try{
                String url = params[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet conexion = new HttpGet(url);
                conexion.setHeader("content-type","application/json");

                HttpResponse resp = httpClient.execute(conexion);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray json = new JSONArray(respStr);

                Log.w("AQUI JSON", json.toString());

                int numObj = json.length();
                for (int i=0; i<numObj; i++){
                    lugares.add(createLugar(json.getJSONObject(i)));
                }
            }
            catch (Exception ex){
                Log.e("Error en AsyncTask",ex.toString());
            }
            return lugares;
        }

        /*
        * Extrae las propiedades de un objeto en JSON, crea un nuevo objeto del tipo LugaresFAdapter
        * el cual es devuelto por la funcion.
        * */
        private LugaresFAdapter createLugar(JSONObject obj) throws Exception{
            id = obj.getString("id");
            name = obj.getString("nombre");
            desc = obj.getString("descripcion");
            cant = obj.getString("canton_nombre");
            img = obj.getString("imagen").replace('\\', '\0');//Para quitar las barra invertida innnesaria
            img = configuraciones.Config.dominio+img;
            calf = obj.getString("calificacion");
            if(calf.equals("null"))
                calf="0";
            return new LugaresFAdapter(id, name, desc, cant, img, calf);
        }

        /*
        * Fianliza el ProgressDialog, Actualiza la lista de Items en el adaptado y
        * se notifica al mismo de que ha habido una actualizacion, para que este a su vez lo
        * notifique al ListView y pueda presentarlos al usuario.
        * */
        @Override
        protected void onPostExecute(List<LugaresFAdapter> lugaresFAdapters) {
            super.onPostExecute(lugaresFAdapters);
            pdialog.dismiss();
            if(lugaresFAdapters.size()!=0){
                adpt.setItemList(lugaresFAdapters);
                adpt.notifyDataSetChanged();
            }
            else{
                TextView txt = new TextView(vistaLug.getContext());
                txt.setText(getString(R.string.sinSitios));
                txt.setGravity(Gravity.CENTER_VERTICAL);
                txt.setGravity(Gravity.CENTER);
                txt.setPadding(10,10,10,10);
                txt.setTypeface(Typeface.DEFAULT_BOLD);
                txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                linearLug.removeAllViews();
                linearLug.addView(txt);
                linearLug.refreshDrawableState();
            }
        }
    }
}
