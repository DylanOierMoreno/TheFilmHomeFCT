package es.barajas.thefilmhomefct.ui.iniciar_sesion

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.adapter.RV_AdaptadorEntradas
import es.barajas.thefilmhomefct.data.ListaEntradas
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.*

class EntradasFragment : Fragment() {
    private var listaEntradas: ArrayList<ListaEntradas>? = null
    private lateinit var txt_consulta_tus_entradas: TextView
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    //Instancia de usuario activo de la BBDD
    private val user = Firebase.auth.currentUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_entradas, container, false)
        txt_consulta_tus_entradas = root.findViewById(R.id.txt_consulta_tus_entradas)
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerview_entradas)
        recyclerView.setHasFixedSize(true)
        construirRecycler(recyclerView)
        return root
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UNCHECKED_CAST")
    @SuppressLint("SimpleDateFormat")
    private fun construirRecycler(recyclerView: RecyclerView) {
        listaEntradas = ArrayList()
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        db.collection("usuarios").document(user?.email!!).get().addOnCompleteListener {
            val document: DocumentSnapshot = it.result!!
            val listaBBDD = document.data!!["lista_entradas"] as ArrayList<HashMap<String, Any>>
            for (i in 0 until listaBBDD.size) {
                val titulo_pelicula: String = listaBBDD[i]["nombrePelicula"].toString()
                val cine: String = listaBBDD[i]["nombreCine"].toString()
                val fecha: String = listaBBDD[i]["fechaSesion"].toString()
                listaEntradas!!.add(ListaEntradas(titulo_pelicula, cine,
                    SimpleDateFormat("dd MMMM yyyy", Locale("es", "ES")).
                    format(SimpleDateFormat("ddMMyyyyy").parse(fecha))))
            }
            if (listaBBDD.size > 0) {
                txt_consulta_tus_entradas.visibility = View.GONE
            }
            //Pasamos al adaptador la lista ccompleta de entradas
            val adapter = RV_AdaptadorEntradas(listaEntradas)
            recyclerView.adapter = adapter
        }
    }
}