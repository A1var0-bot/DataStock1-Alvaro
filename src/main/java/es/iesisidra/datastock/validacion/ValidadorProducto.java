package es.iesisidra.datastock.validacion;

import es.iesisidra.datastock.modelo.Producto;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Reglas de validación comunes de un {@link Producto} (apartado 6 del enunciado).
 * <p>Es la ÚNICA clase que conoce estas reglas. La importación JSON (RF-FIC-02)
 * y la XML (RF-FIC-03) la usan por igual, así que ambos formatos validan de forma
 * idéntica ("exactamente las mismas validaciones").</p>
 */
public final class ValidadorProducto {

    /** Patrón del modelo de datos: letras, dígitos y guion. Se admiten minúsculas
     *  porque la unicidad y las búsquedas ignoran mayúsculas (decisión a documentar). */
    private static final Pattern PATRON_CODIGO =
            Pattern.compile("[A-Z0-9-]+", Pattern.CASE_INSENSITIVE);

    private ValidadorProducto() { }

    /**
     * Comprueba un producto ya construido.
     * @return el motivo del PRIMER error encontrado, o {@code null} si es válido.
     */
    public static String validar(Producto p) {
        // --- código ---
        String codigo = p.getCodigo() == null ? "" : p.getCodigo().trim();
        if (codigo.isEmpty())            return "el código es obligatorio";
        if (codigo.length() > 12)        return "el código no puede superar 12 caracteres";
        if (!PATRON_CODIGO.matcher(codigo).matches())
                                         return "el código solo admite letras, dígitos y guion";

        // --- nombre ---
        String nombre = p.getNombre() == null ? "" : p.getNombre().trim();
        if (nombre.isEmpty())            return "el nombre es obligatorio";
        if (nombre.length() < 3 || nombre.length() > 100)
                                         return "el nombre debe tener entre 3 y 100 caracteres";

        // --- precio ---
        if (p.getPrecio() == null || p.getPrecio().compareTo(BigDecimal.ZERO) <= 0)
                                         return "el precio debe ser mayor que 0";

        // --- stock y claves ---
        if (p.getStock() < 0)            return "el stock no puede ser negativo";
        if (p.getCategoriaId() <= 0)     return "la categoría debe ser un entero positivo";
        if (p.getProveedorId() <= 0)     return "el proveedor debe ser un entero positivo";

        return null; // todo correcto
    }
}
