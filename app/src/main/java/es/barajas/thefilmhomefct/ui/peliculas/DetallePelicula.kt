@file:Suppress("DEPRECATION")

package es.barajas.thefilmhomefct.ui.peliculas

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.*
import com.google.android.youtube.player.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.adapter.VP_AdaptadorDetallePeliculas
import es.barajas.thefilmhomefct.ui.iniciar_sesion.Autenticacion

//INTERFAZ YouTubePlayer.OnInitializedListener para el VÍDEO de youtube
class DetallePelicula : AppCompatActivity(), YouTubePlayer.OnInitializedListener {
    private  val adaptador_viewPager by lazy { VP_AdaptadorDetallePeliculas(this, directorioPelicula)}
    private lateinit var tab_layout_detallePeliculas: TabLayout
    private lateinit var video_trailerPelicula: YouTubePlayerFragment
    private lateinit var txt_tituloPelicula: TextView
    private lateinit var txt_infoPelicula: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var pager: ViewPager2
    private lateinit var tabLayoutMediator: TabLayoutMediator
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    //Consola -> Crear credenciales -> Clave de API (para utilizar los vídeos de youtube)
    private val claveYoutube = "AIzaSyD4gaL6rtgMBO8PTYFeAJxAi9ua-s3jl6w"
    companion object {
        lateinit var nombrePelicula: String
        lateinit var directorioPelicula: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_peliculas)
        inicializarVariables()
        //-----TOOLBAR----
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        recogerIntent()
        //VIEWPAGER
        pager.adapter = adaptador_viewPager
        //TABLAYOUT
        when(directorioPelicula) {
            "peliculasCartelera" -> {
                tabLayoutMediator = TabLayoutMediator(tab_layout_detallePeliculas, pager) { tab, position ->
                    when (position) {
                        0 -> tab.text = "HORARIO"
                        1 -> tab.text = "DESCRIPCIÓN"
                    }
                }
            }
            "peliculasEstrenos" -> {
                tabLayoutMediator = TabLayoutMediator(tab_layout_detallePeliculas, pager) { tab, position ->
                    if (position == 0) tab.text = "DESCRIPCIÓN"
                }
            }
        }
        tabLayoutMediator.attach()
        video_trailerPelicula.initialize(claveYoutube, this)
        consultarPelicula(directorioPelicula, nombrePelicula)
    }

    private fun recogerIntent() {
        nombrePelicula = intent.extras?.getString("NOMBRE")!!
        directorioPelicula = intent.extras?.getString("DIRECTORIO")!!
    }

    private fun inicializarVariables() {
        txt_tituloPelicula = findViewById(R.id.txt_detalleTituloPelicula)
        txt_infoPelicula = findViewById(R.id.txt_detalleInformacionPelicula)
        video_trailerPelicula = fragmentManager.findFragmentById(R.id.video_trailerPelicula) as YouTubePlayerFragment
        toolbar = findViewById(R.id.toolbar_detallePeliculas)
        pager = findViewById(R.id.pager_detallePelicula)
        tab_layout_detallePeliculas = findViewById(R.id.tab_layout_detallePeliculas)
    }

    //MÉTODOS de la INTERFAZ YouTubePlayer.OnInitializedListener
    override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, p1: YouTubePlayer?, p2: Boolean) {
        p1?.fullscreenControlFlags = YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION or
                YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE
        //cargar el trailer
        if(!p2) {
            db.collection(directorioPelicula).document(nombrePelicula).get().addOnSuccessListener {
                p1?.cueVideo(it.get("trailer") as String)
            }.addOnFailureListener {
                System.out.println("========ERROR!!!!!======== " + it)
            }
        }
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        if(p1?.isUserRecoverableError!!) {
            p1.getErrorDialog(this, 1).show()
        } else {
            val error = "Error al inicializar Youtube " + p1.toString()
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }

    //EVENTOS
    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == 1) getYoutubePlayerProvider().initialize(claveYoutube, this)
    }

    private fun getYoutubePlayerProvider(): YouTubePlayer.Provider = video_trailerPelicula

    private fun consultarPelicula(nombreDirectorio: String, nombrePelicula: String){
        db.collection(nombreDirectorio).document(nombrePelicula).get().addOnSuccessListener {
            txt_tituloPelicula.text = it.get("titulo") as String
            txt_infoPelicula.text = it.get("descripcion") as String
        }.addOnFailureListener {
            System.out.println("========ERROR!!!!!======== " + it)
        }
    }
}