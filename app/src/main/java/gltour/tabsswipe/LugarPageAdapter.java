package gltour.tabsswipe;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

/*
* Para poder Administrar las Paginas de un ViewPager es necesario contar con un Adaptador, existen de
* dos tipos el FragmentPageAdapter y FragmentStatePagerAdapter. Este adaptador nos permite administrar
* la informacion que se va a mostrar, cada pagina es un Fragmento y se trata como tal*/
public class LugarPageAdapter extends FragmentStatePagerAdapter {

    public LugarPageAdapter(FragmentManager fm ){
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new InfoFragment();
            case 1:
                return new MapsFragment();
            case 2:
                return new ImagesFragment();
            case 3:
                return new EventsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0: return "Informacion";
            case 1: return "Mapas";
            case 2: return "Imagenes";
            case 3: return "Eventos";
        }
        return null;
    }
}
