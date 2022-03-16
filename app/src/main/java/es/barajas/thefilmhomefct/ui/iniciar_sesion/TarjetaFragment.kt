package es.barajas.thefilmhomefct.ui.iniciar_sesion

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.*
import android.text.Html
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.*
import com.google.zxing.*
import com.google.zxing.qrcode.QRCodeWriter
import es.barajas.thefilmhomefct.R

@Suppress("UNCHECKED_CAST")
class TarjetaFragment : Fragment() {
    private lateinit var imv_codigoQR: ImageView
    private lateinit var btn_pedirTarjeta: Button
    private lateinit var btn_consultarCondiciones: Button
    private lateinit var txt_nombreUsuario: TextView
    private lateinit var txt_numTarjetaTFH: TextView
    private lateinit var txt_puntosTFH: TextView
    private lateinit var chk_condicionesTFH: CheckBox
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    //USUARIO DE FIREBASE
    private val user = FirebaseAuth.getInstance().currentUser
    private var privacidad = false
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private lateinit var bitmap: Bitmap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_tarjeta, container, false)
        inicializarControles(root)
        return root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()
        leerDatosUsuario(context)
        consultarCondiciones()
    }

    private fun inicializarControles(root: View) {
        imv_codigoQR = root.findViewById(R.id.imv_codigoQR)
        btn_pedirTarjeta = root.findViewById(R.id.btn_pedirTarjeta)
        btn_consultarCondiciones = root.findViewById(R.id.btn_consultarCondiciones)
        txt_nombreUsuario = root.findViewById(R.id.txt_nombreUsurio)
        txt_numTarjetaTFH = root.findViewById(R.id.txt_numTarjetaTHH)
        txt_puntosTFH = root.findViewById(R.id.txt_puntosTFH)
        chk_condicionesTFH = root.findViewById(R.id.chkCondicionesTFH)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun pedirTarjeta() {
        btn_pedirTarjeta.setOnClickListener {
            if (privacidad) {
                val tarjetaTFH = TarjetaTFH(requireContext())
                tarjetaTFH.leerCodigoTarjeta()
                Thread.sleep(1000)
                generarQR(tarjetaTFH)
                btn_pedirTarjeta.isEnabled = false
                btn_pedirTarjeta.setBackgroundColor(Color.rgb(213, 212, 212))
                chk_condicionesTFH.isEnabled = false
            } else {
                mostrarMensaje("aceptar privacidad", context)
            }
        }
    }

    //Generar código QR
    private fun generarQR(tarjetaTFH: TarjetaTFH) {
        db.collection("usuarios").document(user!!.email!!).get().addOnCompleteListener {
            val document: DocumentSnapshot = it.result!!
            val lista_datosPersonales: MutableMap<String, Any> = document["lista_datosPersonales"] as MutableMap<String, Any>
            val nombre = lista_datosPersonales["nombre"] as String
            val apellido = lista_datosPersonales["apellidos"] as String
            val texto = nombre + apellido + tarjetaTFH.getNumTarjeta()
            val writer = QRCodeWriter()
            try {
                val bitMatrix = writer.encode(texto, BarcodeFormat.QR_CODE, imv_codigoQR.width, imv_codigoQR.height)
                bitmap = Bitmap.createBitmap(bitMatrix.width, bitMatrix.height, Bitmap.Config.RGB_565)
                for (i in 0 until bitMatrix.width) {
                    for (j in 0 until bitMatrix.height) {
                        bitmap.setPixel(i, j, if (bitMatrix.get(i, j)) Color.BLACK else Color.rgb(205, 204, 204))
                    }
                }
                imv_codigoQR.setImageBitmap(bitmap)
                guardarTarjetaBBDD(tarjetaTFH)
            } catch (e: WriterException) {
                e.printStackTrace()
            }
            ponerTexto(txt_nombreUsuario, "$nombre $apellido")
            ponerTexto(txt_numTarjetaTFH, tarjetaTFH.getNumTarjeta())
            ponerTexto(txt_puntosTFH, tarjetaTFH.getPuntos().toString() + " puntos")
        }
    }

    private fun guardarTarjetaBBDD(tarjetaTFH: TarjetaTFH) {
        db.collection("usuarios").document(user!!.email!!).update("tarjetaTFH", tarjetaTFH)
    }

    private fun ponerTexto(cajaTexto: TextView, texto: String) { cajaTexto.text = texto }

    private fun leerDatosUsuario(context: Context?) {
        db.collection("usuarios").document(user!!.email!!).get().addOnCompleteListener {
            val document: DocumentSnapshot = it.result!!
            val mapa_tarjetaTFH: Map<String, Any> = document.data!!["tarjetaTFH"] as Map<String, Any>
            val numTarjeta = mapa_tarjetaTFH["numTarjeta"] as String
            if (numTarjeta.isNotEmpty()) {//El usurio TIENE la TARJETA
                val mapa_datosPersonales: Map<String, Any> =
                    document.data!!["lista_datosPersonales"] as Map<String, Any>
                val nombre = mapa_datosPersonales["nombre"] as String
                val apellido = mapa_datosPersonales["apellidos"] as String
                ponerTexto(txt_nombreUsuario, "$nombre $apellido")
                ponerTexto(txt_numTarjetaTFH, numTarjeta)
                ponerTexto(txt_puntosTFH, mapa_tarjetaTFH["puntos"].toString() + " puntos")
                val tarjetaTFH = TarjetaTFH(requireContext(), numTarjeta, mapa_tarjetaTFH["puntos"].toString().toInt())
                generarQR(tarjetaTFH)
                btn_pedirTarjeta.isEnabled = false
                btn_pedirTarjeta.setBackgroundColor(Color.rgb(213, 212, 212))
                chk_condicionesTFH.isEnabled = false
            } else {
                mostrarMensaje("noSocio", context)
                aceptarCondiconesTFH()
            }
        }
    }

    //Comprueba si se han haceptado las condiciones de privacidad de la tarjeta TFH. Si no se aceptan, no se puede solicitar
    private fun aceptarCondiconesTFH(){
        chk_condicionesTFH.setOnClickListener {
            //Si se ACEPTAN las condiciones, se puede PEDIR la tarjeta
            //Si NO se ACEPTAN las condiciones, NO se puede PEDIR la tarjeta TFH
            privacidad = chk_condicionesTFH.isChecked
        }
        pedirTarjeta()
    }

    //Pone a la escucha el botón para consultar las condiciones de privacidad de la tarjeta TFH
    @RequiresApi(Build.VERSION_CODES.N)
    private fun consultarCondiciones(){
        btn_consultarCondiciones.setOnClickListener { leerPrivacidadBBDD() }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun leerPrivacidadBBDD(){
        db.collection("privacidad").document("tarjetaTFH").get().addOnSuccessListener {
            val politica = it.get("usoTarjeta") as String
            mostrarMensajePrivacidad(politica)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun mostrarMensaje(mensaje: String, context: Context?) {
        val builder = AlertDialog.Builder(context!!)
        builder.setPositiveButton("Aceptar", null)
        when (mensaje) {
            "noSocio" -> {
                builder.setTitle("¡Regístrate!")
                builder.setMessage("¡Pulsa PIDE AQUÍ TU TARJETA y acumula puntos en todas tus compras!")
            }
            "aceptar privacidad" -> {
                builder.setTitle("Política de Privacidad")
                builder.setMessage("Debes consultar el aviso legal y aceptar la POLÍTICA de PRIVACIDAD")
            }
            "condiciones privacidad" -> {
                builder.setTitle("Política de Privacidad")
                builder.setMessage("En cumplimiento de lo establecido en la normativa de protección " +
                        "de datos española y, en particular, en el artículo 5 de la Ley Orgánica " +
                        "15/1999, de 13 de diciembre, de Protección de Datos de Carácter Personal " +
                        "se informa al Usuario que los datos de carácter personal que proporcione a " +
                        "través de la aplicación móvil de TFH (en adelante, la ”Aplicación”), " +
                        "así como los datos a los que se acceda como consecuencia de su navegación, " +
                        "solicitudes o utilización de las mismas (en adelante, los ”Datos de " +
                        "Carácter Personal”), serán recogidos en un fichero cuyo responsable es " +
                        "The Film Home.//n" +
                        "Las finalidades de la recogida de los Datos de Carácter Personal a través de la son  las siguientes: //n" +
                        "//t(i)             Dar de alta una Cuenta Usuario TFH. //n" +
                        "//t (ii)           Gestionar su Cuenta Usuario TFH. //n" +
                        "//t (iii)          Permitir al Usuario darse de alta como socio para solicitar la ”Tarjeta TFH”.//n" +
                        "//t (iv)          Gestionar la compra de entradas de cine u otros productos o servicios ofrecidos por TFH.//n" +
                        "//t (v)           Personalizar y mejorar las herramientas y funcionalidades del Web o de la Aplicación.//n")
            }
        }
        val dialogo: AlertDialog = builder.create()
        dialogo.show()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun mostrarMensajePrivacidad(texto: String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Aceptar", null)
        builder.setTitle("Política de Privacidad y Uso")
        builder.setMessage(Html.fromHtml(texto, 1))
        val dialogo: AlertDialog = builder.create()
        dialogo.show()
    }
}