package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.List;

/**
 * Resumen de una importación: cuántos registros se encontraron, cuántos se
 * incorporaron y por qué se rechazaron los demás.
 * <p>Se comparte entre la importación JSON y la XML para que el informe final
 * sea idéntico en ambas.</p>
 */
public class ResultadoImportacion {
    private int encontrados;
    private int importados;
    private final List<String> incidencias = new ArrayList<>();

    /** Cuenta un registro leído del fichero (válido o no). */
    public void registrarEncontrado() { encontrados++; }

    /** Cuenta un producto añadido al inventario. */
    public void registrarImportado() { importados++; }

    /** Anota un rechazo con su motivo, p. ej. "TEC-004: el precio debe ser mayor que 0". */
    public void registrarRechazo(String identificador, String motivo) {
        incidencias.add(identificador + ": " + motivo);
    }

    public int getEncontrados() { return encontrados; }
    public int getImportados() { return importados; }
    public int getRechazados() { return incidencias.size(); }
    public List<String> getIncidencias() { return incidencias; }

    /** Muestra el resumen con el formato del enunciado. */
    public void mostrar() {
        System.out.println("\nIMPORTACIÓN DE PRODUCTOS");
        System.out.println("--------------------------------------");
        System.out.println("Productos encontrados: " + encontrados);
        System.out.println("Productos importados: " + importados);
        System.out.println("Productos rechazados: " + getRechazados());
        if (!incidencias.isEmpty()) {
            System.out.println("Incidencias:");
            incidencias.forEach(i -> System.out.println("- " + i));
        }
        System.out.println(incidencias.isEmpty()
                ? "Importación finalizada."
                : "Importación finalizada con incidencias.");
    }
}
