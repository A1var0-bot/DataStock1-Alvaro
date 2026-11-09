package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.dao.CategoriaDao;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

/**
 * Submenú 7.2: gestión de categorías en MySQL (apartado 5.3, RF-JDBC-02).
 * Pide los datos, los valida y llama a CategoriaDAO, que es quien ejecuta el SQL.
 */
public class MenuCategorias {

    private final CategoriaDao dao = new CategoriaDao();

    public void iniciar() {
        int opcion;
        do {
            System.out.println("\n========== MENÚ DE CATEGORÍAS ==========");
            System.out.println("1. Dar de alta una categoría");
            System.out.println("2. Listar categorías");
            System.out.println("3. Modificar el nombre de una categoría");
            System.out.println("4. Eliminar una categoría");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);

            switch (opcion) {
                case 1:
                    alta();
                    break;
                case 2:
                    listar();
                    break;
                case 3:
                    modificar();
                    break;
                case 4:
                    eliminar();
                    break;
                default:
                    break;
            }
            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }

    // ------------------------------------------------------------ 1. ALTA
    private void alta() {
        System.out.println("\nALTA DE CATEGORÍA");
        System.out.println("----------------------------------------");
        int id = LectorConsola.leerEntero("ID de categoría (0 para volver): ");
        if (id == 0) {
            return;
        }
        if (id < 0) {
            Mensajes.error("El ID debe ser un número entero positivo.");
            return;
        }
        String nombre = LectorConsola.leerTexto("Nombre: ").trim();
        String error = validarNombre(nombre);
        if (error != null) {
            Mensajes.error(error);
            return;
        }

        try {
            if (dao.buscarPorId(id) != null) {
                Mensajes.error("Ya existe una categoría con el ID " + id + ".");
                return;
            }
            if (dao.existeNombre(nombre, 0)) {
                Mensajes.error("Ya existe una categoría llamada '" + nombre + "'.");
                return;
            }
            int filas = dao.insertar(new Categoria(id, nombre));
            if (filas == 1) {
                Mensajes.ok("Categoría registrada correctamente.");
            } else {
                Mensajes.error("No se ha registrado la categoría.");
            }
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ 2. LISTADO
    private void listar() {
        System.out.println("\nLISTADO DE CATEGORÍAS");
        System.out.println("----------------------------------------");
        try {
            List<Categoria> categorias = dao.listar();
            if (categorias.isEmpty()) {
                System.out.println("No hay categorías registradas.");
                return;
            }
            System.out.printf("%-6s%s%n", "ID", "NOMBRE");
            System.out.println("----------------------------------------");
            for (Categoria c : categorias) {
                System.out.printf("%-6d%s%n", c.getId(), c.getNombre());
            }
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ 3. MODIFICAR
    private void modificar() {
        System.out.println("\nMODIFICAR CATEGORÍA");
        System.out.println("----------------------------------------");
        int id = LectorConsola.leerEntero("ID de la categoría a modificar (0 para volver): ");
        if (id == 0) {
            return;
        }
        try {
            Categoria categoria = dao.buscarPorId(id);
            if (categoria == null) {
                Mensajes.error("No existe ninguna categoría con el ID " + id + ".");
                return;
            }
            System.out.println("Nombre actual: " + categoria.getNombre());

            String nuevo = LectorConsola.leerTexto("Nuevo nombre: ").trim();
            String error = validarNombre(nuevo);
            if (error != null) {
                Mensajes.error(error);
                return;
            }
            if (dao.existeNombre(nuevo, id)) {
                Mensajes.error("Ya existe otra categoría llamada '" + nuevo + "'.");
                return;
            }
            if (!LectorConsola.confirmar(amarillo("[AVISO] ¿Cambiar '" + categoria.getNombre() + "' por '" + nuevo + "'?"))) {
                Mensajes.aviso("Modificación cancelada. No se ha cambiado nada.");
                return;
            }
            int filas = dao.modificarNombre(id, nuevo);
            if (filas == 1) {
                Mensajes.ok("Categoría " + id + " modificada correctamente.");
            } else {
                Mensajes.error("La categoría " + id + " ya no existe; no se ha modificado nada.");
            }
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ 4. ELIMINAR
    private void eliminar() {
        System.out.println("\nELIMINAR CATEGORÍA");
        System.out.println("----------------------------------------");
        int id = LectorConsola.leerEntero("ID de la categoría a eliminar (0 para volver): ");
        if (id == 0) {
            return;
        }
        try {
            Categoria categoria = dao.buscarPorId(id);
            if (categoria == null) {
                Mensajes.error("No existe ninguna categoría con el ID " + id + ".");
                return;
            }
            System.out.println("ID: " + categoria.getId());
            System.out.println("Nombre: " + categoria.getNombre());

            if (!LectorConsola.confirmar(amarillo("[AVISO] ¿Confirmas la eliminación?"))) {
                Mensajes.aviso("Eliminación cancelada. No se ha borrado nada.");
                return;
            }
            int filas = dao.eliminar(id);
            if (filas == 1) {
                Mensajes.ok("Categoría " + id + " eliminada correctamente.");
            } else {
                Mensajes.error("La categoría " + id + " ya no existe; no se ha borrado nada.");
            }
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    // ------------------------------------------------------------ AYUDAS

    private String validarNombre(String nombre) {
        if (nombre.isEmpty()) {
            return "El nombre es obligatorio.";
        }
        if (nombre.length() > 60) {
            return "El nombre no puede tener más de 60 caracteres.";
        }
        return null;
    }

    private String amarillo(String texto) {
        return ColoresConsola.pintar(texto, ColoresConsola.AMARILLO);
    }
}