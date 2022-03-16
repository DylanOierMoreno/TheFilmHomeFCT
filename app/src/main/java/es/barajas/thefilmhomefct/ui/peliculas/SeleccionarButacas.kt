package es.barajas.thefilmhomefct.ui.peliculas

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.os.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.*
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.*
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.ui.peliculas.elementos.Sala
import java.text.*
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class SeleccionarButacas : AppCompatActivity() {
    //Botones anterior y siguiente
    private lateinit var btnAnterior: Button
    private lateinit var btnSiguiente: Button
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    private lateinit var txtTituloPelicula: TextView
    private lateinit var txtNombreCine: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtNumSala: TextView
    private lateinit var txtHoraSesion: TextView
    private lateinit var txtTipoEntradas: TextView
    private lateinit var imvPelicula: ImageView
    private lateinit var layoutContenedorButacas: LinearLayout
    private lateinit var tblTablaButacas: TableLayout
    private lateinit var peliculaElegida: String
    private lateinit var cineElegido: String
    private lateinit var sesionElegida: String
    private lateinit var diaElegido: String
    private lateinit var precioTotalEntradas: String
    private lateinit var tarjeta: String
    private lateinit var lista_tipoEntradas: ArrayList<String>
    private lateinit var lista_numeroEntradas: ArrayList<Int>
    private var numeroSala: Int = 0
    private lateinit var storageRef: StorageReference
    private var ruta = "cartelera/"
    private var listaAsientos: ArrayList<ImageButton> = ArrayList()
    private lateinit var fechaPulsada: String
    private lateinit var sala: Sala

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_butacas)
        //-----TOOLBAR----
        val toolbar: Toolbar = findViewById(R.id.toolbar_seleccionarButacas)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        txtTituloPelicula = findViewById(R.id.txtTituloPelicula)
        txtNombreCine = findViewById(R.id.txtNombreCine)
        txtHoraSesion = findViewById(R.id.txtHoraSesion)
        txtFecha = findViewById(R.id.txtFecha)
        txtNumSala = findViewById(R.id.txtNumSala)
        txtTipoEntradas = findViewById(R.id.txtTipoEntradas)
        imvPelicula = findViewById(R.id.imagen_pelicula)
        btnAnterior = findViewById(R.id.btn_anterior_seleccionarButacas)
        btnSiguiente = findViewById(R.id.btn_siguiente_seleccionarButacas)
        layoutContenedorButacas = findViewById(R.id.contenedorButacas)
        tblTablaButacas = findViewById(R.id.tablaButacas)
        recogerIntent()
        toolbar.setNavigationOnClickListener { onBackPressed() }
        consultarPelicula()
        generarBotones()
        sala = Sala(this, tblTablaButacas, fechaPulsada, sesionElegida, peliculaElegida, cineElegido, numTotalEntradas())
        sala.dibujarSala()
        btnAnterior.setOnClickListener { onBackPressed() }
        btnSiguiente.setOnClickListener {
            if (numTotalEntradas() == sala.numButacasSeleccionadas()) {
                val intent = Intent(this, PagarEntradas::class.java).apply {
                    putExtra("nombrePelicula", peliculaElegida)
                    putExtra("nombreCine", cineElegido)
                    putExtra("sesionElegida", sesionElegida)
                    putExtra("diaEscogido", diaElegido)
                    putExtra("fecha", fechaPulsada)
                    putExtra("numeroSala", numeroSala.toString())
                    putExtra("precioTotal", precioTotalEntradas)
                    putExtra("tarjeta", tarjeta)
                    putStringArrayListExtra("tipoEntradas", lista_tipoEntradas)
                    putIntegerArrayListExtra("numeroEntradas", lista_numeroEntradas)
                    putExtra("numTotalEntradas", numTotalEntradas())
                }
                startActivity(intent)
            } else {
                if (sala.numButacasSeleccionadas() == 0) {
                    mostrarAlerta("no hay seleccion")
                } else {
                    mostrarAlerta("seleccion incompleta")
                }
            }
        }
    }

    private fun recogerIntent() {
        peliculaElegida = intent.getStringExtra("nombrePelicula")!!
        cineElegido = intent.getStringExtra("nombreCine")!!
        sesionElegida = intent.getStringExtra("sesionElegida")!!
        diaElegido = intent.getStringExtra("diaEscogido")!!
        precioTotalEntradas = intent.getStringExtra("precioTotal")!!
        tarjeta = intent.getStringExtra("tarjeta")!!
        lista_tipoEntradas = intent.getStringArrayListExtra("tipoEntradas") as ArrayList<String>
        lista_numeroEntradas = intent.getIntegerArrayListExtra("numeroEntradas") as ArrayList<Int>
    }

    private fun consultarPelicula() {
        txtTituloPelicula.text = peliculaElegida
        txtNombreCine.text = cineElegido
        txtHoraSesion.text = sesionElegida
        txtFecha.text = darFormatoFecha()
        txtNumSala = findViewById(R.id.txtNumSala)
        txtTipoEntradas.text = leerTipoEntradas()
        descargarFoto()
        db.collection("peliculasCartelera").document(peliculaElegida).get().addOnSuccessListener {
            numeroSala = (it.get("numSala") as Long).toInt()
            txtNumSala.text = numeroSala.toString()
        }.addOnFailureListener {
            System.out.println("========ERROR!!!!!======== " + it)
        }
    }

    //Descargamos fotos desde el STORAGE FIREBASE
    private fun descargarFoto() {
        var bitmap: Bitmap
        storageRef = FirebaseStorage.getInstance().reference.child(buscarFotoPelicula())
        storageRef.getBytes(1024 * 1024).addOnSuccessListener {
            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            imvPelicula.setImageBitmap(bitmap)
        }
    }

    //Busca el nombre que tiene la foto de cada película en Firestore
    private fun buscarFotoPelicula(): String {
        when(peliculaElegida) {
            "El informe Auschwitz" -> ruta += "informe_auschwitz.jpg"
            "Gozilla vs. Kong" -> ruta += "godzilla_vs_kong.jpg"
            "Inmune" -> ruta += "inmune.jpg"
            "Los Croods 2. Una nueva era" -> ruta += "los_croods.jpg"
            "Tom y Jerry" -> ruta += "tom_y_jerry.jpg"
            "Wonder Woman 1984" -> ruta += "wonderwoman.jpg"
        }
        return ruta
    }

    /*  Leer el ArrayList que contine el tipo de entrada/s que se han escogido en ElegirEntradas.kt
        y devolver la cadena de caracteres correspondiente */
    private fun leerTipoEntradas(): String {
        var tipoEntradas: String = lista_numeroEntradas[0].toString() + " x " + lista_tipoEntradas[0]
        if(lista_tipoEntradas.size > 1) tipoEntradas += "..."
        return tipoEntradas
    }

    /* Calcula la fecha que debe aparecer utilizando el índice (Int) del grid donde se elige el
    día y que tiene el tamaño de los días de la semana. Este índice indica qué dia se ha elegido.
    Además, se da formato a la fecha  */
    @SuppressLint("SimpleDateFormat")
    private fun darFormatoFecha(): String {
        val fecha = Date() //día actual
        val calendario = Calendar.getInstance()
        calendario.time = fecha
        //Calculamos una fecha desde el día de hoy (mínimo) hasta el día pulsado en el horario (un índice del grid)
        calendario.add(Calendar.DAY_OF_YEAR, Integer.parseInt(diaElegido))
        val fechaFormateada = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy",
            Locale("es", "ES")).format(calendario.time)
        fechaPulsada = String.format("%02d", calendario.get(Calendar.DAY_OF_MONTH)) +
                //Necesario sumar 1 a los meses porque empiezan desde 0
                String.format("%02d", (calendario.get(Calendar.MONTH) + 1)) +
                calendario.get(Calendar.YEAR)
        return fechaFormateada
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun generarBotones() {
        for (i in 0 until 2) {
            val asiento = ImageButton(this)
            //ancho y alto de los botones, dinámicamente no se puede poner directamente al botón
            val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(98, 96)
            params.leftMargin = 20
            params.rightMargin = 20
            asiento.layoutParams = params
            asiento.setImageResource(R.drawable.asiento_libre)
            layoutContenedorButacas.addView(asiento)
            listaAsientos.add(asiento)
        }
    }

    private fun numTotalEntradas(): Int {
        var numEntradas = 0
        for (i in 0 until lista_numeroEntradas.size) numEntradas += lista_numeroEntradas[i]
        return numEntradas
    }

    //calcula la diferencia entre las entradas que se quieren comprar y las seleccionadas en la pantalla
    private fun entradasRestantes(): Int = numTotalEntradas() - sala.numButacasSeleccionadas()

    //Muestra una alerta con el error que llegue por parámetro
    private fun mostrarAlerta(error: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setPositiveButton("Aceptar",null)
        when (error) {
            "no hay seleccion" -> builder.setMessage("No has seleccionado ninguna butaca")
            "seleccion incompleta" -> builder.setMessage("Tienes que seleccionar " + entradasRestantes() + " butaca/s más")
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}