package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.dao.MovimientoDao;
import es.iesisidra.datastock.dao.ProductoDao;
import es.iesisidra.datastock.modelo.MovimientoStock;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;
import es.iesisidra.datastock.validacion.ValidadorProducto;

public class MenuHistorial {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String LINEA = "------------------------------------------------------------------------------";

    private final MovimientoDao movimientoDao = new MovimientoDao();
    private final ProductoDao productoDao = new ProductoDao();

    public void iniciar() {
        int opcion;
        do {
            System.out.println("\n========== MENÚ DE HISTORIAL ==========");
            System.out.println("1. Consultar todos los movimientos");
            System.out.println("2. Consultar movimientos de un producto");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);

            switch (opcion) {
                case 1:
                    historialCompleto();
                    break;
                case 2:
                    historialProducto();
                    break;
                case 0:{ }
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

    private void historialCompleto() {
        System.out.println("\nHISTORIAL COMPLETO DE MOVIMIENTOS");
        try {
            List<MovimientoStock> movimientos = movimientoDao.listarTodos();
            if (movimientos.isEmpty()) {
                Mensajes.aviso("Sin movimientos registrados.");
                return;
            }
            mostrarTabla(movimientos);
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    private void historialProducto() {
        System.out.println("\nHISTORIAL DE UN PRODUCTO");
        String codigo = ValidadorProducto.normalizarCodigo(LectorConsola.leerTexto("Código del producto (Intro para volver): "));
        if (codigo.isEmpty()) {
            return;
        }
        try {
            Producto producto = productoDao.buscarPorCodigo(codigo);
            if (producto == null) {
                Mensajes.error("No existe ningún producto con el código " + codigo);
                return;
            }
            System.out.println("Producto: " + producto.getCodigo() + " - " + producto.getNombre());
            System.out.println("Stock actual: " + producto.getStock());

            List<MovimientoStock> movimientos = movimientoDao.listarPorProducto(codigo);
            if (movimientos.isEmpty()) {
                Mensajes.aviso("Sin movimientos registrados.");
                return;
            }
            mostrarTabla(movimientos);
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    private void mostrarTabla(List<MovimientoStock> movimientos) {
        System.out.println(LINEA);
        System.out.printf("%-6s%-21s%-14s%-9s%-10s%-12s%s%n", "ID", "FECHA Y HORA", "PRODUCTO", "TIPO", "CANTIDAD", "STOCK ANT.", "STOCK POST.");
        System.out.println(LINEA);
        for (MovimientoStock m : movimientos) {
            System.out.printf("%-6d%-21s%-14s%-9s%-10d%-12d%d%n",
                    m.getId(),
                    m.getFecha().format(FORMATO_FECHA),
                    m.getProductoCodigo(),
                    m.getTipo(),
                    m.getCantidad(),
                    m.getStockAnterior(),
                    m.getStockPosterior());
        }
        System.out.println(LINEA);
        System.out.println("Total de movimientos: " + movimientos.size());
    }
}
