package es.barajas.thefilmhomefct.ui.peliculas.elementos

import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.widget.*
import com.google.firebase.firestore.*
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.data.ButacaFirebase
import es.barajas.thefilmhomefct.data.SalaFirebase

class Sala(actividad: Activity, tabla: TableLayout, diaSesion: String, horaSesion: String, peliculaSesion: String, cineSesion: String, numTotalEntradas: Int) {
    private var tabla: TableLayout //Layout para la tabla
    private var listaFilas: ArrayList<TableRow> //Array de las filas para la tabla
    private var actividad: Activity
    private var rs: Resources
    private var horaSesion: String //Fecha de la sesion
    private var diaSesion: String //Fecha de la sesion
    private var peliculaSesion: String //Fecha de la sesion
    private lateinit var cineSesion: String //Cine donde se va a ver la pelicula
    private var numTotalEntradas: Int
    //Tipos de asiento según el número 0->Libre, 1->Seleccionado, 2->Accesible, 3->Inhabilitado, 4->Ocupado
    private var tipoAsiento: Long = 0L
    private var listaButacas: ArrayList<Butaca> //lISTA de OBJETOS BUTACA
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    //Lista de butacas de tipo BUTACAFIREBASE para la BBDD Firebase
    private var listaButacasFirebase: ArrayList<ButacaFirebase> = ArrayList()
    //Nombre del documento en BBDD
    private var nombreDocumento: String

    init {
        this.actividad = actividad
        this.tabla = tabla
        rs = this.actividad.resources
        listaFilas = ArrayList()
        this.listaButacas = ArrayList()
        this.horaSesion = horaSesion
        this.diaSesion = diaSesion
        this.peliculaSesion = peliculaSesion
        this.cineSesion = cineSesion
        this.numTotalEntradas = numTotalEntradas
        nombreDocumento = diaSesion + horaSesion + eliminarMinusculas(peliculaSesion) + abreviaturaCines(cineSesion)
    }

    //MÉTODOS
    @Suppress("UNCHECKED_CAST")
    fun dibujarSala() {
        val layoutFila: TableRow.LayoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT)
        val layoutCelda: TableRow.LayoutParams = TableRow.LayoutParams(100, 98)
        db.collection("sala").document(nombreDocumento).get().addOnSuccessListener {
            if (it.exists()) {
                db.collection("sala").document(nombreDocumento).get().addOnCompleteListener {
                    val document: DocumentSnapshot = it.result!!
                    if (it.isSuccessful && document.exists()) {
                        var indiceLista = 0
                        for (numFila in 0 until 6) {
                            val fila = TableRow(actividad)
                            fila.layoutParams = layoutFila
                            for (numColumna in 0 until 13) {
                                if (!((numColumna == 2) || ((numFila == 4 || numFila == 5) && (numColumna == 0 || numColumna == 1)))) {
                                    val listaEntera: List<Any?> = document.data?.get("listaButacas") as List<Any?>
                                    val elementoMapa:Map<String, Any?> = listaEntera[indiceLista] as Map<String, Any?>
                                    tipoAsiento = elementoMapa["tipo"] as Long
                                    val butaca = Butaca(actividad, convertirFilaLetra(numFila), numColumna, tipoAsiento)
                                    crearButaca(butaca, layoutCelda, fila, esLeida = true)
                                    indiceLista++
                                } else {
                                    val pasillo = Butaca(actividad, convertirFilaLetra(numFila), numColumna)
                                    dibujarZonasVacias(pasillo)
                                    pasillo.getButacaCine().layoutParams = layoutCelda
                                    fila.addView(pasillo.getButacaCine())
                                }
                            }
                            tabla.addView(fila)
                        }
                        recorrerListaObjetos()
                    }
                }
            }
            else {
                for (numFila in 0 until 6) {
                    val fila = TableRow(actividad)
                    fila.layoutParams = layoutFila
                    for (numColumna in 0 until 13) {
                        val butaca = Butaca(actividad, convertirFilaLetra(numFila), numColumna)
                        crearButaca(butaca, layoutCelda, fila, esLeida = false)
                    }
                    tabla.addView(fila)
                }
                recorrerListaObjetos()
                val salaFirebase = SalaFirebase(diaSesion, horaSesion, peliculaSesion, cineSesion,
                    listaButacasFirebase, nombreDocumento)
                db.collection("sala").document(nombreDocumento).set(salaFirebase)
            }
        }
    }

    private fun convertirFilaLetra(numFila: Int): Char {
        var letraFila = ' '
        when (numFila) {
            0 -> letraFila = 'A'
            1 -> letraFila = 'B'
            2 -> letraFila = 'C'
            3 -> letraFila = 'D'
            4 -> letraFila = 'E'
            5 -> letraFila = 'F'
        }
        return letraFila
    }

    private fun agregarFilaButaca(objetoButaca: Butaca, esLeida: Boolean): ImageButton {
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(100, 98)
        params.leftMargin = 22
        params.rightMargin = 22
        objetoButaca.getButacaCine().layoutParams = params
        crearTabla(objetoButaca, esLeida)
        return objetoButaca.getButacaCine()
    }

    private fun crearButaca(butaca: Butaca, layoutCelda: TableRow.LayoutParams, fila: TableRow, esLeida: Boolean) {
        agregarFilaButaca(butaca, esLeida)
        butaca.getButacaCine().layoutParams = layoutCelda
        fila.addView(butaca.getButacaCine())
        desencadenarClic(butaca)
    }

    private fun crearTabla(objetoButaca: Butaca, esLeida: Boolean) {
        val columnaButaca = objetoButaca.getColumna()
        val filaButaca = objetoButaca.getFila()
        if ((columnaButaca == 2) || ((filaButaca == 'E' || filaButaca == 'F') && (columnaButaca == 0 || columnaButaca == 1)))  {
            dibujarZonasVacias(objetoButaca)
        } else {
            if (esLeida) {
                when(objetoButaca.getTipo()) {
                    0L -> objetoButaca.setImagenButaca(R.drawable.asiento_libre)
                    1L -> objetoButaca.setImagenButaca(R.drawable.asiento_seleccionado)
                    2L -> objetoButaca.setImagenButaca(R.drawable.asiento_accesible)
                    3L -> objetoButaca.setImagenButaca(R.drawable.asiento_inhabilitado)
                    4L -> objetoButaca.setImagenButaca(R.drawable.asiento_ocupado)
                }
            } else {
                objetoButaca.setImagenButaca(R.drawable.asiento_libre)
                asientoAccessible(objetoButaca)
            }
            listaButacas.add(modificarValorColumnas(objetoButaca))
        }
    }

    // Dibuja los asientos accesibles
    private fun asientoAccessible(objetoButaca: Butaca) {
        val columnaButaca = objetoButaca.getColumna()
        val filaButaca = objetoButaca.getFila()
        if (filaButaca == 'F' && (columnaButaca == 3 || columnaButaca == 12)) {
            objetoButaca.setImagenButaca(R.drawable.asiento_accesible)
            objetoButaca.setTipo(2L)
        }
    }

    private fun dibujarZonasVacias(objetoButaca: Butaca){
        val silla: ImageButton = objetoButaca.getButacaCine()
        silla.setImageResource(0)
        silla.setBackgroundColor(Color.WHITE)
        silla.isEnabled = false
    }

    private fun desencadenarClic(objetoButaca: Butaca) {
        val silla = objetoButaca.getButacaCine()
        val butacaSeleccionada = objetoButaca
        silla.setOnClickListener {
            if (entradasRestantes() > 0 || objetoButaca.getTipo() == 1L) {
                val fila = objetoButaca.getFila()
                val columna = objetoButaca.getColumna()
                val tipo = objetoButaca.getTipo()
                if ((tipo == 1L) and (fila == 'F') and ((columna == 3) || (columna == 12))) {
                    objetoButaca.setImagenButaca(R.drawable.asiento_accesible)
                    objetoButaca.setTipo(2L)
                }
                else if ((tipo == 1L) and (fila == 'F') and ((columna != 3) || (columna != 12))) {
                    objetoButaca.setImagenButaca(R.drawable.asiento_libre)
                    objetoButaca.setTipo(0L)
                }
                else if ((tipo == 1L) and (fila != 'F')) {
                    objetoButaca.setImagenButaca(R.drawable.asiento_libre)
                    objetoButaca.setTipo(0L)
                } else if ((tipo == 0L) || (objetoButaca.getTipo() == 2L)) {
                    objetoButaca.setImagenButaca(R.drawable.asiento_seleccionado)
                    objetoButaca.setTipo(1L)
                }
                val butacaActualizada = objetoButaca
                actualizarButacaBBDD(butacaSeleccionada, butacaActualizada)
            }
        }
    }

    //Cambia el valor de las columnas
    private fun modificarValorColumnas(objetoButaca: Butaca): Butaca {
        objetoButaca.setColumna(objetoButaca.getColumna())
        return objetoButaca
    }

    //dibuja una silla inhabilitada y actualiza el código
    private fun dibujarSillaInhabilitada(objetoButaca: Butaca) {
        objetoButaca.setImagenButaca(R.drawable.asiento_inhabilitado)
        objetoButaca.setTipo(3L)
    }

    //Recorremos la lista de objetos butaca
    private fun recorrerListaObjetos() {
        var contadorVueltas = 0
        var listaRecorrida: Boolean
        for (i in 0 until listaButacas.size) {
            if ((contadorVueltas == 3 || contadorVueltas == 6 || contadorVueltas == 8) &&
                (listaButacas[i].getImagenButaca() != R.drawable.asiento_accesible)) {
                dibujarSillaInhabilitada(listaButacas[i])
            }
            contadorVueltas++
            if (contadorVueltas >= 9) contadorVueltas = 0
            listaRecorrida = i == listaButacas.size
            guardarButacaBBDD(listaButacas[i], listaRecorrida)
        }
    }

    fun guardarButacaBBDD(butaca: Butaca, listaRecorrrida: Boolean) {
        val butacaFirebase = ButacaFirebase(butaca.getFila().toString(), butaca.getColumna(), butaca.getTipo())
        db.collection("butaca").document("A0").set(butacaFirebase)
        if (!listaRecorrrida) listaButacasFirebase.add(butacaFirebase)
    }

    fun actualizarButacaBBDD(butacaPulsada: Butaca, butacaActualizada: Butaca) {
        val butacaFirebase = ButacaFirebase(butacaPulsada.getFila().toString(),
            butacaPulsada.getColumna(), butacaPulsada.getTipo())
        val tipo = butacaActualizada.getTipo()
        for (i in 0 until listaButacasFirebase.size) {
            if (butacaFirebase.columna == listaButacasFirebase[i].columna &&
                butacaFirebase.fila.equals(listaButacasFirebase[i].fila)) {
                listaButacasFirebase[i].tipo = tipo
                //---------------SALA---------------
                val salaFirebase = SalaFirebase(diaSesion, horaSesion, peliculaSesion, cineSesion,
                    listaButacasFirebase, nombreDocumento)
                val nombreDocumento = diaSesion + horaSesion + eliminarMinusculas(peliculaSesion) +
                        abreviaturaCines(cineSesion)
                db.collection("sala").document(nombreDocumento).set(salaFirebase)
            }
        }
    }

    /* Función para crear un string formado por las mayúsculas del String que llega por parámetro
     y además elimina los espacios */
    private fun eliminarMinusculas(palabra: String): String {
        var palabraMayusculas = ""
        for (i in palabra.indices) {
            if (palabra[i] == palabra[i].toUpperCase()) palabraMayusculas += palabra[i]
        }
        return palabraMayusculas.replace(" ", "")
    }

    //Crea una abreviatura para el nombre del cine pasdo por parámetro
    private fun abreviaturaCines(nombreCine: String): String {
        var abreviatura = ""
        when (nombreCine) {
            "Parque Corredor" -> abreviatura = "PC"
            "Plenilunio" -> abreviatura = "Pl"
            "Mendez Alvaro" -> abreviatura = "MA"
            "Arenas de Barcelona" -> abreviatura = "AB"
            "Cinemes Verdi" -> abreviatura = "CV"
            "Multicines El Centro" -> abreviatura = "MC"
        }
        return abreviatura
    }

    //Calcula el número de butacas seleccionadas en el dibujo
    fun numButacasSeleccionadas(): Int {
        var numButacas = 0
        for (i in 0 until listaButacas.size) {
            if (listaButacas[i].getTipo() == 1L) {
                numButacas++
            }
        }
        return numButacas
    }

    //calcula la diferencia entre las entradas que se quieren comprar y las seleccionadas en la pantalla
    private fun entradasRestantes(): Int = numTotalEntradas - numButacasSeleccionadas()

}