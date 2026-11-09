package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.dao.ConsultaProductosDao;
import es.iesisidra.datastock.modelo.ProductoDetalle;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;
import es.iesisidra.datastock.validacion.ValidadorProducto;

public class MenuConsultasMySQL {

    private static final String LINEA = "------------------------------------------------------------";

    private final ConsultaProductosDao dao = new ConsultaProductosDao();

    public void iniciar() {
        int opcion;
        do {
            System.out.println("\n========== MENÚ DE CONSULTAS ==========");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("4. Consultar productos con stock inferior a un umbral");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);

            switch (opcion) {
                case 1:
                    listarTodos();
                    break;
                case 2:
                    buscarPorCodigo();
                    break;
                case 3:
                    buscarPorNombre();
                    break;
                case 4:
                    stockBajo();
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

    // ------------------------------------------------------------ LISTAR TODOS
    private void listarTodos() {
        System.out.println("\nPRODUCTOS ALMACENADOS EN MYSQL");
        try {
            List<ProductoDetalle> productos = dao.listarTodos();
            if (productos.isEmpty()) {
                Mensajes.aviso("No hay productos registrados en MySQL.");
                return;
            }
            mostrarLista(productos);
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ POR CÓDIGO
    private void buscarPorCodigo() {
        System.out.println("\nBUSCAR PRODUCTO POR CÓDIGO");
        String codigo = ValidadorProducto.normalizarCodigo(LectorConsola.leerTexto("Código (Intro para volver): "));
        if (codigo.isEmpty()) {
            return;
        }
        try {
            ProductoDetalle producto = dao.buscarPorCodigo(codigo);
            if (producto == null) {
                Mensajes.aviso("No se ha encontrado ningún producto con el código " + codigo + ".");
                return;
            }
            System.out.println(LINEA);
            mostrarFicha(producto);
            System.out.println(LINEA);
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ POR NOMBRE
    private void buscarPorNombre() {
        System.out.println("\nBUSCAR PRODUCTOS POR NOMBRE");
        String fragmento = LectorConsola.leerTexto("Texto a buscar (Intro para volver): ").trim();
        if (fragmento.isEmpty()) {
            return;
        }
        try {
            List<ProductoDetalle> productos = dao.buscarPorNombre(fragmento);
            if (productos.isEmpty()) {
                Mensajes.aviso("No hay productos cuyo nombre contenga '" + fragmento + "'.");
                return;
            }
            mostrarLista(productos);
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ STOCK BAJO
    private void stockBajo() {
        System.out.println("\nPRODUCTOS CON STOCK INFERIOR A UN UMBRAL");
        int umbral = LectorConsola.leerEntero("Umbral de stock (entero mayor o igual que 0): ");
        if (umbral < 0) {
            Mensajes.error("El umbral no puede ser negativo.");
            return;
        }
        try {
            List<ProductoDetalle> productos = dao.stockInferiorA(umbral);
            if (productos.isEmpty()) {
                Mensajes.aviso("No hay productos con stock inferior a " + umbral + ".");
                return;
            }
            mostrarLista(productos);
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ AYUDAS

    // Muestra una lista de productos, cada uno en su ficha, y el total
    private void mostrarLista(List<ProductoDetalle> productos) {
        System.out.println(LINEA);
        for (ProductoDetalle p : productos) {
            mostrarFicha(p);
            System.out.println(LINEA);
        }
        System.out.println("Total de productos encontrados: " + productos.size());
    }

    private void mostrarFicha(ProductoDetalle p) {
        System.out.println("Código: " + p.getCodigo());
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Precio: " + p.getPrecio() + " €");
        System.out.println("Stock: " + p.getStock());
        System.out.println("Categoría: " + p.getCategoria());
        System.out.println("Proveedor: " + p.getProveedor());
    }
}
