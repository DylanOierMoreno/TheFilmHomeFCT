package es.barajas.thefilmhomefct.ui.iniciar_sesion

class Usuario() {
    private var nombre: String
    private var apellidos: String
    private var fechaNac: String
    private var tipoDocumento: String
    private var numDocumento: String
    private var genero: String
    private var email: String
    private var password: String
    private var provincia: String
    private var cine: String

    init {
        this.nombre = ""
        this.apellidos = ""
        this.fechaNac = ""
        this.tipoDocumento = ""
        this.numDocumento = ""
        this.genero = ""
        this.email = ""
        this.password = ""
        this.provincia = "0"
        this.cine = "0"
    }

    constructor(nombre: String, apellidos: String, fechaNac: String, tipoDocumento: String,
                numDocumento: String, genero: String, email: String, password: String) : this() {
        this.nombre = nombre
        this.apellidos = apellidos
        this.fechaNac = fechaNac
        this.tipoDocumento = tipoDocumento
        this.numDocumento = numDocumento
        this.genero = genero
        this.email = email
        this.password = password
        this.provincia = "0"
        this.cine = "0"
    }

    // Este método guarda en un mapa todos los datos personales de un usuario.
    fun crearMapaDatosPersonales() : HashMap<String,Any>{
        val mapaDatosPersonales: HashMap<String, Any> = HashMap<String,Any>()
        mapaDatosPersonales["nombre"] = nombre
        mapaDatosPersonales["apellidos"] = apellidos
        mapaDatosPersonales["fechaNac"] = fechaNac
        mapaDatosPersonales["tipoDocumento"] = tipoDocumento
        mapaDatosPersonales["numDocumento"] = numDocumento
        mapaDatosPersonales["genero"] = genero
        mapaDatosPersonales["email"] = email
        mapaDatosPersonales["password"] = password
        mapaDatosPersonales["provincia"] = provincia
        mapaDatosPersonales["cine"] = cine
        return mapaDatosPersonales
    }
}