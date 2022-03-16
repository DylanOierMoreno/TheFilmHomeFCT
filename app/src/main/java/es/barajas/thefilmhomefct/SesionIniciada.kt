package es.barajas.thefilmhomefct

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import es.barajas.thefilmhomefct.adapter.VP_AdaptadorSesionIniciada
import es.barajas.thefilmhomefct.ui.iniciar_sesion.Autenticacion

class SesionIniciada : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var navIniciarSesion: TextView
    private lateinit var navUserMail: TextView
    private lateinit var navUserPhoto: ShapeableImageView
    private val adaptador by lazy { VP_AdaptadorSesionIniciada(this) }
    //Variables para el menú
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var headerView: View
    private lateinit var toolbar: Toolbar
    private lateinit var pager: ViewPager2
    private lateinit var tabs: TabLayout
    private lateinit var autenticacion: Autenticacion//USUARIO DE FIREBASE
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var storageRef: StorageReference
    private var esCargada = false
    companion object {
        lateinit var emailUser: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sesion_iniciada)
        inicializarVariables()
        //-----TOOLBAR----
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //----MENÚ NAVIGATIONDRAWAER
        drawerLayout = findViewById(R.id.drawer_layout_sesionIniciada)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        navView.setNavigationItemSelectedListener(this)
        recogerIntent()
        VP_AdaptadorSesionIniciada.recogerEmail(emailUser)
        //-----VIEWPAGER2----
        pager.adapter = adaptador
        pager.offscreenPageLimit = 2
        val tabLayoutMediator = TabLayoutMediator(tabs, pager) { tab, position ->
            when (position) {
                0 -> tab.text = "MIS DATOS"
                1 -> tab.text = "ENTRADAS"
                2 -> tab.text = "TARJETA TFH"
            }
        }
        tabLayoutMediator.attach()
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

    private fun inicializarVariables() {
        toolbar = findViewById(R.id.toolbar_sesionIniciada)
        navView = findViewById(R.id.nav_view_sesionIniciada)
        headerView = navView.getHeaderView(0)
        navIniciarSesion = headerView.findViewById(R.id.nav_iniciarSesion)
        navUserMail = headerView.findViewById(R.id.nav_userMail)
        navUserPhoto = headerView.findViewById(R.id.nav_userPhoto)
        pager = findViewById(R.id.pager_iniciarSesion)
        tabs = findViewById(R.id.tab_layout_iniciarsesion)
        storageRef = FirebaseStorage.getInstance().reference
    }

    //Método para cerrar el menú NAVIGATIONDRAWER
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    //Controla lo que se hace al pulsar cada ítem del menú NAVIGATIONDRAWER
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
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

    //Iniciar sesión anónima para que se pueda usar la app aunque nadie haya iniciado sesión, se crean usuarios anónimos
    private fun signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously()
    }

    private fun recogerIntent() {
        val intent = intent.extras
        emailUser = intent?.getString("email").toString()
    }

    private fun enviarIntent(clase: Class<*>) {
        intent = Intent(applicationContext, clase)
        //Cerrar el menú
        onBackPressed()
        startActivity(intent)
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
}