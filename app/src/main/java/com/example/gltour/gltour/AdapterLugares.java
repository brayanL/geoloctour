package com.example.gltour.gltour;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterLugares extends ArrayAdapter<LugaresFAdapter> {
    private List<LugaresFAdapter> itemList;
    private Context context;
    public String calValue;

    public AdapterLugares(List<LugaresFAdapter> itemList, Context ctx){
        super(ctx,0, itemList);
        this.itemList = itemList;
        this.context = ctx;
    }

    public int getCount(){
        if(itemList != null)
            return itemList.size();
        return 0;
    }

    public LugaresFAdapter getItem(int position){
        if (itemList != null)
            return itemList.get(position);
        return null;
    }

    public long geItemId(int position){
        if(itemList!= null)
            return itemList.get(position).hashCode();
        return 0;
    }

    /*
    * En este metodo construimos las vistas que contiene los datos de cada sitio turistico asi como
    * su respectiva imagen, asignamos a objetos TextView, los TextView del Layout que inflamos y
    * modificamos su propiedad text para mostrar la informacion correspondiente.
    * Con la imagen es el mismo proceso a excepcion de que para cargar la imagen utilizamos una
     * libreria llamada Picasso, la cual recibe como parametro la ruta de la imagen, lo increible
     * de esta libreria es que estas imagenes se cargan en cache. Al final del metodo se retorna la
     * vista con la informacion que se cargo.
    * */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);
        View v = convertView;
        if(v == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_view_lugares,null);
        }
        /*Retorna objeto del tipoLugaresFAdapter dada una posicion, esto se extrae de el objeto List
        * del tipo LugaresFAdapter llamado itemList.
        * */

        LugaresFAdapter lg = itemList.get(position);

        TextView textName = (TextView)v.findViewById(R.id.textNameLugar);
        textName.setText(lg.getNombre());

        TextView textDesc = (TextView)v.findViewById(R.id.textDescLugar);
        textDesc.setText(lg.getDescripcion());

        TextView textCant = (TextView)v.findViewById(R.id.textCanton);
        textCant.setText(lg.getCanton());

        ImageView img = (ImageView)v.findViewById(R.id.imageLugar);
        Picasso.with(context).load(lg.getImagen()).resize(130,130).centerCrop().into(img);

        RatingBar ratingBar = (RatingBar)v.findViewById(R.id.ratingLugar);
        calValue = lg.getCalificacion();
        /*if(calValue.equals("null"))
            ratingBar.setRating(0);
        else
            ratingBar.setRating(Float.parseFloat(lg.getCalificacion()));*/
        ratingBar.setRating(Float.parseFloat(lg.getCalificacion()));
        return  v;
    }

    public List<LugaresFAdapter> getItemList(){
        return itemList;
    }

    public void setItemList(List<LugaresFAdapter> itemList){
        this.itemList = itemList;
    }
}
