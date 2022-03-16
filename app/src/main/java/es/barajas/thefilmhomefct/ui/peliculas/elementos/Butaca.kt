package es.barajas.thefilmhomefct.ui.peliculas.elementos

import android.app.Activity
import android.widget.ImageButton
import android.widget.ImageView
import java.util.*

class Butaca(activity: Activity) {
    private var fila: Char //Fila en letra
    private var columna: Int = 0 //Columna en número
    private var tipo: Long //Tipos de asiento según el número 0->Libre, 1->Seleccionada, 2->Accesible, 3->Inhabilitado, 4->Comprada
    private var butacaCine: ImageButton //ImageButton que vamos a ver en la pantalla
    private var imagenButaca: Int = 0 //Imagen que le da forma a la ImageButton (libre, ocupada, accesible, inhabilitada, etc)

    init {
        this.fila = 'A'
        this.columna = 0
        this.tipo = 0L
        this.butacaCine = ImageButton(activity)
    }

    constructor(activity: Activity, fila: Char, columna: Int, tipo: Long) : this(activity) {
        this.fila = fila
        this.columna = columna
        this.tipo = tipo
        this.butacaCine = ImageButton(activity)
    }

    constructor(activity: Activity, fila: Char, columna: Int) : this(activity) {
        this.fila = fila
        this.columna = columna
        this.tipo = 0L
        this.butacaCine = ImageButton(activity)
    }

    //GETTERS
    fun getFila(): Char {
        return fila
    }

    fun getTipo(): Long{
        return tipo
    }

    fun getColumna(): Int{
        return columna
    }

    fun getButacaCine(): ImageButton{
        return butacaCine
    }

    fun getImagenButaca(): Int{
        return imagenButaca
    }

    //SETTERS
    fun setTipo(numTipo: Long){
        this.tipo = numTipo
    }

    fun setColumna(numColumna: Int) {
        if (numColumna == 0 || numColumna == 1) {
            this.columna = numColumna + 1
        } else {
            this.columna = numColumna
        }
    }

    fun setImagenButaca(vistaButaca: Int){
        this.imagenButaca = vistaButaca
        butacaCine.setImageResource(vistaButaca)
    }
}