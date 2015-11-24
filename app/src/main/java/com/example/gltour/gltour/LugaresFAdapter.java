package com.example.gltour.gltour;

import java.io.Serializable;
/*
* Esta clase contiene los atributos y metodos necesarios para poder crear un objeto que contiene los
* datos basicos de un sitio turistico, esta clase es utilizada por la clase AdapterLugares y
* ListaLugares.
* */
public class LugaresFAdapter implements Serializable {

    private String id;
    private String nombre;
    private String descripcion;
    private String imagen;
    private String canton;
    private String calificacion;

    public LugaresFAdapter(String id, String nombre, String descripcion, String canton, String imagen,
                           String calificacion){
        super();
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.canton = canton;
        this.imagen = imagen;
        this.calificacion = calificacion;
    }

    /*public void setId(String id){this.id = id;}
    public void setNombre(String nombre){this.nombre = nombre;}
    public void setDescripcion(String descripcion){this.descripcion = descripcion;}
    public void setCanton(String canton){this.canton = canton;}
    public void setImagen(String imagen){this.imagen = imagen;}*/

    public String getId(){return this.id;}
    public String getNombre(){return this.nombre;}
    public String getDescripcion(){return this.descripcion;}
    public String getCanton(){return this.canton;}
    public String getImagen(){return this.imagen;}
    public String getCalificacion(){return this.calificacion;}
}
