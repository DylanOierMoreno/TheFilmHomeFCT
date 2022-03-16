package es.barajas.thefilmhomefct.adapter

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import es.barajas.thefilmhomefct.ui.peliculas.DescripcionPeliculaFragment
import es.barajas.thefilmhomefct.ui.peliculas.DetallePelicula
import es.barajas.thefilmhomefct.ui.peliculas.HorarioPeliculaFragment

class VP_AdaptadorDetallePeliculas (fr: FragmentActivity, pelicula: String) : FragmentStateAdapter(fr){
    private val directorioPelicula = pelicula

    override fun getItemCount(): Int {
        return if(directorioPelicula.equals("peliculasCartelera")) {
            2
        } else {
            1
        }
    } //Hay 2 páginas (horarios e información)

    override fun createFragment(position: Int): Fragment {
        if(directorioPelicula.equals("peliculasCartelera")){
            return when (position){
                0 -> HorarioPeliculaFragment(DetallePelicula.nombrePelicula)
                1 -> DescripcionPeliculaFragment(DetallePelicula.directorioPelicula, DetallePelicula.nombrePelicula)
                else -> HorarioPeliculaFragment(DetallePelicula.nombrePelicula)
            }
        }
        else{
            return DescripcionPeliculaFragment(DetallePelicula.directorioPelicula, DetallePelicula.nombrePelicula)
        }
    }
}