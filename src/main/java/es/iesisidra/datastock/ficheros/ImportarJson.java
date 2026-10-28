package es.iesisidra.datastock.ficheros;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class ImportarJson {

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
    
    
}
