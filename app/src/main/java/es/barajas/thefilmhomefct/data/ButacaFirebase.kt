package es.barajas.thefilmhomefct.data

// Esta clase DATA define los atributos que va a tener una butaca en la BBDD FIREBASE
data class ButacaFirebase (val fila: String? = null, val columna: Int? = null, var tipo: Long? = null)