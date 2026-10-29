package es.iesisidra.datastock.complementarias;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import es.iesisidra.datastock.ficheros.ConsultarProductos;
import es.iesisidra.datastock.ficheros.ImporExeption;
import es.iesisidra.datastock.ficheros.ImportarJson;
import es.iesisidra.datastock.ficheros.ImportarXml;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.LectorConsola;

public final class AccesoAleatorioActividad {

    private static final int LARGO_CODIGO = 12;   
    private static final int TAM_REGISTRO = 36;    
    private static final int DESP_STOCK = 32;       

    private AccesoAleatorioActividad() { }

    public static void main(String[] args) {
        // Inventario propio de esta actividad (no es el del menú principal)
        Inventario inventario = new Inventario();

        System.out.println("\n===== ACCESO ALEATORIO =====");
        String texto = LectorConsola.leerTexto("Fichero binario (Intro = datos/stock.bin): ").trim();
        File fichero;
        if (texto.isEmpty()) {
            fichero = new File("datos/stock.bin");
        } else {
            fichero = new File(texto);
        }

        int opcion;
        do {
            System.out.println("\nFichero: " + fichero.getPath() + " | Productos en memoria: " + inventario.getProductos().size());
            System.out.println("1. Cargar productos en memoria (JSON o XML)");
            System.out.println("2. Generar stock.bin a partir de la memoria");
            System.out.println("3. Mostrar todos los registros (lectura secuencial)");
            System.out.println("4. Consultar un registro por posición");
            System.out.println("5. Modificar el stock de un registro");
            System.out.println("6. Ver el stock de los productos en memoria");
            System.out.println("0. Salir");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 6);
            switch (opcion) {
                case 1 -> cargarProductos(inventario);
                case 2 -> generar(fichero, inventario);
                case 3 -> mostrarTodos(fichero);
                case 4 -> consultar(fichero);
                case 5 -> modificarStock(fichero);
                case 6 -> verMemoria(inventario);
                default -> System.out.println("Hasta pronto.");
            }
        } while (opcion != 0);
    }

    // Carga productos en la memoria de esta actividad reutilizando los importadores
    private static void cargarProductos(Inventario inventario) {
        String ruta = LectorConsola.leerTexto("Ruta del JSON o XML: ").trim();
        try {
            if (ruta.toLowerCase().endsWith(".xml")) {
                new ImportarXml(inventario).importar(ruta).mostrar();
            } else {
                new ImportarJson(inventario).importacion(ruta).mostrar();
            }
        } catch (ImporExeption e) {
            System.out.println("No se pudieron cargar los productos: " + e.getMessage());
        }
    }

    // Escribe un registro de 36 bytes por cada producto, ordenados por código
    private static void generar(File fichero, Inventario inventario) {
        if (inventario.getProductos().isEmpty()) {
            System.out.println("No hay productos en memoria: no se genera el fichero. Carga productos primero (opción 1).");
            return;
        }
        File carpeta = fichero.getAbsoluteFile().getParentFile();
        if (carpeta == null || !carpeta.isDirectory()) {
            System.out.println("El directorio de destino no existe: " + carpeta);
            return;
        }
        if (fichero.isDirectory()) {
            System.out.println("La ruta es un directorio; indica también el nombre del fichero.");
            return;
        }
        if (fichero.exists() && !LectorConsola.confirmar("El fichero ya existe. ¿Sobrescribirlo?")) {
            System.out.println("Generación cancelada. El fichero no se ha modificado.");
            return;
        }

        List<Producto> productos = new ConsultarProductos(inventario).listarOrdenados();
        try (RandomAccessFile raf = new RandomAccessFile(fichero, "rw")) {
            raf.setLength(0);
            for (Producto p : productos) {
                raf.writeChars(String.format("%-12s", p.getCodigo()));
                raf.writeDouble(p.getPrecio().doubleValue());
                raf.writeInt(p.getStock());
            }
            System.out.println("Fichero generado: " + fichero.getPath());
            System.out.println("Registros: " + productos.size() + " | Tamaño: " + raf.length() + " bytes");
        } catch (IOException e) {
            System.out.println("Error al escribir el fichero: " + e.getMessage());
        }
    }

    // Lee el fichero de principio a fin, registro tras registro (acceso secuencial)
    private static void mostrarTodos(File fichero) {
        int registros = comprobarFichero(fichero);
        if (registros < 0) {
            return;
        }
        if (registros == 0) {
            System.out.println("El fichero no contiene registros.");
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(fichero, "r")) {
            for (int posicion = 0; posicion < registros; posicion++) {
                String codigo = leerCodigo(raf);
                double precio = raf.readDouble();
                int stock = raf.readInt();
                System.out.printf("[%d] %-12s | %10.2f € | stock %d%n", posicion, codigo, precio, stock);
            }
        } catch (IOException e) {
            System.out.println("Error al leer el fichero: " + e.getMessage());
        }
    }

    private static void consultar(File fichero) {
        int registros = comprobarFichero(fichero);
        if (registros < 0) {
            return;
        }
        int posicion = pedirPosicion(registros);
        if (posicion < 0) {
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(fichero, "r")) {
            raf.seek((long) posicion * TAM_REGISTRO);
            String codigo = leerCodigo(raf);
            double precio = raf.readDouble();
            int stock = raf.readInt();
            System.out.printf("%s | %.2f € | stock %d%n", codigo, precio, stock);
        } catch (IOException e) {
            System.out.println("Error al leer el fichero: " + e.getMessage());
        }
    }

    private static void modificarStock(File fichero) {
        int registros = comprobarFichero(fichero);
        if (registros < 0) {
            return;
        }
        int posicion = pedirPosicion(registros);
        if (posicion < 0) {
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(fichero, "rw")) {
            // Leer el registro para mostrarlo antes de cambiarlo
            raf.seek((long) posicion * TAM_REGISTRO);
            String codigo = leerCodigo(raf);
            double precio = raf.readDouble();
            int stockAnterior = raf.readInt();
            System.out.printf("%s | %.2f € | stock %d%n", codigo, precio, stockAnterior);

            int nuevoStock = LectorConsola.leerEntero("Nuevo stock: ");
            if (nuevoStock < 0) {
                System.out.println("El stock no puede ser negativo. No se ha modificado nada.");
                return;
            }

            // Saltar directamente al campo stock y escribir solo ese int
            raf.seek((long) posicion * TAM_REGISTRO + DESP_STOCK);
            raf.writeInt(nuevoStock);

            System.out.println("Actualización correcta: stock " + stockAnterior + " → " + nuevoStock);
            System.out.println("(El inventario en memoria NO cambia: stock.bin es una copia independiente.)");
        } catch (IOException e) {
            System.out.println("Error al modificar el fichero: " + e.getMessage());
        }
    }

    // Sirve para comprobar que cambiar stock.bin no cambia los productos en memoria
    private static void verMemoria(Inventario inventario) {
        if (inventario.getProductos().isEmpty()) {
            System.out.println("No hay productos en memoria.");
            return;
        }
        for (Producto p : new ConsultarProductos(inventario).listarOrdenados()) {
            System.out.println(p.getCodigo() + " | stock en memoria: " + p.getStock());
        }
    }

    private static int comprobarFichero(File fichero) {
        if (!fichero.exists()) {
            System.out.println("El fichero no existe: " + fichero.getPath() + ". Genéralo primero (opción 2).");
            return -1;
        }
        if (!fichero.isFile()) {
            System.out.println("La ruta no es un fichero: " + fichero.getPath());
            return -1;
        }
        long tamano = fichero.length();
        if (tamano % TAM_REGISTRO != 0) {
            System.out.println("Fichero inválido: su tamaño (" + tamano + " bytes) no es múltiplo de " + TAM_REGISTRO);
            return -1;
        }
        int registros = (int) (tamano / TAM_REGISTRO);
        System.out.println("Registros: " + registros + " | Tamaño: " + tamano + " bytes");
        return registros;
    }

    private static int pedirPosicion(int registros) {
        if (registros == 0) {
            System.out.println("El fichero no contiene registros.");
            return -1;
        }
        int posicion = LectorConsola.leerEntero("Posición (0-" + (registros - 1) + "): ");
        if (posicion < 0 || posicion >= registros) {
            System.out.println("Posición fuera de rango. Debe estar entre 0 y " + (registros - 1));
            return -1;
        }
        return posicion;
    }

    // Lee los 12 caracteres del código y quita los espacios de relleno
    private static String leerCodigo(RandomAccessFile raf) throws IOException {
        String codigo = "";
        for (int i = 0; i < LARGO_CODIGO; i++) {
            codigo = codigo + raf.readChar();
        }
        return codigo.trim();
    }
}
