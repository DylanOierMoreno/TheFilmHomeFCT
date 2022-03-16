package es.barajas.thefilmhomefct.ui.generadores

import android.graphics.*
import com.google.zxing.*
import com.google.zxing.common.BitMatrix

class GeneradorBarcode {
    companion object {
        private lateinit var multiFormatWriter: MultiFormatWriter
        private lateinit var bitMatrix: BitMatrix
        private lateinit var bitmap: Bitmap
        private var alto = 250
        private var ancho = 1000
        private val EAN = generarRandom()

        //Creamos el código de barras con el estándar EAN13 que necesita 12 dígitos más un dígito de control
        fun crearBarcode(): Bitmap {
            multiFormatWriter = MultiFormatWriter()
            try {
                bitMatrix = multiFormatWriter.encode(EAN + calcularDigitoControl().toString(),
                    BarcodeFormat.EAN_13, ancho, alto)
                bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.RGB_565)
                for (i in 0 until ancho) {
                    for (j in 0 until alto) {
                        bitmap.setPixel(i, j, if (bitMatrix.get(i, j)) Color.BLACK else Color.WHITE)
                    }
                }
            } catch (e: WriterException) {
                e.printStackTrace()
            }
            return bitmap
        }

        //Calculamos dígito de control con los dígitos que contiene EAN en String
        fun calcularDigitoControl(): Int {
            var pares = 0
            var impares = 0
            var digito: Int
            for (i in EAN.indices) {
                digito = EAN[i].toString().toInt()
                if (i % 2 != 0) {
                    impares += digito
                } else {
                    pares += digito
                }
            }
            digito = impares * 3 + pares
            //Obtenemos el valor del dígito de control
            return if (digito.toString()[digito.toString().length - 1].toString().toInt() == 0) {
                0
            } else {
                10 - digito.toString()[digito.toString().length - 1].toString().toInt()
            }
        }

        //Creamos un número aleatorio de 12 dígitos 84 + 10 números aleatorios más
        private fun generarRandom(): String {
            var numeroAleatorio = "84"
            for (i in 0..9) {
                numeroAleatorio += ((Math.random() * 10).toInt()).toString()
            }
            return numeroAleatorio
        }
    }
}