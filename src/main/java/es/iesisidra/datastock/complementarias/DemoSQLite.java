package es.iesisidra.datastock.complementarias;

import es.iesisidra.datastock.util.Mensajes;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DemoSQLite {

    private static final String CARPETA = "datos/sqlite";
    private static final String FICHERO = CARPETA + "/productos_demo.db";
    private static final String URL = "jdbc:sqlite:" + FICHERO;

    public static void main(String[] args) {
        System.out.println("DEMOSTRACIÓN SQLITE (gestor embebido)");
        System.out.println("----------------------------------------");

        File carpeta = new File(CARPETA);
        if (!carpeta.exists()) {
            if (carpeta.mkdirs()) {
                Mensajes.ok("Carpeta creada: " + CARPETA);
            } else {
                Mensajes.error("No se pudo crear la carpeta " + CARPETA);
                return;
            }
        }

        File ficheroBD = new File(FICHERO);
        boolean existiaAntes = ficheroBD.exists();

        try (Connection conexion = DriverManager.getConnection(URL)) {
            if (existiaAntes) {
                Mensajes.ok("Conexión SQLite abierta: " + FICHERO + " (el fichero ya existía)");
            } else {
                Mensajes.ok("Conexión SQLite abierta: " + FICHERO + " (fichero creado ahora)");
            }

            crearTabla(conexion);
            insertarDatos(conexion);
            mostrarDatos(conexion);

        } catch (SQLException e) {
            Mensajes.error("Error de SQLite: " + e.getMessage());
            return;
        }

        Mensajes.ok("Conexión, sentencias y resultados cerrados (try-with-resources).");
        if (ficheroBD.exists()) {
            Mensajes.ok("El fichero sigue existiendo tras cerrar: " + ficheroBD.getAbsolutePath()
                    + " (" + ficheroBD.length() + " bytes)");
        } else {
            Mensajes.error("No se encuentra el fichero " + FICHERO);
        }
    }

    private static void crearTabla(Connection conexion) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS producto_demo ("
                   + "codigo TEXT PRIMARY KEY NOT NULL, "
                   + "nombre TEXT NOT NULL)";
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute(sql);
        }
        Mensajes.ok("Tabla producto_demo disponible.");
    }

    private static void insertarDatos(Connection conexion) throws SQLException {
        String sql = "INSERT OR IGNORE INTO producto_demo (codigo, nombre) VALUES (?, ?)";
        String[][] datos = {
                {"SQL-001", "Producto demo A"},
                {"SQL-002", "Producto demo B"}
        };
        int insertados = 0;
        int existentes = 0;

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (String[] fila : datos) {
                sentencia.setString(1, fila[0]);
                sentencia.setString(2, fila[1]);
                int filas = sentencia.executeUpdate();
                if (filas == 1) {
                    insertados++;
                } else {
                    existentes++;
                }
            }
        }
        System.out.println("Insertados: " + insertados + " | Ya existentes: " + existentes);
    }

    private static void mostrarDatos(Connection conexion) throws SQLException {
        String sql = "SELECT codigo, nombre FROM producto_demo ORDER BY codigo";
        try (Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {
            while (resultado.next()) {
                System.out.println(resultado.getString("codigo") + " | " + resultado.getString("nombre"));
            }
        }
    }
}