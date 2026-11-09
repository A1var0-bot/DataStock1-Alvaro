package es.iesisidra.datastock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;

public class ProductoDao {

    /** Inserta un producto. @return filas insertadas */
    public int insertar(Producto producto) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) " + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, producto.getCodigo());
            sentencia.setString(2, producto.getNombre());
            sentencia.setBigDecimal(3, producto.getPrecio());
            sentencia.setInt(4, producto.getStock());
            sentencia.setInt(5, producto.getCategoriaId());
            sentencia.setInt(6, producto.getProveedorId());
            return sentencia.executeUpdate();
        }
    }

    /** Busca un producto por su código. @return el producto, o null si no existe. */
    public Producto buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT codigo, nombre, precio, stock, categoria_id, proveedor_id " + "FROM producto WHERE codigo = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, codigo);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return new Producto(
                            resultado.getString("codigo"),
                            resultado.getString("nombre"),
                            resultado.getBigDecimal("precio"),
                            resultado.getInt("stock"),
                            resultado.getInt("categoria_id"),
                            resultado.getInt("proveedor_id"));
                }
                return null;
            }
        }
    }

    public int modificar(Producto producto) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? " + "WHERE codigo = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, producto.getNombre());
            sentencia.setBigDecimal(2, producto.getPrecio());
            sentencia.setInt(3, producto.getCategoriaId());
            sentencia.setInt(4, producto.getProveedorId());
            sentencia.setString(5, producto.getCodigo());
            return sentencia.executeUpdate();
        }
    }

    public int eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, codigo);
            return sentencia.executeUpdate();
        }
    }
}
