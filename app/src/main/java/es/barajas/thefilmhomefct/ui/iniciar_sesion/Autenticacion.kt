package es.barajas.thefilmhomefct.ui.iniciar_sesion

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import es.barajas.thefilmhomefct.IniciarSesion
import es.barajas.thefilmhomefct.SesionIniciada

class Autenticacion(navIniciarSesion: TextView, navUserMail: TextView, actividad: Activity) {
    private var navIniciarSesion: TextView
    private var navUserMail: TextView
    private var actividad: Activity
    private var user = FirebaseAuth.getInstance().currentUser
    //Instancia de la BBDD de FIREBASE
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    companion object {
        var isClose = false
    }

    init {
        this.navIniciarSesion = navIniciarSesion
        this.navUserMail = navUserMail
        this.actividad = actividad
    }

    fun onClick() {
        navIniciarSesion.setOnClickListener {
            if (navIniciarSesion.text.equals("Iniciar Sesión")) {
                enviarElementosMenu()
            } else {
                val intent = Intent(actividad, SesionIniciada::class.java).apply {
                    putExtra("email", user?.email)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                actividad.applicationContext.startActivity(intent)
            }
        }
    }

    //Si un usuario tiene abierta la sesión, el textot del botón "Iniciar sesion" tendrá algunos datos personales suyos
    fun getUserInfo() {
        if (!user!!.isAnonymous) {
            db.collection("usuarios").document(user!!.email!!).get().addOnCompleteListener {
                val document: DocumentSnapshot = it.result!!
                if (it.isSuccessful && document.exists()) {
                    val mapa_datosPersonales = document.data!!["lista_datosPersonales"] as Map<*, *>
                    navIniciarSesion.text = mapa_datosPersonales["nombre"].toString()
                    navUserMail.text = user!!.email!!
                }
            }
        }
    }

    //Navega a IniciarSesion enviando los ids de los componentes necesarios para abrir el menú desde esa activity
    private fun enviarElementosMenu() {
        val intent = Intent(actividad, IniciarSesion::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        actividad.applicationContext.startActivity(intent)
    }
}