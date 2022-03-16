package es.barajas.thefilmhomefct.data

/* Un usuario de Firebase va a tener un mapa de datos personles que son todos los datos que se guardan
cuando se da de alta como usuario (nombre, apellidos, correo, etc) y una lista donde se guardarán
las EntradasFirebase que compre */
data class UsuarioFirebase(val lista_datosPersonales: Map<String, Any>,
   val lista_entradas: ArrayList<HashMap<String, Any>>?, val tarjetaTFH: Map<String, Any>?)
