package es.iesisidra.datastock.comprobaciones;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.util.Mensajes;

public class ComprobarConexion {

    public static void mostrarInformacion() {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement("SELECT COUNT(*) FROM producto");
             ResultSet resultado = sentencia.executeQuery()) {

            Mensajes.ok("Conexión establecida correctamente.");

            DatabaseMetaData datos = conexion.getMetaData();
            System.out.println("Base de datos: " + conexion.getCatalog());
            System.out.println("Gestor: " + datos.getDatabaseProductName());
            System.out.println("Versión: " + datos.getDatabaseProductVersion());

            int productos = 0;
            if (resultado.next()) {
                productos = resultado.getInt(1);
            }
            System.out.println("Productos almacenados: " + productos);

        } catch (SQLException e) {
            Mensajes.error("No se pudo conectar con MySQL: " + ConexionBD.explicarError(e));
        }
    }

    // Para probar el apartado 4 antes de tener el menú 7
    public static void main(String[] args) {
        mostrarInformacion();
    }
    
}
