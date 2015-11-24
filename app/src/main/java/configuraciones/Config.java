package configuraciones;

import android.widget.TextView;

/*
* Estableceemos el dominio de donde se hace las peticiones GET Y POST al servicio
* REST, el cual es declarado como final static porque es un valor constante que no se modificara
* en tiempo de ejecucion.
* Las cadenas id_cliente y nombre_cli, son modificadas la primera vez que la aplicacion es iniciada,
* o cuando se inicia sesion en Facebbook.
 */
public class Config {
    //public static final String dominio="http://192.168.0.103:8000";
    public static final String dominio="http://geoloctour.com";
    public static String id_cliente="0";
    public static String nombre_cli="";
}