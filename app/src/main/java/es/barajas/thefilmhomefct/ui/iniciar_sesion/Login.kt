package es.barajas.thefilmhomefct.ui.iniciar_sesion

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.*
import android.widget.*
import androidx.appcompat.app.*
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import es.barajas.thefilmhomefct.SesionIniciada
import es.barajas.thefilmhomefct.R
import java.util.*

class Login : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var et_loginEmail: EditText
    private lateinit var et_loginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnPasswordShow: ImageButton
    private var isEmpty_email: Boolean = true
    private var isEmpty_password: Boolean = true
    //Variable para programar el botón que muestra la contraseña
    private var mostrarPassword = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //INICIALIZACIONES
        toolbar = findViewById(R.id.toolbar_login)
        et_loginEmail = findViewById(R.id.et_loginEmail)
        et_loginPassword = findViewById(R.id.et_loginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnPasswordShow = findViewById(R.id.btn_passwordShow)
        //-----TOOLBAR----
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        et_loginEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isEmpty_email = s!!.isEmpty()
                btnLogin.isEnabled = !isEmpty_email and !isEmpty_password
                if (btnLogin.isEnabled) {
                    btnLogin.setBackgroundColor(Color.rgb(92, 92, 92))
                } else {
                    btnLogin.setBackgroundColor(Color.rgb(213, 212, 212))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_loginPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isEmpty_password = s!!.isEmpty()
                btnLogin.isEnabled = !isEmpty_email and !isEmpty_password
                btnLogin.isEnabled = !isEmpty_email and !isEmpty_password
                if (btnLogin.isEnabled) {
                    btnLogin.setBackgroundColor(Color.rgb(92, 92, 92))
                } else {
                    btnLogin.setBackgroundColor(Color.rgb(213, 212, 212))
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        //iniciarSesion
        iniciarSesion()
        btnPasswordShow.setOnClickListener {
            if (!mostrarPassword) {
                et_loginPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                btnPasswordShow.setImageResource(R.drawable.ic_visibility)
                mostrarPassword = true
            }
            else {
                et_loginPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                btnPasswordShow.setImageResource(R.drawable.ic_visibility_off)
                mostrarPassword = false
            }
        }
    }

    //Función para INICIAR SESIÓN cuando el usuario YA EXISTE en la BBDD
    private fun iniciarSesion() {
        btnLogin.setOnClickListener {
            //addOnCompleteListener -> Para notificar si la operación de registrao ha sido satisfactoria o no
            FirebaseAuth.getInstance().signInWithEmailAndPassword(et_loginEmail.text.toString(),
                et_loginPassword.text.toString()).addOnCompleteListener {
                if (it.isSuccessful) {
                    enviarEmail()
                } else {
                    mostrarAlerta()
                }
            }
        }
    }

    //Muestra una alerta si ha habido un error al autenticar
    private fun mostrarAlerta() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario \n¿Te has REGISTRADO antes?")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Navega a Activity_home
    private fun enviarEmail() {
        val intent = Intent(this, SesionIniciada::class.java).apply {
            putExtra("email", et_loginEmail.text.toString().toLowerCase(Locale.getDefault()))
        }
        startActivity(intent)
    }
}