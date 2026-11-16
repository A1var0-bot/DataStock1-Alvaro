package es.iesisidra.datastock.componentes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.modelo.ProductoDetalle;

/**
 * Componente reutilizable que consulta un producto de MySQL por su código mediante JDBC
 * Obtiene la conexión con {@link ConexionBD#obtenerConexion()} (fichero datastock.properties
 * o variables de entorno) y ejecuta un {@link PreparedStatement} con JOIN a categoria 
 * No muestra nada por consola: devuelve el resultado y lanza la excepción, para que lo presente
 * quien lo use (un menú, otra aplicación o una prueba).
 */
public class ConsultaProductoJdbc {

    private static final String SQL =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, "
          + "c.nombre AS categoria, pr.nombre AS proveedor "
          + "FROM producto p "
          + "JOIN categoria c ON p.categoria_id = c.id "
          + "JOIN proveedor pr ON p.proveedor_id = pr.id "
          + "WHERE p.codigo = ?";

    /**
     * Busca un producto por su código exacto.
     * @param codigo código del producto, ya normalizado (por ejemplo {@code "TEC-001"})
     * @return un {@link Optional} con el producto y los nombres de su categoría y proveedor,
     *o {@link Optional#empty()} si no existe ningún producto con ese código o el código está vacío
     * @throws SQLException si no se puede conectar con MySQL o falla la consulta
     */
    public Optional<ProductoDetalle> buscarPorCodigo(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL)) {
            sentencia.setString(1, codigo);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    ProductoDetalle producto = new ProductoDetalle(
                            resultado.getString("codigo"),
                            resultado.getString("nombre"),
                            resultado.getBigDecimal("precio"),
                            resultado.getInt("stock"),
                            resultado.getString("categoria"),
                            resultado.getString("proveedor"));
                    return Optional.of(producto);
                }
                return Optional.empty();
            }
        }
    }
}
