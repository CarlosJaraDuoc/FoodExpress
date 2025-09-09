// Importar biblioteca coroutines para manejar tareas asíncronicas
import kotlinx.coroutines.*

// Inicializar variables relacionadas al catalogo, usuario e iva
val catalogo = GestorPedidos.inicializarCatalogo()
val iva = 0.19
val codigos = mutableListOf<Int>()
val correos = mutableListOf<String>()
val nombres = mutableListOf<String>()
val apellidos = mutableListOf<String>()
var codigoSecuencial = 0
val contrasenas = mutableListOf<String>()

fun generarCodigo(): Int {
    codigoSecuencial++
    return codigoSecuencial
}

fun validarCorreo(input: String?): Boolean {
    if (input.isNullOrBlank()) return false
    val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return regex.matches(input)
}

fun validarNombre(input: String?): Boolean {
    if (input.isNullOrBlank()) return false
    val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")
    return regex.matches(input)
}

fun validarContrasena(input: String?): Boolean {
    // contraseña válida solo si no es nula, vacia o sin espacios
    return !input.isNullOrBlank()
}

// Función principal para el programa
// runBlocking es necesario para ejecutar corrutinas en una función sin suspender
fun main() = runBlocking {
    println("--- BIENVENIDO A FOODEXPRESS ---")

    // Bucle para mantener el programa en ejecución
    while(true) {

        // Registro/Login de cliente cada vez
        var clienteActual: Int? = null
        while(clienteActual == null) {
            println("\n--- REGISTRO/LOGIN DE CLIENTE ---")
            print("Correo: ")
            val correo = readlnOrNull()?.trim() ?: ""

            if (!validarCorreo(correo)) {
                println("Error: Ingrese un correo válido con formato @dominio.com.")
                continue
            }

            val indexExistente = correos.indexOf(correo)
            if (indexExistente != -1) {
                // Cliente ya registrado → pedir contraseña
                print("Correo ya registrado. Ingrese la contraseña: ")
                val intento = readlnOrNull()?.trim() ?: ""
                if (intento == contrasenas[indexExistente]) {
                    println("Acceso concedido. Bienvenido nuevamente, ${nombres[indexExistente]}")
                    clienteActual = codigos[indexExistente]
                } else {
                    println("Contraseña incorrecta. Intente nuevamente.")
                }
            } else {
                // Cliente nuevo → pedir datos completos
                print("Nombre: ")
                val nombre = readlnOrNull()?.trim() ?: ""
                if (!validarNombre(nombre)) {
                    println("Error: Nombre inválido.")
                    continue
                }

                print("Apellidos: ")
                val apellido = readlnOrNull()?.trim() ?: ""
                if (!validarNombre(apellido)) {
                    println("Error: Apellido inválido.")
                    continue
                }

                print("Contraseña: ")
                val contrasena = readlnOrNull()?.trim() ?: ""
                if (!validarContrasena(contrasena)) {
                    println("Error: Contraseña inválida.")
                    continue
                }

                // Guardar cliente
                val codigoCliente = generarCodigo()
                println("Código asignado automáticamente: $codigoCliente")
                codigos.add(codigoCliente)
                correos.add(correo)
                nombres.add(nombre)
                apellidos.add(apellido)
                contrasenas.add(contrasena)
                clienteActual = codigoCliente
                println("Registro exitoso. Bienvenido, $nombre")
            }
        }

        GestorPedidos.mostrarCatalogo(catalogo)

        // Variables para controlar bucle de validación entrada
        var entradaValida = false
        var pedido: List<Producto> = emptyList()

        // Selección de productos
        do {
            try {
                println("Seleccione los productos (números separados por coma):")
                val inputProducto = readlnOrNull()
                val indiceSeleccionado = inputProducto?.split(",")?.map { it.trim().toInt() - 1 } ?: emptyList()
                val maximo = indiceSeleccionado.size
                if (maximo > 10) {
                    println("Error: No puedes seleccionar más de 10 productos. Por favor, inténtelo de nuevo.")
                    continue
                }

                pedido = GestorPedidos.crearPedido(indiceSeleccionado, catalogo)

                if (pedido.isEmpty()) {
                    println("No se seleccionaron productos válidos. Inténtelo de nuevo.")
                    continue
                }


                // validacion reglas negocio
                val tieneComida = pedido.any { it is Comida }
                val tieneBebida = pedido.any { it is Bebida }
                if (!tieneComida || !tieneBebida) {
                    println("Debe incluir al menos una comida y una bebida.")
                    continue
                }

                entradaValida = true

            } catch (_: NumberFormatException) {
                println("Error: La entrada es inválida. Asegúrate de ingresar solo números. Inténtelo de nuevo.")
            } catch (e: IllegalArgumentException) {
                println("Error en la creación del catálogo: ${e.message}")
                return@runBlocking
            }

        } while (!entradaValida)

        // Tipo de cliente
        var tipoClienteValido = false
        var tipoCliente: String
        val tipoValidos = listOf("regular", "estudiante", "premium")

        do {
            println("Cliente tipo (regular/estudiante/premium):")
            tipoCliente = readlnOrNull()?.lowercase() ?: ""
            if (tipoValidos.contains(tipoCliente)) {
                tipoClienteValido = true
            } else {
                println("Tipo de cliente inválido. Por favor, ingrese 'regular', 'estudiante' o 'premium'.")
            }
        } while(!tipoClienteValido)

        println("Procesando pedido...")

        // Corrutina para procesar pedido
        val estadoJob = launch {
            GestorPedidos.procesarPedidoAsincrono(pedido)
        }
        estadoJob.join()

        // Resumen del pedido
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

        // Preguntar si desea realizar otro pedido
        var continuarValido = false
        var continuar: String
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
