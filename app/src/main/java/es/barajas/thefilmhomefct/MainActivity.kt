package es.barajas.thefilmhomefct

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.*
import es.barajas.thefilmhomefct.ui.iniciar_sesion.Autenticacion
import java.util.*

class MainActivity : AppCompatActivity(),  NavigationView.OnNavigationItemSelectedListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var headerView: View
    private lateinit var navIniciarSesion: TextView
    private lateinit var navUserMail: TextView
    private lateinit var navUserPhoto: ShapeableImageView
    private lateinit var autenticacion: Autenticacion
    //USUARIO DE FIREBASE
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var storageRef: StorageReference
    private var esCargada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //----TOOLBAR
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //----MENÚ
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        //----INICIALIZACIONES
        headerView = navView.getHeaderView(0)
        navIniciarSesion = headerView.findViewById(R.id.nav_iniciarSesion)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_peliculas, R.id.nav_promociones,
            R.id.nav_ubicacion, R.id.nav_iniciarSesion), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
        navUserMail = headerView.findViewById(R.id.nav_userMail)
        navUserPhoto = headerView.findViewById(R.id.nav_userPhoto)
        storageRef = FirebaseStorage.getInstance().reference
        //Check App
        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance())
    }

    override fun onResume() {
        super.onResume()
        autenticacion = Autenticacion(navIniciarSesion, navUserMail, this)
        if (user == null) {
            signInAnonymously()
        } else if (user.isAnonymous) {
            navUserPhoto.setImageResource(R.drawable.ic_account_circle)
            navUserPhoto.setBackgroundColor(Color.parseColor("#00FFFFFF"))
        } else {
            autenticacion.getUserInfo()
            if (!esCargada) descargarPerfilBBDD()
        }
        autenticacion.onClick()
        navUserPhoto.setOnClickListener { if (!user!!.isAnonymous) { cargarImagen() } }
    }

    //Método para acceder a la galería y poder escoger una foto para ponerla en el perfil
    @Suppress("DEPRECATION")
    @SuppressLint("IntentReset")
    private fun cargarImagen() {
        val intent = Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/"
        esCargada = true
        startActivityForResult(Intent.createChooser(intent, "Seleccione la Aplicación"), 10)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val path = data?.data
            navUserPhoto.setImageURI(path)
            navUserPhoto.setBackgroundColor(Color.parseColor("#FFFFFF"))
            //Ruta dónde se guardará la foto de perfil en el Storage
            val filePath = storageRef.child("imagenPerfil").child(user!!.email!!)
            filePath.putFile(path!!)
        }
    }

    private fun descargarPerfilBBDD() {
        var bitmap: Bitmap?
        storageRef.child("imagenPerfil").child(user!!.email!!).getBytes(1024 * 1024).addOnSuccessListener {
            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            navUserPhoto.setImageBitmap(bitmap)
            navUserPhoto.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }.addOnFailureListener {
            navUserPhoto.setImageResource(R.drawable.ic_account_circle)
            navUserPhoto.setBackgroundColor(Color.parseColor("#00FFFFFF"))
            System.out.println("========ERROR!!!!!======== " + it)
        }
    }

    //Iniciar sesión anónima para que se pueda usar la app aunque nadie haya iniciado sesión, se crean usuarios anónimos
    private fun signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.nav_peliculas -> {
                enviarIntent(Peliculas::class.java)
                return true
            }
            R.id.nav_promociones -> {
                enviarIntent(Promociones::class.java)
                return true
            }
            R.id.nav_ubicacion -> {
                enviarIntent(Ubicacion::class.java)
                return true
            }
            R.id.nav_cerrarAp -> {
                intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                return true
            }
        }
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun enviarIntent(clase: Class<*>) {
        intent = Intent(applicationContext, clase)
        onBackPressed()
        startActivity(intent)
    }
}