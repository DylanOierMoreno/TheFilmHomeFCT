package es.barajas.thefilmhomefct.ui.iniciar_sesion

import android.app.DatePickerDialog
import android.content.Intent
import android.os.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import es.barajas.thefilmhomefct.MainActivity
import es.barajas.thefilmhomefct.R
import java.util.*
import kotlin.collections.ArrayList

// Este fragment recibe por parámetro el email de la persona que ha iniciado sesión
@Suppress("UNCHECKED_CAST")
class MisDatosFragment(emailUser: String) : Fragment() {
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    //USUARIO DE FIREBASE
    private val user = FirebaseAuth.getInstance().currentUser
    //Email del usuario que ha iniciado sesión y que llega por parámetro
    private val emailParametro = emailUser
    private lateinit var ib_editNombre: ImageButton
    private lateinit var ib_editApellidos: ImageButton
    private lateinit var ib_editFechaNac: ImageButton
    private lateinit var ib_editGenero: ImageButton
    private lateinit var ib_guardarCuenta: ImageButton
    private lateinit var ib_guardarDatosPersonales: ImageButton
    private lateinit var ib_guardarPrefCine: ImageButton
    private lateinit var ib_provincia: ImageButton
    private lateinit var ib_cine: ImageButton
    private lateinit var et_userNombre: EditText
    private lateinit var et_userApellidos: EditText
    private lateinit var et_userFechaNac: EditText
    private lateinit var et_userEmail: EditText
    private lateinit var et_userPassword: EditText
    private lateinit var et_userNumDoc: EditText
    private lateinit var tv_tipoDocumento: TextView
    private lateinit var tv_numeroDocumento: TextView
    private lateinit var rg_generoConsulta: RadioGroup
    private lateinit var rb_MasculinoConsulta: RadioButton
    private lateinit var rb_FemeninoConsulta: RadioButton
    private lateinit var rb_NoBinarioConsulta: RadioButton
    private lateinit var btnCerrarSesion: Button
    private lateinit var spinner_provincias: Spinner
    private lateinit var spinner_cine: Spinner
    //variable para marcar el RadioButton de género escogido por el usuario en el registro
    private lateinit var indice_Genero: String
    //RADIOBUTTON
    private lateinit var radioButtonMarcado: View
    private var posicion_provincia = "0"
    private var posicion_cine = "0"
    //Lista de provincias, para gestionar la BBDD
    private val array_provincias: Array<String> = arrayOf("", "Madrid", "Barcelona", "Cadiz")
    private lateinit var root: View

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        root = inflater.inflate(R.layout.fragment_misdatos, container, false)
        //------------Inicialización de COMPONENTES
        inicializarImageButton()
        inicializarEditText()
        tv_tipoDocumento = root.findViewById(R.id.tv_tipoDocConsulta)
        tv_numeroDocumento = root.findViewById(R.id.tv_numeroDocConsulta)
        inicializarRadioGroup()
        btnCerrarSesion = root.findViewById(R.id.btnCerrarSesion)
        spinner_provincias = root.findViewById(R.id.spinner_provincias)
        spinner_cine = root.findViewById(R.id.spinner_cine)
        //-----------------------------------------------------------------------
        //2. Se EDITAN los datos
        ib_editNombre.setOnClickListener {
            //activar caja de texto nombre, deactivar ib_Nombre, activar ib_guardarCuenta
            activarControl(et_userNombre, ib_editNombre)
        }
        ib_editApellidos.setOnClickListener {
            activarControl(et_userApellidos, ib_editApellidos)
        }
        ib_editFechaNac.setOnClickListener {
            //Ponemos a la escucha et_userFechaNac que debe poder reaccionar para elegir la nueva fecha
            activarControl(et_userFechaNac, ib_editFechaNac)
            et_userFechaNac.setOnClickListener { escogerFechaNacimiento() }
        }
        ib_editGenero.setOnClickListener {
            activarGenero(true)
        }
        ib_provincia.setOnClickListener {
            activarControl(spinner_provincias, ib_provincia)
            activarControl(spinner_cine, ib_cine)
            editarProvinciaBBDD()
            editarPrefCineBBDD()
        }
        ib_cine.setOnClickListener {
            activarControl(spinner_cine, ib_cine)
            editarPrefCineBBDD()
        }
        //Ponemos a la ESCUCHA los botones para GUARDAR
        ib_guardarCuenta.setOnClickListener {
            //Si las 3 cajas de texto están desactivadas en este momento es porque no se ha editado nada
            if (!et_userNombre.isEnabled && !et_userApellidos.isEnabled && !et_userFechaNac.isEnabled) {
                mostrarMensajeCambioContenido("ninguno")
            } //Si alguna de las cajas está activa, se ha editado algo
            if (et_userNombre.isEnabled) {
                actualizarCampo("nombre", et_userNombre.text.toString())
                //Activamos de nuevo el botón editar (para poder editar) y desactivamos la caja de texto
                activarControl(ib_editNombre, et_userNombre)
            }
            if (et_userApellidos.isEnabled) {
                actualizarCampo("apellidos", et_userApellidos.text.toString())
                //Activamos de nuevo el botón editar (para poder editar) y desactivamos la caja de texto
                activarControl(ib_editApellidos, et_userApellidos)
            }
            if (et_userFechaNac.isEnabled) {
                actualizarCampo("fechaNac", et_userFechaNac.text.toString())
                //Activamos de nuevo el botón editar (para poder editar) y desactivamos la caja de texto
                activarControl(ib_editFechaNac, et_userFechaNac)
            }
        }
        //Guardar DATOS PERSONALES -> genero
        ib_guardarDatosPersonales.setOnClickListener {
            //Si rb_FemeninoConsulta está activo, es porque los otros dos también porque se activan y desactivan a la vez
            if (rb_FemeninoConsulta.isEnabled) {
                actualizarCampo("genero",
                    rg_generoConsulta.indexOfChild(obtenerGeneroMarcado()).toString())
                mostrarMensajeCambioContenido("genero")
                activarGenero(false)
            } else { //Si no se ha modificado nada, avisamos
                mostrarMensajeCambioContenido("ninguno")
            }
        }
        ib_guardarPrefCine.setOnClickListener {
            actualizarCampo("cine", posicion_cine)
            activarControl(ib_cine, spinner_cine)
            if (spinner_provincias.isEnabled) {
                actualizarCampo("provincia", posicion_provincia)
                activarControl(ib_provincia, spinner_provincias)
            }
        }
        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener {
                Autenticacion.isClose = true
                val intent = Intent(context, MainActivity::class.java)
                context?.startActivity(intent)
            }.addOnFailureListener {
                Toast.makeText(context, "no se ha iniciado SESION ANONIMA", Toast.LENGTH_SHORT).show()
            }
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        //1. Se MUESTRAN los datos personales que el usuario indicó en el registro
        recuperarDatosPersonales()
    }

    private fun inicializarRadioGroup() {
        rg_generoConsulta = root.findViewById(R.id.rg_userGeneroConsulta)
        rb_MasculinoConsulta = root.findViewById(R.id.rb_MasculinoConsulta)
        rb_FemeninoConsulta = root.findViewById(R.id.rb_FemeninoConsulta)
        rb_NoBinarioConsulta = root.findViewById(R.id.rb_NoBinarioConsulta)
    }

    private fun inicializarEditText() {
        et_userNombre = root.findViewById(R.id.et_userNombreConsulta)
        et_userApellidos = root.findViewById(R.id.et_userApellidosConsulta)
        et_userFechaNac = root.findViewById(R.id.et_userFechaNacConsulta)
        et_userEmail = root.findViewById(R.id.et_userMailConsulta)
        et_userPassword = root.findViewById(R.id.et_userPasswordConsulta)
        et_userNumDoc = root.findViewById(R.id.et_userNumeroDocConsulta)
    }

    private fun inicializarImageButton() {
        ib_editNombre = root.findViewById(R.id.ib_editarNombre)
        ib_editApellidos = root.findViewById(R.id.ib_editarApellidos)
        ib_editFechaNac = root.findViewById(R.id.ib_editarFechaNac)
        ib_editGenero = root.findViewById(R.id.ib_editarGenero)
        ib_guardarCuenta = root.findViewById(R.id.ib_guardarCuenta)
        ib_guardarDatosPersonales = root.findViewById(R.id.ib_guardarDatosPersonales)
        ib_guardarPrefCine = root.findViewById(R.id.ib_guardarPrefCine)
        ib_provincia = root.findViewById(R.id.ib_provincia)
        ib_cine = root.findViewById(R.id.ib_cine)
    }

    //Función para leer de la BBDD Firebase los datos guardados de esta persona
    private fun recuperarDatosPersonales() {
        db.collection("usuarios").document(user!!.email!!).get().addOnCompleteListener {
            val document: DocumentSnapshot = it.result!!
            if (it.isSuccessful && document.exists()) {
                val mapaDatosPersonales: Map<String, Any> =
                    document.data!!["lista_datosPersonales"] as Map<String, Any>
                et_userNombre.setText(mapaDatosPersonales["nombre"] as String?)
                et_userApellidos.setText(mapaDatosPersonales["apellidos"] as String?)
                et_userFechaNac.setText(mapaDatosPersonales["fechaNac"] as String?)
                et_userEmail.setText(mapaDatosPersonales["email"] as String?)
                tv_tipoDocumento.text = mapaDatosPersonales["tipoDoc"] as String?
                tv_numeroDocumento.text = mapaDatosPersonales["numDoc"] as String?
                et_userPassword.setText(mapaDatosPersonales["password"] as String?)
                indice_Genero = mapaDatosPersonales["genero"] as String
                marcarGenero(indice_Genero)
                posicion_provincia = mapaDatosPersonales["provincia"] as String
                posicion_cine = mapaDatosPersonales["cine"] as String
                Thread.sleep(4000)
                spinners_ubicacion()
            }
        }
    }

    //lee de la BBDD la provincia y el cine que tiene guardadas el usuario
    private fun spinners_ubicacion() {
        spinner_provincias.isEnabled = false
        spinner_cine.isEnabled = false
        //Adaptador del spinner de provincias
        val adaptadorProvincias: ArrayAdapter<*> = ArrayAdapter(spinner_provincias.context,
            android.R.layout.simple_spinner_dropdown_item, array_provincias)
        spinner_provincias.adapter = adaptadorProvincias //Asignamos el adaptador al spinner
        leerCinesBBDD()
    }

    private fun leerCinesBBDD() {
        //Leemos
        var adaptadorCines: ArrayAdapter<*>
        if (posicion_provincia.toInt() > 0) {
            db.collection("provincias").document(array_provincias[posicion_provincia.toInt()]).get().addOnSuccessListener { it ->
                //Leemos la lista de cines de la BBDD, esta lista es una lista de referencias
                val cines = it.get("listaCines") as List<DocumentReference>
                val array_cines: ArrayList<String> = ArrayList()
                for (cine in cines) { //Recorremos la lista de cines elemento a elemento
                    //Usamos la referencia para poder leer de la BBDD la información de cada uno de los cines
                    cine.get().addOnSuccessListener {
                        if (it.exists()) {
                            //Si la referencia existe entonces obtendremos el nombre del cine
                            val nombre = it.getString("nombre") as String
                            array_cines.add(nombre)
                            if ((array_cines.size == 3 && posicion_provincia.toInt() == 1) || (array_cines.size == 2 &&
                                        posicion_provincia.toInt() == 2) || (array_cines.size == 1 &&
                                        posicion_provincia.toInt() == 3)) {
                                //Adaptador para llenar el spinner de cines
                                adaptadorCines = ArrayAdapter(spinner_cine.context, android.R.layout.simple_spinner_dropdown_item, array_cines)
                                spinner_cine.adapter = adaptadorCines //Asignamos el adaptador al spinner
                                seleccionarItems()
                            }
                        }
                    }
                }
            }
        } else { //Si posicion_provincia = 0 en el spinner de cine no habrá nada seleccionado y tampoco en el spinner de cines
            adaptadorCines = ArrayAdapter(spinner_cine.context,
                android.R.layout.simple_spinner_dropdown_item, arrayListOf(""))
            spinner_cine.adapter = adaptadorCines //Asignamos el adaptador al spinner
            seleccionarItems()
        }
    }

    private fun seleccionarItems() {
        spinner_provincias.setSelection(posicion_provincia.toInt())
        spinner_cine.setSelection(posicion_cine.toInt())
    }

    private fun editarPrefCineBBDD() {
        spinner_cine.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                posicion_cine = position.toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    //Solo edita en la BBDD la PROVINCIA
    private fun editarProvinciaBBDD() {
        spinner_provincias.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                posicion_provincia = position.toString()
                leerCinesBBDD()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun activarControl(controlActivar: View, controlDesactivar: View) {
        controlActivar.isEnabled = true
        controlDesactivar.isEnabled = false
    }

    private fun activarGenero(activar: Boolean) {
        rb_MasculinoConsulta.isEnabled = activar
        rb_FemeninoConsulta.isEnabled = activar
        rb_NoBinarioConsulta.isEnabled = activar
        ib_editGenero.isEnabled = !activar
    }

    //Función para marcar el RadioButton de género que haya escogido el usuario en el registro
    private fun marcarGenero(indiceRadioButton_Elegido: String) {
        rg_generoConsulta.check(rg_generoConsulta.getChildAt(indiceRadioButton_Elegido.toInt()).id)
    }

    // Actualiza la BBDD
    private fun actualizarCampo(campo_bbdd: String, dato: String) {
        db.collection("usuarios").document(user!!.email!!).get().addOnCompleteListener { it ->
            val document = it.result
            val mapa_datosPersonales: MutableMap<String, Any> =
                document!!.data!!["lista_datosPersonales"] as MutableMap<String, Any>
            mapa_datosPersonales[campo_bbdd] = dato
            db.collection("usuarios").document(user.email!!).update(
                hashMapOf("lista_datosPersonales" to mapa_datosPersonales) as Map<String, Any>).addOnCompleteListener {
                if (it.isSuccessful) { //Si se ha modificado bien, avisamos
                    mostrarMensajeCambioContenido(campo_bbdd)
                } else { //Si no se ha modificado bien, avisamos del error
                    mostrarErrorCambioContenido(campo_bbdd)
                }
            }
        }
    }

    //Obtenemos el RadioButton dentro del RadioGroup que ha sido marcado por el usuario mediante su id
    private fun obtenerGeneroMarcado(): View {
        //Capturamos el RadioButton marcado a través de su id
        radioButtonMarcado = rg_generoConsulta.findViewById(rg_generoConsulta.checkedRadioButtonId)
        return radioButtonMarcado
    }

    // Esta función se activa al pulsar sobre et_userFechaNacimiento,
    @RequiresApi(Build.VERSION_CODES.N)
    fun escogerFechaNacimiento() {
        val calendario = Calendar.getInstance() //Instancia de tipo Calendar
        val locale = Locale("es", "ES") //Mostrar el calendario en español
        Locale.setDefault(locale)
        //Mostrar el diálogo con DatePicker en formato Spinner, usamos DatePickerDialog
        val datePickerFragment = DatePickerDialog(
            requireContext(), R.style.dlg_datePicker, { _, year, month, day ->
                val fechaSeleccionada = day.toString() + "/" + (month + 1) + "/" + year
                et_userFechaNac.setText(fechaSeleccionada)
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
        datePickerFragment.show()
    }

    //Muestra mensajes de ERROR al actualizar datos
    private fun mostrarErrorCambioContenido(error: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setPositiveButton("Aceptar", null)
        when (error) {
            "nombre" -> builder.setMessage("Se ha producido un ERROR al modificar el NOMBRE")
            "apellidos" -> builder.setMessage("Se ha producido un ERROR al modificar los APELLIDOS")
            "fechaNac" -> builder.setMessage("Se ha producido un ERROR al modificar la FECHA DE NACIMIENTO")
            "genero" -> builder.setMessage("Se ha producido un ERROR al modificar el GÉNERO")
            "provincia" -> builder.setMessage("Se ha producido un ERROR al modificar la PROVINCIA")
            "cine" -> builder.setMessage("Se ha producido un ERROR al modificar el CINE")
        }
        val dialogo: AlertDialog = builder.create()
        dialogo.show()
    }

    //Muestra mensajes de RETROALIMENTACIÓN POSITIVA porque la actualización ha sido satisfactoria
    private fun mostrarMensajeCambioContenido(campoModificado: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Campo Modificado")
        builder.setPositiveButton("Aceptar", null)
        when (campoModificado) {
            "nombre" -> builder.setMessage("Se ha modificado el NOMBRE")
            "apellidos" -> builder.setMessage("Se han modificado los APELLIDOS")
            "fechaNac" -> builder.setMessage("Se ha modificado la FECHA DE NACIMIENTO")
            "genero" -> builder.setMessage("Se ha modificado el GÉNERO")
            "provincia" -> builder.setMessage("Se ha modificado la PROVINCIA")
            "cine" -> builder.setMessage("Se ha modificado el CINE")
            "ninguno" -> builder.setMessage("No has modificado ningún dato")
        }
        val dialogo: AlertDialog = builder.create()
        dialogo.show()
    }
}