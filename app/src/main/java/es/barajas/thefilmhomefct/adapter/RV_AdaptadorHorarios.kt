package es.barajas.thefilmhomefct.adapter

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Color
import android.opengl.Visibility
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.data.ListaHorarios
import es.barajas.thefilmhomefct.ui.peliculas.ElegirEntradas
import es.barajas.thefilmhomefct.ui.peliculas.LoginCompra
import java.util.*
import kotlin.collections.ArrayList

class RV_AdaptadorHorarios() : RecyclerView.Adapter<RV_AdaptadorHorarios.ViewHolderHorarios>() {
    //Esta lista contiene los nombres de los cines cuyos horarios vamos a leer. Se llena en HorarioPeliculaFragment.kt
    private var listaHorarios: ArrayList<ListaHorarios>? = null
    private lateinit var contexto: Context
    private lateinit var nombrePelicula: String
    private var diaPulsado: Int = -1
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    //USUARIO DE FIREBASE
    private val user = FirebaseAuth.getInstance().currentUser
    private var cine: String = ""

    constructor(listaHorarios: ArrayList<ListaHorarios>?, contexto: Context, nombrePelicula: String) : this() {
        this.listaHorarios = listaHorarios
        this.contexto = contexto
        this.nombrePelicula = nombrePelicula
    }

    class ViewHolderHorarios(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nombre: TextView? = itemView.findViewById(R.id.txt_nombreCine)
        var direccion: TextView? = itemView.findViewById(R.id.txt_direccionCine)
        var contenedorBotones: LinearLayout = itemView.findViewById(R.id.layoutHorariosBotones)
    }

    //Se infla items_horarios.xml
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderHorarios {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.items_horarios,
            null, false)
        return ViewHolderHorarios(view)
    }

    // Reemplaza el contenido de una vista de elementos de lista
    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: ViewHolderHorarios, position: Int) {
        if (user!!.isAnonymous) {
            holder.nombre?.text = listaHorarios!![position].nombre
            consultarHorario(holder, listaHorarios!![position].nombre, "direccion")
            calcularHorarios(holder, listaHorarios!![position].nombre)
        } else {
            val arrayProvincias = arrayListOf("", "Madrid", "Barcelona", "Cadiz")
            db.collection("usuarios").document(user.email!!).get().addOnCompleteListener { it ->
                val document = it.result
                val mapa_datosPersonales =
                    document!!.data?.get("lista_datosPersonales") as Map<String, Any>
                val provincia = mapa_datosPersonales["provincia"] as String
                val cine = mapa_datosPersonales["cine"] as String
                if (!provincia.equals("0")) {
                    db.collection("provincias").document(arrayProvincias[provincia.toInt()]).get()
                        .addOnCompleteListener { it1 ->
                            val listaCines =
                                it1.result!!.data?.get("listaCines") as List<DocumentReference>
                            val refCine = listaCines[cine.toInt()]
                            refCine.get().addOnCompleteListener {
                                val nombreCine = it.result!!.data?.get("nombre") as String
                                if (listaHorarios!![position].nombre != nombreCine) {
                                    holder.nombre?.text = listaHorarios!![position].nombre
                                    consultarHorario(holder, listaHorarios!![position].nombre, "direccion")
                                    calcularHorarios(holder, listaHorarios!![position].nombre)
                                } else {
                                    holder.nombre?.visibility = View.GONE
                                    holder.direccion?.visibility = View.GONE
                                }
                            }
                        }
                } else {
                    holder.nombre?.text = listaHorarios!![position].nombre
                    consultarHorario(holder, listaHorarios!![position].nombre, "direccion")
                    calcularHorarios(holder, listaHorarios!![position].nombre)
                }
            }
        }
    }

    override fun getItemCount(): Int = listaHorarios!!.size

    //Leer el nombre y horario de cada cine de la BBDD FIREBASE
    @Suppress("SpellCheckingInspection", "SameParameterValue")
    private fun consultarHorario(holder: ViewHolderHorarios, nombreCine: String, dato: String) {
        db.collection("cines").document(nombreCine).get().addOnSuccessListener {
            when (dato) { "direccion" -> holder.direccion?.text = it.get(dato) as String }
        }.addOnFailureListener {
            System.out.println("========ERROR!!!!!======== " + it)
        }
    }

    // Leemos las horas de apertura y cierre del cine y la duración de la película en la que estamos.
    @Suppress("SameParameterValue")
    private fun calcularHorarios(holder: ViewHolderHorarios, nombreCine: String) {
        var apertura: String
        var cierre: String
        var descripcionPelicula: String
        var duracionPelicula: Int
        db.collection("cines").document(nombreCine).get().addOnCompleteListener { it ->
            val document: DocumentSnapshot = it.result!!
            apertura = document.data?.get("apertura") as String
            cierre = document.data?.get("cierre") as String
            db.collection("peliculasCartelera").document(nombrePelicula).get().addOnSuccessListener {
                descripcionPelicula = it.get("descripcion") as String
                duracionPelicula = descripcionPelicula.substring(descripcionPelicula.indexOf('/') + 2,
                    descripcionPelicula.lastIndexOf(' ')).toInt()
                calcularHoras(holder, apertura, cierre, duracionPelicula, nombreCine)
            }
        }
    }

    /* Calcula los horario posibles entre hora de apertura y hora de cierre teneindo en cuenta la
       duración de la película */
    private fun calcularHoras(holder: ViewHolderHorarios, apertura: String, cierre: String, duracion: Int, nombreCine: String) {
        var numeroVueltas = 0
        val arrayHorarios: ArrayList<String> = ArrayList()
        var horaApertura: Int
        var minutosApertura: Int
        var horaHorario = 0
        var minutosHorario: Int
        horaApertura = apertura.substring(0, apertura.indexOf(':')).toInt()
        minutosApertura = apertura.substring(apertura.indexOf(':') + 1, apertura.length).toInt()
        val horaCierre: Int = cierre.substring(0, cierre.indexOf(':')).toInt()
        val resultadoHora: Int = duracion.div(60) //duracion / 60 Cociente de la división
        val resultadoMinutos: Int = duracion.rem(60) //duracion % 60 Resto de la división
        while ((horaHorario + resultadoHora + 1) < horaCierre) {
            minutosHorario = minutosApertura + resultadoMinutos
            if (minutosHorario >= 60) {
                horaHorario = horaApertura + resultadoHora + 1
                minutosHorario = minutosHorario.rem(60)
            } else {
                horaHorario = horaApertura + resultadoHora
            }
            horaApertura = horaHorario
            minutosApertura = minutosHorario
            numeroVueltas++
            //damos formato a los minutos para que cuando son menores de 10, tengan un 0 delante
            arrayHorarios.add(horaHorario.toString() + ":" + String.format("%02d", minutosHorario))
        }
        generarBotones(numeroVueltas, holder, arrayHorarios, nombreCine)
    }

    //Generamos DINÁMICAMENTE los BOTONES necesarios, tanto como sesiones se calculen para cada película
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun generarBotones(elementos: Int, holder: ViewHolderHorarios, arrayHorarios: ArrayList<String>, nombreCine: String) {
        val listaBotones: ArrayList<Button> = ArrayList()
        listaBotones.clear()
        lateinit var sesionElegida: String
        for (i in 0 until elementos) {
            val boton = Button(contexto)
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(260, 135)
            params.rightMargin = 70
            params.topMargin = 20
            boton.layoutParams = params
            boton.text = arrayHorarios[i]
            boton.setTextColor(Color.WHITE)
            boton.gravity = View.TEXT_ALIGNMENT_GRAVITY
            boton.background = ContextCompat.getDrawable(contexto, R.drawable.boton_redondeado)
            listaBotones.add(boton)
            holder.contenedorBotones.addView(boton)
        }
        for (i in 0 until listaBotones.size) {
            listaBotones[i].setOnClickListener {
                sesionElegida = listaBotones[i].text.toString()
                if (diaPulsado != -1) {
                    val clase: Class<*> = if (user!!.isAnonymous) {
                        LoginCompra::class.java
                    } else {
                        ElegirEntradas::class.java
                    }
                    val intent = Intent(contexto, clase).apply {
                        putExtra("nombrePelicula", nombrePelicula)
                        putExtra("nombreCine", nombreCine)
                        putExtra("sesionElegida", sesionElegida)
                        putExtra("diaEscogido", diaPulsado.toString())
                    }
                    contexto.startActivity(intent)
                } else {
                    mostrarAlerta()
                }
            }
        }
    }

    /* Función PÚBLICA para poder acceder a ella desde otras clases. Recoge el día elegido
       en HorarioPeliculaFragment (índice del grid) y lo devuelve para utilizarlo en otras clases. */
    fun recogerDiaElegido(diaElegido: Int) {
        diaPulsado = diaElegido
    }

    private fun mostrarAlerta() {
        val builder = AlertDialog.Builder(contexto)
        builder.setTitle("Error")
        builder.setPositiveButton("Aceptar", null)
        builder.setMessage("Debes seleccionar un día de la semana")
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}