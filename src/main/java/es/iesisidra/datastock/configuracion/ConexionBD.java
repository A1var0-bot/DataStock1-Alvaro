package es.iesisidra.datastock.configuracion;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {

    private static final String fichero = "datastock.properties";
    private static final int SEGUNDOS_ESPERA = 5;

    /**
     * @throws SQLException si falta la configuración o MySQL rechaza la conexión
     */
    public static Connection obtenerConexion() throws SQLException {
        String url;
        String usuario;
        String clave;

        Properties config = leerFichero();
        if (config != null) {
            url = config.getProperty("url");
            usuario = config.getProperty("usuario");
            clave = config.getProperty("clave");
        } else {
            url = System.getenv("JDBC_URL");
            usuario = System.getenv("JDBC_USER");
            clave = System.getenv("JDBC_PASSWORD");
        }

        if (url == null || url.isBlank() || usuario == null || usuario.isBlank() || clave == null) {
            throw new SQLException("faltan datos de conexión: crea " + fichero + " (copia datastock.properties.ejemplo) o define JDBC_URL, JDBC_USER y JDBC_PASSWORD.");
        }

        DriverManager.setLoginTimeout(SEGUNDOS_ESPERA);
        return DriverManager.getConnection(url.trim(), usuario.trim(), clave);
    }

    private static Properties leerFichero() {
        Properties config = new Properties();
        try (FileInputStream entrada = new FileInputStream(fichero)) {
            config.load(entrada);
            return config;
        } catch (IOException e) {
            return null;
        }
    }

    public static String explicarError(SQLException e) {
        int codigo = e.getErrorCode();
        String estado = e.getSQLState();

        if (codigo == 1045) {
            return "usuario o contraseña incorrectos.";
        }
        if (codigo == 1049) {
            return "la base de datos no existe. Ejecuta antes el script DataStock_BD_MySQL.sql.";
        }
        if (estado != null && estado.startsWith("08")) {
            return "no se puede conectar con el servidor. Comprueba que MySQL está arrancado y la URL es correcta.";
        }
        if (codigo == 0 && estado == null) {
            return "Error. " + e.getMessage();
        }
        return "error de base de datos (código " + codigo + "): " + e.getMessage();
    }
    
}
