package es.iesisidra.datastock.comprobaciones;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComprobacionesBD {

    public static boolean existeProducto(Connection conexion, String codigo) throws SQLException {
        return existe(conexion, "SELECT 1 FROM producto WHERE codigo = ?", codigo);
    }

    public static boolean existeCategoria(Connection conexion, int id) throws SQLException {
        return existe(conexion, "SELECT 1 FROM categoria WHERE id = ?", id);
    }

    public static boolean existeProveedor(Connection conexion, int id) throws SQLException {
        return existe(conexion, "SELECT 1 FROM proveedor WHERE id = ?", id);
    }

    
    private static boolean existe(Connection conexion, String sql, Object valor) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setObject(1, valor);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }
    

}
