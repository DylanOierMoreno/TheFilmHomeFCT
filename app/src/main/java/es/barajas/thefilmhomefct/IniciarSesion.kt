package es.barajas.thefilmhomefct

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import es.barajas.thefilmhomefct.ui.iniciar_sesion.Login
import es.barajas.thefilmhomefct.ui.iniciar_sesion.Registro

class IniciarSesion : AppCompatActivity() {
    private lateinit var btnIniciarSesion: Button
    private lateinit var btnRegistrarse: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iniciarsesion)
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion)
        btnRegistrarse = findViewById(R.id.btnRegistrarse)
        btnIniciarSesion.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
        btnRegistrarse.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            intent.putExtra("nombreActivity", "IniciarSesion")

            startActivity(intent)
        }
    }
}