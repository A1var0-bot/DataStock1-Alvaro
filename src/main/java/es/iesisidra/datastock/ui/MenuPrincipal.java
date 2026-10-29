package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.ficheros.ConsultarProductos;
import es.iesisidra.datastock.ficheros.ExploradorRutas;
import es.iesisidra.datastock.ficheros.ExportarProductos;
import es.iesisidra.datastock.ficheros.ImporExeption;
import es.iesisidra.datastock.ficheros.ImportarJson;
import es.iesisidra.datastock.ficheros.ImportarXml;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * Presentación de menús y navegación de la primera fase de DataStock.
 * <p>Los métodos marcados TODO son puntos de partida, NO soluciones de los
 * requisitos. El alumnado deberá delegar el acceso a ficheros, la validación
 * y el procesamiento en métodos o clases independientes.</p>
 */
public class MenuPrincipal {
    private final Inventario inventario;
    // Todas las clases reciben el MISMO inventario, así lo importado se ve en las consultas
    private final ImportarJson importadorJson;
    private final ImportarXml importadorXml;
    private final ConsultarProductos consultas;
    private final ExportarProductos exportador;

    /** @param inventario colección común de productos de esta ejecución. */
    public MenuPrincipal(Inventario inventario) {
        this.inventario = inventario;
        this.importadorJson = new ImportarJson(inventario);
        this.importadorXml = new ImportarXml(inventario);
        this.consultas = new ConsultarProductos(inventario);
        this.exportador = new ExportarProductos(inventario);
    }

    /** Ejecuta el menú principal hasta que el usuario selecciona salir. */
    public void iniciar() {
        int opcion;
        do {
            mostrarCabecera();
            System.out.println("1. Explorar ficheros y directorios");
            System.out.println("2. Importar productos desde JSON");
            System.out.println("3. Importar productos desde XML");
            System.out.println("4. Consultar productos");
            System.out.println("5. Exportar productos");
            System.out.println("6. Guardar y recuperar inventario");
            System.out.println("0. Salir");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 6);
            switch (opcion) {
                case 1 -> explorarRutas();
                case 2 -> importarJson();
                case 3 -> importarXml();
                case 4 -> menuConsultas();
                case 5 -> menuExportaciones();
                case 6 -> menuSerializacion();
                case 0 -> System.out.println(ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void mostrarCabecera() {
        System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
        System.out.println(ColoresConsola.pintar("                 FASE 1 - UT1", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
        System.out.println(ColoresConsola.pintar("Productos en memoria: " + inventario.getProductos().size(), ColoresConsola.GRIS));
    }

    private void menuConsultas() {
        int opcion;
        do {
            System.out.println("\n---------- CONSULTA DE PRODUCTOS ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 3);
            switch (opcion) {
                case 1 -> { consultas.mostrarListado(); LectorConsola.pausar(); }
                case 2 -> { consultas.mostrarBusquedaCodigo(); LectorConsola.pausar(); }
                case 3 -> { consultas.mostrarBusquedaNombre(); LectorConsola.pausar(); }
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuExportaciones() {
        int opcion;
        do {
            System.out.println("\n---------- EXPORTAR PRODUCTOS ----------");
            System.out.println("1. Exportar productos a JSON");
            System.out.println("2. Exportar productos a XML");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> { exportador.exportar("JSON"); LectorConsola.pausar(); }
                case 2 -> { exportador.exportar("XML"); LectorConsola.pausar(); }
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    private void menuSerializacion() {
        int opcion;
        do {
            System.out.println("\n---------- GUARDAR Y RECUPERAR INVENTARIO ----------");
            System.out.println("1. Guardar inventario en fichero .dat");
            System.out.println("2. Recuperar inventario desde fichero .dat");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            switch (opcion) {
                case 1 -> pendiente("RF-FIC-06 · Guardar inventario");
                case 2 -> pendiente("RF-FIC-06 · Recuperar inventario");
                case 0 -> { }
                default -> throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    // Opción 1 (RF-FIC-01): pide una ruta y la explora
    private void explorarRutas() {
        System.out.println("\n===== EXPLORAR FICHEROS =====");
        String ruta = LectorConsola.leerTexto("Ruta: ");
        new ExploradorRutas().explorar(ruta);
        LectorConsola.pausar();
    }

    // Opción 2 (RF-FIC-02): pide la ruta, llama al importador JSON y muestra el resultado
    private void importarJson() {
        System.out.println("\n===== IMPORTAR JSON =====");
        String ruta = LectorConsola.leerTexto("Ruta: ");
        try {
            ResultadoImportacion resultado = importadorJson.importacion(ruta);
            resultado.mostrar();
        } catch (ImporExeption e) {
            System.out.println("Importación cancelada: " + e.getMessage());
            System.out.println("No se ha añadido ningún producto; el inventario no ha cambiado.");
        }
        LectorConsola.pausar();
    }

    // Opción 3 (RF-FIC-03): igual que la 2 pero con el importador XML
    private void importarXml() {
        System.out.println("\n===== IMPORTAR XML =====");
        String ruta = LectorConsola.leerTexto("Ruta: ");
        try {
            ResultadoImportacion resultado = importadorXml.importar(ruta);
            resultado.mostrar();
        } catch (ImporExeption e) {
            System.out.println("Importación cancelada: " + e.getMessage());
            System.out.println("No se ha añadido ningún producto; el inventario no ha cambiado.");
        }
        LectorConsola.pausar();
    }

    /** Sustituir las llamadas por servicios propios a medida que se implementen los RF. */
    private void pendiente(String requisito) {
        System.out.println(ColoresConsola.pintar("[PENDIENTE] " + requisito, ColoresConsola.AMARILLO));
        System.out.println("Esta funcionalidad debe desarrollarla el alumnado.");
        LectorConsola.pausar();
    }
}
