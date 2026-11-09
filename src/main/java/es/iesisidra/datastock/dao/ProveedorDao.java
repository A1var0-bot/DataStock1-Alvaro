package es.iesisidra.datastock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.modelo.Proveedor;

public class ProveedorDao {

    public int insertar(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, proveedor.getId());
            sentencia.setString(2, proveedor.getNombre());
            ponerEmail(sentencia, 3, proveedor.getEmail());
            return sentencia.executeUpdate();
        }
    }

    public List<Proveedor> listar() throws SQLException {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, email FROM proveedor ORDER BY id";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                Proveedor proveedor = new Proveedor(resultado.getInt("id"),
                        resultado.getString("nombre"), resultado.getString("email"));
                lista.add(proveedor);
            }
        }
        return lista;
    }

    public Proveedor buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, email FROM proveedor WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return new Proveedor(resultado.getInt("id"),
                            resultado.getString("nombre"), resultado.getString("email"));
                }
                return null;
            }
        }
    }

    public boolean existeEmail(String email, int idExcluido) throws SQLException {
        String sql = "SELECT 1 FROM proveedor WHERE email = ? AND id <> ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, email);
            sentencia.setInt(2, idExcluido);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public int modificar(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, proveedor.getNombre());
            ponerEmail(sentencia, 2, proveedor.getEmail());
            sentencia.setInt(3, proveedor.getId());
            return sentencia.executeUpdate();
        }
    }

    public int eliminar(int id) throws SQLException {
        String sql = "DELETE FROM proveedor WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate();
        }
    }

    private void ponerEmail(PreparedStatement sentencia, int posicion, String email) throws SQLException {
        if (email == null) {
            sentencia.setNull(posicion, Types.VARCHAR);
        } else {
            sentencia.setString(posicion, email);
        }
    }
}
