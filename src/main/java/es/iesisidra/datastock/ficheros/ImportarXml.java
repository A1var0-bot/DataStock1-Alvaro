package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.validacion.ValidadorProducto;

public class ImportarXml {
    private Inventario inventario;

    public ImportarXml(Inventario inventario) {
        this.inventario = inventario;
    }

    public ResultadoImportacion importar(String textoRuta) throws ImporExeption {
        Path ruta = comprobarFichero(textoRuta);
        Document documento = leerDocumento(ruta);
        Element raiz = comprobarRaiz(documento);
        return procesarProductos(raiz);
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

    private Document leerDocumento(Path ruta) throws ImporExeption {
        try {
            DocumentBuilder constructor = crearConstructorSeguro();
            return constructor.parse(ruta.toFile());
        } catch (SAXParseException e) {
            if (e.getMessage() != null && e.getMessage().contains("DOCTYPE")) {
                throw new ImporExeption("el documento contiene un DOCTYPE, no se admite por seguridad (XXE).");
            }
            throw new ImporExeption("el XML está mal formado (línea " + e.getLineNumber() + ").");
        } catch (SAXException e) {
            throw new ImporExeption("el fichero no es un XML válido.");
        } catch (IOException e) {
            throw new ImporExeption("no se pudo leer el fichero: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new ImporExeption("no se pudo preparar el lector de XML.");
        }
    }

     private DocumentBuilder crearConstructorSeguro() throws ParserConfigurationException {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        fabrica.setFeature("http://xml.org/sax/features/external-general-entities", false);
        fabrica.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        fabrica.setXIncludeAware(false);
        fabrica.setExpandEntityReferences(false);

        DocumentBuilder constructor = fabrica.newDocumentBuilder();
        constructor.setErrorHandler(new DefaultHandler());
        return constructor;
    }

    private Element comprobarRaiz(Document documento) throws ImporExeption {
        Element raiz = documento.getDocumentElement();
        if (!raiz.getTagName().equals("catalogo")) {
            throw new ImporExeption("el elemento raíz debe ser <catalogo> y es <" + raiz.getTagName() + ">.");
        }
        return raiz;
    }

     private ResultadoImportacion procesarProductos(Element raiz) {
        ResultadoImportacion resultado = new ResultadoImportacion();
        NodeList hijos = raiz.getChildNodes();
        int numero = 0;

        for (int i = 0; i < hijos.getLength(); i++) {
            Node nodo = hijos.item(i);

            if (nodo.getNodeType() != Node.ELEMENT_NODE || !nodo.getNodeName().equals("producto")) {
                continue;
            }

            Element producto = (Element) nodo;
            numero++;
            resultado.registrarEncontrado();

            String motivo = intentarImportar(producto);

            if (motivo == null) {
                resultado.registrarImportado();
            } else {
                resultado.registrarRechazo(nombreParaIncidencia(producto, numero), motivo);
            }
        }
        return resultado;
    }

    private String intentarImportar(Element registro) {

        String codigo = texto(registro, "codigo");
        String nombre = texto(registro, "nombre");
        String precio = texto(registro, "precio");
        String stock = texto(registro, "stock");
        String categoriaId = texto(registro, "categoriaId");
        String proveedorId = texto(registro, "proveedorId");

        String error = comprobarDecimal(precio, "precio");
        if (error != null) {
            return error;
        }

        //Los tres enteros los compruebo con un bucle
        String[] camposEnteros = {"stock", "categoriaId", "proveedorId"};
        String[] valoresEnteros = {stock, categoriaId, proveedorId};
        for (int i = 0; i < camposEnteros.length; i++) {
            error = comprobarEntero(valoresEnteros[i], camposEnteros[i]);
            if (error != null) {
                return error;
            }
        }

         Producto producto = new Producto(codigo, nombre, new BigDecimal(precio),
                Integer.parseInt(stock), Integer.parseInt(categoriaId), Integer.parseInt(proveedorId));

        //Compruebo las reglas del producto (las mismas que en JSON)
        error = ValidadorProducto.validar(producto);
        if (error != null) {
            return error;
        }

        //Compruebo que no está repetido (ni por JSON, ni antes en este mismo fichero)
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

    private String nombreParaIncidencia(Element registro, int numero) {
        String codigo = texto(registro, "codigo");
        if (codigo != null && !codigo.isEmpty()) {
            return codigo;
        }
        return "Producto " + numero;
    }

    private String texto(Element registro, String campo) {
        NodeList hijos = registro.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Node nodo = hijos.item(i);
            if (nodo.getNodeType() == Node.ELEMENT_NODE && nodo.getNodeName().equals(campo)) {
                return nodo.getTextContent().trim();
            }
        }
        return null;
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
            return null;
        } catch (NumberFormatException e) {
            return "el campo '" + campo + "' debe ser un número entero válido";
        }
    }

    public static void main(String[] args) {
        Inventario inventario = new Inventario();
        ImportarXml importador = new ImportarXml(inventario);

        System.out.println("\n===== IMPORTAR XML =====");
        String ruta = LectorConsola.leerTexto("Ruta: ");
        try {
            ResultadoImportacion resultado = importador.importar(ruta);
            resultado.mostrar();
        } catch (ImporExeption e) {
            System.out.println("Importación cancelada: " + e.getMessage());
            System.out.println("No se ha añadido ningún producto; el inventario no ha cambiado.");
        }
    }
}


