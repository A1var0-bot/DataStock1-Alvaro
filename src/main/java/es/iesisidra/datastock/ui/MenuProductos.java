package es.iesisidra.datastock.ui;

import java.math.BigDecimal;
import java.sql.SQLException;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.dao.CategoriaDao;
import es.iesisidra.datastock.dao.ProductoDao;
import es.iesisidra.datastock.dao.ProveedorDao;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.modelo.Proveedor;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;
import es.iesisidra.datastock.validacion.ValidadorProducto;

public class MenuProductos {

    private final ProductoDao productoDao = new ProductoDao();
    private final CategoriaDao categoriaDao = new CategoriaDao();
    private final ProveedorDao proveedorDao = new ProveedorDao();

    public void iniciar() {
        int opcion;
        do {
            System.out.println("\n========== MENÚ DE PRODUCTOS ==========");
            System.out.println("1. Dar de alta un producto");
            System.out.println("2. Consultar producto por código");
            System.out.println("3. Modificar un producto");
            System.out.println("4. Eliminar un producto");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);

            switch (opcion) {
                case 1:
                    alta();
                    break;
                case 2: 
                    consultar();
                    break;
                case 3: 
                    modificar();
                    break;
                case 4:
                    eliminar();
                    break;
                case 0:{ 
                    break;
                }
                default:
                    Mensajes.aviso("Opción no válida.");
                    break;
            }
            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }

    // ------------------------------------------------------------ ALTA
    private void alta() {
        System.out.println("\nALTA DE PRODUCTO");
        System.out.println("----------------------------------------");

        // Código: se pasa a mayúsculas y se valida
        String codigo = ValidadorProducto.normalizarCodigo(LectorConsola.leerTexto("Código (Intro para volver): "));
        if (codigo.isEmpty()) {
            return;
        }
        String error = ValidadorProducto.validarCodigo(codigo);
        if (error != null) {
            Mensajes.error(error);
            return;
        }

        // Nombre
        String nombre = ValidadorProducto.normalizarNombre(LectorConsola.leerTexto("Nombre: "));
        error = ValidadorProducto.validarNombre(nombre);
        if (error != null) {
            Mensajes.error(error);
            return;
        }

        // Precio
        BigDecimal precio = leerPrecio(LectorConsola.leerTexto("Precio (ejemplo 49.90): "));
        if (precio == null) {
            Mensajes.error("El precio debe ser un número (ejemplo: 49.90).");
            return;
        }
        error = ValidadorProducto.validarPrecio(precio);
        if (error != null) {
            Mensajes.error(error);
            return;
        }

        // Stock inicial
        int stock = LectorConsola.leerEntero("Stock inicial: ");
        error = ValidadorProducto.validarStock(stock);
        if (error != null) {
            Mensajes.error(error);
            return;
        }

        int categoriaId = LectorConsola.leerEntero("ID de categoría: ");
        error = ValidadorProducto.validarId(categoriaId, "categoría");
        if (error != null) {
            Mensajes.error(error);
            return;
        }
        int proveedorId = LectorConsola.leerEntero("ID de proveedor: ");
        error = ValidadorProducto.validarId(proveedorId, "proveedor");
        if (error != null) {
            Mensajes.error(error);
            return;
        }

        try {
            if (productoDao.buscarPorCodigo(codigo) != null) {
                Mensajes.error("Ya existe un producto con el código " + codigo + ".");
                return;
            }
            if (categoriaDao.buscarPorId(categoriaId) == null) {
                Mensajes.error("No existe la categoría con ID " + categoriaId
                        + ". Un producto solo puede asociarse a una categoría existente.");
                return;
            }
            if (proveedorDao.buscarPorId(proveedorId) == null) {
                Mensajes.error("No existe el proveedor con ID " + proveedorId
                        + ". Un producto solo puede asociarse a un proveedor existente.");
                return;
            }
            Producto producto = new Producto(codigo, nombre, precio, stock, categoriaId, proveedorId);
            int filas = productoDao.insertar(producto);
            if (filas == 1) {
                Mensajes.ok("Producto " + codigo + " guardado correctamente.");
            } else {
                Mensajes.error("No se ha guardado el producto.");
            }
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ CONSULTAR
    private void consultar() {
        System.out.println("\nCONSULTAR PRODUCTO");
        System.out.println("----------------------------------------");
        String codigo = ValidadorProducto.normalizarCodigo(LectorConsola.leerTexto("Código (Intro para volver): "));
        if (codigo.isEmpty()) {
            return;
        }
        try {
            Producto producto = productoDao.buscarPorCodigo(codigo);
            if (producto == null) {
                Mensajes.aviso("No existe ningún producto con el código " + codigo + ".");
                return;
            }
            mostrarProducto(producto);
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ MODIFICAR
    private void modificar() {
        System.out.println("\nMODIFICAR PRODUCTO");
        System.out.println("----------------------------------------");
        String codigo = ValidadorProducto.normalizarCodigo(LectorConsola.leerTexto("Código (Intro para volver): "));
        if (codigo.isEmpty()) {
            return;
        }
        try {
            Producto producto = productoDao.buscarPorCodigo(codigo);
            if (producto == null) {
                Mensajes.error("No existe ningún producto con el código " + codigo + ".");
                return;
            }
            mostrarProducto(producto);
            System.out.println("(Pulsa Intro para conservar cada valor. El código y el stock no se modifican aquí;");
            System.out.println(" el stock cambia con las entradas y salidas del menú de movimientos.)");

            // Nombre
            String texto = LectorConsola.leerTexto("Nuevo nombre: ");
            if (!texto.isBlank()) {
                String nombre = ValidadorProducto.normalizarNombre(texto);
                String error = ValidadorProducto.validarNombre(nombre);
                if (error != null) {
                    Mensajes.error(error);
                    return;
                }
                producto.setNombre(nombre);
            }

            // Precio
            texto = LectorConsola.leerTexto("Nuevo precio: ");
            if (!texto.isBlank()) {
                BigDecimal precio = leerPrecio(texto);
                if (precio == null) {
                    Mensajes.error("El precio debe ser un número (ejemplo: 49.90).");
                    return;
                }
                String error = ValidadorProducto.validarPrecio(precio);
                if (error != null) {
                    Mensajes.error(error);
                    return;
                }
                producto.setPrecio(precio);
            }

            // Categoría
            texto = LectorConsola.leerTexto("Nuevo ID de categoría: ");
            if (!texto.isBlank()) {
                int categoriaId = leerId(texto);
                if (categoriaId <= 0) {
                    Mensajes.error("El ID de categoría debe ser un número entero positivo.");
                    return;
                }
                if (categoriaDao.buscarPorId(categoriaId) == null) {
                    Mensajes.error("No existe la categoría con ID " + categoriaId + ".");
                    return;
                }
                producto.setCategoriaId(categoriaId);
            }

            // Proveedor
            texto = LectorConsola.leerTexto("Nuevo ID de proveedor: ");
            if (!texto.isBlank()) {
                int proveedorId = leerId(texto);
                if (proveedorId <= 0) {
                    Mensajes.error("El ID de proveedor debe ser un número entero positivo.");
                    return;
                }
                if (proveedorDao.buscarPorId(proveedorId) == null) {
                    Mensajes.error("No existe el proveedor con ID " + proveedorId + ".");
                    return;
                }
                producto.setProveedorId(proveedorId);
            }

            System.out.println("\nQuedará así:");
            mostrarProducto(producto);
            if (!LectorConsola.confirmar(amarillo("[AVISO] ¿Guardar los cambios?"))) {
                Mensajes.aviso("Modificación cancelada. No se ha cambiado nada.");
                return;
            }
            int filas = productoDao.modificar(producto);
            if (filas == 1) {
                Mensajes.ok("Producto " + codigo + " modificado correctamente.");
            } else {
                Mensajes.error("El producto " + codigo + " ya no existe; no se ha modificado nada.");
            }
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ ELIMINAR
    private void eliminar() {
        System.out.println("\nELIMINAR PRODUCTO");
        System.out.println("----------------------------------------");
        String codigo = ValidadorProducto.normalizarCodigo(LectorConsola.leerTexto("Código (Intro para volver): "));
        if (codigo.isEmpty()) {
            return;
        }
        try {
            Producto producto = productoDao.buscarPorCodigo(codigo);
            if (producto == null) {
                Mensajes.error("No existe ningún producto con el código " + codigo + ".");
                return;
            }
            mostrarProducto(producto);
            if (!LectorConsola.confirmar(amarillo("[AVISO] ¿Confirmas la eliminación?"))) {
                Mensajes.aviso("Eliminación cancelada. No se ha borrado nada.");
                return;
            }
            int filas = productoDao.eliminar(codigo);
            if (filas == 1) {
                Mensajes.ok("Producto " + codigo + " eliminado correctamente.");
            } else {
                Mensajes.error("El producto " + codigo + " ya no existe; no se ha borrado nada.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                Mensajes.error("No se puede eliminar " + codigo + " porque tiene movimientos de stock registrados."
                        + " Se conserva el producto y su historial.");
            } else {
                Mensajes.error(ConexionBD.explicarError(e));
            }
        }
    }

    // ------------------------------------------------------------ AYUDAS

    private void mostrarProducto(Producto p) throws SQLException {
        Categoria categoria = categoriaDao.buscarPorId(p.getCategoriaId());
        Proveedor proveedor = proveedorDao.buscarPorId(p.getProveedorId());

        System.out.println("Código: " + p.getCodigo());
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Precio: " + p.getPrecio() + " €");
        System.out.println("Stock: " + p.getStock());
        if (categoria != null) {
            System.out.println("Categoría: " + p.getCategoriaId() + " - " + categoria.getNombre());
        } else {
            System.out.println("Categoría: " + p.getCategoriaId());
        }
        if (proveedor != null) {
            System.out.println("Proveedor: " + p.getProveedorId() + " - " + proveedor.getNombre());
        } else {
            System.out.println("Proveedor: " + p.getProveedorId());
        }
    }

    private BigDecimal leerPrecio(String texto) {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int leerId(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String amarillo(String texto) {
        return ColoresConsola.pintar(texto, ColoresConsola.AMARILLO);
    }
}
