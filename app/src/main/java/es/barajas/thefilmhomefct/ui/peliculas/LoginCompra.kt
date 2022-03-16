package es.barajas.thefilmhomefct.ui.peliculas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.ui.iniciar_sesion.Registro

/*Clase similar a Login que utilizamos para que un usuario que va a comprar una entrada pueda iniciar
sesión antes de comprar para que la entrada se guarde en la BBDD. Si el usuario no quiere iniciar sesión,
podrá comprar y obtendrá el pdf con la entrada pero no se guardará ningún registro en BBDD */
class LoginCompra : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    //Controles
    private lateinit var et_emailCompra: EditText
    private lateinit var et_passwordCompra: EditText
    private lateinit var btn_mostrarPass: ImageButton//mostrar u ocultar la contraseña
    private lateinit var btn_loginCompra: Button //un usuario existente inicia sesion
    private lateinit var btn_registroCompra: Button//Usuario no registrado se registra
    private lateinit var btn_continuarCompra: Button//Comprar sin iniciar sesion ni registrarse
    //Variables para recoger INTENT que viene de RV_AdaptadorHorario
    private lateinit var peliculaElegida: String
    private lateinit var cineElegido: String
    private lateinit var sesionElegida: String
    private lateinit var diaElegido: String
    //Variables para controlar si el email y contraseña están o no vacíos
    private var isEmpty_email: Boolean = true
    private var isEmpty_password: Boolean = true
    private var mostrarPassword = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_compra)
        inicializarVariables()
        //-----TOOLBAR----
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //Línea para quitar el título del APPBAR
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        recogerIntent()
        activar_btnMostrarContrasenia()
        programarCajasEmailPassword()
        btn_registroCompra.setOnClickListener { enviarIntentRegistro() }
        btn_continuarCompra.setOnClickListener { enviarIntent() }
    }

    //Inicializar controles
    private fun inicializarVariables() {
        toolbar = findViewById(R.id.toolbar_loginCompra)
        et_emailCompra = findViewById(R.id.et_loginEmailCompra)
        et_passwordCompra = findViewById(R.id.et_loginPasswordCompra)
        btn_loginCompra = findViewById(R.id.btnLoginCompra)
        btn_registroCompra = findViewById(R.id.btnRegistroCompra)
        btn_continuarCompra = findViewById(R.id.btnContinuarCompra)
        btn_mostrarPass = findViewById(R.id.btn_passwordShowCompra)
    }

    private fun recogerIntent() {
        peliculaElegida = intent.getStringExtra("nombrePelicula")!!
        cineElegido = intent.getStringExtra("nombreCine")!!
        sesionElegida = intent.getStringExtra("sesionElegida")!!
        diaElegido = intent.getStringExtra("diaEscogido")!!
    }

    private fun enviarIntent() {
        val intent = Intent(this, ElegirEntradas::class.java).apply {
            putExtra("nombrePelicula", peliculaElegida)
            putExtra("nombreCine", cineElegido)
            putExtra("sesionElegida", sesionElegida)
            putExtra("diaEscogido", diaElegido)
        }
        this.startActivity(intent)
    }

    private fun enviarIntentRegistro() {
        val intent = Intent(this, Registro::class.java).apply {
            putExtra("nombreActivity", "LoginCompra")
            putExtra("nombrePelicula", peliculaElegida)
            putExtra("nombreCine", cineElegido)
            putExtra("sesionElegida", sesionElegida)
            putExtra("diaEscogido", diaElegido)
        }
        this.startActivity(intent)
    }

    //---------------------------1. INICIAR SESION USUARIO EXISTENTE---------------------------
    private fun activar_btnMostrarContrasenia(){
        btn_mostrarPass.setOnClickListener {
            if (!mostrarPassword) {
                et_passwordCompra.transformationMethod = PasswordTransformationMethod.getInstance()
                btn_mostrarPass.setImageResource(R.drawable.ic_visibility)
                mostrarPassword = true
            }
            else {
                et_passwordCompra.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btn_mostrarPass.setImageResource(R.drawable.ic_visibility_off)
                mostrarPassword = false
            }
        }
    }

    // Activa el botón para hacer login si email y contraseña tienen contenido.
    private fun programarCajasEmailPassword() {
        et_emailCompra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isEmpty_email = s!!.isEmpty()
                btn_loginCompra.isEnabled = !isEmpty_email and !isEmpty_password
                if (btn_loginCompra.isEnabled) {
                    btn_loginCompra.setBackgroundColor(Color.rgb(92, 92, 92))
                } else {
                    btn_loginCompra.setBackgroundColor(Color.rgb(213, 212, 212))
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_passwordCompra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isEmpty_password = s!!.isEmpty()
                btn_loginCompra.isEnabled = !isEmpty_email and !isEmpty_password
                if (btn_loginCompra.isEnabled) {
                    btn_loginCompra.setBackgroundColor(Color.rgb(92, 92, 92))
                } else {
                    btn_loginCompra.setBackgroundColor(Color.rgb(213, 212, 212))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        iniciarSesion()
    }

    //Función para INICIAR SESIÓN cuando el usuario YA EXISTE en la BBDD
    private fun iniciarSesion() {
        btn_loginCompra.setOnClickListener {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(et_emailCompra.text.toString(),
                et_passwordCompra.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    mostrarAlerta("Sesion iniciada")
                } else {
                    mostrarAlerta("Error autenticacion")
                }
            }
        }
    }


    //Muestra una alerta si ha habido un error al autenticar
    private fun mostrarAlerta(mensaje: String) {
        val builder = AlertDialog.Builder(this)
        when (mensaje) {
            "Error autenticacion" -> {
                builder.setTitle("Error")
                builder.setMessage("Se ha producido un error autenticando al usuario \n ¿Te has REGISTRADO antes?")
                builder.setPositiveButton("Aceptar", null)
            }
            "Sesion iniciada" -> {
                builder.setTitle("Información")
                builder.setMessage("Sesión INICIADA con éxito. Ya puedes CONTINUAR TU COMPRA")
                builder.setPositiveButton("Aceptar") { _, _ -> enviarIntent() }
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}