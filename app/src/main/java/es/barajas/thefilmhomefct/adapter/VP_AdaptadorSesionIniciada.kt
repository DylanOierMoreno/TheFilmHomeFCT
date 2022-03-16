package es.barajas.thefilmhomefct.adapter

import androidx.fragment.app.*
import androidx.viewpager2.adapter.FragmentStateAdapter
import es.barajas.thefilmhomefct.ui.iniciar_sesion.EntradasFragment
import es.barajas.thefilmhomefct.SesionIniciada
import es.barajas.thefilmhomefct.ui.iniciar_sesion.MisDatosFragment
import es.barajas.thefilmhomefct.ui.iniciar_sesion.TarjetaFragment

class VP_AdaptadorSesionIniciada (fa: FragmentActivity) : FragmentStateAdapter(fa){
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
       return when (position){
           0 -> MisDatosFragment(recogerEmail(SesionIniciada.emailUser))
           1 -> EntradasFragment()
           2 -> TarjetaFragment()
           else -> MisDatosFragment(recogerEmail(SesionIniciada.emailUser))
       }
    }

    companion object{
        /* Función de clase para poder acceder a ella desde otras clases
        La utilizamos para recoger el Email de Login.kt y pasárselo en esta clase al Fragment
        MisDatosFragment() por parámetro.*/
         fun recogerEmail(emailUser: String): String{
            return emailUser
        }
    }
}