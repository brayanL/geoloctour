package com.example.gltour.gltour;

import android.hardware.camera2.params.Face;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.Profile;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.ArrayList;

import configuraciones.Config;


public class MainActivity extends ActionBarActivity {

    private static String TAG = MainActivity.class.getSimpleName();

    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    public TextView userNav, descNav;


    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instanciamos solo una vez el Sdk de Facebook

        /*int cont =0;
        while(!FacebookSdk.isInitialized()){
            cont+=1;
            Log.w("Entro:",""+cont);
        }
        setEstadoUser();*/

        mNavItems.add(new NavItem("Lugares", getString(R.string.descLug), R.drawable.ic_lugares));
        mNavItems.add(new NavItem("Mis Favoritos", getString(R.string.descFav), R.drawable.ic_mfavoritos));
        mNavItems.add(new NavItem("Mas Votados", getString(R.string.desMV), R.drawable.ic_mvotados));
        mNavItems.add(new NavItem("Perfil", getString(R.string.descPerfil), R.drawable.ic_perfil));
        mNavItems.add(new NavItem("Acerca de", getString(R.string.descAD), R.drawable.ic_acercade));

        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        // Populate the Navigtion Drawer with options
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });


        //Ultimo Agregado
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "onDrawerClosed: " + getTitle());

                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);


        //Para poder abrir el Navigation Drawable al tocar el icono del menu
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void selecItem(int position){
        if(mDrawerList != null)
            mDrawerList.setItemChecked(position,true);

    }

    //Es utilizada para saber cuando el usario dio clic en el Icono del Menu y Desplegar el
    // Navigation Drawable

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    //Para actualizar el estado del icono del menu indicando si el menu esta abierto o cerrado
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        this.setEstadoUser();
        //selectItemFromDrawer(2);
    }

    //Para poder llamar a los fragmentos relacionados con cada item del listview
    private void selectItemFromDrawer(int position) {
        Fragment fragment = null;

        switch (position){
            case 0:
                fragment = new Lugares();
                break;
            case 1:
                if(!Config.id_cliente.equals("0")){
                    Bundle bdl = new Bundle();
                    bdl.putString("parametro",Config.id_cliente);
                    bdl.putString("url","/rest/get_favoritos/");
                    fragment = new ListaLugares();
                    fragment.setArguments(bdl);
                    break;
                }
                else
                    Toast.makeText(this, getString(R.string.txtInfoNL),
                            Toast.LENGTH_LONG).show();
                break;
            case 2:
                //fragment = new MasVotado();
                if(!Config.id_cliente.equals("0")){
                    Bundle bdl = new Bundle();
                    bdl.putString("parametro","");
                    bdl.putString("url","/rest/get_masVotados/");
                    fragment = new ListaLugares();
                    fragment.setArguments(bdl);
                    break;
                }
                else
                    Toast.makeText(this, getString(R.string.txtInfoNL),
                            Toast.LENGTH_LONG).show();
                break;
            case 3:
                fragment = new Perfil();
                break;
            case 4:
                fragment = new AcercaDe();
                break;
        }

        if(fragment!=null){
            FragmentManager fragmentManager = this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.mainContent, fragment)
                    .commit();
        }

        mDrawerList.setItemChecked(position, true);
        setTitle(mNavItems.get(position).mTitle);

        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    /*
        * Para personalizar el contenido de cada item en el listview
        * */
    class NavItem {
        String mTitle;
        String mSubtitle;
        int mIcon;

        public NavItem(String title, String subtitle, int icon) {
            mTitle = title;
            mSubtitle = subtitle;
            mIcon = icon;
        }
    }

    class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<NavItem> mNavItems;

        public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
            mContext = context;
            mNavItems = navItems;
        }

        @Override
        public int getCount() {
            return mNavItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNavItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            titleView.setText( mNavItems.get(position).mTitle );
            subtitleView.setText( mNavItems.get(position).mSubtitle );
            iconView.setImageResource(mNavItems.get(position).mIcon);

            return view;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("LlamoResume", "si");
        //selectItemFromDrawer(4);
        //setEstadoUser();
    }

    /*
    * Esta funcion permite actualizar el menu lateral(Navigation Drawer)
    * La funcion fetchProfileForCurrentAccessToken() de la clase Profile
    * busca el token de Acceso
    * */

     public void setEstadoUser(){
         //AccessToken.getCurrentAccessToken();
         //Profile.fetchProfileForCurrentAccessToken();
         Log.w("Ingreso",Profile.getCurrentProfile()+"");
         userNav = (TextView)findViewById(R.id.userName);
         descNav = (TextView)findViewById(R.id.desc);

        if(Profile.getCurrentProfile()!=null){
            Log.w("setEstadoUser","Obtuvo Token");
            userNav.setText(Profile.getCurrentProfile().getFirstName()+" " +
                    Profile.getCurrentProfile().getLastName());
            descNav.setText("Ir a Perfil para Cerrar Sesion");
            new getIdCliente().execute(Profile.getCurrentProfile().getId());
        }
        else{
            Log.w("setEstadoUser","NO Obtuvo Token");
            Config.id_cliente="0";
            Config.nombre_cli="";
            userNav.setText(getString(R.string.nameuser));
            descNav.setText(getString(R.string.info));
        }
    }

    /*
    * Funcion para comprobar si el suario esta logeado si es asi el servidor me devolvera
    * el id del cliente, para almacenarlo en una variable estatica y poder acceder a esa
    * informacion desde cualquier parte de la aplicacion. Caso contrario me devolvera un cero, con
     * lo cual se procedera a registrar ese usuario en la base de datos. Esta funcion sera llamada
     * cuando el usuario inicia por primera vez la aplicacion y cuando cierre sesion en la misma.
    * */

     public class getIdCliente extends AsyncTask<String,Void,Void>{
         private String id_facebook;

        @Override
        protected Void doInBackground(String... params) {
            id_facebook = params[0];
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet conexion = new HttpGet(Config.dominio+"/rest/check_client/"+id_facebook);
            try{
                HttpResponse resp = httpClient.execute(conexion);
                String datos = EntityUtils.toString(resp.getEntity());
                if(datos.equals("0")){
                    //Registra el usuario
                    Profile.fetchProfileForCurrentAccessToken();
                    conexion.setURI(URI.create(Config.dominio + "/rest/reg_client/" +
                            id_facebook + "/" +
                            Profile.getCurrentProfile().getFirstName().replace(" ", "%20") + "%20" +
                            Profile.getCurrentProfile().getLastName().replace(" ", "%20")));
                    resp = httpClient.execute(conexion);
                    datos = EntityUtils.toString(resp.getEntity());

                    Config.id_cliente = datos;
                    Config.nombre_cli= Profile.getCurrentProfile().getFirstName()+" "+
                    Profile.getCurrentProfile().getLastName();
                    Log.w("MENSAJE", "Se registro Usuario " + datos);
                }
                else{
                    Config.id_cliente = datos;
                    Config.nombre_cli= Profile.getCurrentProfile().getFirstName()+" "+
                            Profile.getCurrentProfile().getLastName();
                }

                Log.e("ID_CLIENTE",Config.id_cliente);

            }catch (Exception ex){
                Log.e("Error: ",ex.toString());
            }
            return null;
        }
     }
}
