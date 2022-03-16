package es.barajas.thefilmhomefct.ui.inicio

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.*
import es.barajas.thefilmhomefct.R
import java.text.SimpleDateFormat
import java.util.*

class InicioFragment : Fragment() {
    private lateinit var inicioViewModel: InicioViewModel
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        inicioViewModel = ViewModelProvider(this).get(InicioViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_inicio, container, false)
        buscarSalas()
        return root
    }

    // Lee de la BBDD todas las salas y extrae su fecha y su referneica.
    private fun buscarSalas() {
        db.collection("sala").get().addOnCompleteListener {
            val document: QuerySnapshot = it.result!!
            document.forEach {
                val fecha = it.get("fecha") as String
                val referencia = it.get("referencia") as String
                compararFechas(fecha, referencia)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun compararFechas(diaLeidoBBDD: String, id: String) {
        val calendario = Calendar.getInstance()
        val df = SimpleDateFormat("ddMMyyyy")
        val diaLeido: Date = SimpleDateFormat("ddMMyyyy").parse(diaLeidoBBDD)!!
        val diaActual: Date = SimpleDateFormat("ddMMyyyy").parse(df.format(calendario.time))!!
        if (diaLeido.before(diaActual)) {
            db.collection("sala").document(id).delete().addOnSuccessListener {
            }.addOnFailureListener {
                Toast.makeText(context, "Error al BORRAR Sala", Toast.LENGTH_SHORT).show()
            }
        }
    }
}