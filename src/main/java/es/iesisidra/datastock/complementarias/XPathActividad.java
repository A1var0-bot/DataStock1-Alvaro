package es.iesisidra.datastock.complementarias;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import es.iesisidra.datastock.util.LectorConsola;

/**
 * Punto de entrada reservado para RF-FIC-07: consultas XPath.
 */
public final class XPathActividad {
    private XPathActividad() { }
    
    // Abre el XML una sola vez y permite hacer varias consultas sobre él hasta pulsar 0
    public static void main(String[] args) {
        System.out.println("===== CONSULTAS XPATH =====");
        String ruta = LectorConsola.leerTexto("Fichero: ").trim();
        Document documento = abrirDocumento(ruta);
        if (documento == null) {
            return;
        }
        XPath xpath = XPathFactory.newInstance().newXPath();

        int opcion;
        do {
            System.out.println("\n1. Por código | 2. Por categoría | 3. Precio superior | 4. Contar | 0. Salir");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 4);
            try {
                switch (opcion) {
                    case 1 -> buscarPorCodigo(documento, xpath);
                    case 2 -> buscarPorCategoria(documento, xpath);
                    case 3 -> buscarPorPrecio(documento, xpath);
                    case 4 -> contarProductos(documento, xpath);
                    default -> System.out.println("Hasta pronto.");
                }
            } catch (XPathExpressionException e) {
                System.out.println("Error al ejecutar la consulta XPath: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    // Abre el XML de forma segura y comprueba que la raíz es la buena; si falla devuelve null
    private static Document abrirDocumento(String ruta) {
        File fichero = new File(ruta);
        if (!fichero.exists()) {
            System.out.println("El fichero no existe: " + ruta);
            return null;
        }
        if (fichero.isDirectory()) {
            System.out.println("La ruta es un directorio, no un fichero.");
            return null;
        }

        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setFeature("http://xml.org/sax/features/external-general-entities", false);
            fabrica.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            fabrica.setXIncludeAware(false);
            fabrica.setExpandEntityReferences(false);

            DocumentBuilder constructor = fabrica.newDocumentBuilder();
            constructor.setErrorHandler(new DefaultHandler());
            Document documento = constructor.parse(fichero);

            if (!documento.getDocumentElement().getTagName().equals("catalogo")) {
                System.out.println("La raíz debe ser <catalogo> y es <"
                        + documento.getDocumentElement().getTagName() + ">.");
                return null;
            }
            System.out.println("Documento abierto correctamente.");
            return documento;

        } catch (SAXParseException e) {
            if (e.getMessage() != null && e.getMessage().contains("DOCTYPE")) {
                System.out.println("El documento contiene un DOCTYPE: se rechaza por seguridad (XXE).");
            } else {
                System.out.println("El XML está mal formado (línea " + e.getLineNumber() + ").");
            }
        } catch (SAXException e) {
            System.out.println("El fichero no es un XML válido.");
        } catch (IOException e) {
            System.out.println("No se pudo leer el fichero: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println("No se pudo preparar el lector de XML.");
        }
        return null;
    }

    // Coge todos los productos con XPath y compara el código en Java
    private static void buscarPorCodigo(Document documento, XPath xpath) throws XPathExpressionException {
        String codigo = LectorConsola.leerTexto("Código: ").trim();
        if (codigo.isEmpty()) {
            System.out.println("No has escrito ningún código.");
            return;
        }

        NodeList productos = (NodeList) xpath.evaluate("/catalogo/producto", documento, XPathConstants.NODESET);

        int encontrados = 0;
        for (int i = 0; i < productos.getLength(); i++) {
            Node producto = productos.item(i);
            String codigoProducto = xpath.evaluate("codigo", producto).trim();
            if (codigoProducto.equalsIgnoreCase(codigo)) {
                mostrarProducto(producto, xpath);
                encontrados++;
            }
        }
        if (encontrados == 0) {
            System.out.println("No hay ningún producto con el código " + codigo + ".");
        }
    }

    // Valida que la categoría sea un entero positivo
    private static void buscarPorCategoria(Document documento, XPath xpath) throws XPathExpressionException {
        String texto = LectorConsola.leerTexto("Categoría: ").trim();
        int categoria;
        try {
            categoria = Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            System.out.println("La categoría debe ser un número entero.");
            return;
        }
        if (categoria <= 0) {
            System.out.println("La categoría debe ser un entero positivo.");
            return;
        }

        String expresion = "/catalogo/producto[categoriaId = " + categoria + "]";
        NodeList productos = (NodeList) xpath.evaluate(expresion, documento, XPathConstants.NODESET);
        mostrarLista(productos, xpath);
    }

    private static void buscarPorPrecio(Document documento, XPath xpath) throws XPathExpressionException {
        String texto = LectorConsola.leerTexto("Precio mínimo: ").trim();
        BigDecimal precio;
        try {
            precio = new BigDecimal(texto);
        } catch (NumberFormatException e) {
            System.out.println("El precio debe ser un número (por ejemplo 100 o 19.95).");
            return;
        }
        if (precio.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("El precio no puede ser negativo.");
            return;
        }

        String expresion = "/catalogo/producto[precio > " + precio.toPlainString() + "]";
        NodeList productos = (NodeList) xpath.evaluate(expresion, documento, XPathConstants.NODESET);
        mostrarLista(productos, xpath);
    }

    private static void contarProductos(Document documento, XPath xpath) throws XPathExpressionException {
        Double total = (Double) xpath.evaluate("count(/catalogo/producto)", documento, XPathConstants.NUMBER);
        System.out.println("Número de productos en el documento: " + total.intValue());
    }

    private static void mostrarLista(NodeList productos, XPath xpath) throws XPathExpressionException {
        if (productos.getLength() == 0) {
            System.out.println("No hay productos que cumplan la condición.");
            return;
        }
        for (int i = 0; i < productos.getLength(); i++) {
            mostrarProducto(productos.item(i), xpath);
        }
        System.out.println("Productos encontrados: " + productos.getLength());
    }

    private static void mostrarProducto(Node producto, XPath xpath) throws XPathExpressionException {
        String codigo = xpath.evaluate("codigo", producto).trim();
        String nombre = xpath.evaluate("nombre", producto).trim();
        String precio = xpath.evaluate("precio", producto).trim();
        String categoria = xpath.evaluate("categoriaId", producto).trim();
        System.out.printf("%-10s %-28s %10s €   (categoría %s)%n", codigo, nombre, precio, categoria);
    }
}
