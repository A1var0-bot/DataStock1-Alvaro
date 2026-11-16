package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Estado común del inventario durante la ejecución de UT1.
 * <p>Esta clase solo proporciona la colección compartida. No implementa
 * importación, validación, búsqueda, exportación ni persistencia: esos son
 * ejercicios que deberá desarrollar el alumnado.</p>
 */
public class Inventario {
    private final List<Producto> productos = new ArrayList<>();

    /**
     * Obtiene la colección que comparten las opciones del menú.
     * <p>ATENCIÓN: la lista es modificable. El alumnado deberá aplicar las
     * reglas del enunciado antes de añadir o sustituir elementos.</p>
     */

    /**
     * Añade al inventario los productos válidos de una lectura.
     * <p>Si un código ya existe en el inventario (sin distinguir mayúsculas), ese producto
     * no se añade y se anota como «código duplicado» en el resultado.</p>
     * @param resultado resultado devuelto por {@code LectorProductosJson#leer};                                                                                       sus contadores de importados y sus incidencias
     */
    public void agregarProductos(ResultadoImportacion resultado) {
        for (Producto nuevo : resultado.getProductos()) {
            if (existeCodigo(nuevo.getCodigo())) {
                resultado.registrarRechazo(nuevo.getCodigo(), "código duplicado");
            } else {
                productos.add(nuevo);
                resultado.registrarImportado();
            }
        }
    }

    private boolean existeCodigo(String codigo) {
        for (Producto p : productos) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                return true;
            }
        }
        return false;
    }
     
    public List<Producto> getProductos() { return productos; }
}
