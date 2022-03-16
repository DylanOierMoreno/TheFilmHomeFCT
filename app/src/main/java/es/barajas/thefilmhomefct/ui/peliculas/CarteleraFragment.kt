package es.barajas.thefilmhomefct.ui.peliculas

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.Utilidades
import es.barajas.thefilmhomefct.adapter.RV_AdaptadorPeliculas
import es.barajas.thefilmhomefct.data.ListaPeliculas

class CarteleraFragment() : Fragment() {
    private lateinit var recyclerView: RecyclerView
    var listaPeliculas: ArrayList<ListaPeliculas>? = null
    private var boton_pulsado = false
    private lateinit var btnVistas: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_cartelera, container, false)
        //-----RECYCLERVIEW con los títulos e imágenes de las películas-------
        recyclerView = root.findViewById(R.id.recyclerview_cartelera)
        recyclerView.setHasFixedSize(true)
        btnVistas = root.findViewById(R.id.btnVistas_Cartelera)
        construirRecycler(recyclerView)
        btnVistas.setOnClickListener {
            if (boton_pulsado) {
                boton_pulsado = false
                btnVistas.setImageResource(R.drawable.ic_list_view)
                Utilidades.visualizacion = Utilidades.GRID
                construirRecycler(recyclerView)
            } else {
                boton_pulsado = true
                btnVistas.setImageResource(R.drawable.ic_grid_view)
                Utilidades.visualizacion = Utilidades.LIST
                construirRecycler(recyclerView)
            }
        }
        return root
    }

    private fun construirRecycler(recyclerView: RecyclerView) {
        listaPeliculas = ArrayList()
        if(Utilidades.visualizacion == Utilidades.LIST){
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        } else {
            recyclerView.layoutManager = GridLayoutManager(context, 2)
        }
        llenarPeliculas()
        val adapter = RV_AdaptadorPeliculas(listaPeliculas)
        recyclerView.adapter = adapter
    }

    //Llenar la lista con los datos de cada ítem
    private fun llenarPeliculas(){
        listaPeliculas!!.add(
            ListaPeliculas(
                "Los Croods 2. Una nueva era",
                "peliculasCartelera",
                "cartelera/los_croods.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "El informe Auschwitz",
                "peliculasCartelera",
                "cartelera/informe_auschwitz.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "Gozilla vs. Kong",
                "peliculasCartelera",
                "cartelera/godzilla_vs_kong.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "Tom y Jerry",
                "peliculasCartelera",
                "cartelera/tom_y_jerry.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "Wonder Woman 1984",
                "peliculasCartelera",
                "cartelera/wonderwoman.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "Inmune",
                "peliculasCartelera",
                "cartelera/inmune.jpg"
            )
        )
    }
}