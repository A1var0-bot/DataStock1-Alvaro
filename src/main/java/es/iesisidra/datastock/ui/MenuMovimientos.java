package es.iesisidra.datastock.ui;

import java.sql.SQLException;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.dao.MovimientoDao;
import es.iesisidra.datastock.dao.ProductoDao;
import es.iesisidra.datastock.modelo.MovimientoStock;
import es.iesisidra.datastock.modelo.MovimientoStock.TipoMovimiento;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;
import es.iesisidra.datastock.validacion.ValidadorProducto;

public class MenuMovimientos {

    private final MovimientoDao movimientoDao = new MovimientoDao();
    private final ProductoDao productoDao = new ProductoDao();

    public void iniciar() {
        int opcion;
        do {
            System.out.println("\n========== MENÚ DE MOVIMIENTOS ==========");
            System.out.println("1. Registrar una ENTRADA de stock");
            System.out.println("2. Registrar una SALIDA de stock");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);

            switch (opcion) {
                case 1:
                    registrar(TipoMovimiento.ENTRADA);
                    break;
                case 2:
                    registrar(TipoMovimiento.SALIDA);
                    break;
                case 0:{ 

                }
                break;
                default:
                Mensajes.aviso("Opción no válida.");
                break;
            }
            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }

    private void registrar(TipoMovimiento tipo) {
        System.out.println("\nREGISTRAR " + tipo);
        System.out.println("----------------------------------------");
        String codigo = ValidadorProducto.normalizarCodigo(LectorConsola.leerTexto("Código del producto (Intro para volver): "));
        if (codigo.isEmpty()) {
            return;
        }
        try {
            Producto producto = productoDao.buscarPorCodigo(codigo);
            if (producto == null) {
                Mensajes.error("No existe ningún producto con el código " + codigo + ".");
                return;
            }
            System.out.println("Producto: " + producto.getNombre());
            System.out.println("Stock actual: " + producto.getStock());

            int cantidad = LectorConsola.leerEntero("Cantidad: ");
            if (cantidad <= 0) {
                Mensajes.error("La cantidad debe ser un número entero mayor que 0.");
                return;
            }

            String pregunta = "[AVISO] ¿Registrar " + tipo + " de " + cantidad + " unidades de " + codigo + "?";
            if (!LectorConsola.confirmar(ColoresConsola.pintar(pregunta, ColoresConsola.AMARILLO))) {
                Mensajes.aviso("Operación cancelada. No se ha cambiado nada.");
                return;
            }

            movimientoDao.registrar(codigo, tipo, cantidad);

            MovimientoStock movimiento = movimientoDao.ultimoMovimiento(codigo);
            Mensajes.ok("Movimiento registrado correctamente.");
            System.out.println("Tipo: " + movimiento.getTipo());
            System.out.println("Stock anterior: " + movimiento.getStockAnterior());
            System.out.println("Cantidad: " + movimiento.getCantidad());
            System.out.println("Stock posterior: " + movimiento.getStockPosterior());

        } catch (SQLException e) {
            Mensajes.error(explicarErrorMovimiento(e));
        }
    }

    private String explicarErrorMovimiento(SQLException e) {
        if ("45000".equals(e.getSQLState())) {
            String texto = e.getMessage();
            if (texto.contains("Stock insuficiente")) {
                return "Stock insuficiente: no hay unidades suficientes para esa salida." + " No se ha modificado el stock ni el historial.";
            }
            if (texto.contains("Producto inexistente")) {
                return "El producto no existe. No se ha registrado ningún movimiento.";
            }
            if (texto.contains("Tipo o cantidad invalidos")) {
                return "Tipo o cantidad no válidos. No se ha registrado ningún movimiento.";
            }
            return "El movimiento no se ha registrado: " + texto;
        }
        return ConexionBD.explicarError(e);
    }
}
