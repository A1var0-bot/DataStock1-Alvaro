package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.comprobaciones.ComprobacionesBD;
import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.dao.CategoriaDao;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

import java.sql.Connection;
import java.sql.SQLException;

public class DemoTransacciones {

    private static final String LINEA = "------------------------------------------------";
    private static final int ID_MINIMO_DEMO = 9000;

    private final CategoriaDao categoriaDao = new CategoriaDao();

    public void ejecutar() {
        System.out.println("\nDEMOSTRACIÓN DE TRANSACCIONES JDBC (COMMIT Y ROLLBACK)");
        System.out.println(LINEA);

        int idA1;
        try {
            idA1 = Math.max(categoriaDao.maximoId(), ID_MINIMO_DEMO) + 1;
        } catch (SQLException e) {
            Mensajes.error("No se pudo conectar con MySQL: " + ConexionBD.explicarError(e));
            return;
        }
        int idA2 = idA1 + 1;
        int idB1 = idA1 + 2;

        System.out.println("Es una prueba técnica: se usan categorías de prueba con IDs libres (a partir de " + ID_MINIMO_DEMO + ").");
        System.out.println("Escenario A (commit):   insertar las categorías " + idA1 + " y " + idA2 + " y confirmar con commit.");
        System.out.println("Escenario B (rollback): insertar la categoría " + idB1 + " y, en la misma transacción,");
        System.out.println("                        volver a insertar la " + idA1 + " (clave duplicada) para provocar un error.");
        if (!LectorConsola.confirmar(ColoresConsola.pintar("[AVISO] ¿Comenzar la demostración?", ColoresConsola.AMARILLO))) {
            Mensajes.aviso("Demostración cancelada. No se ha cambiado nada.");
            return;
        }

        try (Connection conexion = ConexionBD.obtenerConexion()) {
            boolean autoCommitOriginal = conexion.getAutoCommit();
            try {
                conexion.setAutoCommit(false);
                escenarioCommit(conexion, idA1, idA2);
                escenarioRollback(conexion, idA1, idB1);
            } finally {
                conexion.setAutoCommit(autoCommitOriginal);
                System.out.println("\nautoCommit restaurado a " + conexion.getAutoCommit() + ". Recursos cerrados.");
            }
        } catch (SQLException e) {
            Mensajes.error("Error inesperado en la demostración: " + ConexionBD.explicarError(e));
        }

        mostrarLimpieza(idA1, idA2);
    }

    private void escenarioCommit(Connection conexion, int idA1, int idA2) throws SQLException {
        System.out.println("\n" + LINEA);
        System.out.println("ESCENARIO A: COMMIT");
        System.out.println(LINEA);
        System.out.println("setAutoCommit(false)");
        try {
            categoriaDao.insertar(conexion, new Categoria(idA1, "Demo transacción " + idA1));
            System.out.println("INSERT categoría " + idA1);
            categoriaDao.insertar(conexion, new Categoria(idA2, "Demo transacción " + idA2));
            System.out.println("INSERT categoría " + idA2);
            conexion.commit();
            System.out.println("commit()");
        } catch (SQLException e) {
            conexion.rollback();
            Mensajes.error("Fallo inesperado en el escenario A; se ha hecho rollback: " + ConexionBD.explicarError(e));
            return;
        }

        boolean existeA1 = ComprobacionesBD.existeCategoria(conexion, idA1);
        boolean existeA2 = ComprobacionesBD.existeCategoria(conexion, idA2);
        System.out.println("Comprobación con SELECT -> categoría " + idA1 + ": " + existeA1 + " | categoría " + idA2 + ": " + existeA2);
        if (existeA1 && existeA2) {
            Mensajes.ok("Commit correcto: las dos categorías se han guardado en MySQL.");
        } else {
            Mensajes.error("Algo ha fallado: no aparecen las dos categorías.");
        }
    }

    private void escenarioRollback(Connection conexion, int idA1, int idB1) throws SQLException {
        System.out.println("\n" + LINEA);
        System.out.println("ESCENARIO B: ROLLBACK");
        System.out.println(LINEA);
        try {
            categoriaDao.insertar(conexion, new Categoria(idB1, "Demo transacción " + idB1));
            System.out.println("INSERT categoría " + idB1 + " (pendiente de confirmar)");
            System.out.println("INSERT categoría " + idA1 + " otra vez (clave duplicada)...");
            categoriaDao.insertar(conexion, new Categoria(idA1, "Demo duplicada " + idA1));
            conexion.commit();
            Mensajes.error("No se ha producido el error esperado.");
        } catch (SQLException e) {
            Mensajes.error("Error esperado: " + ConexionBD.explicarError(e) + " (código " + e.getErrorCode() + ")");
            conexion.rollback();
            System.out.println("rollback()");
        }

        boolean existeB1 = ComprobacionesBD.existeCategoria(conexion, idB1);
        System.out.println("Comprobación con SELECT -> categoría " + idB1 + ": " + existeB1);
        if (!existeB1) {
            Mensajes.ok("Rollback correcto: la categoría " + idB1 + " NO ha quedado guardada. No hay inserciones parciales.");
        } else {
            Mensajes.error("Algo ha fallado: la categoría " + idB1 + " sigue en MySQL.");
        }
    }

    private void mostrarLimpieza(int idA1, int idA2) {
        System.out.println("\n" + LINEA);
        System.out.println("DATOS DE DEMOSTRACIÓN");
        System.out.println(LINEA);
        System.out.println("Han quedado guardadas las categorías de prueba " + idA1 + " y " + idA2 + " (no tienen productos).");
        System.out.println("Para borrarlas: menú 7.2 > 4 (Eliminar categoría) con cada ID, o en MySQL Workbench:");
        System.out.println("    DELETE FROM categoria WHERE id IN (" + idA1 + ", " + idA2 + ");");
    }
}
