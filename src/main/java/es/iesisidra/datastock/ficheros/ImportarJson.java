package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.componentes.LectorJson;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;

public class ImportarJson {

    private final Inventario inventario;
    private final LectorJson lector = new LectorJson();

    public ImportarJson(Inventario inventario) {
        this.inventario = inventario;
    }

    public ResultadoImportacion importacion(String textoRuta) throws ImporExeption {
        ResultadoImportacion resultado = lector.leer(textoRuta);
        inventario.agregarProductos(resultado);
        return resultado;
    }
}