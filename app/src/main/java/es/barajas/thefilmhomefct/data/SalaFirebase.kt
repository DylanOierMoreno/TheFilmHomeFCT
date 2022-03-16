package es.barajas.thefilmhomefct.data

// Esta clase DATA define los atributos que va a tener una sala de cine en la BBDD FIREBASE
data class SalaFirebase(val fecha: String?, val horaSesion: String?, val tituloPelicula: String?,
    val nombreCine: String?, val listaButacas: List<ButacaFirebase>?, val referencia: String?)