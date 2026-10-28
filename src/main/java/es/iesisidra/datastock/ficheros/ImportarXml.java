package es.iesisidra.datastock.ficheros;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;

public class ImportarXml {
    private Inventario inventario;

    public ImportarXml(Inventario inventario) {
        this.inventario = inventario;
    }

    public ResultadoImportacion importar(String textoRuta) throws ImporExeption {
        Path ruta = comprobarFichero(textoRuta);
        //Document documento = leerDocumento(ruta);
        //Element raiz = comprobarRaiz(documento);
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

}
