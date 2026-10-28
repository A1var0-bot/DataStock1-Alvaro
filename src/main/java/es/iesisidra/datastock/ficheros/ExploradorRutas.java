package es.iesisidra.datastock.ficheros;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * RF-FIC-01: información básica sobre una ruta del sistema de archivos.
 * <p>Usa {@link File}, la clase clásica de Java para trabajar con rutas,
 * ficheros y directorios (criterio RA1-a).</p>
 */
public class ExploradorRutas {

    /** Muestra por consola qué es la ruta indicada y, si es un directorio, su contenido inmediato. */
    public void explorar(String textoRuta) {
        if (textoRuta == null || textoRuta.isBlank()) {
            System.out.println("Error: no has introducido ninguna ruta.");
            return;
        }
        File ruta = new File(textoRuta.trim());

        if (!ruta.exists()) {
            System.out.println("Error: la ruta no existe: " + ruta.getPath());
        } else if (ruta.isFile()) {
            mostrarFichero(ruta);
        } else if (ruta.isDirectory()) {
            mostrarDirectorio(ruta);
        } else {
            System.out.println("La ruta existe pero no es un fichero ni un directorio normal.");
        }
    }

    private void mostrarFichero(File fichero) {
        System.out.println("[FICHERO] " + fichero.getName());
        System.out.println("Ruta absoluta: " + fichero.getAbsolutePath());
        System.out.println("Tamaño: " + fichero.length() + " bytes");
    }

    private void mostrarDirectorio(File directorio) {
        // listFiles() devuelve null si no se puede leer el directorio (permisos, error de E/S)
        File[] hijos = directorio.listFiles();
        if (hijos == null) {
            System.out.println("Error: no se puede acceder al contenido de " + directorio.getPath());
            return;
        }
        if (hijos.length == 0) {
            System.out.println("El directorio está vacío.");
            return;
        }
        // Primero los ficheros y luego los directorios; dentro de cada grupo, por nombre
        Arrays.sort(hijos, Comparator.comparing(File::isDirectory)
                .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        int ficheros = 0, directorios = 0;
        for (File hijo : hijos) {
            if (hijo.isDirectory()) {
                System.out.println("[DIR]     " + hijo.getName());
                directorios++;
            } else {
                System.out.println("[FICHERO] " + hijo.getName() + " " + hijo.length() + " bytes");
                ficheros++;
            }
        }
        System.out.println("Total ficheros: " + ficheros + " | Directorios: " + directorios);
    }
}
