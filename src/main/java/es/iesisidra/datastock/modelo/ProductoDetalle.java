package es.iesisidra.datastock.modelo;

import java.math.BigDecimal;

public class ProductoDetalle {
    private final String codigo;
    private final String nombre;
    private final BigDecimal precio;
    private final int stock;
    private final String categoria;
    private final String proveedor;

    public ProductoDetalle(String codigo, String nombre, BigDecimal precio, int stock,
                           String categoria, String proveedor) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.proveedor = proveedor;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getCategoria() { return categoria; }
    public String getProveedor() { return proveedor; }
}