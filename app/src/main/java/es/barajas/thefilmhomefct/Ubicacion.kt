@file:Suppress("UNCHECKED_CAST")

package es.barajas.thefilmhomefct

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import es.barajas.thefilmhomefct.ui.iniciar_sesion.Autenticacion
import kotlin.collections.ArrayList

class Ubicacion : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    //Variables para el menú
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView
    private lateinit var headerView: View
    private lateinit var navIniciarSesion: TextView
    private lateinit var navUserMail: TextView
    private lateinit var navUserPhoto: ShapeableImageView
    private lateinit var spinner_provincias: Spinner
    private lateinit var spinner_cine: Spinner
    private lateinit var btnGuardarUbicacion: Button
    private lateinit var autenticacion: Autenticacion
    //USUARIO DE FIREBASE
    private val user = FirebaseAuth.getInstance().currentUser
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    private lateinit var storageRef: StorageReference
    private lateinit var provincia: String
    private lateinit var cine_preferencia: String
    private val array_provincias: Array<String> = arrayOf("Seleccione", "Madrid", "Barcelona", "Cadiz")
    private var esCargada = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion)
        inicializarVariables()
        spinner_cine.isEnabled = false
        //-----TOOLBAR----
        toolbar.navigationIcon?.setTint(Color.WHITE)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //----MENÚ NAVIGATIONDRAWAER
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        navView.setNavigationItemSelectedListener(this)
        navView.menu.findItem(R.id.nav_ubicacion).isChecked = true
        //Adaptador del spinner de provincias
        val adaptadorProvincias: ArrayAdapter<*> = ArrayAdapter(spinner_provincias.context,
            android.R.layout.simple_spinner_dropdown_item, array_provincias)
        spinner_provincias.adapter = adaptadorProvincias
        onItemSelectedListener_spinnerProvincias()
        onItemSelectedListener_spinnerCines()
        onClickListener_btnGuardar()
    }

    override fun onResume() {
        super.onResume()
        autenticacion = Autenticacion(navIniciarSesion, navUserMail, this)
        when {
            user == null -> {
                signInAnonymously()
            }
            user.isAnonymous -> {
                spinner_provincias.isEnabled = false
                mostrarAlerta("sesion")
                navUserPhoto.setImageResource(R.drawable.ic_account_circle)
                navUserPhoto.setBackgroundColor(Color.parseColor("#00FFFFFF"))
            }
            else -> {
                autenticacion.getUserInfo()
                if (!esCargada) descargarPerfilBBDD()
                spinner_provincias.isEnabled = true
            }
        }
        autenticacion.onClick()
        navUserPhoto.setOnClickListener { if (!user!!.isAnonymous) { cargarImagen() } }
    }

    private fun inicializarVariables() {
        spinner_provincias = findViewById(R.id.spinner_provincias)
        spinner_cine = findViewById(R.id.spinner_cine)
        btnGuardarUbicacion = findViewById(R.id.btnGuardarUbicacion)
        toolbar = findViewById(R.id.toolbar_ubicacion)
        drawerLayout = findViewById(R.id.drawer_layout_ubicacion)
        navView = findViewById(R.id.nav_view_ubicacion)
        headerView = navView.getHeaderView(0)
        navIniciarSesion = headerView.findViewById(R.id.nav_iniciarSesion)
        navUserMail = headerView.findViewById(R.id.nav_userMail)
        navUserPhoto = headerView.findViewById(R.id.nav_userPhoto)
        storageRef = FirebaseStorage.getInstance().reference
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
                //inicia la pantalla de inicio.
                intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                return true
            }
        }
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    //Iniciar sesión anónima para que se pueda usar la app aunque nadie haya iniciado sesión, se crean usuarios anónimos
    private fun signInAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously()
    }

    private fun onItemSelectedListener_spinnerProvincias() {
        spinner_provincias.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            /* Evento que responde al seleccionar un ítem del SPINNER
               Al elegir una provincia (SPINNER1), mostraremos solo los cines que correspondan a esa
               provincia (SPINNER2) */
            @Suppress("UNCHECKED_CAST")
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                //Habilitamos el spinner de los cines cuando el usuario haya seleccionado alguna provincia, la posición 0 es "Seleccione"
                spinner_cine.isEnabled = position > 0
                provincia = position.toString()
                val array_cines: ArrayList<String> = arrayListOf("Seleccione")
                if (position > 0) { //Cuando haya alguna provoncia seleccionada, buscamos en la BBDD
                    db.collection("provincias").document(array_provincias[position]).get().addOnSuccessListener {
                        //Leemos la lista de cines de la provincia correspondiente en la BBDD, esta lista es una lista de referencias
                        val cines = it.get("listaCines") as List<DocumentReference>
                        llenarListaCines(cines, array_cines)
                        //Adaptador del spinner de cines
                        val adaptadorCines: ArrayAdapter<*> = ArrayAdapter(spinner_cine.context,
                            android.R.layout.simple_spinner_dropdown_item, array_cines)
                        spinner_cine.adapter = adaptadorCines //Asignamos el adaptador al spinner
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun llenarListaCines(cines: List<DocumentReference>, array_cines: ArrayList<String>) {
        for (cine in cines) { //Recorremos la lista de cines elemento a elemento
            //Usamos la referencia para poder leer de la BBDD la información de cada uno de los cines
            cine.get().addOnSuccessListener {
                if (it.exists()) {
                    //Si la referencia existe entonces obtendremos el nombre del cine
                    val nombre = it.getString("nombre") as String
                    array_cines.add(nombre) //Añadimos los nombres a la lista de cines
                }
            }
        }
    }

    private fun onItemSelectedListener_spinnerCines() {
        spinner_cine.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                //Si el usuario a seleccionado algún cine se habilitará el botón para guardar la ubicación
                btnGuardarUbicacion.isEnabled = position > 0
                if (btnGuardarUbicacion.isEnabled) {
                    btnGuardarUbicacion.setBackgroundColor(Color.rgb(92, 92, 92))
                } else {
                    btnGuardarUbicacion.setBackgroundColor(Color.rgb(213, 212, 212))
                }
                cine_preferencia = (position - 1).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun onClickListener_btnGuardar() {
        //Actualizamos la lista_datosPersonales para guardar la provincia y el cine que haya elegido el usuario
        btnGuardarUbicacion.setOnClickListener {
            db.collection("usuarios").document(user!!.email!!).get().addOnCompleteListener {
                val document = it.result!!
                val mapa_datosPersonales: MutableMap <String, Any> = document.data!!["lista_datosPersonales"]
                        as MutableMap <String,Any>
                mapa_datosPersonales["provincia"] = provincia
                mapa_datosPersonales["cine"] = cine_preferencia
                db.collection("usuarios").document(user.email!!)
                    .update(hashMapOf("lista_datosPersonales" to mapa_datosPersonales) as Map<String, Any>)
                mostrarAlerta("guardar")
            }
        }
    }

    private fun enviarIntent(clase: Class<*>) {
        intent = Intent(applicationContext, clase)
        /* Cerrar el menú para que si se retrocede con la flecha atrás, se pueda ver
           el layout de sesion iniciada en lugar del NAVIGATIONDRAWER */
        onBackPressed()
        startActivity(intent)
    }

    //Muestra una alerta con el error que llegue por parámetro
    private fun mostrarAlerta(alerta: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Información")
        builder.setPositiveButton("Aceptar", null)
        when (alerta) {
            "sesion" -> builder.setMessage("Para poder guardar tu ubicación, primero tienes que iniciar sesión")
            "guardar" -> builder.setMessage("Tu ubicación se ha guardado satisfactoriamente")
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
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
        //Sabiendo las rutas, descargamos cada foto byte a byte en este bitmap
        storageRef.child("imagenPerfil").child(user!!.email!!).getBytes(1024 * 1024).addOnSuccessListener {
            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            navUserPhoto.setImageBitmap(bitmap)
            //Poner fondo de color blanco
            navUserPhoto.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }.addOnFailureListener {
            navUserPhoto.setImageResource(R.drawable.ic_account_circle)
            navUserPhoto.setBackgroundColor(Color.parseColor("#00FFFFFF"))
            System.out.println("========ERROR!!!!!======== " + it)
        }
    }
}