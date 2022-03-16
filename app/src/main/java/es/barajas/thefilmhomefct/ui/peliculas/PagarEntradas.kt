package es.barajas.thefilmhomefct.ui.peliculas

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.*
import android.os.Bundle
import android.renderscript.*
import android.widget.*
import androidx.appcompat.app.*
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.*
import com.google.firebase.storage.*
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.adapter.RV_AdaptadorTipoEntradas
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class PagarEntradas : AppCompatActivity() {
    //BOTONES
    private lateinit var btnAnterior: Button
    private lateinit var btnSiguiente: Button
    private lateinit var cvPagarTarjeta: CardView
    private lateinit var cvPagarBizum: CardView
    private lateinit var txtCondicionesContratacion: TextView
    //Variables para recoger información del INTENT
    private lateinit var peliculaElegida: String
    private lateinit var cineElegido: String
    private lateinit var sesionElegida: String
    private lateinit var diaElegido: String
    private lateinit var numeroSala: String
    private lateinit var precioTotalEntradas: String
    private lateinit var tarjeta: String
    private lateinit var fecha: String
    private lateinit var lista_tipoEntradas: ArrayList<String>
    private lateinit var lista_numeroEntradas: ArrayList<Int>
    private var numTotalEntradas: Int = 0
    private lateinit var chkAceptarCondiciones: CheckBox
    private lateinit var layoutPagarEntradas: LinearLayout
    //variable para descargar fotos de STORAGE FIREBASE
    private lateinit var storageRef: StorageReference
    private var ruta = "cartelera/"
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView //---------------RECYCLERVIEW
    //Adaptador para el RecyvlerView
    private lateinit var adapter: RV_AdaptadorTipoEntradas
    //Componentes de la PANTALLA
    private lateinit var txtTituloPelicula: TextView
    private lateinit var txtCine: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtSala: TextView
    private lateinit var txtHora: TextView
    private lateinit var txtPrecioTotal: TextView
    private lateinit var txtTextoTarjeta: TextView
    private lateinit var txtTextoBizum: TextView
    //Variable para controlar el clic en el tipo de tarjeta de pago
    private var tarjetaPulsada = false
    private var bizumPulsado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagar_entradas)
        inicializarVariables()
        //-----TOOLBAR----
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        recogerIntent() //Recoger INTENT
        rellenarInformacion()
        descargarFoto() //Descargar FOTO para el fondo
        //RECYCLERVIEW
        recyclerView.setHasFixedSize(true)
        construirRecycler(recyclerView)
        //CARDVIEW
        ponerALaEscuchaCardView()
        btnAnterior.setOnClickListener { onBackPressed() }
        btnSiguiente.setOnClickListener {
            if (!chkAceptarCondiciones.isChecked) {
                mostrarAlerta("contratacion")
            } else if (!tarjetaPulsada && !bizumPulsado) {
                mostrarAlerta("tarjeta")
            } else {
                enviarIntent()
            }
        }
        txtCondicionesContratacion.setOnClickListener {
            mostrarAlerta("condiciones")
        }
    }

    private fun inicializarVariables() {
        toolbar = findViewById(R.id.toolbar_pagarEntradas)
        btnAnterior = findViewById(R.id.btn_anterior_pagarEntradas)
        btnSiguiente = findViewById(R.id.btn_siguiente_pagarEntradas)
        chkAceptarCondiciones = findViewById(R.id.chkAceptarCondiciones)
        layoutPagarEntradas = findViewById(R.id.layoutPagarEntradas)
        recyclerView = findViewById(R.id.recyclerview_tipoEntrada)
        txtCondicionesContratacion = findViewById(R.id.txtCondicionesContratacion)
        txtTituloPelicula = findViewById(R.id.txtTituloPelicula)
        txtCine = findViewById(R.id.txtNombreCine)
        txtFecha = findViewById(R.id.txtFecha)
        txtSala = findViewById(R.id.txtNumSala)
        txtHora = findViewById(R.id.txtHora)
        txtPrecioTotal = findViewById(R.id.txtPrecioTotalEntradas)
        cvPagarTarjeta = findViewById(R.id.cvPagarTarjeta)
        cvPagarBizum = findViewById(R.id.cvPagarBizum)
        txtTextoTarjeta = findViewById(R.id.txtTextoTarjeta)
        txtTextoBizum = findViewById(R.id.txtPagoMasterPass)
    }

    //Recoge en distintas variables la información que llega en el INTENT y establece el texto en los controles
    private fun recogerIntent() {
        peliculaElegida = intent.getStringExtra("nombrePelicula")!!
        cineElegido = intent.getStringExtra("nombreCine")!!
        sesionElegida = intent.getStringExtra("sesionElegida")!!
        diaElegido = intent.getStringExtra("diaEscogido")!!
        fecha = intent.getStringExtra("fecha")!!
        numeroSala = intent.getStringExtra("numeroSala")!!
        precioTotalEntradas = intent.getStringExtra("precioTotal")!!
        tarjeta = intent.getStringExtra("tarjeta")!!
        lista_tipoEntradas = intent.getStringArrayListExtra("tipoEntradas") as ArrayList<String>
        lista_numeroEntradas = intent.getIntegerArrayListExtra("numeroEntradas") as ArrayList<Int>
        numTotalEntradas = intent.getIntExtra("numTotalEntradas", 0)
    }

    private fun enviarIntent() {
        var clase: Class<*>? = null
        when (true) {
            tarjetaPulsada -> clase = DatosPagoTarjeta::class.java
            bizumPulsado -> clase = DatosPagoBizum::class.java
        }
        val intent = Intent (this, clase).apply {
            putExtra("importe", precioTotalEntradas)
            putExtra("fechaCompra", fechaCompra())
            putExtra("fechaSesion", fecha)
            putExtra("fechaFormateada", txtFecha.text)
            putExtra("hora", sesionElegida)
            putExtra("pelicula", peliculaElegida)
            putExtra("cine", cineElegido)
            putExtra("numSala", numeroSala)
            putExtra("listaTipoEntradas", lista_tipoEntradas)
            putExtra("listaNumeroEntradas", lista_numeroEntradas)
            putExtra("numTotalEntradas",numTotalEntradas)
            putExtra("tarjeta", tarjeta)
        }
        startActivity(intent)
    }

    //Muestra una alerta con el error que llegue por parámetro
    private fun mostrarAlerta(alerta: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setPositiveButton("Aceptar",null)
        when (alerta) {
            "contratacion" -> builder.setMessage("Debes aceptar las condiciones de contratación")
            "tarjeta" -> builder.setMessage("Debes seleccionar al menos un tipo de tarjeta de pago")
            "condiciones" -> {
                builder.setTitle("Términos y condiciones")
                builder.setPositiveButton("Cerrar",null)
                builder.setMessage("Por favor, revisa la película, fecha, sesión y que la información " +
                        "que arriba se muestra es la que has seleccionado para tus entradas de cine " +
                        "ya que no se podrá hacer cambios ni devoluciones una vez finalizada la compra." +
                        "Recuerda que debes de presentar tu tarjeta de crédito e identificación oficial al " +
                        "recoger tus entradas en la taquilla de tu cine elegido. Por favor, introduce " +
                        "la información correspondiente para llevar a cabo el pago de las entradas. Verifique " +
                        "dos veces que esta información es la correcta y haga clic en el botón SIGUIENTE que " +
                        "está abajo para procesar su transacción. El límite para recoger las entradas es " +
                        "el final de la sesión escogida.")
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun rellenarInformacion() {
        txtTituloPelicula.text = peliculaElegida
        txtCine.text = cineElegido
        txtSala.text = "Sala $numeroSala"
        txtHora.text = sesionElegida
        txtFecha.text = darFormatoFecha()
        txtPrecioTotal.text = "Total: $precioTotalEntradas"
    }

    @SuppressLint("SimpleDateFormat")
    private fun darFormatoFecha(): String {
        val fecha = Date() //día actual
        val calendario = Calendar.getInstance()
        calendario.time = fecha
        //Calculamos una fecha desde el día de hoy (mínimo) hasta el día pulsado en el horario (un índice del grid)
        calendario.add(Calendar.DAY_OF_YEAR, Integer.parseInt(diaElegido))
        val fechaFormateada = SimpleDateFormat("EE dd'/'MM'/'yyyy",
            Locale("es", "ES")).format(calendario.time)
        return fechaFormateada
    }

    //Calcular la FECHA y la HORA a la que se compra
    @SuppressLint("SimpleDateFormat")
    private fun fechaCompra(): String {
        val fecha = Date() //día actual
        val calendario = Calendar.getInstance()
        calendario.time = fecha
        val fechaFormateada = SimpleDateFormat("dd'/'MM'/'yyyy HH:mm",
            Locale("es", "ES")).format(calendario.time)
        return fechaFormateada
    }

    private fun ponerALaEscuchaCardView(){
        cvPagarTarjeta.setOnClickListener {
            if (txtTextoTarjeta.currentTextColor == Color.BLUE) {
                txtTextoTarjeta.setTextColor(Color.BLACK)
                tarjetaPulsada = false
            } else {
                txtTextoTarjeta.setTextColor(Color.BLUE)
                txtTextoBizum.setTextColor(Color.BLACK)
                tarjetaPulsada = true
                bizumPulsado = false
            }
        }
        cvPagarBizum.setOnClickListener {
            if (txtTextoBizum.currentTextColor == Color.BLUE) {
                txtTextoBizum.setTextColor(Color.BLACK)
                bizumPulsado = false
            } else {
                txtTextoBizum.setTextColor(Color.BLUE)
                txtTextoTarjeta.setTextColor(Color.BLACK)
                bizumPulsado = true
                tarjetaPulsada = false
            }
        }
    }

    //RecyclerView que carga cada cine con sus sesiones
    private fun construirRecycler(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = RV_AdaptadorTipoEntradas(lista_numeroEntradas, lista_tipoEntradas)
        recyclerView.adapter = adapter
    }

    //Descargamos fotos desde el STORAGE FIREBASE
    private fun descargarFoto() {
        var bitmap: Bitmap
        storageRef = FirebaseStorage.getInstance().reference.child(buscarFotoPelicula())
        storageRef.getBytes(1024 * 1024).addOnSuccessListener {
            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            desenfocarFondo(bitmap)
        }
    }

    //Método para poner fondo desenfocado al layout
    @Suppress("DEPRECATION")
    private fun desenfocarFondo(bitmap: Bitmap){
        val blurred: Bitmap = blurRenderScript(bitmap) //second parametre is radius
        val fondo: Drawable = BitmapDrawable(resources, blurred)
        fondo.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY) //Oscurecer foto fondo
        layoutPagarEntradas.background = fondo
    }

    //Método para desenfocar la imagen que se pasa en BitMap
    @Suppress("NAME_SHADOWING")
    @SuppressLint("NewApi")
    private fun blurRenderScript(smallBitmap: Bitmap): Bitmap {
        var smallBitmap = smallBitmap
        try {
            smallBitmap = RGB565toARGB888(smallBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val bitmap = Bitmap.createBitmap(smallBitmap.width, smallBitmap.height, Bitmap.Config.ARGB_8888)
        val renderScript: RenderScript = RenderScript.create(this)
        val blurInput: Allocation = Allocation.createFromBitmap(renderScript, smallBitmap)
        val blurOutput: Allocation = Allocation.createFromBitmap(renderScript, bitmap)
        val blur: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        blur.setInput(blurInput)
        blur.setRadius(25F)
        blur.forEach(blurOutput)
        blurOutput.copyTo(bitmap)
        renderScript.destroy()
        return bitmap
    }

    //Pasa el bitmap de RBG a ARGB
    @Throws(Exception::class)
    private fun RGB565toARGB888(img: Bitmap): Bitmap {
        val numPixels = img.width * img.height
        val pixels = IntArray(numPixels)
        img.getPixels(pixels, 0, img.width, 0, 0, img.width, img.height)
        val result = Bitmap.createBitmap(img.width, img.height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    //Busca el nombre que tiene la foto de cada película en Firestore
    private fun buscarFotoPelicula(): String {
        when (peliculaElegida) {
            "El informe Auschwitz" -> ruta += "informe_auschwitz.jpg"
            "Gozilla vs. Kong" -> ruta += "godzilla_vs_kong.jpg"
            "Inmune" -> ruta += "inmune.jpg"
            "Los Croods 2. Una nueva era" -> ruta += "los_croods.jpg"
            "Tom y Jerry" -> ruta += "tom_y_jerry.jpg"
            "Wonder Woman 1984" -> ruta += "wonderwoman.jpg"
        }
        return ruta
    }
}