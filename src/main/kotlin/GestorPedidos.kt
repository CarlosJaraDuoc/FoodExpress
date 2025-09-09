import kotlinx.coroutines.delay

object GestorPedidos{

    fun inicializarCatalogo(): List<Producto>{
        return listOf(
            Comida("Hamburgesa", "Versión Clásica", 8990.0, false),
            Comida("Salmón Grillado", "Origen sureño", 15990.0, true),
            Bebida("Coca Cola", "En lata", 1990.0, "Gaseosa", "mediano"),
            Bebida("Jugo Natural", "Con fruta fresca", 2990.0, "Jugo Natural", "grande"),
            Bebida("Té Helado", "Sabor limón", 2100.0, "Infusión", "pequeño"),
            Bebida("Agua Mineral", "Sin gas", 1500.0, "Agua", "grande")
        )
    }

    fun mostrarCatalogo(catalogo: List<Producto>){
        for (i in catalogo.indices){
            val producto = catalogo[i]
            val detalles = when (producto) {
                is Comida -> "(${if (producto.esPremium) "Premium" else "Normal"})"
                is Bebida -> "${producto.categoria}, tamaño ${producto.tamanio}"
                else -> ""
            }
            println("${i+1}. ${producto.nombre} - $${producto.precioFinal.toInt()} $detalles")
        }
    }

    fun crearPedido(indices: List<Int>, catalogo: List<Producto>): List<Producto> {
        val pedido = mutableListOf<Producto>()
        // Usamos un bucle for para seleccionar los productos uno a uno.
        for (indice in indices) {
            if (indice in catalogo.indices) {
                pedido.add(catalogo[indice])
            }
        }
        return pedido.toList()
    }

    // Calcula el subtotal del pedido.
    fun calcularSubtotal(pedido: List<Producto>): Double {
        return pedido.sumOf { it.precioFinal }
    }

    fun obtenerPorcentajeDescuento(tipoCliente: String): Int {
        return when (tipoCliente) {
            "estudiante" -> 10
            "premium" -> 15
            else -> 5
        }
    }

    fun calcularDescuento(tipoCliente: String, subtotal: Double): Double {
        val porcentaje = obtenerPorcentajeDescuento(tipoCliente)
        return subtotal * (porcentaje / 100.0)
    }

    // Tarea asíncrona usando corrutinas.
    suspend fun procesarPedidoAsincrono(pedido: List<Producto>) {
        println("Estado: ${EstadoPedido.Pendiente::class.simpleName}")
        delay(1000)
        println("Estado: ${EstadoPedido.EnPreparacion::class.simpleName}")
        delay(3000)
        println("Estado: ${EstadoPedido.ListoParaEntregar::class.simpleName}")
        delay(1000)
    }

}