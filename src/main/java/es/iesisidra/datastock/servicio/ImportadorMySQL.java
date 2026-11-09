package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.comprobaciones.ComprobacionesBD;
import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.dao.ProductoDao;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;
import es.iesisidra.datastock.validacion.ValidadorProducto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ImportadorMySQL {

    private static final String LINEA = "------------------------------------------------";

    private final Inventario inventario;
    private final ProductoDao productoDao = new ProductoDao();

    private int analizados;
    private int insertados;
    private int duplicados;
    private int rechazados;

    public ImportadorMySQL(Inventario inventario) {
        this.inventario = inventario;
    }

    public void importar() {
        System.out.println("\nIMPORTAR PRODUCTOS DE MEMORIA A MYSQL");
        System.out.println(LINEA);

        List<Producto> productos = inventario.getProductos();
        System.out.println("Productos disponibles en memoria: " + productos.size());
        if (productos.isEmpty()) {
            Mensajes.aviso("No hay productos en memoria para importar. Importa antes un JSON o un XML (opciones 2 y 3).");
            return;
        }
        if (!LectorConsola.confirmar(amarillo("[AVISO] ¿Deseas importar estos productos a MySQL?"))) {
            Mensajes.aviso("Importación cancelada. No se ha cambiado nada.");
            return;
        }
        boolean simularFallo = LectorConsola.confirmar(
                amarillo("[AVISO] ¿Forzar un error SQL al final para comprobar el rollback?"));

        analizados = 0;
        insertados = 0;
        duplicados = 0;
        rechazados = 0;

        System.out.println("Iniciando comprobación de productos...");

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                Producto primerInsertado = null;

                for (Producto original : productos) {
                    analizados++;
                    Producto producto = copiaNormalizada(original);
                    System.out.println("\nComprobando " + producto.getCodigo() + " - " + producto.getNombre() + "...");

                    if (procesarProducto(conexion, producto)) {
                        productoDao.insertar(conexion, producto);
                        insertados++;
                        if (primerInsertado == null) {
                            primerInsertado = producto;
                        }
                    }
                }

                if (simularFallo && primerInsertado != null) {
                    System.out.println("\nForzando un error SQL: se intenta insertar otra vez " + primerInsertado.getCodigo() + "...");
                    productoDao.insertar(conexion, primerInsertado);
                }

                conexion.commit();
                mostrarResumen();
                Mensajes.ok("Importación finalizada. Los productos válidos se han guardado en MySQL.");

            } catch (SQLException e) {
                conexion.rollback();
                Mensajes.error("Error SQL durante la importación: " + ConexionBD.explicarError(e));
                Mensajes.error("Se ha hecho ROLLBACK: no se ha confirmado ninguna inserción de este lote.");
                insertados = 0;
                mostrarResumen();
            }
        } catch (SQLException e) {
            Mensajes.error("No se pudo conectar con MySQL: " + ConexionBD.explicarError(e));
        }
    }

    private boolean procesarProducto(Connection conexion, Producto producto) throws SQLException {
        String error = ValidadorProducto.validarProducto(producto);
        if (error != null) {
            Mensajes.error("Datos incorrectos: " + error);
            System.out.println("        El producto no se puede importar.");
            rechazados++;
            return false;
        }
        if (ComprobacionesBD.existeProducto(conexion, producto.getCodigo())) {
            Mensajes.aviso("El producto ya existe en MySQL. Se omite.");
            duplicados++;
            return false;
        }
        if (!ComprobacionesBD.existeCategoria(conexion, producto.getCategoriaId())) {
            Mensajes.error("No existe la categoría con ID " + producto.getCategoriaId() + ".");
            System.out.println("        El producto no se puede importar.");
            rechazados++;
            return false;
        }
        if (!ComprobacionesBD.existeProveedor(conexion, producto.getProveedorId())) {
            Mensajes.error("No existe el proveedor con ID " + producto.getProveedorId() + ".");
            System.out.println("        El producto no se puede importar.");
            rechazados++;
            return false;
        }
        Mensajes.ok("Producto válido para importar.");
        return true;
    }

    private Producto copiaNormalizada(Producto original) {
        return new Producto(
                ValidadorProducto.normalizarCodigo(original.getCodigo()),
                ValidadorProducto.normalizarNombre(original.getNombre()),
                original.getPrecio(),
                original.getStock(),
                original.getCategoriaId(),
                original.getProveedorId());
    }

    private void mostrarResumen() {
        System.out.println("\n" + LINEA);
        System.out.println("RESULTADO DE LA IMPORTACIÓN");
        System.out.println(LINEA);
        System.out.printf("%-40s%d%n", "Productos analizados:", analizados);
        System.out.printf("%-40s%d%n", "Productos insertados:", insertados);
        System.out.printf("%-40s%d%n", "Omitidos por código duplicado:", duplicados);
        System.out.printf("%-40s%d%n", "Rechazados por datos incorrectos:", rechazados);
        System.out.println(LINEA);
    }

    private String amarillo(String texto) {
        return ColoresConsola.pintar(texto, ColoresConsola.AMARILLO);
    }
}