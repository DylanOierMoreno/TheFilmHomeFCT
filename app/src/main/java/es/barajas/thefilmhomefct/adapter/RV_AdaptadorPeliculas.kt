package es.barajas.thefilmhomefct.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.*
import es.barajas.thefilmhomefct.data.ListaPeliculas
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.Utilidades
import es.barajas.thefilmhomefct.ui.peliculas.DetallePelicula

class RV_AdaptadorPeliculas() : RecyclerView.Adapter<RV_AdaptadorPeliculas.ViewHolderPeliculas>() {
    private var listaPeliculas: ArrayList<ListaPeliculas>? = null
    //variable para descargar fotos de STORAGE FIREBASE
    private lateinit var storageRef: StorageReference
    //Instancia de la BBDD de Firebase
    private val db = FirebaseFirestore.getInstance()

    constructor(listaPeliculas: ArrayList<ListaPeliculas>?) : this() {
        this.listaPeliculas = listaPeliculas
    }

    class ViewHolderPeliculas(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nombre: TextView? = itemView.findViewById(R.id.idNombre)
        var info: TextView? = itemView.findViewById(R.id.idInfo)
        var director: TextView? = itemView.findViewById(R.id.idDirector)
        var foto: ImageView = itemView.findViewById(R.id.idImagen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPeliculas {
        val layout: Int = if(Utilidades.visualizacion == Utilidades.LIST) {
            R.layout.items_peliculas
        } else {
            R.layout.grid_item
        }
        val view: View = LayoutInflater.from(parent.context).inflate(layout, null, false)
        return ViewHolderPeliculas(view)
    }

    // Reemplaza el contenido de una vista de elementos de lista
    override fun onBindViewHolder(holder: ViewHolderPeliculas, position: Int) {
        if (Utilidades.visualizacion == Utilidades.LIST) {
            holder.nombre?.setText(listaPeliculas!![position].nombre)
            consultarPelicula(holder, listaPeliculas!![position].directorio, listaPeliculas!![position].nombre, "descripcion")
            consultarPelicula(holder, listaPeliculas!![position].directorio, listaPeliculas!![position].nombre, "director")
        }
        descargarFotos(holder, position)
        holder.foto.setOnClickListener {
            val intent = Intent(holder.foto.context, DetallePelicula::class.java)
            intent.putExtra("NOMBRE", listaPeliculas!!.get(position).nombre)
            intent.putExtra("DIRECTORIO", listaPeliculas!!.get(position).directorio)
            holder.foto.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaPeliculas!!.size

    //Descargamos fotos desde el STORAGE FIREBASE
    private fun descargarFotos(holder: ViewHolderPeliculas, position: Int){
        var bitmap: Bitmap?
        storageRef = FirebaseStorage.getInstance().reference
                .child(listaPeliculas!![position].foto)
        storageRef.getBytes(1024 * 1024).addOnSuccessListener {
            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            holder.foto.setImageBitmap(bitmap)
        }.addOnFailureListener {
            System.out.println("========ERROR!!!!!======== " + it)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun consultarPelicula(holder: ViewHolderPeliculas, nombreDirectorio: String, nombrePelicula: String, dato: String){
        db.collection(nombreDirectorio).document(nombrePelicula).get().addOnSuccessListener {
            when (dato) {
                "descripcion" -> holder.info?.text = it.get(dato) as String
                "director" -> holder.director?.text = "Director / " + it.get(dato) as String
            }
        }.addOnFailureListener {
            System.out.println("========ERROR!!!!!======== " + it)
        }
    }
}