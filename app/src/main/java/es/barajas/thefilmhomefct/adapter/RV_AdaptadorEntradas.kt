package es.barajas.thefilmhomefct.adapter

import android.annotation.SuppressLint
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.data.ListaEntradas

class RV_AdaptadorEntradas() : RecyclerView.Adapter<RV_AdaptadorEntradas.ViewHolderEntradas>() {
    private var listaEntradas: ArrayList<ListaEntradas>? = null

    constructor(listaEntradas: ArrayList<ListaEntradas>?) : this() {
        this.listaEntradas = listaEntradas
    }

    class ViewHolderEntradas(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titulo_pelicula: TextView? = itemView.findViewById(R.id.idTituloPelicula)
        var cine: TextView? = itemView.findViewById(R.id.idCine)
        var fecha: TextView? = itemView.findViewById(R.id.idFecha)
    }

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderEntradas {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.items_entradas,
            null, false)
        return ViewHolderEntradas(view)
    }

    override fun onBindViewHolder(holder: ViewHolderEntradas, position: Int) {
        holder.titulo_pelicula?.text = listaEntradas!![position].titulo_pelicula
        holder.cine?.text = listaEntradas!![position].cine
        holder.fecha?.text = listaEntradas!![position].fecha
    }

    override fun getItemCount(): Int = listaEntradas!!.size
}