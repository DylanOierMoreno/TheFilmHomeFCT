package es.barajas.thefilmhomefct.ui.peliculas

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.*
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import es.barajas.thefilmhomefct.R
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class ElegirEntradas : AppCompatActivity() {
    private lateinit var btn_siguiente: Button
    private lateinit var btnAdd_adulto: ImageButton
    private lateinit var btnAdd_joven: ImageButton
    private lateinit var btnAdd_nino: ImageButton
    private lateinit var btnAdd_senior: ImageButton
    private lateinit var btnRemove_adulto: ImageButton
    private lateinit var btnRemove_joven: ImageButton
    private lateinit var btnRemove_nino: ImageButton
    private lateinit var btnRemove_senior: ImageButton
    private lateinit var etCantidad_Adulto: EditText
    private lateinit var etCantidad_Joven: EditText
    private lateinit var etCantidad_Nino: EditText
    private lateinit var etCantidad_Senior: EditText
    private lateinit var et_numTarjeta: EditText
    private lateinit var tvPrecioEntradas: TextView
    private lateinit var txt_precioAdulto: TextView
    private lateinit var txt_precioJoven: TextView
    private lateinit var txt_precioNino: TextView
    private lateinit var txt_precioSenior: TextView
    private lateinit var info_canjear: ImageButton
    //ARRAYLIST para enviar por INTENT los tipos y número de cada entrada seleccionada
    private lateinit var lista_tipoEntradasSeleccionadas: ArrayList<String>
    private lateinit var lista_numeroEntradasSeleccionadas: ArrayList<Int>
    private lateinit var nombrePelicula: String
    private lateinit var nombreCine: String
    private lateinit var sesionElegida: String
    private lateinit var diaEscogido: String
    //Instancia de la BASE DE DATOS de FIREBASE
    private val db = FirebaseFirestore.getInstance()
    //USUARIO DE FIREBASE
    private val user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elegir_entradas)
        //-----TOOLBAR----
        val toolbar: Toolbar = findViewById(R.id.toolbar_elegirEntradas)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        recogerIntent()
        inicializarBotones()
        inicializarEditText()
        inicializarTextView()
        toolbar.setNavigationOnClickListener { onBackPressed() }
        deshabilitarBotones()
        //--------------------------------BOTONES---------------------------------------
        //Poner a la ESCUCHA botones de AUMENTAR y RESTAR las ENTRADAS
        onClickListener(btnAdd_adulto, btnRemove_adulto, etCantidad_Adulto)
        onClickListener(btnAdd_joven, btnRemove_joven, etCantidad_Joven)
        onClickListener(btnAdd_nino, btnRemove_nino, etCantidad_Nino)
        onClickListener(btnAdd_senior, btnRemove_senior, etCantidad_Senior)
        btn_siguiente.setOnClickListener {
            if (leerNumeroEntradas()) {
                //comprobar qué tipo de entrada/s y cuánta/s de cada se han seleccionado
                buscarTipoEntradasSeleccionadas()
                comprobarNumTarjeta()
            } else {
                mostrarAlerta("numero entradas")
            }
        }
        info_canjear.setOnClickListener {
            mostrarAlerta("canjear")
        }
    }

    private fun inicializarBotones() {
        btnAdd_adulto = findViewById(R.id.btnAdd_adulto)
        btnAdd_joven = findViewById(R.id.btnAdd_joven)
        btnAdd_adulto = findViewById(R.id.btnAdd_adulto)
        btnAdd_nino = findViewById(R.id.btnAdd_nino)
        btnAdd_senior = findViewById(R.id.btnAdd_senior)
        btnRemove_adulto = findViewById(R.id.btnRemove_adulto)
        btnRemove_joven = findViewById(R.id.btnRemove_joven)
        btnRemove_nino = findViewById(R.id.btnRemove_nino)
        btnRemove_senior = findViewById(R.id.btnRemove_senior)
        btn_siguiente = findViewById(R.id.btn_siguiente_elegirEntradas)
        info_canjear = findViewById(R.id.info_canjear)
    }

    private fun inicializarEditText() {
        etCantidad_Adulto = findViewById(R.id.et_entradaAdulto)
        etCantidad_Joven = findViewById(R.id.et_entradaJoven)
        etCantidad_Nino = findViewById(R.id.et_entradaNino)
        etCantidad_Senior = findViewById(R.id.et_entradaSenior)
        et_numTarjeta = findViewById(R.id.et_numTarjeta)
        if(user!!.isAnonymous) et_numTarjeta.isEnabled = false

    }

    private fun inicializarTextView() {
        tvPrecioEntradas = findViewById(R.id.tv_precioEntradas)
        txt_precioAdulto = findViewById(R.id.txt_precioAdulto)
        txt_precioJoven = findViewById(R.id.txt_precioJoven)
        txt_precioNino = findViewById(R.id.txt_precioNino)
        txt_precioSenior = findViewById(R.id.txt_precioSenior)
    }

    private fun onClickListener(btnAdd: ImageButton, btnRemove: ImageButton, etCantidad: EditText) {
        btnAdd.setOnClickListener {
            sumarEntradas(etCantidad, btnRemove)
            calcularPrecio()
        }
        btnRemove.setOnClickListener {
            restarEntradas(etCantidad, btnRemove)
            calcularPrecio()
        }
    }

    private fun recogerIntent() {
        nombrePelicula = intent.getStringExtra("nombrePelicula")!!
        nombreCine = intent.getStringExtra("nombreCine")!!
        sesionElegida = intent.getStringExtra("sesionElegida")!!
        diaEscogido = intent.getStringExtra("diaEscogido")!!
    }

    private fun enviarIntent() {
        val intent = Intent(this, SeleccionarButacas::class.java).apply {
            putExtra("nombrePelicula", nombrePelicula)
            putExtra("nombreCine", nombreCine)
            putExtra("sesionElegida", sesionElegida)
            putExtra("diaEscogido", diaEscogido)
            putExtra("precioTotal", tvPrecioEntradas.text)
            putExtra("tarjeta", et_numTarjeta.text.toString())
            putStringArrayListExtra("tipoEntradas", lista_tipoEntradasSeleccionadas)
            putIntegerArrayListExtra("numeroEntradas", lista_numeroEntradasSeleccionadas)
        }
        startActivity(intent)
    }

    @Suppress("UNCHECKED_CAST")
    private fun comprobarNumTarjeta() {
        val patron = "TFH[0-9]{10}"
        val pat = Pattern.compile(patron)
        val mat = pat.matcher(et_numTarjeta.text.toString())
        if (user!!.isAnonymous) {
            enviarIntent()
        } else {
            var coincidencia: Boolean
            db.collection("usuarios").document(user.email!!).get().addOnCompleteListener {
                if (mat.matches() || et_numTarjeta.text.toString().isEmpty()) {
                    coincidencia = if (et_numTarjeta.text.toString().isNotEmpty()) {
                        val document: DocumentSnapshot = it.result!!
                        val mapa_tarjetaTFH: Map<String, Any> =
                            document["tarjetaTFH"] as Map<String, Any>
                        val tarjetaBBDD = mapa_tarjetaTFH["numTarjeta"] as String
                        tarjetaBBDD.equals(et_numTarjeta.text.toString())
                    } else {
                        true
                    }
                    if (coincidencia) {
                        enviarIntent()
                    } else {
                        mostrarAlerta("codigoTarjeta incorrecto")
                    }
                } else {
                    mostrarAlerta("formato tarjetaTFH")
                }
            }
        }
    }

    //Deshabilitar botones para restar entradas
    private fun deshabilitarBotones() {
        btnRemove_adulto.isEnabled = false
        btnRemove_joven.isEnabled = false
        btnRemove_nino.isEnabled = false
        btnRemove_senior.isEnabled = false
    }

    //Aumenta el número de entradas en la caja de texto que se rebibe por parámetro hasta 9 ENTRADAS
    private fun sumarEntradas(cajaTexto: EditText, botonDisminuir: ImageButton) {
        if (((Integer.parseInt(etCantidad_Adulto.text.toString()) +
                    Integer.parseInt(etCantidad_Joven.text.toString()) +
                    Integer.parseInt(etCantidad_Nino.text.toString()) +
                    Integer.parseInt(etCantidad_Senior.text.toString())) < 9)) {
            btnAdd_adulto.isEnabled = true
            btnAdd_joven.isEnabled = true
            btnAdd_nino.isEnabled = true
            btnAdd_senior.isEnabled = true
            botonDisminuir.isEnabled = true
            cajaTexto.setText((Integer.parseInt(cajaTexto.text.toString()) + 1).toString())
        }
        if (((Integer.parseInt(etCantidad_Adulto.text.toString()) +
                    Integer.parseInt(etCantidad_Joven.text.toString()) +
                    Integer.parseInt(etCantidad_Nino.text.toString()) +
                    Integer.parseInt(etCantidad_Senior.text.toString())) >= 9)) {
            botonDisminuir.isEnabled = true
            btnAdd_adulto.isEnabled = false
            btnAdd_joven.isEnabled = false
            btnAdd_nino.isEnabled = false
            btnAdd_senior.isEnabled = false
        }
        if (Integer.parseInt(etCantidad_Adulto.text.toString()) >= 9) {
            botonDisminuir.isEnabled = true
            btnAdd_adulto.isEnabled = false
        }
        if (Integer.parseInt(etCantidad_Joven.text.toString()) >= 9) {
            botonDisminuir.isEnabled = true
            btnAdd_joven.isEnabled = false
        }
        if (Integer.parseInt(etCantidad_Nino.text.toString()) >= 9) {
            botonDisminuir.isEnabled = true
            btnAdd_nino.isEnabled = false
        }
        if (Integer.parseInt(etCantidad_Senior.text.toString()) >= 9) {
            botonDisminuir.isEnabled = true
            btnAdd_senior.isEnabled = false
        }
    }

    //Resta la cantidad de entradas en la caja de texto que se pasa por parametro hasta 0
    private fun restarEntradas(cajaTexto: EditText, botonDisminuir: ImageButton) {
        if (Integer.parseInt(etCantidad_Adulto.text.toString()) >= 1 && botonDisminuir.equals(
                btnRemove_adulto)) {
            btnRemove_adulto.isEnabled = true
            btnAdd_adulto.isEnabled = true
            cajaTexto.setText((Integer.parseInt(cajaTexto.text.toString()) - 1).toString())
        }
        if (Integer.parseInt(etCantidad_Joven.text.toString()) >= 1 && botonDisminuir.equals(
                btnRemove_joven)) {
            btnRemove_joven.isEnabled = true
            btnAdd_joven.isEnabled = true
            cajaTexto.setText((Integer.parseInt(cajaTexto.text.toString()) - 1).toString())
        }
        if (Integer.parseInt(etCantidad_Nino.text.toString()) >= 1 && botonDisminuir.equals(
                btnRemove_nino)) {
            btnRemove_nino.isEnabled = true
            btnAdd_nino.isEnabled = true
            cajaTexto.setText((Integer.parseInt(cajaTexto.text.toString()) - 1).toString())
        }
        if (Integer.parseInt(etCantidad_Senior.text.toString()) >= 1 && botonDisminuir.equals(
                btnRemove_senior)) {
            btnRemove_senior.isEnabled = true
            btnAdd_senior.isEnabled = true
            cajaTexto.setText((Integer.parseInt(cajaTexto.text.toString()) - 1).toString())
        }
        if ((Integer.parseInt(etCantidad_Adulto.text.toString()) < 1)) {
            btnRemove_adulto.isEnabled = false
            btnAdd_adulto.isEnabled = true
        }
        if (Integer.parseInt(etCantidad_Joven.text.toString()) < 1) {
            btnRemove_joven.isEnabled = false
            btnAdd_joven.isEnabled = true
        }
        if (Integer.parseInt(etCantidad_Nino.text.toString()) < 1) {
            btnRemove_nino.isEnabled = false
            btnAdd_nino.isEnabled = true
        }
        if (Integer.parseInt(etCantidad_Senior.text.toString()) < 1) {
            btnRemove_senior.isEnabled = false
            btnAdd_senior.isEnabled = true
        }
    }

    /*Calcula el precio total de todas las entradas y lo formatea para que siempre aparezcan
    al menos 2 dígitos delante de la coma y otros dos detrás*/
    @SuppressLint("SetTextI18n")
    private fun calcularPrecio() {
        tvPrecioEntradas.text = String.format(
            "%02.2f", (Integer.parseInt(etCantidad_Adulto.text.toString())
                    * (txt_precioAdulto.text.toString()
                .substring(0, txt_precioAdulto.text.toString().length - 1)).toDouble() +
                    Integer.parseInt(etCantidad_Joven.text.toString()) *
                    (txt_precioJoven.text.toString()
                        .substring(0, txt_precioJoven.text.toString().length - 1)).toDouble() +
                    Integer.parseInt(etCantidad_Nino.text.toString()) *
                    (txt_precioNino.text.toString()
                        .substring(0, txt_precioNino.text.toString().length - 1)).toDouble() +
                    Integer.parseInt(etCantidad_Senior.text.toString()) *
                    (txt_precioSenior.text.toString()
                        .substring(0, txt_precioSenior.text.toString().length - 1)).toDouble())) + "€"
    }

    /*Se lee el número de entradas de cada caja y si todas están vacías, se retorna FALSO
    pero con que una de ellas tenga un número disntinto de cero, se retorna VERDARERO*/
    private fun leerNumeroEntradas(): Boolean {
        return (Integer.parseInt(etCantidad_Adulto.text.toString()) != 0) or
                (Integer.parseInt(etCantidad_Joven.text.toString()) != 0) or
                (Integer.parseInt(etCantidad_Senior.text.toString()) != 0) or
                (Integer.parseInt(etCantidad_Nino.text.toString()) != 0)
    }

    //Muestra una alerta para avisar de que se debe seleccionar al menos una entrada
    private fun mostrarAlerta(mensaje: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setPositiveButton("Aceptar", null)
        when (mensaje) {
            "numero entradas" -> builder.setMessage("Debes seleccionar al menos UNA ENTRADA")
            "formato tarjetaTFH" -> builder.setMessage("Debes introducir un código con el formato: TFH0000000000")
            "codigoTarjeta incorrecto" -> {
                builder.setMessage("Tu CÓDIGO de TARJETA TFH es INCORRECTO. Por favor, comprueba tu " +
                        "código en tus datos personales")
            }
            "canjear" -> {
                builder.setTitle("Número de tarjeta")
                builder.setPositiveButton("Cerrar", null)
                builder.setMessage("Si has solicitado la tarjeta TFH, encontrarás tu número de tarjeta en el apartado \"TARJETA TFH\". Este número sigue el patrón \"TFH0000000000\"")
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // Comprobamos qué tipos de entradas han sido seleccionadas y el número exacto de cada una de ellas
    private fun buscarTipoEntradasSeleccionadas() {
        lista_numeroEntradasSeleccionadas = ArrayList()
        lista_tipoEntradasSeleccionadas = ArrayList()
        if (Integer.parseInt(etCantidad_Adulto.text.toString()) != 0) {
            lista_tipoEntradasSeleccionadas.add("Adulto")
            lista_numeroEntradasSeleccionadas.add(Integer.parseInt(etCantidad_Adulto.text.toString()))
        }
        if (Integer.parseInt(etCantidad_Joven.text.toString()) != 0) {
            lista_tipoEntradasSeleccionadas.add("Joven")
            lista_numeroEntradasSeleccionadas.add(Integer.parseInt(etCantidad_Joven.text.toString()))
        }
        if (Integer.parseInt(etCantidad_Nino.text.toString()) != 0) {
            lista_tipoEntradasSeleccionadas.add("Niño")
            lista_numeroEntradasSeleccionadas.add(Integer.parseInt(etCantidad_Nino.text.toString()))
        }
        if (Integer.parseInt(etCantidad_Senior.text.toString()) != 0) {
            lista_tipoEntradasSeleccionadas.add("Senior")
            lista_numeroEntradasSeleccionadas.add(Integer.parseInt(etCantidad_Senior.text.toString()))
        }
    }
}