package es.barajas.thefilmhomefct.ui.peliculas

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.adapter.RV_AdaptadorHorarios
import es.barajas.thefilmhomefct.data.ListaHorarios
import java.util.*
import kotlin.collections.*

@Suppress("DEPRECATION")
class HorarioPeliculaFragment(nombrePelicula: String) : Fragment() {
    private lateinit var gridHorarios: GridView
    private var listaHorarios: ArrayList<ListaHorarios>? = null
    private val pelicula = nombrePelicula
    //Cine prefediro (Este layout estará oculto si no se ha escogido cine preferido)
    private lateinit var layout_cinePreferido: LinearLayout
    private lateinit var layout_horariosPref: LinearLayout
    private lateinit var txv_cinePref: TextView
    private lateinit var txv_dirreccionCinePref: TextView
    private var diaPulsado: Int = -1
    private lateinit var adapter: RV_AdaptadorHorarios
    //Color de fondo para el grid que contiene los días
    private val colorFondo: Int = Color.rgb(201, 201, 201)
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    //Instancia para los USUARIOS de Firebase
    private val user = Firebase.auth.currentUser
    private lateinit var root: View
    private lateinit var recyclerView: RecyclerView
    private val array_provincias: Array<String> = arrayOf("", "Madrid", "Barcelona", "Cadiz")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        root = inflater.inflate(R.layout.fragment_horario_pelicula, container, false)
        inicializarVariables()
        mostrarCinePreferido()
        //-----RECYCLERVIEW con los CINES y sus HORARIOS-------
        recyclerView = root.findViewById(R.id.recyclerview_horarios)
        recyclerView.setHasFixedSize(true)
        construirRecycler(recyclerView)
        return root
    }

    private fun inicializarVariables() {
        gridHorarios = root.findViewById(R.id.lst_diasHorario)
        layout_cinePreferido = root.findViewById(R.id.layoutCinePreferido)
        layout_horariosPref = root.findViewById(R.id.layoutHorariosPreferente)
        txv_cinePref = root.findViewById(R.id.txt_nombreCinePreferente)
        txv_dirreccionCinePref = root.findViewById(R.id.txt_direccionCinePreferente)
    }

    override fun onResume() {
        super.onResume()
        hashmapHorario(root)
    }

    //Crea un mapa donde se guarda dia en número y letra durante una semana desde el día de hoy
    private fun hashmapHorario(view: View) {
        val mapaHorario: LinkedHashMap<String, Int> = LinkedHashMap()
        var dia: Int
        lateinit var diaLetra: String
        val d = Date()
        val cal = GregorianCalendar()
        cal.time = d
        dia = cal[Calendar.DAY_OF_WEEK]
        for (num in 0..6) {
            val diaNumero = sumarDiasAFecha(d, num).date
            when (dia) {
                1 -> diaLetra = "Dom"
                2 -> diaLetra = "Lun"
                3 -> diaLetra = "Mar"
                4 -> diaLetra = "Mie"
                5 -> diaLetra = "Jue"
                6 -> diaLetra = "Vie"
                7 -> diaLetra = "Sab"
            }
            mapaHorario[diaLetra] = diaNumero
            if (dia < 7) {
                dia++
            } else {
                dia = 1
            }
        }
        val listItem: ArrayList<LinkedHashMap<String, String>> = ArrayList()
        val adaptador = SimpleAdapter(view.context, listItem, R.layout.map_horario_item,
            arrayOf("titulo", "subtitulo"), intArrayOf(R.id.txt_TituloHorario, R.id.txt_SubtituloHorario))
        val iterator = mapaHorario.entries.iterator()
        while (iterator.hasNext()) {
            val resultsMap: LinkedHashMap<String, String> = LinkedHashMap()
            val pair = iterator.next()
            resultsMap["titulo"] = pair.key
            resultsMap["subtitulo"] = pair.value.toString()
            listItem.add(resultsMap)
        }
        gridHorarios.adapter = adaptador
        ponerALaEscuchaListaGrid()
    }

    //Método para poder calcular una fecha a partir de la fecha actual
    private fun sumarDiasAFecha(fecha: Date, dias: Int): Date {
        if (dias == 0) return fecha
        val calendario = Calendar.getInstance()
        calendario.time = fecha
        calendario.add(Calendar.DAY_OF_YEAR, dias)
        return calendario.time
    }

    //Poner a la escucha los ítems del grid (días que contiene las sesiones del cine)
    @SuppressLint("ResourceAsColor")
    private fun ponerALaEscuchaListaGrid() {
        gridHorarios.onItemClickListener = OnItemClickListener { parent, _, position, _ ->
            parent[position].setBackgroundColor(colorFondo)
            for (i in 0 until gridHorarios.numColumns) {
                if ((parent.getChildAt(i) != parent.getChildAt(position))) {
                    parent[i].setBackgroundColor(Color.TRANSPARENT)
                } else {
                    diaPulsado = position
                    adapter.recogerDiaElegido(diaPulsado)
                }
            }
        }
    }

    //RecyclerView que carga cada cine con sus sesiones
    private fun construirRecycler(recyclerView: RecyclerView) {
        listaHorarios = ArrayList()
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        llenarHorario()
    }

    //Añadimos a la lista los cines cuyos horarios queremos ver y que se recorren en RV_AdaptadorHorarios.kt
    @Suppress("UNCHECKED_CAST")
    private fun llenarHorario() {
        if (user!!.isAnonymous) {
            llenarListaAnonymous()
            adapter = RV_AdaptadorHorarios(listaHorarios, requireContext(), pelicula)
            recyclerView.adapter = adapter
        } else {
            db.collection("usuarios").document(user.email!!).get().addOnCompleteListener {
                val documentUser: DocumentSnapshot = it.result!!
                val mapa_DatosPersonales: Map<String, Any> =
                    documentUser.data!!["lista_datosPersonales"] as Map<String, Any>
                val indiceProvincia =
                    mapa_DatosPersonales["provincia"] as String
                val provincia = array_provincias[indiceProvincia.toInt()]
                if (indiceProvincia.equals("0")) {
                    llenarListaAnonymous()
                } else {
                    layout_cinePreferido.visibility = View.VISIBLE
                    when (provincia) {
                        "Madrid" -> {
                            listaHorarios!!.add(ListaHorarios("Parque Corredor"))
                            listaHorarios!!.add(ListaHorarios("Plenilunio"))
                            listaHorarios!!.add(ListaHorarios("Mendez Alvaro"))
                        }
                        "Barcelona" -> {
                            listaHorarios!!.add(ListaHorarios("Arenas de Barcelona"))
                            listaHorarios!!.add(ListaHorarios("Cinemes Verdi"))
                        }
                        "Cadiz" -> listaHorarios!!.add(ListaHorarios("Multicines El Centro"))
                    }
                }
                adapter = RV_AdaptadorHorarios(listaHorarios, requireContext(), pelicula)
                recyclerView.adapter = adapter
            }
        }
    }

    private fun llenarListaAnonymous() {
        listaHorarios!!.add(ListaHorarios("Parque Corredor"))
        listaHorarios!!.add(ListaHorarios("Plenilunio"))
        listaHorarios!!.add(ListaHorarios("Mendez Alvaro"))
        listaHorarios!!.add(ListaHorarios("Arenas de Barcelona"))
        listaHorarios!!.add(ListaHorarios("Cinemes Verdi"))
        listaHorarios!!.add(ListaHorarios("Multicines El Centro"))
    }

    /* Si hay un usuario NO ANÓNIMO, se mostrará la preferencia de cine que haya indicado en la BBDD */
    @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
    private fun mostrarCinePreferido() {
        if (!user!!.isAnonymous) {
            db.collection("usuarios").document(user.email!!).get().addOnCompleteListener { it ->
                val documentUser: DocumentSnapshot = it.result!!
                val mapa_DatosPersonales: Map<String, Any> =
                    documentUser.data!!["lista_datosPersonales"] as Map<String, Any>
                val indiceProvincia =
                    mapa_DatosPersonales["provincia"] as String
                if (indiceProvincia != "0") {
                    val provincia = array_provincias[indiceProvincia.toInt()]
                    val indiceCine = mapa_DatosPersonales["cine"] as String
                    db.collection("provincias").document(provincia).get().addOnCompleteListener { it1 ->
                        val documentProvincia: DocumentSnapshot = it1.result!!
                        val listaCines = documentProvincia.data!!["listaCines"] as List<DocumentReference>
                        val refCine = listaCines[indiceCine.toInt()]
                        refCine.get().addOnSuccessListener {
                            if (it.exists()) {
                                val nombre = it.getString("nombre") as String
                                txv_cinePref.text = nombre
                                val direccion = it.getString("direccion") as String
                                txv_dirreccionCinePref.text = direccion
                                calcularHorarios(nombre)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun calcularHorarios(nombreCine: String) {
        var apertura: String
        var cierre: String
        var descripcionPelicula: String
        var duracionPelicula: Int
        db.collection("cines").document(nombreCine).get().addOnCompleteListener { it ->
            val document: DocumentSnapshot = it.result!!
            apertura = document.data?.get("apertura") as String
            cierre = document.data?.get("cierre") as String
            db.collection("peliculasCartelera").document(pelicula).get().addOnSuccessListener {
                descripcionPelicula = it.get("descripcion") as String
                duracionPelicula = descripcionPelicula.substring(descripcionPelicula.indexOf('/') + 2,
                    descripcionPelicula.lastIndexOf(' ')).toInt()
                calcularHoras(apertura, cierre, duracionPelicula, nombreCine)
            }
        }
    }

    /* Calcula los horario posibles entre hora de apertura y hora de cierre teniendo en cuenta la
      duración de la película */
    private fun calcularHoras(apertura: String, cierre: String, duracion: Int, nombreCine: String) {
        var numeroVueltas = 0
        val arrayHorarios: ArrayList<String> = ArrayList()
        var horaApertura: Int
        var minutosApertura: Int
        var horaHorario = 0
        var minutosHorario: Int
        horaApertura = apertura.substring(0, apertura.indexOf(':')).toInt()
        minutosApertura = apertura.substring(apertura.indexOf(':') + 1, apertura.length).toInt()
        val horaCierre: Int = cierre.substring(0, cierre.indexOf(':')).toInt()
        val resultadoHora: Int = duracion.div(60)
        val resultadoMinutos: Int = duracion.rem(60)
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
            arrayHorarios.add(horaHorario.toString() + ":" + String.format("%02d", minutosHorario))
        }
        generarBotones(numeroVueltas, arrayHorarios, nombreCine)
    }

    //Generamos DINÁMICAMENTE los BOTONES necesarios, tanto como sesiones se calculen para cada película
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun generarBotones(elementos: Int, arrayHorarios: ArrayList<String>, nombreCine: String) {
        val listaBotones: ArrayList<Button> = ArrayList()
        lateinit var sesionElegida: String
        for (i in 0 until elementos) {
            val boton = Button(requireContext())
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(260, 135)
            params.rightMargin = 70
            params.topMargin = 20
            boton.layoutParams = params
            boton.text = arrayHorarios[i]
            boton.setTextColor(Color.WHITE)
            boton.gravity = View.TEXT_ALIGNMENT_GRAVITY
            boton.background = ContextCompat.getDrawable(requireContext(), R.drawable.boton_redondeado)
            listaBotones.add(boton)
            layout_horariosPref.addView(boton)
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
                    val intent = Intent(requireContext(), clase).apply {
                        putExtra("nombrePelicula", pelicula)
                        putExtra("nombreCine", nombreCine)
                        putExtra("sesionElegida", sesionElegida)
                        putExtra("diaEscogido", diaPulsado.toString())
                    }
                    startActivity(intent)
                } else {
                    mostrarAlerta()
                }
            }
        }
    }

    private fun mostrarAlerta() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setPositiveButton("Aceptar", null)
        builder.setMessage("Debes seleccionar un día de la semana")
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}