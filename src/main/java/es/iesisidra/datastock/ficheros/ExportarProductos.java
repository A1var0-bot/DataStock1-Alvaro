package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.LectorConsola;

public class ExportarProductos {

    private ConsultarProductos consulta;

    public ExportarProductos(Inventario inventario) {
        this.consulta = new ConsultarProductos(inventario);
    }

    public void exportar(String formato) {
        if (consulta.listarOrdenados().isEmpty()) {
            System.out.println("El inventario está vacío: no hay productos para exportar.");
            return;
        }

        String texto = LectorConsola.leerTexto("Ruta: ");
        try {
            Path ruta = comprobarDestino(texto);

            if (Files.exists(ruta) && !LectorConsola.confirmar("El fichero ya existe. ¿Sobrescribirlo?")) {
                System.out.println("Exportación cancelada: el fichero original no se ha modificado.");
                return;
            }

            int total;
            if (formato.equals("JSON")) {
                total = exportarJson(ruta);
            } else {
                total = exportarXml(ruta);
            }

            System.out.println("Fichero generado correctamente.");
            System.out.println("Ruta: " + ruta);
            System.out.println("Formato: " + formato);
            System.out.println("Productos exportados: " + total);
        } catch (ImporExeption e) {
            System.out.println("Exportación cancelada: " + e.getMessage());
        }
    }

    public Path comprobarDestino(String textoRuta) throws ImporExeption {
        if (textoRuta == null || textoRuta.isBlank()) {
            throw new ImporExeption("no se ha indicado ninguna ruta.");
        }

        Path ruta;
        try {
            ruta = Path.of(textoRuta.trim()).toAbsolutePath();
        } catch (InvalidPathException e) {
            throw new ImporExeption("la ruta contiene caracteres no válidos.");
        }

        if (Files.isDirectory(ruta)) {
            throw new ImporExeption("la ruta es un directorio; indica también el nombre del fichero.");
        }

         Path carpeta = ruta.getParent();
        if (carpeta == null || !Files.isDirectory(carpeta)) {
            throw new ImporExeption("el directorio de destino no existe: " + carpeta);
        }
        if (!Files.isWritable(carpeta)) {
            throw new ImporExeption("no hay permisos de escritura en: " + carpeta);
        }
        if (Files.exists(ruta) && !Files.isWritable(ruta)) {
            throw new ImporExeption("el fichero existe y no se puede sobrescribir: " + ruta);
        }
        return ruta;
    }

    public int exportarJson(Path ruta) throws ImporExeption {
        List<Producto> productos = consulta.listarOrdenados();

        Map<String, List<Producto>> raiz = new LinkedHashMap<>();
        raiz.put("productos", productos);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (Writer escritor = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
            gson.toJson(raiz, escritor);
        } catch (IOException e) {
            throw new ImporExeption("error al escribir el fichero: " + e.getMessage());
        }
        return productos.size();
    }

    public int exportarXml(Path ruta) throws ImporExeption {
        List<Producto> productos = consulta.listarOrdenados();

        try {
            DocumentBuilder constructor = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document documento = constructor.newDocument();

            Element catalogo = documento.createElement("catalogo");
            documento.appendChild(catalogo);

            for (Producto p : productos) {
                Element producto = documento.createElement("producto");
                anadirCampo(documento, producto, "codigo", p.getCodigo());
                anadirCampo(documento, producto, "nombre", p.getNombre());
                anadirCampo(documento, producto, "precio", p.getPrecio().toPlainString());
                anadirCampo(documento, producto, "stock", String.valueOf(p.getStock()));
                anadirCampo(documento, producto, "categoriaId", String.valueOf(p.getCategoriaId()));
                anadirCampo(documento, producto, "proveedorId", String.valueOf(p.getProveedorId()));
                catalogo.appendChild(producto);
            }

            Transformer transformador = TransformerFactory.newInstance().newTransformer();
            transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformador.setOutputProperty(OutputKeys.INDENT, "yes");
            transformador.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            try (Writer escritor = Files.newBufferedWriter(ruta, StandardCharsets.UTF_8)) {
                transformador.transform(new DOMSource(documento), new StreamResult(escritor));
            }

        } catch (ParserConfigurationException | TransformerException e) {
            throw new ImporExeption("no se pudo generar el XML: " + e.getMessage());
        } catch (IOException e) {
            throw new ImporExeption("error al escribir el fichero: " + e.getMessage());
        }
        return productos.size();
    }
    private void anadirCampo(Document documento, Element producto, String campo, String valor) {
        Element elemento = documento.createElement(campo);
        elemento.setTextContent(valor);
        producto.appendChild(elemento);
    }

    public static void main(String[] args) {
        Inventario inventario = new Inventario();

        String rutaJson = LectorConsola.leerTexto("Ruta JSON a cargar (Intro para ninguna): ");
        if (!rutaJson.isBlank()) {
            try {
                new ImportarJson(inventario).importacion(rutaJson).mostrar();
            } catch (ImporExeption e) {
                System.out.println("No se pudo cargar el JSON: " + e.getMessage());
            }
        }
        String rutaXml = LectorConsola.leerTexto("Ruta XML a cargar (Intro para ninguna): ");
        if (!rutaXml.isBlank()) {
            try {
                new ImportarXml(inventario).importar(rutaXml).mostrar();
            } catch (ImporExeption e) {
                System.out.println("No se pudo cargar el XML: " + e.getMessage());
            }
        }

        ExportarProductos exportador = new ExportarProductos(inventario);
        int opcion;
        do {
            System.out.println("\n===== EXPORTAR CATÁLOGO =====");
            System.out.println("1. JSON | 2. XML | 0. Volver");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 2);
            switch (opcion) {
                case 1 -> exportador.exportar("JSON");
                case 2 -> exportador.exportar("XML");
                default -> { }
            }
        } while (opcion != 0);
    }
}
