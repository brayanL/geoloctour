package gltour.tabsswipe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gltour.gltour.LugaresContainer;
import com.example.gltour.gltour.R;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import configuraciones.Config;

public class InfoFragment extends Fragment {
    public ImageView imgPrincipal;
    public static TextView txtCanton, txtDesc, txtDirec, txtFono, txtWeb, txtHorario;
    public static View vistaInf;
    public static ListView listViewCP, listViewCT;
    public static CheckBox checkBoxFav;
    public static RatingBar ratingUsuario, ratingBarTotal;
    public Button btnCalificar;
    public RatingBar ratingBarCal;
    private String url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.w("Inflater:", "Se Inflo el Layout Info");
        vistaInf = inflater.inflate(R.layout.fragment_info,container, false);
        vistaInf.setBackgroundColor(Color.WHITE);

        imgPrincipal = (ImageView)vistaInf.findViewById(R.id.imagePrincipal);
        ratingUsuario = (RatingBar)vistaInf.findViewById(R.id.ratingBarUsuario);
        ratingBarTotal = (RatingBar)vistaInf.findViewById(R.id.ratingBarTotal);

        txtCanton = (TextView)vistaInf.findViewById(R.id.cantonInf);
        txtDesc = (TextView)vistaInf.findViewById(R.id.descInf);
        txtDirec = (TextView)vistaInf.findViewById(R.id.direccInf);
        txtFono = (TextView)vistaInf.findViewById(R.id.fonoInf);
        txtWeb = (TextView)vistaInf.findViewById(R.id.swebInf);
        txtHorario = (TextView)vistaInf.findViewById(R.id.horarioInf);
        checkBoxFav = (CheckBox)vistaInf.findViewById(R.id.checkFavorite);

        listViewCP = (ListView)vistaInf.findViewById(R.id.listViewCP);
        listViewCT = (ListView)vistaInf.findViewById(R.id.listViewCT);

        Picasso.with(getActivity().getApplication().getApplicationContext()).
                load(LugaresContainer.sitio[3]).resize(600, 200).into(imgPrincipal);

        btnCalificar = (Button)vistaInf.findViewById(R.id.btnCalificar);
        btnCalificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Config.id_cliente.equals("0"))
                    if(LugaresContainer.bcoment_ind)
                        dialogComentVer();
                    else
                        dialogComentario();
                else
                    Toast.makeText(vistaInf.getContext(), getString(R.string.no_login),
                            Toast.LENGTH_LONG).show();
            }
        });

        /*Para detectar el evento cuando se dio clic en el boton Favorito, solo si el usuario esta
        * logeado podrar agregar un sitio como favorito. Cuando se da clic este control es deshabilitado
        * hasta que ese sitio haya sido agregado como favorito para el usuario actual*/
        checkBoxFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Config.id_cliente.equals("0")){
                    if(checkBoxFav.isChecked()){
                        mensajes(1);
                        checkBoxFav.setEnabled(false);
                        url = Config.dominio+"/rest/reg_favorito/"+LugaresContainer.sitio[0]+"/"+
                                Config.id_cliente;
                        new regFavorito().execute(url);
                    }
                    else{
                        mensajes(1);
                        checkBoxFav.setEnabled(false);
                        url = Config.dominio+"/rest/del_favorito/"+LugaresContainer.id_favorito;
                        new regFavorito().execute(url);
                    }
                }
                else
                    Toast.makeText(vistaInf.getContext(),getString(R.string.nofavorito),
                            Toast.LENGTH_LONG).show();
            }
        });
        return vistaInf;
    }

    /*
        * Dialogo que permite al usuario valorar el sitio con una cantidad de estrellas(del 1 al 5),
        * y enviar un comentario, estos dos parametros deben ser proporcionados por el usuario para que
        * los mismo puedan ser enviados al servidor. El comentario y la calificacion seran moderados por
        * un administrador del sistema web, una vez que hayan sido aprobados se podran visualizar en el
        * sitio turistico.
        * */
    public void dialogComentario(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_calificacion, null);
        final EditText edtComent = (EditText)view.findViewById(R.id.edtComentario);
        ratingBarCal = (RatingBar)view.findViewById(R.id.ratingBarCal);

        builder.setTitle("Calificar el Sitio");
        builder.setView(view)
                .setPositiveButton(R.string.enviar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(ratingBarCal.getRating()!=0 && (!edtComent.getText().toString().equals(""))){
                            Toast.makeText(vistaInf.getContext(), getString(R.string.msj_envio),
                                    Toast.LENGTH_SHORT).show();
                            //Enviar parametros a tarea asincrona: id sitio, id cliente, comentario, estrellas
                            new sendCalificacion().execute(new String[]{LugaresContainer.sitio[0],
                                    Config.id_cliente, edtComent.getText().toString(),
                                    String.valueOf(ratingBarCal.getRating())});
                        }
                        else
                            Toast.makeText(vistaInf.getContext(), getString(R.string.empty_datos),Toast.LENGTH_SHORT).show();
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

    /*
    * Aqui solo se Presenta un AlertDialog informativo indicando que el usuario ya califico el sitio*/
    public void dialogComentVer(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_calificacion, null);
        final EditText edtComent = (EditText)view.findViewById(R.id.edtComentario);
        ratingBarCal = (RatingBar)view.findViewById(R.id.ratingBarCal);

        edtComent.setText(LugaresContainer.comentario_ind[0]);
        edtComent.setEnabled(false);

        ratingBarCal.setRating(Float.parseFloat(LugaresContainer.comentario_ind[1]));
        ratingBarCal.setEnabled(false);

        builder.setTitle("Sitio ya Calificado");
        builder.setView(view);

        AlertDialog alert = builder.create();
        alert.show();
    }

    /*Aqui permite guardar el comentario realizado por el usurio el cual debe ser moderado para ser
    * visible a los demas usuarios*/
    public class sendCalificacion extends AsyncTask<String[],Void,Void>{
        @Override
        protected Void doInBackground(String[]... params) {
            String[] datos_coment = params[0];
            String comentario = datos_coment[2].trim().replace(" ","%20");
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet conexion = new HttpGet(Config.dominio+"/rest/reg_calificacion/"+
                        datos_coment[0]+"/"+datos_coment[1]+"/"+comentario+"/"+datos_coment[3]);
                HttpResponse response = httpClient.execute(conexion);
                String datos = EntityUtils.toString(response.getEntity());
                if(datos.equals("true")){
                    Log.i("Guardado", "Se guardo satisfactoriamente Comentario");
                    float num_stars = Float.parseFloat(datos_coment[3]);
                    ratingUsuario.setRating(num_stars);
                }
            }
            catch (Exception ex){
                Log.e("Guardar comentario: ", ex.toString());
            }
            return null;
        }
    }

    /*
    * Esta tarea asincrona se ejecuta cuando el usuario dio clic en el boton me gusta, y agrega o
    * elimina el sitio actual como favorito*/
    public class regFavorito extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            Log.w("url",url);
            try{
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet conexion = new HttpGet(url);
                HttpResponse respuesta = httpClient.execute(conexion);
                LugaresContainer.id_favorito = EntityUtils.toString(respuesta.getEntity());
            }
            catch (Exception ex){
                Log.w("Excepcion: ",ex.toString());
                mensajes(0);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            checkBoxFav.setEnabled(true);
            if(checkBoxFav.isChecked())
                mensajes(2);
            else
                mensajes(3);
            Log.w("FAvoritoID:",LugaresContainer.id_favorito);
        }
    }

    public void mensajes(int estado){
        if(estado ==1)
            Toast.makeText(vistaInf.getContext(), "Enviando Informacion", Toast.LENGTH_SHORT).show();
        if(estado==2)
            Toast.makeText(vistaInf.getContext(), "Este sitio ha sido agregado como favorito..!", Toast.LENGTH_LONG).show();
        if(estado ==3)
            Toast.makeText(vistaInf.getContext(), "Este sitio se a eliminado como favorito..!", Toast.LENGTH_LONG).show();
        if(estado==0)
            Toast.makeText(vistaInf.getContext(), "Hubo un error al procesar los datos", Toast.LENGTH_LONG).show();
    }
}
