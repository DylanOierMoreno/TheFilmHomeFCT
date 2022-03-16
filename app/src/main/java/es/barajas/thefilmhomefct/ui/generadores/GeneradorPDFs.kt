package es.barajas.thefilmhomefct.ui.generadores

import android.annotation.SuppressLint
import android.app.*
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.google.firebase.storage.*
import es.barajas.thefilmhomefct.R
import es.barajas.thefilmhomefct.data.ButacaFirebase
import java.io.*
import java.nio.file.*
import java.security.SecureRandom
import java.util.*

class GeneradorPDFs() : Application() {
    private lateinit var scaledbmp: Bitmap
    private lateinit var canvas: Canvas
    private lateinit var myPdfDocument: PdfDocument
    private lateinit var myPageInfo: PdfDocument.PageInfo
    private lateinit var myPage: PdfDocument.Page
    private lateinit var activity: Activity
    private lateinit var nombrePelicula: String
    private lateinit var paint: Paint
    private lateinit var titlePaint: Paint
    //guardará la ruta de la foto de la película que se quiere visualizar
    private var ruta = "cartelera/"
    //variable para descargar fotos de STORAGE FIREBASE
    private lateinit var storageRef: StorageReference
    companion object {
         var isCorrect: Boolean = false
    }

    constructor(activity: Activity, nombrePelicula: String) : this() {
        this.activity = activity
        this.nombrePelicula = nombrePelicula
        this.myPdfDocument = PdfDocument()
        this.myPageInfo = PdfDocument.PageInfo.Builder(2100, 2970, 1).create()
        this.myPage = myPdfDocument.startPage(myPageInfo)
        this.canvas = myPage.canvas
        this.paint = Paint()
        this.titlePaint = Paint()
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    fun crearPDF(cine: String, horaSesion: String, fecha: String,
        lista_tipoEntradas: ArrayList<String>, lista_numeroEntradas: ArrayList<Int>,
        numSala: String, listaButacas: ArrayList<ButacaFirebase>) {
        var bitmap: Bitmap
        storageRef = FirebaseStorage.getInstance().reference.child(buscarFotoPelicula())
        storageRef.getBytes(1024 * 1024).addOnSuccessListener {
            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            scaledbmp = Bitmap.createScaledBitmap(bitmap, 600, 1000, false)
            canvas.drawBitmap(scaledbmp, 90f, 500f, paint)
            //titulo
            titlePaint.textAlign = Paint.Align.CENTER
            titlePaint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            titlePaint.textSize = 100f
            canvas.drawText("THE FILM HOME", 1000f, 250f, titlePaint)
            bitmap = BitmapFactory.decodeResource(activity.resources, R.drawable.ic_logo)
            scaledbmp = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
            canvas.drawBitmap(bitmap, 1500f, 125f, paint)
            paint.textSize = 95f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(cine, 775f, 450f, paint)
            paint.textSize = 85f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(nombrePelicula.toUpperCase(Locale.ROOT), 775f, 600f, paint)
            canvas.drawText(fecha, 775f, 750f, paint)
            canvas.drawText(horaSesion + 'h', 1425f, 750f, paint)
            canvas.drawText("SALA $numSala", 1725f, 750f, paint)
            canvas.drawText("Tipo de entradas:", 775f, 900f, paint)
            var y = 900f
            for (i in 0 until lista_numeroEntradas.size) {
                y += 100f
                canvas.drawText(lista_numeroEntradas[i].toString() + " x " + lista_tipoEntradas[i],
                    950f, y, paint)
            }
            y += 150f
            canvas.drawText("Asientos:", 800f, y, paint)
            for (i in 0 until listaButacas.size) {
                y += 100f
                if (i == listaButacas.size - 1) {
                    canvas.drawText("Fila " + listaButacas[i].fila + "-Butaca " + listaButacas[i].columna,
                        950f, y, paint)
                } else {
                    canvas.drawText("Fila " + listaButacas[i].fila + "-Butaca " + listaButacas[i].columna + ',',
                        950f, y, paint)
                }
            }
            canvas.drawBitmap(GeneradorBarcode.crearBarcode(), 550f, y + 300f, paint)
            myPdfDocument.finishPage(myPage)
            val random = SecureRandom.getInstance("SHA1PRNG")
            val fichero = Paths.get("/storage/emulated/0/Download/entradas_" + random.nextInt() + ".pdf")
            isCorrect = try {
                myPdfDocument.writeTo(Files.newOutputStream(fichero))
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
            myPdfDocument.close()
        }
    }

    //Busca el nombre que tiene la foto de cada película en Firestore
    private fun buscarFotoPelicula(): String {
        when (nombrePelicula) {
            "El informe Auschwitz" -> ruta += "informe_auschwitz.jpg"
            "Gozilla vs. Kong" -> ruta += "godzilla_vs_kong.jpg"
            "Inmune" -> ruta += "inmune.jpg"
            "Los Croods 2. Una nueva era" -> ruta += "los_croods.jpg"
            "Tom y Jerry" -> ruta += "tom_y_jerry.jpg"
            "Wonder Woman 1984" -> ruta += "wonderwoman.jpg"
        }
        return ruta
    }
}