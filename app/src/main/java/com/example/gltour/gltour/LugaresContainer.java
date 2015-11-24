package com.example.gltour.gltour;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import configuraciones.Config;
import gltour.comentarios.AdapterComentarios;
import gltour.comentarios.Comentarios;
import gltour.tabsswipe.InfoFragment;
import gltour.tabsswipe.LugarPageAdapter;

/*
* Esta clase es la base sobre la cual se inicia el proceso de presentacion de la informacion de sitios
* turisticos, como los mapas, imagenes, eventos.*/

public class LugaresContainer extends Fragment {
    public ViewPager vPager;
    public LugarPageAdapter mAdapter;
    public Bundle bdl;
    public static String sitio[];
    public static String[] sitiosInfo, comentario_ind;
    public static String id_favorito;
    public ImageView imgPrincipal;
    public View vistaCont;
    public ViewGroup viewGroup;
    public List<Comentarios> coment;
    public static boolean bcoment_ind, bcoment_total;

    /*
    * Para poder mejorar la presentacion de la informacion de un sitio turistico, se a utilizado el
    * control ViewPager(Vista por Paginas), con el gesto Swipe, que permite al usuario desplazarse
    * entre las paginas al deslizar su dedo tanto a la izquierda como a la derecha.*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        vistaCont = inflater.inflate(R.layout.fragment_container, container, false);
        this.viewGroup = viewGroup;

        //Recibimos el array con los datos del sitio turistico
        bdl = this.getArguments();
        sitio = bdl.getStringArray("sitio");

        //Obtengo la imagen del layout
        imgPrincipal = (ImageView)vistaCont.findViewById(R.id.imagePrincipal);

        //Picasso.with(context).load(lg.getImagen()).resize(130,130).centerCrop().into(img);
        getActivity().setTitle(sitio[1]);

        //Cargamos los datos Iniciales
        new asyncInfo().execute();
        return vistaCont;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static LugaresContainer newInstace(){
        return new LugaresContainer();
    }

    /*
    * Esta tarea asincrona obtiene toda la informacion adicional de un sitio turistico, asi como los
    * comentarios individuales y de todos los demas personas en el caso que hubieren*/
    public class asyncInfo extends AsyncTask<Void,Void,String[]>{
        public ProgressDialog dialogp;
        protected String nombre, desc, stars;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogp = new ProgressDialog(vistaCont.getContext());
            dialogp.setMessage(getString(R.string.carga));
            dialogp.setCancelable(false);
            dialogp.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialogp.show();
        }

        @Override
        protected String[] doInBackground(Void... params) {

            //Hacemos la peticion de la Informacion adicional del sitio turistico al servidor
            //Aqui conodicion para saber si el usuario esta logeado se hace una
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet conexion = new HttpGet(configuraciones.Config.dominio+"/rest/sitio/"+
                    LugaresContainer.sitio[0]);
            try{
                //Obteniendo informacion adicional del sitio
                HttpResponse resp = httpClient.execute(conexion);
                String datos = EntityUtils.toString(resp.getEntity());
                JSONArray json = new JSONArray(datos);
                JSONObject unsitio;
                unsitio = json.getJSONObject(0);
                sitiosInfo = new String[unsitio.length()];
                sitiosInfo[0] = unsitio.getString("nombre");
                sitiosInfo[1] = unsitio.getString("descripcion");
                sitiosInfo[2] = unsitio.getString("direccion");
                sitiosInfo[3] = unsitio.getString("latitud");
                sitiosInfo[4] = unsitio.getString("longitud");
                sitiosInfo[5] = unsitio.getString("telefono");
                sitiosInfo[6] = unsitio.getString("horario");
                sitiosInfo[7] = unsitio.getString("website");

                /*Obteniendo el comentario del usuario logeado, pasamos como parametro el id del sitio
                * y el id del cliente, si el usuario actual no ha calificado el sitio, no se mostrara
                * nada
                * */
                conexion.setURI(URI.create(Config.dominio + "/rest/get_cali_ind/" + sitio[0] + "/" +
                        Config.id_cliente));
                resp = httpClient.execute(conexion);
                datos = EntityUtils.toString(resp.getEntity());
                Log.w("JsonComentIndv:", datos.length()+"");

                if(!datos.equals("{}")){
                    JSONObject comentJson = new JSONObject(datos);
                    comentario_ind = new String[comentJson.length()];
                    comentario_ind[0] = comentJson.getString("comentario");
                    comentario_ind[1] = comentJson.getString("cant_stars");
                    bcoment_ind = true;
                }
                else
                    bcoment_ind = false;

                /*
                * Para Obtener la lista de Comentarios de todas las demas personas*/
                conexion.setURI(URI.create(Config.dominio+"/rest/get_coment_sitio/"+sitio[0]+"/"+
                        Config.id_cliente));
                resp = httpClient.execute(conexion);
                datos = EntityUtils.toString(resp.getEntity());
                Log.w("JsonComentTotal:", datos.length()+"");

                if(!datos.equals("[]")){
                    coment = new ArrayList<>();
                    json = new JSONArray(datos);
                    int num = json.length();
                    for(int i=0; i<num; i++){
                        coment.add(creaComentario(json.getJSONObject(i)));
                    }
                    bcoment_total = true;
                }
                else
                    bcoment_total = false;

                /*
                * Consulta para determinar si el sitio actual es el favorito del usuario*/
                conexion.setURI(URI.create(Config.dominio+"/rest/get_favorito/"+Config.id_cliente+
                "/"+sitio[0]));
                resp = httpClient.execute(conexion);
                datos = EntityUtils.toString(resp.getEntity());
                Log.w("Favorito:", datos+"");

                if(!datos.equals("")){
                    Log.w("EntroFavorito","Favoritos");
                    JSONObject favorito = new JSONObject(datos);
                    id_favorito = favorito.getString("id");
                }
                else
                    id_favorito ="0";

            }
            catch (Exception ex){
                Log.w("Error Fatal: ", ex.toString());
            }
            return sitiosInfo;
        }

        protected Comentarios creaComentario(JSONObject obj) throws Exception{
            nombre = obj.getString("usuario");
            desc = obj.getString("comentario");
            stars = obj.getString("cant_stars");
            return new Comentarios(nombre, desc, stars);
        }

        /*
        * Cuando la Tarea asincrona finaliza procedemos a crear las paginas que se mostraran en el
        * view Pager. A excepcion de la pagina informacion todas las demas paginas es decir fragmentos
        * se encargan de hacer la peticion y presentacion de informacion en el background sin interrumpir
        * la interaccion del usuario con la aplicacion. Solo para obtener la informacion comun que
        * utilizaran las demas paginas se hace que el usuario espere al principio.*/
        @Override
        protected void onPostExecute(String[] sitiosInfo) {
            super.onPostExecute(sitiosInfo);
            dialogp.dismiss();
            try{
                mAdapter = new LugarPageAdapter(getChildFragmentManager());
                mAdapter.finishUpdate(viewGroup);
                vPager = (ViewPager)vistaCont.findViewById(R.id.pager);
                vPager.setAdapter(mAdapter); //Establecemos el PageAdapter para el ViewPager
                vPager.setOffscreenPageLimit(3); //Establecemos el numero de Paginas que se mantendran en memoria sin eliminarse, tanto a la izquierda como a la derecha de la pagina Actual

                cargaInfo();

                if(bcoment_ind)
                    cargaComentPersonal();
                if(bcoment_total)
                    cargarAllComent();
            }
            catch (Exception ex){
                Log.w("DATOS SITIO:", ex.toString());}
        }
    }

    /*Esta funcion carga informacion del Sitio Turistico en la primera pagina, "Informacion"*/
    public void cargaInfo(){
        InfoFragment.txtCanton.setText(sitio[4]);
        InfoFragment.txtDesc.setText(sitio[2]);
        InfoFragment.txtDirec.setText(sitiosInfo[2]);
        InfoFragment.txtFono.setText(sitiosInfo[5]);
        InfoFragment.txtWeb.setText(sitiosInfo[7]);
        InfoFragment.txtHorario.setText(sitiosInfo[6]);
        InfoFragment.ratingBarTotal.setRating(Float.parseFloat(sitio[5]));
        if(bcoment_ind){
            InfoFragment.ratingUsuario.setRating(Float.parseFloat(comentario_ind[1]));
        }
        if(!LugaresContainer.id_favorito.equals("0")){
            InfoFragment.checkBoxFav.setChecked(true);
        }
    }


    /*
    * Funcion para poder presentar el comentario individual del usuario que esta logeado en la aplicacion
    * */

     public void cargaComentPersonal(){
        List<Comentarios> list = new ArrayList<>();
        list.add(new Comentarios(Config.nombre_cli, comentario_ind[0], comentario_ind[1]));
        AdapterComentarios adpt_coment = new AdapterComentarios(list,InfoFragment.vistaInf.getContext());
        InfoFragment.listViewCP.setAdapter(adpt_coment);
    }

    public void cargarAllComent(){
        Log.w("Cantidad", coment.size() + "");
        AdapterComentarios adpt_comentAll = new AdapterComentarios(coment,InfoFragment.vistaInf.getContext());

        InfoFragment.listViewCT.setAdapter(adpt_comentAll);

        ListAdapter listAdapter = InfoFragment.listViewCT.getAdapter();
        if(InfoFragment.listViewCT == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(InfoFragment.listViewCT.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;

        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, InfoFragment.listViewCT);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, RelativeLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = InfoFragment.listViewCT.getLayoutParams();
        params.height = totalHeight + (InfoFragment.listViewCT.getDividerHeight() * (listAdapter.getCount() - 1));
        InfoFragment.listViewCT.setLayoutParams(params);
        InfoFragment.listViewCT.requestLayout();
    }
}
