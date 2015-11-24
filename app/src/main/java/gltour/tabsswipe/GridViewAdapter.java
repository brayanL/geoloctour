package gltour.tabsswipe;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.gltour.gltour.R;
import com.squareup.picasso.Picasso;

public class GridViewAdapter extends BaseAdapter {
    private Context contexto;
    private String[] url_img;

    public GridViewAdapter(Context contexto, String[] url_img){
        super();
        this.contexto = contexto;
        this.url_img = url_img;
    }

    @Override
    public int getCount() {
        return url_img.length;
    }

    @Override
    public Object getItem(int position) {
        return url_img[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView img;
        if(convertView ==null){
            img = new ImageView(contexto);
            convertView = img;
            //img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        else{
            img = (ImageView)convertView;
        }
        Picasso.with(contexto)
                .load(configuraciones.Config.dominio+url_img[position])
                .placeholder(R.drawable.ic_img_avatar)
                .resize(240,200)
                .into(img);
        return convertView;
    }
}
