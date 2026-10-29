package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.validacion.ValidadorProducto;

public class ControlarInventario {

    private Inventario inventario;

    public ControlarInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    //guardo toda la colección en un fichero .dat
    public void guardarInventario() {
        System.out.println("\n===== GUARDAR INVENTARIO =====");
        if (inventario.getProductos().isEmpty()) {
            System.out.println("El inventario está vacío: no hay nada que guardar.");
            return;
        }
        String texto = LectorConsola.leerTexto("Ruta: ");
        try {
            Path ruta = comprobarDestino(texto);
            if (Files.exists(ruta) && !LectorConsola.confirmar("El fichero ya existe. ¿Sobrescribirlo?")) {
                System.out.println("Guardado cancelado: el fichero original no se ha modificado.");
                return;
            }
            int total = guardar(ruta);
            System.out.println("Inventario guardado correctamente en " + ruta);
            System.out.println("Productos guardados: " + total);
        } catch (ImporExeption e) {
            System.out.println("Guardado cancelado: " + e.getMessage());
        }
    }

    public void recuperarInventario() {
        System.out.println("\n===== RECUPERAR INVENTARIO =====");
        String texto = LectorConsola.leerTexto("Ruta: ");
        try {
            Path ruta = comprobarOrigen(texto);
            List<Producto> recuperados = leer(ruta);

            int actuales = inventario.getProductos().size();
            if (actuales > 0) {
                System.out.println("Actualmente hay " + actuales + " productos en memoria.");
                System.out.println("La recuperación sustituirá el inventario actual.");
                if (!LectorConsola.confirmar("¿Continuar?")) {
                    System.out.println("Recuperación cancelada: el inventario actual se conserva.");
                    return;
                }
            }

            inventario.getProductos().clear();
            inventario.getProductos().addAll(recuperados);

            System.out.println("Inventario recuperado correctamente.");
            System.out.println("Productos disponibles: " + inventario.getProductos().size());
        } catch (ImporExeption e) {
            System.out.println("Recuperación cancelada: " + e.getMessage());
            System.out.println("El inventario en memoria no ha cambiado.");
        }
    }

     public Path comprobarDestino(String textoRuta) throws ImporExeption {
        Path ruta = convertirRuta(textoRuta);

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
        return ruta;
    }

    public int guardar(Path ruta) throws ImporExeption {
        ArrayList<Producto> copia = new ArrayList<>(inventario.getProductos());

        try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(ruta))) {
            salida.writeObject(copia);
        } catch (IOException e) {
            throw new ImporExeption("error al escribir el fichero: " + e.getMessage());
        }
        return copia.size();
    }

    public Path comprobarOrigen(String textoRuta) throws ImporExeption {
        Path ruta = convertirRuta(textoRuta);

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

     public List<Producto> leer(Path ruta) throws ImporExeption {
        Object leido;

     ObjectInputFilter filtro = ObjectInputFilter.Config.createFilter(
                "java.util.ArrayList;java.lang.Object;es.iesisidra.datastock.modelo.Producto;"
                + "java.math.BigDecimal;java.math.BigInteger;java.lang.Number;!*");

        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(ruta))) {
            entrada.setObjectInputFilter(filtro);
            leido = entrada.readObject();
        } catch (ClassNotFoundException e) {
            throw new ImporExeption("el fichero contiene datos que no son de DataStock.");
        } catch (IOException e) {
            throw new ImporExeption("el fichero está dañado o no es un inventario de DataStock.");
        }

        if (!(leido instanceof List)) {
            throw new ImporExeption("el fichero no contiene un inventario de DataStock.");
        }
        List<String> lista = (List<String>) leido;

        List<Producto> productos = new ArrayList<>();
        List<String> codigos = new ArrayList<>();
        for (Object elemento : lista) {
            if (!(elemento instanceof Producto)) {
                throw new ImporExeption("el fichero contiene un elemento que no es un producto.");
            }
            Producto p = (Producto) elemento;

            String error = ValidadorProducto.validar(p);
            if (error != null) {
                throw new ImporExeption("el fichero contiene un producto incorrecto: " + error);
            }

            String codigo = p.getCodigo().trim().toUpperCase();
            if (codigos.contains(codigo)) {
                throw new ImporExeption("el fichero contiene el código repetido " + p.getCodigo());
            }
            codigos.add(codigo);
            productos.add(p);
        }
        return productos;
    }

    private Path convertirRuta(String textoRuta) throws ImporExeption {
        if (textoRuta == null || textoRuta.isBlank()) {
            throw new ImporExeption("no se ha indicado ninguna ruta.");
        }
        try {
            return Path.of(textoRuta.trim()).toAbsolutePath();
        } catch (InvalidPathException e) {
            throw new ImporExeption("la ruta contiene caracteres no válidos.");
        }
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

        ControlarInventario control = new ControlarInventario(inventario);
        int opcion;
        do {
            System.out.println("\n===== GUARDAR Y RECUPERAR INVENTARIO =====");
            System.out.println("Productos en memoria: " + inventario.getProductos().size());
            System.out.println("1. Guardar | 2. Recuperar | 0. Volver");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 2);
            switch (opcion) {
                case 1 -> control.guardarInventario();
                case 2 -> control.recuperarInventario();
                default -> { }
            }
        } while (opcion != 0);
    }
}
