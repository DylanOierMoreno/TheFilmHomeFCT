package es.barajas.thefilmhomefct.adapter

import android.annotation.SuppressLint
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.barajas.thefilmhomefct.R

/* Esta clase controla el RecyclerView que se crea para listar el tipo de entradas compradas en
activity_pagar_entradas.xml de la clase PagarEntradas.kt */
//La clase tiene que HEREDAR de RecyclerView.Adapter
class RV_AdaptadorTipoEntradas() :  RecyclerView.Adapter<RV_AdaptadorTipoEntradas.ViewHolderTipoEntradas>() {
    private var lista_numEntradas: ArrayList<Int>? = null //Número de entradas compradas
    private lateinit var lista_tipoEntrada: ArrayList<String> //Tipo de entrada (joven, adulto, etc)

    constructor(lista_numEntradas: ArrayList<Int>, lista_tipoEntrada: ArrayList<String>) : this() {
        this.lista_numEntradas = lista_numEntradas
        this.lista_tipoEntrada = lista_tipoEntrada
    }

    //Se rellenan los elementos de items_tipo_entradas.xml
    class ViewHolderTipoEntradas(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var numEntradas: TextView? = itemView.findViewById(R.id.txtNumEntradas)
        var tipoEntrada: TextView? = itemView.findViewById(R.id.txtTipoEntrada)
        var precioEntrada: TextView? = itemView.findViewById(R.id.txtPrecioIndividual)
    }

    //Se infla items_tipo_entradas.xml
    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTipoEntradas {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.items_tipo_entradas,
            null, false)
        return ViewHolderTipoEntradas(view)
    }

    // Reemplaza el contenido de una vista de elementos de lista
    override fun onBindViewHolder(holder: ViewHolderTipoEntradas, position: Int) {
        holder.numEntradas?.setText(lista_numEntradas?.get(position)!!.toString())
        holder.tipoEntrada?.text = lista_tipoEntrada[position]
        holder.precioEntrada?.text = asignarPrecioEntrada(lista_tipoEntrada[position])
    }

    //Retorna el precio del tipo de entrada que reciba por parámetro
    private fun asignarPrecioEntrada(tipoEntrada: String): String {
        return if (tipoEntrada.equals("Adulto")) {
            "5.90€"
        } else {
            "5.40€"
        }
    }

    override fun getItemCount(): Int = lista_tipoEntrada.size
}