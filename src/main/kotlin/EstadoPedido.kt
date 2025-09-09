sealed class EstadoPedido{
    object Pendiente : EstadoPedido()
    object EnPreparacion : EstadoPedido()
    object ListoParaEntregar : EstadoPedido()
    object Entregado : EstadoPedido()
}