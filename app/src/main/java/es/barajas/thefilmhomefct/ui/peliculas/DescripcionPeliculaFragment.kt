package es.barajas.thefilmhomefct.ui.peliculas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import es.barajas.thefilmhomefct.R

class DescripcionPeliculaFragment(directorioPelicula: String, nombrePelicula: String): Fragment() {
    private lateinit var txtSinopsis: TextView
    private lateinit var txtDirector: TextView
    private lateinit var txtReparto: TextView
    private lateinit var txtPais: TextView
    private lateinit var txtEstreno: TextView
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()
    private val directorio = directorioPelicula
    private val pelicula = nombrePelicula

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_descripcion_pelicula, container, false)
        txtSinopsis = root.findViewById(R.id.txtSinopsisContenido)
        txtDirector = root.findViewById(R.id.txtDirectorContenido)
        txtReparto = root.findViewById(R.id.txtRepartoContenido)
        txtPais = root.findViewById(R.id.txtPaisContenido)
        txtEstreno = root.findViewById(R.id.txtEstrenoContenido)
        consultarPelicula()
        return root
    }

    /*  Consultamos los datos de la película sobre la que se ha hecho clic en la Cartelera
        Reemplazamos /n por \n */
    private fun consultarPelicula(){
        db.collection(directorio).document(pelicula).get().addOnSuccessListener {
            val sinopsis = it.get("sinopsis") as String
            txtSinopsis.text = sinopsis.replace("/n", "\n\n")
            val direcor = it.get("director") as String
            txtDirector.text = direcor.replace("/n", "\n")
            val reparto = it.get("reparto") as String
            txtReparto.text = reparto.replace("/n", "\n")
            val pais = it.get("pais") as String
            txtPais.text = pais.replace("/n", "\n")
            txtEstreno.text = it.get("estreno") as String
        }.addOnFailureListener {
            System.out.println("========ERROR al leer los datos de la película en DescripcionPeliculaFragment======== " + it)
        }
    }
}