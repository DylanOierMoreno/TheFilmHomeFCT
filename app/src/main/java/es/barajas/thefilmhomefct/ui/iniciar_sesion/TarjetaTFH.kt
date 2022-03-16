package es.barajas.thefilmhomefct.ui.iniciar_sesion

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class TarjetaTFH {
    //ATRIBUTOS
    private lateinit var numTarjeta: String
    private var puntos: Int = 0
    //Instancia de la BASE DE DATOS de FIREBASE
    private val db = FirebaseFirestore.getInstance()
    private var contexto: Context

    constructor(contexto: Context) {
        this.contexto = contexto
        this.puntos = 0
    }

    //CONSTRUCTOR con parámetros
    constructor(contexto: Context, numTarjeta: String, puntos: Int) : this(contexto) {
        this.contexto = contexto
        this.numTarjeta = numTarjeta
        this.puntos = puntos
    }

    //Lee el código de tarjeta de la BBDD
     fun leerCodigoTarjeta() {
        var codigo: String
        db.collection("codigoTarjeta").document("codigo").get().addOnSuccessListener {
            var numeroTarjeta = it.get("codigo") as Long
            codigo = "TFH" + String.format("%010d", ++numeroTarjeta)
            db.collection("codigoTarjeta").document("codigo").update("codigo", numeroTarjeta)
            setNumTarjeta(codigo)
        }
    }

    //GETTERS
    fun getNumTarjeta(): String = numTarjeta

    fun getPuntos(): Int = puntos

    //SETTERS
    fun setNumTarjeta(numTarjeta: String){
        this.numTarjeta = numTarjeta
    }

    fun setPuntos(puntos: Int) {
        this.puntos = puntos
    }
}