package es.barajas.thefilmhomefct.adapter

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import es.barajas.thefilmhomefct.ui.peliculas.CarteleraFragment
import es.barajas.thefilmhomefct.ui.peliculas.ProximosEstrenosFragment

class VP_AdaptadorPeliculas(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                CarteleraFragment()
            }
            1 -> {
                ProximosEstrenosFragment()
            }
            else -> CarteleraFragment()
        }
    }
}