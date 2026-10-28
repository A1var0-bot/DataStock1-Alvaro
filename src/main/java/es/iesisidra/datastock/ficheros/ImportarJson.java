package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.validacion.ValidadorProducto;

public class ImportarJson {

    private Inventario inventario;

    public ImportarJson(Inventario inventario) {
        this.inventario = inventario;
    }

    public ResultadoImportacion importacion(String textoRuta) throws ImporExeption {
        Path ruta = comprobarFichero(textoRuta);
        JsonElement raiz = leerDocumento(ruta);
        JsonArray productos = localizarArray(raiz);
        return procesarRegistros(productos);
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
            // Algunos editores guardan una marca BOM al principio; Gson no la entiende y se quita
            if (contenido.startsWith("﻿")) {
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
        int numero = 0;

        for (JsonElement elemento : registros) {
            numero++;
            resultado.registrarEncontrado();

            String motivo = intentarImportar(elemento);

            if (motivo == null) {
                resultado.registrarImportado();
            } else {
                resultado.registrarRechazo(nombreParaIncidencia(elemento, numero), motivo);
            }
        }
        return resultado;
    }

    private String intentarImportar(JsonElement elemento) {

        if (!elemento.isJsonObject()) {
            return "el registro no es un objeto JSON";
        }
        JsonObject registro = elemento.getAsJsonObject();

        String codigo = texto(registro, "codigo");
        String nombre = texto(registro, "nombre");
        String precio = texto(registro, "precio");
        String stock = texto(registro, "stock");
        String categoriaId = texto(registro, "categoriaId");
        String proveedorId = texto(registro, "proveedorId");

        // 1) Comprobar que los números son números
        String error = comprobarDecimal(precio, "precio");
        if (error != null) {
            return error;
        }
        error = comprobarEntero(stock, "stock");
        if (error != null) {
            return error;
        }
        error = comprobarEntero(categoriaId, "categoriaId");
        if (error != null) {
            return error;
        }
        error = comprobarEntero(proveedorId, "proveedorId");
        if (error != null) {
            return error;
        }

        Producto producto = new Producto(codigo, nombre, new BigDecimal(precio),
                Integer.parseInt(stock), Integer.parseInt(categoriaId), Integer.parseInt(proveedorId));

        error = ValidadorProducto.validar(producto);
        if (error != null) {
            return error;
        }

        if (existeCodigo(codigo)) {
            return "código duplicado";
        }

        inventario.getProductos().add(producto);
        return null;
    }

    private boolean existeCodigo(String codigo) {
        for (Producto p : inventario.getProductos()) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                return true;
            }
        }
        return false;
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
            return "el campo '" + campo + "' debe ser un número entero válido";
        }
    }

    public static void main(String[] args) {
        Inventario inventario = new Inventario();
        ImportarJson importador = new ImportarJson(inventario);

        System.out.println("\n===== IMPORTAR JSON =====");
        String ruta = LectorConsola.leerTexto("Ruta: ");
        try {
            ResultadoImportacion resultado = importador.importacion(ruta);
            resultado.mostrar();
        } catch (ImporExeption e) {
            System.out.println("Importación cancelada: " + e.getMessage());
            System.out.println("No se ha añadido ningún producto; el inventario no ha cambiado.");
        }
    }
}
