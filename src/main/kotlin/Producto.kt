open  class Producto(val nombre: String, val descripcion: String, val precio: Double){
    // Validación precio no negativo
    init {
        require(precio >=0){ "El precio no puede ser negativo."}
    }

    // Polimorfismo: este metodo será sobrescrito en clases hijas.
    open val precioFinal: Double
    get() = precio
}

class Comida(nombre: String, descripcion: String, precio: Double, val esPremium: Boolean) : Producto(nombre, descripcion, precio){
    // Se sobreescribe la propiedad para aplicar un ajuste de precio si se es premium.
    override val precioFinal: Double
        get()= if(esPremium) precio * 1.2 else precio
}

class Bebida(nombre: String, descripcion: String, precio: Double, val categoria: String, val tamaño: String) : Producto(nombre, descripcion, precio){
    // Se sobreescribe la propiedad para ajustar el precio según tamaño
    override val precioFinal: Double
        get(){
            var precioAjustado = precio
            when (tamaño.lowercase()){
                "mediano" -> precioAjustado += 290.0
                "grande" -> precioAjustado += 690.0
            }
            return precioAjustado
        }
}