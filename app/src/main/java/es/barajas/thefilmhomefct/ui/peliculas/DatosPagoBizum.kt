package es.barajas.thefilmhomefct.ui.peliculas

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.*
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import es.barajas.thefilmhomefct.MainActivity
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.data.ButacaFirebase
import es.barajas.thefilmhomefct.data.UsuarioFirebase
import es.barajas.thefilmhomefct.ui.generadores.GeneradorPDFs
import es.barajas.thefilmhomefct.ui.iniciar_sesion.TarjetaTFH
import java.util.regex.Pattern
import javax.microedition.khronos.egl.EGLDisplay

class DatosPagoBizum : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    //Variable que recoge el precio total de las entradas por intent y otra información
    private lateinit var precioTotal: String
    private lateinit var fechaCompra: String
    private lateinit var fechaSesion: String
    private lateinit var fechaFormateada: String
    private lateinit var horaSesion: String
    private lateinit var cineElegido: String
    private lateinit var peliculaElegida: String
    private lateinit var numSala: String
    private lateinit var tarjeta: String
    private lateinit var tarjetaTFH: TarjetaTFH
    //Variable que guardará el nombre de un documento Sala (Sala de BBDD donde estamos comprando)
    private lateinit var nombreDocumento: String
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    //Instancia para los USUARIOS de Firebase
    private val user = Firebase.auth.currentUser
    //Lista auxiliar de tipo ButacaFirebase, para guardar las butacas que se están comprando
    private var  listaAuxButacas: ArrayList<ButacaFirebase> = ArrayList()
    //lista numero de entrada de cada tipo
    private var lista_numEntradas: ArrayList<Int> = ArrayList()
    //lista tipo de entrada de cada tipo
    private var lista_tipoEntradas: ArrayList<String> = ArrayList()
    //lista que guardará numero+tipo de entradas
    private var lista_numTipoEntradas: ArrayList<String> = ArrayList()
    private lateinit var btnPagarBizum: Button
    private lateinit var et_numTelf: EditText
    private lateinit var tvImporte: TextView
    private lateinit var tvFechaCompra: TextView
    private var numTotalEntradas: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_pago_bizum)
        inicializarVariables()
        recogerIntent()
        //-----TOOLBAR----
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        btnPagarBizum.setOnClickListener {
            if (comprobarTelf()) {
                //PERMISOS
                ActivityCompat.requestPermissions(this, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE),
                    PackageManager.PERMISSION_GRANTED)
            }
        }
    }

    private fun inicializarVariables() {
        toolbar = findViewById(R.id.toolbar_pagoBizum)
        btnPagarBizum = findViewById(R.id.btnPagarBizum)
        et_numTelf = findViewById(R.id.et_numTelf)
        tvImporte = findViewById(R.id.tvImporteBizum)
        tvFechaCompra = findViewById(R.id.tvFechaCompraBizum)
    }

    private fun comprobarTelf(): Boolean {
        val patronTelf = "^[67][0-9]{8}$"
        val pat = Pattern.compile(patronTelf)
        val mat = pat.matcher(et_numTelf.text.toString())
        if (!mat.matches()) {
            if (et_numTelf.text.toString().length < 9) {
                mostrarAlerta("longitud telf")
            } else {
                mostrarAlerta("telf patron")
            }
            return false
        }
        return true
    }

    //Crear la NOTIFICACIÓN
    private fun crearNotificacion() {
        val notificacion: NotificationCompat.Builder = NotificationCompat.Builder(this,"4321")
            .setSmallIcon(R.drawable.ic_file_download)
            .setWhen(System.currentTimeMillis())
            .setContentTitle("Descarga entradas")
            .setChannelId("4321")
            .setAutoCancel(true)
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(56320, notificacion.build())
    }

    //Crear CANAL de NOTIFICACIONES
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel("4321",
                "CHANNEL_NEWS_BIZUM", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = "Descripción"
            notificationChannel.setShowBadge(true)
            val notificationManager: NotificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    //Recoge en distintas variables la información que llega en el INTENT y establece el texto en los controles
    private fun recogerIntent() {
        precioTotal = intent.getStringExtra("importe")!!
        fechaCompra = intent.getStringExtra("fechaCompra")!!
        fechaSesion = intent.getStringExtra("fechaSesion")!!
        fechaFormateada = intent.getStringExtra("fechaFormateada")!!
        horaSesion = intent.getStringExtra("hora")!!
        cineElegido = intent.getStringExtra("cine")!!
        peliculaElegida = intent.getStringExtra("pelicula")!!
        numSala = intent.getStringExtra("numSala")!!
        lista_numEntradas = intent.getIntegerArrayListExtra("listaNumeroEntradas")!!
        lista_tipoEntradas = intent.getStringArrayListExtra("listaTipoEntradas")!!
        numTotalEntradas = intent.getIntExtra("numTotalEntradas", 0)
        tarjeta = intent.getStringExtra("tarjeta")!!
        leerNumeroTipoEntradas(lista_numEntradas, lista_tipoEntradas, lista_numTipoEntradas)
        tvImporte.text = precioTotal
        tvFechaCompra.text = fechaCompra
    }

    //Método que lee el número y el tipo de cada entrada que llega por parámetro en dos listas distintas y lo junta en una única lista
    private fun leerNumeroTipoEntradas(listaNumeroEntradas: ArrayList<Int>, listaTipoEntradas: ArrayList<String>,
                                       lista_tipoNumeroEntradas: ArrayList<String>){
        var numeroAuxiliar: String
        var tipoAuxiliar: String
        for (i in 0 until listaNumeroEntradas.size){
            numeroAuxiliar = listaNumeroEntradas[i].toString()
            tipoAuxiliar = listaTipoEntradas[i]
            lista_tipoNumeroEntradas.add(numeroAuxiliar+tipoAuxiliar)
        }
    }

    //Recorre la lista de butacas y pasa las seleccionadas (1) a compradas (4) en la BBDD
    @Suppress("UNCHECKED_CAST")
    private fun leerButacasBBDD() {
        nombreDocumento = fechaSesion + horaSesion + eliminarMinusculas(peliculaElegida) + abreviaturaCines(cineElegido)
        var listaEntera: MutableList<Any?>
        db.collection("sala").document(nombreDocumento).get().addOnCompleteListener {
            val document: DocumentSnapshot = it.result!!
            if (it.isSuccessful && document.exists()) {
                listaEntera = document.data?.get("listaButacas") as MutableList<Any?>
                for (i in listaEntera.indices) {
                    val elementoMapa: MutableMap<String, Any?> = listaEntera[i] as MutableMap<String, Any?>
                    val tipoAsiento = elementoMapa["tipo"] as Long
                    if (tipoAsiento == 1L) modificarButacasCompradas(i, listaEntera, elementoMapa)
                }
            }
        }
    }

    //Modifica el tipo de la butaca de ocupada(1) a comprada(4)
    @Suppress("UNCHECKED_CAST")
    private fun modificarButacasCompradas(indiceLista: Int, listaEntera: MutableList<Any?>, elementoMapa: Map<String, Any?> ) {
        db.collection("sala").document(nombreDocumento).get().addOnCompleteListener {
            listaEntera[indiceLista] = mapOf("columna" to elementoMapa["columna"], "fila" to
                    elementoMapa["fila"], "tipo" to 4L) as HashMap<String, Any>
            val mapaActualiado = mapOf("fecha" to fechaSesion, "horaSesion" to horaSesion,
                "listaButacas" to listaEntera, "nombreCine" to cineElegido,
                "referencia" to nombreDocumento, "tituloPelicula" to peliculaElegida)
            db.collection("sala").document(nombreDocumento).update(mapaActualiado)
            val fila = elementoMapa["fila"] as String
            val columna = elementoMapa["columna"] as Long
            val tipoAsiento = 4L
            rellenarListaButacasCompradas(fila, columna.toInt(), tipoAsiento)
            guardarEntradaBBDD()
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

    @Suppress("SameParameterValue")
    //Guarda en una lista las ButacasFirebase que se están comprando. Esta lista después se guardará en EntradaFirebase
    private fun rellenarListaButacasCompradas(fila: String, columna: Int, tipo: Long){
        val butacaFirebase = ButacaFirebase(fila, columna, tipo)
        listaAuxButacas.add(butacaFirebase)
    }

    //Guarda toda la información de la clase EntradaFirebases para el usuario que está comprando ahora
    @Suppress("UNCHECKED_CAST")
    private fun guardarEntradaBBDD(){
        if (!user!!.isAnonymous) {
            db.collection("usuarios").document(user.email!!).get().addOnCompleteListener  {
                val document: DocumentSnapshot = it.result!!
                val mapa_datosPersonalesBBDD: Map <String, Any> = document.data!!["lista_datosPersonales"] as Map <String,Any>
                val lista_entradasBBDD: ArrayList<HashMap<String, Any>> = document.data!!["lista_entradas"]
                        as ArrayList<HashMap<String, Any>>
                val tarjetaTFH: Map<String, Any>? = document.data!!["tarjetaTFH"] as Map<String, Any>?
                lista_entradasBBDD.add(hashMapOf("fechaSesion" to fechaSesion,
                    "horaSesion" to horaSesion, "nombreCine" to cineElegido, "nombrePelicula" to peliculaElegida,
                    "numEntrada_tiposEntrada" to lista_numTipoEntradas, "numSala" to numSala,
                    "precioTotal" to precioTotal))
                val usuarioFirebase = UsuarioFirebase(mapa_datosPersonalesBBDD, lista_entradasBBDD, tarjetaTFH)
                guardarEntrada(usuarioFirebase)
            }
        }
    }

    private fun guardarEntrada(usuarioFirebase: UsuarioFirebase){
        db.collection("usuarios").document(user?.email!!).set(usuarioFirebase)
    }

    //Muestra una alerta con el error que llegue por parámetro
    private fun mostrarAlerta(alerta: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setPositiveButton("Aceptar", null)
        when (alerta) {
            "longitud telf" -> builder.setMessage("El número de teléfono tiene que tener 9 dígitos")
            "telf patron" -> builder.setMessage("El número de teléfono tiene que empezar por 6 o 7")
            "pdf" -> builder.setMessage("Error al generar el PDF")
            "permisos" -> {
                builder.setMessage("Debes ACEPTAR los permisos para poder DESCARGAR tus ENTRADAS TFH")
                builder.setPositiveButton("Aceptar") { _, _ ->
                    Intent(this, MainActivity::class.java)
                    //PERMISOS
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE),
                        PackageManager.PERMISSION_GRANTED)
                }
            }
            "compra" -> {
                builder.setTitle("Información")
                builder.setMessage("Tu compra ha finalizado con éxito. \nConsulta tu entrada en el " +
                        "apartardo de Descargas de tu dispositivo")
                builder.setPositiveButton("Aceptar") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
            "canjear puntos" -> {
                builder.setTitle("Información")
                builder.setMessage(
                    "Tienes " + tarjetaTFH.getPuntos() + " puntos acumulados. " +
                            "\n¿Quieres canjearlos por " + calcularNumEntradasCanjeables() + " entrada/s?")
                builder.setPositiveButton("Aceptar") { _, _ ->
                    restarPrecioEntradas()
                    restarPuntos()
                    sumarPuntos()
                }.setNegativeButton("Cancelar") { _, _ ->
                    sumarPuntos()
                }
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Leemos los puntos de BBDD que tiene esta tarjeta
    @Suppress("UNCHECKED_CAST")
    private fun leerPuntos() {
        if (!user!!.isAnonymous) {
            db.collection("usuarios").document(user.email!!).get().addOnCompleteListener {
                val document: DocumentSnapshot = it.result!!
                val mapa_tarjetaTFH: Map<String, Any> = document.data!!["tarjetaTFH"] as Map<String, Any>
                if (tarjeta.isNotEmpty()) {
                    tarjetaTFH = TarjetaTFH(applicationContext, mapa_tarjetaTFH["numTarjeta"] as
                            String, mapa_tarjetaTFH["puntos"].toString().toInt())
                    val numPuntos = mapa_tarjetaTFH["puntos"].toString().toInt()
                    if (numPuntos >= 100) {
                        mostrarAlerta("canjear puntos")
                    } else {
                        sumarPuntos()
                    }
                }
            }
        }
    }

    //Cambia el valor de tvImporte si el usuario puede canjear entradas, le será más barato
    @SuppressLint("SetTextI18n")
    @Suppress("EqualsBetweenInconvertibleTypes")
    private fun restarPrecioEntradas() {
        val precioEntradas = precioTotal.substring(0, precioTotal.length - 1).toDouble()
        var ahorro = 0.0
        for ((contador, i) in lista_tipoEntradas.indices.withIndex()) {
            if (contador < calcularNumEntradasCanjeables()) {
                ahorro += if (!i.equals("Adulto")) {
                    5.40
                } else {
                    5.90
                }
            }
        }
        tvImporte.text = String.format("%.2f", precioEntradas - ahorro) + '€'
    }

    /*Calcula el número de entradas que un usuario podría canjear según los puntos que tenga y según
    el número de ENTRADAS que esté comprando en este momento*/
    private fun calcularNumEntradasCanjeables(): Int {
        return when {
            tarjetaTFH.getPuntos() >= 300 -> {
                when {
                    numTotalEntradas >= 3 ->  3
                    numTotalEntradas == 2 -> 2
                    numTotalEntradas == 1 -> 1
                    else -> 0
                }
            }
            tarjetaTFH.getPuntos() >= 200 -> {
                when (numTotalEntradas) {
                    2 -> 2
                    1 -> 1
                    else -> 0
                }
            }
            tarjetaTFH.getPuntos() >= 100 -> 1
            else -> 0
        }
    }

    /*Sumamos los puntos de la nueva compra a los puntos que el usuario ya tiene guardados en la
    BBDD por haber hecho compras anteriores */
    private fun sumarPuntos(): Int {
        val puntosAcumulados: Int
        if (tarjetaTFH.getPuntos() + (numTotalEntradas * 2) > 350 ){
            puntosAcumulados = 350
        } else {
            puntosAcumulados = tarjetaTFH.getPuntos() + numTotalEntradas * 2
        }
        tarjetaTFH.setPuntos(puntosAcumulados)
        db.collection("usuarios").document(user!!.email!!).update(
            "tarjetaTFH",
            mapOf("numTarjeta" to tarjetaTFH.getNumTarjeta(), "puntos" to puntosAcumulados))
        return puntosAcumulados
    }

    //Resta los puntos correspondientes según las entradas que el usuario pueda canjear
    private fun restarPuntos(): Int {
        val puntosRestantes: Int
        when (calcularNumEntradasCanjeables()) {
            3 -> {
                puntosRestantes = tarjetaTFH.getPuntos() - 300
                tarjetaTFH.setPuntos(puntosRestantes)
                return puntosRestantes
            }
            2 -> {
                puntosRestantes = tarjetaTFH.getPuntos() - 200
                tarjetaTFH.setPuntos(puntosRestantes)
                return puntosRestantes
            }
            1 -> {
                puntosRestantes = tarjetaTFH.getPuntos() - 100
                tarjetaTFH.setPuntos(puntosRestantes)
                return puntosRestantes
            }
            else -> return 0
        }
    }

    //EVENTO para saber si el usuario acepta o no la petición de PERMISOS de STORAGE
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            mostrarAlerta("permisos")
        } else {
            leerPuntos()
            leerButacasBBDD()
            val pdf = GeneradorPDFs(this, peliculaElegida)
            try{
                pdf.crearPDF(cineElegido, horaSesion, fechaFormateada,
                    lista_tipoEntradas, lista_numEntradas, numSala, listaAuxButacas)
                createNotificationChannel()
                crearNotificacion()
                mostrarAlerta("compra")
            }catch (e: Error) {
                mostrarAlerta("pdf")
            }
        }
    }
}