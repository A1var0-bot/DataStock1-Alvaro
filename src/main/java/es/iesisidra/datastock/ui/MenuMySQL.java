package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.comprobaciones.ComprobarConexion;
import es.iesisidra.datastock.servicio.ImportadorMySQL;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

public class MenuMySQL {

    private final Inventario inventario;

    public MenuMySQL(Inventario inventario) {
        this.inventario = inventario;
    }

    public void iniciar() {
        int opcion;
        do {
            System.out.println("\n========== GESTIÓN MYSQL - UT2 ==========");
            System.out.println("1. Comprobar conexión y mostrar información de la BD");
            System.out.println("2. Gestionar categorías");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultar y buscar productos");
            System.out.println("6. Registrar entrada o salida de stock");
            System.out.println("7. Consultar historial de movimientos");
            System.out.println("8. Importar productos de memoria a MySQL");
            System.out.println("9. Demostración de transacción con commit/rollback");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 9);

            switch (opcion) {
                case 1: 
                    ComprobarConexion.mostrarInformacion();
                    LectorConsola.pausar();
                    break;
                case 2: 
                    new MenuCategorias().iniciar();
                    break;
                      
                case 3:
                    new MenuProveedores().iniciar();
                    break;
                case 4:
                    new MenuProductos().iniciar();
                    break;
                case 5:
                    new MenuConsultasMySQL().iniciar();
                    break;
                case 6:
                    new MenuMovimientos().iniciar();
                    break;
                case 7:
                    new MenuHistorial().iniciar();
                    break;
                case 8:
                    new ImportadorMySQL(inventario).importar();
                    LectorConsola.pausar();
                    break;
                case 9:
                    pendiente("7.9 Demostración de transacción (RF-JDBC-08)");
                    break;
                default:
                    break;
            }
        } while (opcion != 0);
    }

    private void pendiente(String opcion) {
        Mensajes.aviso("Opción " + opcion + " todavía no implementada.");
        LectorConsola.pausar();
    }
}