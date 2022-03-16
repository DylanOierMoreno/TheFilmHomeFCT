package es.barajas.thefilmhomefct.ui.iniciar_sesion

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.text.Html
import android.text.method.*
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.*
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.SesionIniciada
import es.barajas.thefilmhomefct.data.UsuarioFirebase
import es.barajas.thefilmhomefct.ui.dialog.DatePickerFragment
import es.barajas.thefilmhomefct.ui.peliculas.ElegirEntradas
import java.util.*
import java.util.regex.*

class Registro : AppCompatActivity() {
    private lateinit var btnGuardarDatos: Button
    private lateinit var btn_password: ImageButton
    private lateinit var btn_password2: ImageButton
    private lateinit var txt_privacidadRegistro: TextView
    private lateinit var et_userNombre: EditText
    private lateinit var et_userApellidos: EditText
    private lateinit var et_userMail: EditText
    private lateinit var et_userFechaNac: EditText
    private lateinit var et_userPassword: EditText
    private lateinit var et_userPassword2: EditText
    private lateinit var et_userNumDocumento: EditText
    private lateinit var rg_userGenero: RadioGroup
    private lateinit var chk_aceptarCondiciones: CheckBox
    private lateinit var spinner_tiposDocumentos: Spinner
    //Variables para la programación de los botones para mostrar la contraseña
    private var mostrarPassword = true
    private var mostrarPassword2 = true
    //Instancia de la BASE DE DATOS de FIREBASE
    private val db = FirebaseFirestore.getInstance()
    //RADIOBUTTON
    private lateinit var radioButtonMarcado: View
    //ACTIVITY desde donde se envió un INTENT (puede ser IniciarSesion o LoginCompra)
    private lateinit var activityEmisora: String
    private lateinit var nombrePelicula: String
    private lateinit var nombreCine: String
    private lateinit var sesionElegida: String
    private lateinit var diaEscogido: String

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
        btnGuardarDatos = findViewById(R.id.btnGuardarDatos)
        btn_password = findViewById(R.id.btn_password)
        btn_password2 = findViewById(R.id.btn_password2)
        txt_privacidadRegistro = findViewById(R.id.txt_privacidadRegistro)
        et_userNombre = findViewById(R.id.et_userNombre)
        et_userApellidos = findViewById(R.id.et_userApellidos)
        et_userMail = findViewById(R.id.et_userMail)
        et_userFechaNac = findViewById(R.id.et_userFechaNac)
        et_userPassword = findViewById(R.id.et_userPassword)
        et_userPassword2 = findViewById(R.id.et_userPassword2)
        et_userNumDocumento = findViewById(R.id.userNumeroDoc)
        rg_userGenero = findViewById(R.id.rg_userGenero)
        chk_aceptarCondiciones = findViewById(R.id.chkAceptarCondiciones)
        spinner_tiposDocumentos = findViewById(R.id.spinner_tiposDocumentos)
        //-----TOOLBAR----
        val toolbar: Toolbar = findViewById(R.id.toolbar_iniciarSesion)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        et_userFechaNac.setOnClickListener { escogerFechaNacimiento() }
        btn_password.setOnClickListener {
            if (!mostrarPassword) {
                et_userPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btn_password.setImageResource(R.drawable.ic_visibility)
                mostrarPassword = true
            } else {
                et_userPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btn_password.setImageResource(R.drawable.ic_visibility_off)
                mostrarPassword = false
            }
        }
        btn_password2.setOnClickListener {
            if (!mostrarPassword2) {
                et_userPassword2.transformationMethod = PasswordTransformationMethod.getInstance()
                btn_password2.setImageResource(R.drawable.ic_visibility)
                mostrarPassword2 = true
            } else {
                et_userPassword2.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                btn_password2.setImageResource(R.drawable.ic_visibility_off)
                mostrarPassword2 = false
            }
        }
        recogerIntent()
        consultarPrivacidadBBDD()
        btnGuardarDatos.setOnClickListener {
            if (comprobarCampos()) {
                if (comprobarFechaNacimiento()) {
                    if (comprobarContrasenia()) {
                        if (comprobarGenero()) {
                            if (comprobarSpinnerdocs()) {
                                if (comprobarPrivacidad()) {
                                    registrarUsuario()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Esta función se activa al pulsar sobre et_userFechaNacimiento
    private fun escogerFechaNacimiento() {
        val locale = Locale("es", "ES")
        Locale.setDefault(locale)
        val datePickerFragment =
            DatePickerFragment.newInstance(DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val fechaSeleccionada = day.toString() + "/" + (month + 1) + "/" + year
                et_userFechaNac.setText(fechaSeleccionada)
            })
        datePickerFragment.show(supportFragmentManager, "datePicker")
    }

    //Comprueba si los campos están vacíos o no. Si están vacios, notificamos error
    private fun comprobarCampos(): Boolean {
        if (et_userNombre.text.isEmpty() or et_userApellidos.text.isEmpty() or
            et_userMail.text.isEmpty() or et_userPassword.text.isEmpty() or
            et_userPassword2.text.isEmpty() or et_userNumDocumento.text.isEmpty()) {
            mostrarAlerta("campo vacio")
            return false
        } else {
            return true
        }
    }

    //Comprueba si la contraseña y la confirmacion de contraseña coninciden para notificar error si no lo hacen
    private fun comprobarContrasenia(): Boolean {
        val coinciden = et_userPassword.text.toString().equals(et_userPassword2.text.toString())
        return if (!coinciden) {
            mostrarAlerta("contraseña no coincidente")
            false
        } else {
            true
        }
    }

    //Comprueba si se ha seleccionado DNI o NIE en el Spinner para notifcarlo si no se hace
    private fun comprobarSpinnerdocs(): Boolean {
        var docCorrecto = true
        when (spinner_tiposDocumentos.selectedItem.toString()) {
            "Seleccione" -> {
                mostrarAlerta("documento no seleccionado")
                docCorrecto = false
            }
            "DNI" -> docCorrecto = comprobarDNI()
            "NIE" -> docCorrecto = comprobarNIE()
        }
        return docCorrecto
    }

    private fun comprobarPrivacidad(): Boolean {
        return if (chk_aceptarCondiciones.isChecked) {
            true
        } else {
            mostrarAlerta("privacidad")
            false
        }
    }

    private fun comprobarFechaNacimiento(): Boolean {
        return if (et_userFechaNac.text.isEmpty()) {
            mostrarAlerta("fecha de nacimiento")
            false
        } else {
            true
        }
    }

    private fun comprobarGenero(): Boolean {
        return if (rg_userGenero.checkedRadioButtonId == -1) {
            mostrarAlerta("genero")
            false
        } else {
            radioButtonMarcado = rg_userGenero.findViewById(rg_userGenero.checkedRadioButtonId)
            true
        }
    }

    private fun comprobarDNI(): Boolean {
        val dniCorrecto: Boolean
        val LETRAS_NIF = "TRWAGMYFPDXBNJZSQVHLCKE"
        val dniAuxiliar = et_userNumDocumento.text
        val patronDNI = "^[0-9]{8}[A-Za-z]$"
        val pat: Pattern = Pattern.compile(patronDNI)
        val mat: Matcher = pat.matcher(dniAuxiliar.toString())
        if (mat.matches()) {
            val numNif = Integer.parseInt(dniAuxiliar.substring(0, dniAuxiliar.length - 1))
            val letraNif =
                dniAuxiliar.substring(dniAuxiliar.length - 1)
            val letraCalculada = LETRAS_NIF[numNif % 23]
            if (letraCalculada.toString() != letraNif.toUpperCase(Locale.getDefault())) {
                mostrarAlerta("dni incorrecto")
                dniCorrecto = false
            } else {
                dniCorrecto = true
            }
        } else {
            mostrarAlerta("dni patron")
            dniCorrecto = false
        }
        return dniCorrecto
    }

    private fun comprobarNIE(): Boolean {
        val nieCorrecto: Boolean
        val LETRAS_NIF = "TRWAGMYFPDXBNJZSQVHLCKE"
        val nieAuxiliar = et_userNumDocumento.text
        var primerDigitoNie = ""
        val patronDNI = "[X-Zx-z][0-9]{7}-[A-Za-z]"
        val pat: Pattern = Pattern.compile(patronDNI)
        val mat: Matcher = pat.matcher(nieAuxiliar.toString())
        if (mat.matches()) {
            val letraNie = nieAuxiliar[0].toString().toUpperCase(Locale.getDefault())
            val numeros = nieAuxiliar.substring(1, nieAuxiliar.length - 2)
            val letraNif = nieAuxiliar.substring(nieAuxiliar.length - 1)
            when (letraNie) {
                "X" -> primerDigitoNie = "0"
                "Y" -> primerDigitoNie = "1"
                "Z" -> primerDigitoNie = "2"
            }
            val numNif = Integer.parseInt(primerDigitoNie + numeros)
            val letraCalculada = LETRAS_NIF[numNif % 23]
            nieCorrecto =
                if (letraCalculada.toString() != letraNif.toUpperCase(Locale.getDefault())) {
                    mostrarAlerta("nie incorrecto")
                    false
                } else {
                    true
                }
        } else {
            mostrarAlerta("nie patron")
            nieCorrecto = false
        }
        return nieCorrecto
    }

    private fun mostrarAlerta(error: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setPositiveButton("Aceptar", null)
        when (error) {
            "campo vacio" -> builder.setMessage("Al menos uno de los campos está vacío")
            "contraseña no coincidente" -> builder.setMessage("Las contraseñas no coinciden")
            "documento no seleccionado" -> builder.setMessage("Debes seleccionar un tipo de documento (DNI ó NIE)")
            "privacidad" -> builder.setMessage("Tienes que aceptar las condiciones para poder registrarte")
            "fecha de nacimiento" -> builder.setMessage("Debes escoger tu fecha de nacimiento")
            "genero" -> builder.setMessage("Debes escoger el género con el que te identifiques")
            "autenticacion email/password" -> builder.setMessage("Problema de autenticación email/contraseña al registrar el usuario")
            "dni incorrecto" -> builder.setMessage("El DNI no es correcto, revise que la parte numérica y la letra son las suyas")
            "dni patron" -> builder.setMessage("El DNI introducido no coincide con el patrón 00000000X")
            "nie incorrecto" -> builder.setMessage("El NIE no es correcto, revise que la parte numérica y la letra son las suyas")
            "nie patron" -> builder.setMessage("El NIE introducido no coincide con el patrón X0000000-A")
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun mostrarMensajeBienvenida() {
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton("Aceptar") { _, _ -> volverAlInicio() }
        builder.setMessage("¡Bienvenido a The Film Home!")
        builder.setTitle("Usuario Registrado")
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun registrarUsuario() {
        //REGISTRAMOS el usuario con EMAIL/CONTRASEÑA
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(et_userMail.text.toString(),
            et_userPassword.text.toString()).addOnCompleteListener {
            if (it.isSuccessful) {
                val usuario = Usuario(et_userNombre.text.toString(),
                    et_userApellidos.text.toString(),
                    et_userFechaNac.text.toString(),
                    spinner_tiposDocumentos.selectedItem.toString(),
                    et_userNumDocumento.text.toString(),
                    rg_userGenero.indexOfChild(radioButtonMarcado).toString(),
                    et_userMail.text.toString().toLowerCase(Locale.getDefault()),
                    et_userPassword.text.toString())
                val listaEntradasVacia: ArrayList<HashMap<String, Any>> = ArrayList()
                val usuarioFirebase = UsuarioFirebase(usuario.crearMapaDatosPersonales(),
                    listaEntradasVacia, mapOf("numTarjeta" to "", "puntos" to 0))
                db.collection("usuarios").document(et_userMail.text.toString()
                    .toLowerCase(Locale.getDefault())).set(usuarioFirebase)
                mostrarMensajeBienvenida()
            } else {
                mostrarAlerta("autenticacion email/password")
            }
        }
    }

    private fun volverAlInicio() {
        if (activityEmisora == "IniciarSesion") {
            val intent = Intent(this, SesionIniciada::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, ElegirEntradas::class.java).apply {
                putExtra("nombrePelicula", nombrePelicula)
                putExtra("nombreCine", nombreCine)
                putExtra("sesionElegida", sesionElegida)
                putExtra("diaEscogido", diaEscogido)
            }
            startActivity(intent)
        }
    }

    private fun recogerIntent() {
        activityEmisora = intent.getStringExtra("nombreActivity")!!
        if (activityEmisora.equals("LoginCompra")) {
            nombrePelicula = intent.getStringExtra("nombrePelicula")!!
            nombreCine = intent.getStringExtra("nombreCine")!!
            sesionElegida = intent.getStringExtra("sesionElegida")!!
            diaEscogido = intent.getStringExtra("diaEscogido")!!
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun consultarPrivacidadBBDD(){
        txt_privacidadRegistro.setOnClickListener {
            db.collection("privacidad").document("privacidad").get().addOnSuccessListener {
                val texto = it.get("condiciones") as String
                val builder = AlertDialog.Builder(this)
                builder.setPositiveButton("Cerrar", null)
                builder.setTitle("Política de Privacidad")
                builder.setMessage(Html.fromHtml(texto,1))
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }
}