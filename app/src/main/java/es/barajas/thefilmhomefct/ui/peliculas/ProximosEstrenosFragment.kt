package es.barajas.thefilmhomefct.ui.peliculas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.ImageButton
import androidx.recyclerview.widget.*
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.Utilidades
import es.barajas.thefilmhomefct.adapter.RV_AdaptadorPeliculas
import es.barajas.thefilmhomefct.data.ListaPeliculas

class ProximosEstrenosFragment() : Fragment() {
    private var listaPeliculas: ArrayList<ListaPeliculas>? = null
    private lateinit var btnVistas: ImageButton
    private var boton_pulsado: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_proximos_estrenos, container, false)
        //----RECYCLERVIEW con los títulos e imágenes de las películas--------
        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerview_estrenos_grid)
        recyclerView.setHasFixedSize(true)
        btnVistas = root.findViewById(R.id.btnVistas_Estrenos)
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
        }
        else {
            recyclerView.layoutManager = GridLayoutManager(context, 2)
        }
        llenarPeliculas()
        val adapter = RV_AdaptadorPeliculas(listaPeliculas)
        recyclerView.adapter = adapter
    }

    private fun llenarPeliculas(){
        listaPeliculas!!.add(
            ListaPeliculas(
                "Space Jam: A New Legacy",
                "peliculasEstrenos",
                "proximosEstrenos/spacejam.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "Viuda Negra",
                "peliculasEstrenos",
                "proximosEstrenos/viudanegra.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "Mortal Kombat",
                "peliculasEstrenos",
                "proximosEstrenos/mortalkombat.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "Detective Conan: La bala escarlata",
                "peliculasEstrenos",
                "proximosEstrenos/detectiveconan.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "¡Upsss!2 ¿Y ahora dónde está Noé?",
                "peliculasEstrenos",
                "proximosEstrenos/ooops.jpg"
            )
        )
        listaPeliculas!!.add(
            ListaPeliculas(
                "Cruella",
                "peliculasEstrenos",
                "proximosEstrenos/cruella.jpg"
            )
        )
    }
}