package es.iesisidra.datastock.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.modelo.MovimientoStock;
import es.iesisidra.datastock.modelo.MovimientoStock.TipoMovimiento;

public class MovimientoDao {

    public void registrar(String codigo, TipoMovimiento tipo, int cantidad) throws SQLException {
        String sql = "{CALL registrar_movimiento(?, ?, ?)}";
        try (Connection conexion = ConexionBD.obtenerConexion();
             CallableStatement llamada = conexion.prepareCall(sql)) {
            llamada.setString(1, codigo);
            llamada.setString(2, tipo.name());  
            llamada.setInt(3, cantidad);
            llamada.execute();
        }
    }

    public MovimientoStock ultimoMovimiento(String codigo) throws SQLException {
        String sql = "SELECT id, producto_codigo, tipo, cantidad, fecha, stock_anterior, stock_posterior " + "FROM movimiento_stock WHERE producto_codigo = ? ORDER BY id DESC LIMIT 1";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, codigo);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return leerMovimiento(resultado);
                }
                return null;
            }
        }
    }

    private MovimientoStock leerMovimiento(ResultSet resultado) throws SQLException {
        return new MovimientoStock(
                resultado.getLong("id"),
                resultado.getString("producto_codigo"),
                TipoMovimiento.valueOf(resultado.getString("tipo")),
                resultado.getInt("cantidad"),
                resultado.getTimestamp("fecha").toLocalDateTime(),
                resultado.getInt("stock_anterior"),
                resultado.getInt("stock_posterior"));
    }
}