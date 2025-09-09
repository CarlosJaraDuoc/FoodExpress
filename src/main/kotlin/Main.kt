// Importar biblioteca coroutines para manejar tareas asíncronicas
import kotlinx.coroutines.*

// Inicializar variables relacionadas al catalogo e iva
val catalogo = GestorPedidos.inicializarCatalogo()
val iva = 0.19

// Función principal para el programa
// runBlocking es necesario para ejecutar corrutinas en una función sin suspender
fun main() = runBlocking {
    println("--- BIENVENIDO A FOODEXPRESS ---")

    // Bucle para mantener el programa en ejecución
    while(true){

    GestorPedidos.mostrarCatalogo(catalogo)

    // Variables para controlar bucle de validación entrada
    var entradaValida = false
    // creación de lista mutable para guardar productos seleccionados
    var pedido = emptyList<Producto>()

    do {

        // Bloque try catch para manejar errores de usuario
        try {
            println("Seleccione los productos (números separados por coma):")
            val inputProducto = readlnOrNull()

            // Llamada a función crearPedido para generación de lista productos
            val indiceSeleccionado = inputProducto?.split(",")?.map { it.trim().toInt() - 1 } ?: emptyList()
            pedido = GestorPedidos.crearPedido(indiceSeleccionado, catalogo)

            if (pedido.isEmpty()) {
                println("No se seleccionaron productos válidos. Inténtelo de nuevo.")
                // La entrada no es válida, el bucle se repite
                continue
            }

            // si no hay error, se considera valido para continuar
            entradaValida = true

        } catch (e: NumberFormatException) {
            println("Error: La entrada es inválida. Asegúrate de ingresar solo números. Inténtelo de nuevo.")
        } catch (e: IllegalArgumentException) {
            println("Error en la creación del catálogo: ${e.message}")
            // Este error es fatal para este contexto, así que salimos.
            return@runBlocking
        }

    } while (!entradaValida)

        var tipoClienteValido = false
        var tipoCliente = ""
        var tipoValidos = listOf("regular", "estudiante", "premium")

        do{
            println("Cliente tipo (regular/estudiante/premium):")
            tipoCliente = readlnOrNull()?.lowercase() ?: ""
            if (tipoValidos.contains(tipoCliente)){
                tipoClienteValido = true
            } else {
                println("Tipo de cliente inválido. Por favor, ingrese 'regular', 'estudiante' o 'premium'.")
            }
        } while(!tipoClienteValido)

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

            for (item in pedido) {
                println("- ${item.nombre}: $${item.precioFinal}")
            }

        println("Subtotal: $${String.format("%.0f", subtotal)}")
        println("Descuento ${tipoCliente.uppercase()} (${GestorPedidos.obtenerPorcentajeDescuento(tipoCliente)}%): -$${String.format("%.0f", descuento)}")
        println("IVA (19%): $${String.format("%.0f", ivaCalculado)}")
        println("TOTAL: $${String.format("%.0f", total)}")
        println("Estado final: ${EstadoPedido.Entregado::class.simpleName}")

        // Bucle para validar la opción de continuar
        var continuarValido = false
        var continuar = ""
        do {
            println("\n¿Desea realizar otro pedido? (si/no)")
            continuar = readlnOrNull()?.lowercase() ?: ""
            if (continuar == "si" || continuar == "no") {
                continuarValido = true
            } else {
                println("Opción inválida. Por favor, ingrese 'si' o 'no'.")
            }
        } while (!continuarValido)

        if (continuar != "si") {
            println("Gracias por su visita. Saliendo del sistema.")
            break
        }

    }
}