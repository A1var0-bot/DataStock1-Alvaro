package es.iesisidra.datastock.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.modelo.ProductoDetalle;

public class ConsultaProductosDao {

    private static final String SELECT_CON_JOIN =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, "
          + "c.nombre AS categoria, pr.nombre AS proveedor "
          + "FROM producto p "
          + "JOIN categoria c ON p.categoria_id = c.id "
          + "JOIN proveedor pr ON p.proveedor_id = pr.id ";

    public List<ProductoDetalle> listarTodos() throws SQLException {
        String sql = SELECT_CON_JOIN + "ORDER BY p.codigo";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            return leerFilas(resultado);
        }
    }

    /** 2. Búsqueda exacta por código. @return el producto, o null si no existe. */
    public ProductoDetalle buscarPorCodigo(String codigo) throws SQLException {
        String sql = SELECT_CON_JOIN + "WHERE p.codigo = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, codigo);
            try (ResultSet resultado = sentencia.executeQuery()) {
                List<ProductoDetalle> lista = leerFilas(resultado);
                if (lista.isEmpty()) {
                    return null;
                }
                return lista.get(0);
            }
        }
    }

    public List<ProductoDetalle> buscarPorNombre(String fragmento) throws SQLException {
        String sql = SELECT_CON_JOIN + "WHERE p.nombre LIKE ? ORDER BY p.codigo";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, "%" + fragmento + "%");
            try (ResultSet resultado = sentencia.executeQuery()) {
                return leerFilas(resultado);
            }
        }
    }

    public List<ProductoDetalle> stockInferiorA(int umbral) throws SQLException {
        String sql = SELECT_CON_JOIN + "WHERE p.stock < ? ORDER BY p.stock, p.codigo";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, umbral);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return leerFilas(resultado);
            }
        }
    }

    private List<ProductoDetalle> leerFilas(ResultSet resultado) throws SQLException {
        List<ProductoDetalle> lista = new ArrayList<>();
        while (resultado.next()) {
            ProductoDetalle producto = new ProductoDetalle(
                    resultado.getString("codigo"),
                    resultado.getString("nombre"),
                    resultado.getBigDecimal("precio"),
                    resultado.getInt("stock"),
                    resultado.getString("categoria"),
                    resultado.getString("proveedor"));
            lista.add(producto);
        }
        return lista;
    }
}
