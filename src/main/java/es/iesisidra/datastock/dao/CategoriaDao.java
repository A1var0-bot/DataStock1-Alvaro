package es.iesisidra.datastock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.modelo.Categoria;

public class CategoriaDao {

    /** Inserta una categoría. @return filas insertadas (1 si ha ido bien). */
    public int insertar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, categoria.getId());
            sentencia.setString(2, categoria.getNombre());
            return sentencia.executeUpdate();
        }
    }

    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre FROM categoria ORDER BY id";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                Categoria categoria = new Categoria(resultado.getInt("id"), resultado.getString("nombre"));
                lista.add(categoria);
            }
        }
        return lista;
    }

    public Categoria buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM categoria WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return new Categoria(resultado.getInt("id"), resultado.getString("nombre"));
                }
                return null;
            }
        }
    }

    public boolean existeNombre(String nombre, int idExcluido) throws SQLException {
        String sql = "SELECT 1 FROM categoria WHERE nombre = ? AND id <> ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nombre);
            sentencia.setInt(2, idExcluido);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next();
            }
        }
    }

    public int modificarNombre(int id, String nuevoNombre) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, nuevoNombre);
            sentencia.setInt(2, id);
            return sentencia.executeUpdate();
        }
    }

    public int eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate();
        }
    }
}