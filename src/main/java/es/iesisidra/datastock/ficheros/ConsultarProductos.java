package es.iesisidra.datastock.ficheros;

import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.LectorConsola;

public class ConsultarProductos {

    private Inventario inventario;

    public ConsultarProductos(Inventario inventario) {
        this.inventario = inventario;
    }

    public List<Producto> listarOrdenados() {
        List<Producto> copia = new ArrayList<>(inventario.getProductos());
        copia.sort((a, b) -> a.getCodigo().compareToIgnoreCase(b.getCodigo()));
        return copia;
    }

    public Producto buscarPorCodigo(String codigo) {
        String buscado = codigo.trim();
        for (Producto p : inventario.getProductos()) {
            if (p.getCodigo().equalsIgnoreCase(buscado)) {
                return p;
            }
        }
        return null;
    }

       public List<Producto> buscarPorNombre(String texto) {
        List<Producto> encontrados = new ArrayList<>();
        String buscado = texto.trim().toLowerCase();
        for (Producto p : listarOrdenados()) {
            if (p.getNombre().toLowerCase().contains(buscado)) {
                encontrados.add(p);
            }
        }
        return encontrados;
    }

    // listo todos los productos ordenados por código
    public void mostrarListado() {
        List<Producto> productos = listarOrdenados();
        if (productos.isEmpty()) {
            System.out.println("El inventario está vacío: importa productos antes de consultar.");
            return;
        }
        for (Producto p : productos) {
            mostrarProducto(p);
        }
        System.out.println("Total: " + productos.size() + " productos.");
    }

    // pido un código el cual muestra el producto, o avisa si no existe
    public void mostrarBusquedaCodigo() {
        String codigo = LectorConsola.leerTexto("Código: ");
        if (codigo.isBlank()) {
            System.out.println("No has introducido ningún código.");
            return;
        }
        Producto p = buscarPorCodigo(codigo);
        if (p == null) {
            System.out.println("No existe ningún producto con el código " + codigo.trim() + ".");
        } else {
            mostrarProducto(p);
        }
    }

    //pido un texto y muestra los productos cuyo nombre lo contiene
    public void mostrarBusquedaNombre() {
        String texto = LectorConsola.leerTexto("Texto del nombre: ");
        if (texto.isBlank()) {
            System.out.println("No has introducido ningún texto de búsqueda.");
            return;
        }
        List<Producto> encontrados = buscarPorNombre(texto);
        if (encontrados.isEmpty()) {
            System.out.println("Ningún producto contiene \"" + texto.trim() + "\" en su nombre.");
            return;
        }
        for (Producto p : encontrados) {
            mostrarProducto(p);
        }
        System.out.println("Coincidencias: " + encontrados.size());
    }

    // Muestro los seis datos de un producto con el formato del enunciado
    private void mostrarProducto(Producto p) {
        System.out.println("Código: " + p.getCodigo());
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Precio: " + p.getPrecio() + " | Stock: " + p.getStock());
        System.out.println("Categoría: " + p.getCategoriaId() + " | Proveedor: " + p.getProveedorId());
        System.out.println("--------------------------------------");
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

        ConsultarProductos consultas = new ConsultarProductos(inventario);
        int opcion;
        do {
            System.out.println("\n===== CONSULTAR PRODUCTOS =====");
            System.out.println("1. Listar | 2. Por código | 3. Por nombre | 0. Volver");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 3);
            switch (opcion) {
                case 1 -> consultas.mostrarListado();
                case 2 -> consultas.mostrarBusquedaCodigo();
                case 3 -> consultas.mostrarBusquedaNombre();
                default -> { }
            }
        } while (opcion != 0);
    }
}
