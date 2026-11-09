package es.iesisidra.datastock.util;

public final class Mensajes {

    private Mensajes() { }

    public static void error(String texto) {
        System.out.println(ColoresConsola.pintar("[ERROR] " + texto, ColoresConsola.ROJO));
    }

    public static void ok(String texto) {
        System.out.println(ColoresConsola.pintar("[OK] " + texto, ColoresConsola.VERDE));
    }

    public static void aviso(String texto) {
        System.out.println(ColoresConsola.pintar("[AVISO] " + texto, ColoresConsola.AMARILLO));
    }
}
