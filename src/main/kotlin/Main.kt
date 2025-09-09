// Importar biblioteca coroutines para manejar tareas asíncronicas
import kotlinx.coroutines.*

// Inicializar variables relacionadas al catalogo e iva
val catalogo = GestorPedidos.inicializarCatalogo()
val iva = 0.19

// Función principal para el programa
// runBlocking es necesario para ejecutar corrutinas en una función sin suspender
fun main() = runBlocking {
    println("--- BIENVENIDO A FOODEXPRESS ---")

    GestorPedidos.mostrarCatalogo(catalogo)

    // Bloque try catch para manejar errores de usuario
    try {
        println("Seleccione los productos (números separados por coma):")
        val inputProducto = readlnOrNull()
        // creación de lista mutable para guardar productos seleccionados
        val pedido = mutableListOf<Producto>()

        if (inputProducto != null) {
            val numeroTexto = inputProducto.split(",")
            for (textoNumero in numeroTexto) {
                // Limpiar espacios y luego convertir a número
                val numero = textoNumero.trim().toInt()

                val i = numero - 1
                if (i >= 0 && i < catalogo.size) {
                    val productoSeleccionado = catalogo[i]
                    pedido.add(productoSeleccionado)
                }
            }
        }
        if (pedido.isEmpty()) {
            println("No se seleccionaron productos válidos, finalizando el programa.")
            return@runBlocking
        }

        println("Cliente tipo (regular/estudiante/premium):")
        val tipoCliente = readlnOrNull()?.lowercase() ?: "regular"

        println("Procesando pedido...")
        val estadoJob = launch {
            GestorPedidos.procesarPedidoAsincrono(pedido)
        }

        estadoJob.join()

        println("\n--- RESUMEN DEL PEDIDO ----")
        val subtotal = GestorPedidos.calcularSubtotal(pedido)
        val descuento = GestorPedidos.calcularDescuento(tipoCliente, subtotal)
        val ivaCalculado = (subtotal - descuento) * iva
        val total = subtotal - descuento + ivaCalculado

        for (item in pedido){
            println("- ${item.nombre}: $${item.precioFinal}")
        }

        println("Subtotal: $${String.format("%.0f", subtotal)}")
        println("Descuento ${tipoCliente.uppercase()} (${GestorPedidos.obtenerPorcentajeDescuento(tipoCliente)}%): -$${String.format("%.0f", descuento)}")
        println("IVA (19%): $${String.format("%.0f", ivaCalculado)}")
        println("TOTAL: $${String.format("%.0f", total)}")
        println("Estado final: ${EstadoPedido.Entregado::class.simpleName}")
    } catch (e: NumberFormatException) {
        println("Error: La entrada es inválida. Asegúrate de ingresar solo números. ${e.message}")
    }
    catch (e: Exception) {
        println("Error: La entrada es inválida. Asegúrate de ingresar números separados por comas. ${e.message}")
    }
    catch (e: IllegalArgumentException) {
        // Maneja la excepción de precio negativo
        println("Error en la creación del catálogo: ${e.message}")
    }
}