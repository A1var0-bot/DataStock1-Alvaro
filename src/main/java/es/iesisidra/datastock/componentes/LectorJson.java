package es.iesisidra.datastock.componentes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import es.iesisidra.datastock.ficheros.ImporExeption;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.ResultadoImportacion;
import es.iesisidra.datastock.validacion.ValidadorProducto;

/**
 * Componente reutilizable que lee productos desde un fichero JSON (UT3, apartado 4.1).
 * <p>
 * Formato esperado: un objeto raíz con la clave {@code "productos"}, que contiene un array
 * de objetos con los campos {@code codigo}, {@code nombre}, {@code precio}, {@code stock},
 * {@code categoriaId} y {@code proveedorId}.
 * </p>
 * <p>
 * El componente solo lee y valida: no muestra mensajes por consola, no lee del teclado,
 * no modifica el inventario y no accede a MySQL. Devuelve los productos válidos y el motivo
 * de cada registro rechazado para que quien lo use decida qué hacer con ellos.
 * </p>
 */

public class LectorJson {
    
     /**
     * Lee un fichero JSON de productos y valida cada registro.
     * <p>
     * Cada registro se comprueba con las mismas reglas que en la UT1 ({@link ValidadorProducto#validar}).
     * Si un código aparece repetido dentro del mismo fichero, solo se acepta la primera vez.
     * </p>
     *
     * @param textoRuta ruta relativa o absoluta del fichero JSON
     * @return un {@link ResultadoImportacion} con los productos válidos
     *         ({@link ResultadoImportacion#getProductos()}), el número de registros encontrados
     *         y una incidencia por cada registro rechazado (datos inválidos o código repetido)
     * @throws ImporExeption si la ruta está vacía o no es válida, el fichero no existe, es un
     *         directorio, no se puede leer, el JSON está mal formado o no tiene la estructura
     *         esperada. En ese caso no se devuelve ningún producto
     */
    public ResultadoImportacion leer(String textoRuta) throws ImporExeption {
        Path ruta = comprobarFichero(textoRuta);
        JsonElement raiz = leerDocumento(ruta);
        JsonArray registros = localizarArray(raiz);
        return procesarRegistros(registros);
    }

    private Path comprobarFichero(String textoRuta) throws ImporExeption {
        if (textoRuta == null || textoRuta.isBlank()) {
            throw new ImporExeption("no se ha indicado ninguna ruta.");
        }

        Path ruta;
        try {
            ruta = Path.of(textoRuta.trim());
        } catch (InvalidPathException e) {
            throw new ImporExeption("la ruta contiene caracteres no válidos.");
        }

        if (!Files.exists(ruta)) {
            throw new ImporExeption("el fichero no existe: " + ruta);
        }
        if (Files.isDirectory(ruta)) {
            throw new ImporExeption("la ruta es un directorio, no un fichero: " + ruta);
        }
        if (!Files.isReadable(ruta)) {
            throw new ImporExeption("no hay permisos de lectura sobre: " + ruta);
        }
        return ruta;
    }

    private JsonElement leerDocumento(Path ruta) throws ImporExeption {
        try {
            String contenido = Files.readString(ruta, StandardCharsets.UTF_8);
            if (!contenido.isEmpty() && contenido.charAt(0) == 0xFEFF) {
                contenido = contenido.substring(1);
            }
            return JsonParser.parseString(contenido);
        } catch (JsonParseException e) {
            throw new ImporExeption("el fichero no es un JSON bien formado.");
        } catch (IOException e) {
            throw new ImporExeption("no se pudo leer el fichero: " + e.getMessage());
        }
    }

    private JsonArray localizarArray(JsonElement raiz) throws ImporExeption {
        if (!raiz.isJsonObject()) {
            throw new ImporExeption("la raíz del documento debe ser un objeto JSON con la clave 'productos'.");
        }
        JsonElement productos = raiz.getAsJsonObject().get("productos");
        if (productos == null) {
            throw new ImporExeption("falta la clave 'productos' en el documento.");
        }
        if (!productos.isJsonArray()) {
            throw new ImporExeption("la clave 'productos' debe contener un array.");
        }
        return productos.getAsJsonArray();
    }

    private ResultadoImportacion procesarRegistros(JsonArray registros) {
        ResultadoImportacion resultado = new ResultadoImportacion();
        List<String> codigosLeidos = new ArrayList<>();
        int numero = 0;

        for (JsonElement elemento : registros) {
            numero++;
            resultado.registrarEncontrado();

            String motivo = comprobarRegistro(elemento);
            if (motivo != null) {
                resultado.registrarRechazo(nombreParaIncidencia(elemento, numero), motivo);
                continue;
            }

            Producto producto = crearProducto(elemento.getAsJsonObject());
            String codigo = producto.getCodigo().toUpperCase();
            if (codigosLeidos.contains(codigo)) {
                resultado.registrarRechazo(producto.getCodigo(), "código duplicado");
                continue;
            }
            codigosLeidos.add(codigo);
            resultado.agregarProductoValido(producto);
        }
        return resultado;
    }

    private String comprobarRegistro(JsonElement elemento) {
        if (!elemento.isJsonObject()) {
            return "el registro no es un objeto JSON";
        }
        JsonObject registro = elemento.getAsJsonObject();

        String error = comprobarDecimal(texto(registro, "precio"), "precio");
        if (error != null) {
            return error;
        }
        error = comprobarEntero(texto(registro, "stock"), "stock");
        if (error != null) {
            return error;
        }
        error = comprobarEntero(texto(registro, "categoriaId"), "categoriaId");
        if (error != null) {
            return error;
        }
        error = comprobarEntero(texto(registro, "proveedorId"), "proveedorId");
        if (error != null) {
            return error;
        }
        return ValidadorProducto.validar(crearProducto(registro));
    }

    private Producto crearProducto(JsonObject registro) {
        return new Producto(
                texto(registro, "codigo"),
                texto(registro, "nombre"),
                new BigDecimal(texto(registro, "precio")),
                Integer.parseInt(texto(registro, "stock")),
                Integer.parseInt(texto(registro, "categoriaId")),
                Integer.parseInt(texto(registro, "proveedorId")));
    }

    private String nombreParaIncidencia(JsonElement elemento, int numero) {
        if (elemento.isJsonObject()) {
            String codigo = texto(elemento.getAsJsonObject(), "codigo");
            if (codigo != null && !codigo.isEmpty()) {
                return codigo;
            }
        }
        return "Producto " + numero;
    }

    private String texto(JsonObject registro, String campo) {
        JsonElement valor = registro.get(campo);
        if (valor == null || valor.isJsonNull()) {
            return null;
        }
        if (valor.isJsonPrimitive()) {
            return valor.getAsString().trim();
        }
        return valor.toString();
    }

    private String comprobarDecimal(String texto, String campo) {
        if (texto == null) {
            return "falta el campo '" + campo + "'";
        }
        if (texto.isEmpty()) {
            return "el campo '" + campo + "' está vacío";
        }
        try {
            new BigDecimal(texto);
            return null;
        } catch (NumberFormatException e) {
            return "el campo '" + campo + "' debe ser un número";
        }
    }

    private String comprobarEntero(String texto, String campo) {
        if (texto == null) {
            return "falta el campo '" + campo + "'";
        }
        if (texto.isEmpty()) {
            return "el campo '" + campo + "' está vacío";
        }
        try {
            Integer.parseInt(texto);
            return null;
        } catch (NumberFormatException e) {
            return "el campo '" + campo + "' debe ser un número entero";
        }
    }
}
