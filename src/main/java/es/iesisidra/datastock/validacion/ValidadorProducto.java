package es.iesisidra.datastock.validacion;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import es.iesisidra.datastock.modelo.Producto;

public final class ValidadorProducto {

    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");
    private static final Pattern PATRON_CODIGO = Pattern.compile("[A-Z0-9-]+", Pattern.CASE_INSENSITIVE);

    // Validar Codigo ====================================================
    public static String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }
        return codigo.trim().toUpperCase();
    }

    public static String validarCodigo(String codigo) {
        if (codigo == null || codigo.isEmpty()) {
            return "el código es obligatorio.";
        }
        if (codigo.length() > 12) {
            return "el código no puede tener más de 12 caracteres.";
        }
        if (!codigo.matches("[A-Z0-9-]+")) {
            return "el código solo puede contener letras mayúsculas (A-Z), números (0-9) y guiones (-).";
        }
        return null;
    }

    //Validar nombre ================================================
     public static String normalizarNombre(String nombre) {
        if (nombre == null) {
            return "";
        }
        return nombre.trim();
    }

    public static String validarNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return "el nombre es obligatorio.";
        }
        if (nombre.length() < 3) {
            return "el nombre debe tener al menos 3 caracteres.";
        }
        if (nombre.length() > 100) {
            return "el nombre no puede tener más de 100 caracteres.";
        }
        return null;
    }

    //Validar precio==========================================================
     public static String validarPrecio(BigDecimal precio) {
        if (precio == null) {
            return "el precio es obligatorio.";
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            return "el precio debe ser mayor que 0.";
        }
        if (precio.stripTrailingZeros().scale() > 2) {
            return "el precio no puede tener más de 2 decimales.";
        }
        if (precio.compareTo(PRECIO_MAXIMO) > 0) {
            return "el precio no puede ser mayor que " + PRECIO_MAXIMO + ".";
        }
        return null;
    }

    //Validar stock ===================================================================
      public static String validarStock(int stock) {
        if (stock < 0) {
            return "el stock no puede ser negativo.";
        }
        return null;
    }

    //Validar categoria y proveedor ==========================================================0
     public static String validarId(int id, String campo) {
        if (id <= 0) {
            return "el identificador de " + campo + " debe ser un entero positivo.";
        }
        return null;
    }

    public static String validarProducto(Producto p) {
        String error = validarCodigo(normalizarCodigo(p.getCodigo()));
        if (error != null) {
            return error;
        }
        error = validarNombre(normalizarNombre(p.getNombre()));
        if (error != null) {
            return error;
        }
        error = validarPrecio(p.getPrecio());
        if (error != null) {
            return error;
        }
        error = validarStock(p.getStock());
        if (error != null) {
            return error;
        }
        error = validarId(p.getCategoriaId(), "categoría");
        if (error != null) {
            return error;
        }
        return validarId(p.getProveedorId(), "proveedor");
    }

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
