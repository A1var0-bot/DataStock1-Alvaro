package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.configuracion.ConexionBD;
import es.iesisidra.datastock.dao.ProveedorDao;
import es.iesisidra.datastock.modelo.Proveedor;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

public class MenuProveedores {

    private final ProveedorDao dao = new ProveedorDao();

    public void iniciar() {
        int opcion;
        do {
            System.out.println("\n========== MENÚ DE PROVEEDORES ==========");
            System.out.println("1. Dar de alta un proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Modificar un proveedor");
            System.out.println("4. Eliminar un proveedor");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);

            switch (opcion) {
                case 1:
                    alta();
                    break;
                case 2:
                    listar();
                    break;
                case 3:
                    modificar();
                    break;
                case 4:
                    eliminar();
                    break;
                default:
                    break;
            }
            if (opcion != 0) {
                LectorConsola.pausar();
            }
        } while (opcion != 0);
    }

    private void alta() {
        System.out.println("\nALTA DE PROVEEDOR");
        System.out.println("----------------------------------------");
        int id = LectorConsola.leerEntero("ID de proveedor (0 para volver): ");
        if (id == 0) {
            return;
        }
        if (id < 0) {
            Mensajes.error("El ID debe ser un número entero positivo.");
            return;
        }
        String nombre = LectorConsola.leerTexto("Nombre: ").trim();
        if (!nombreValido(nombre)) {
            return;
        }
        String email = LectorConsola.leerTexto("Email (opcional, Intro para dejarlo vacío): ").trim();
        if (email.isEmpty()) {
            email = null;
        } else if (!emailValido(email)) {
            return;
        }

        try {
            if (dao.buscarPorId(id) != null) {
                Mensajes.error("Ya existe un proveedor con el ID " + id + ".");
                return;
            }
            if (email != null && dao.existeEmail(email, 0)) {
                Mensajes.error("El email " + email + " ya está registrado en otro proveedor.");
                return;
            }
            dao.insertar(new Proveedor(id, nombre, email));
            Mensajes.ok("Proveedor registrado correctamente.");
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    private void listar() {
        System.out.println("\nLISTADO DE PROVEEDORES");
        System.out.println("------------------------------------------------------------");
        try {
            List<Proveedor> proveedores = dao.listar();
            if (proveedores.isEmpty()) {
                Mensajes.aviso("No hay proveedores registrados.");
                return;
            }
            System.out.printf("%-6s%-30s%s%n", "ID", "NOMBRE", "EMAIL");
            System.out.println("------------------------------------------------------------");
            for (Proveedor p : proveedores) {
                System.out.printf("%-6d%-30s%s%n", p.getId(), p.getNombre(), mostrarEmail(p.getEmail()));
            }
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    private void modificar() {
        System.out.println("\nMODIFICAR PROVEEDOR");
        System.out.println("----------------------------------------");
        int id = LectorConsola.leerEntero("ID del proveedor a modificar (0 para volver): ");
        if (id == 0) {
            return;
        }
        try {
            Proveedor proveedor = dao.buscarPorId(id);
            if (proveedor == null) {
                Mensajes.error("No existe ningún proveedor con el ID " + id + ".");
                return;
            }
            System.out.println("Nombre actual: " + proveedor.getNombre());
            System.out.println("Email actual:  " + mostrarEmail(proveedor.getEmail()));
            System.out.println("(Pulsa Intro para conservar el dato actual. En el email, escribe - para dejarlo vacío.)");

            String nombre = LectorConsola.leerTexto("Nuevo nombre: ").trim();
            if (nombre.isEmpty()) {
                nombre = proveedor.getNombre();
            }
            if (!nombreValido(nombre)) {
                return;
            }

            String email = LectorConsola.leerTexto("Nuevo email: ").trim();
            if (email.isEmpty()) {
                email = proveedor.getEmail();
            } else if (email.equals("-")) {
                email = null;
            } else {
                if (!emailValido(email)) {
                    return;
                }
                if (dao.existeEmail(email, id)) {
                    Mensajes.error("El email " + email + " ya está registrado en otro proveedor.");
                    return;
                }
            }

            System.out.println("Quedará así -> Nombre: " + nombre + " | Email: " + mostrarEmail(email));
            if (!LectorConsola.confirmar(amarillo("[AVISO] ¿Guardar los cambios?"))) {
                Mensajes.aviso("Modificación cancelada. No se ha cambiado nada.");
                return;
            }
            dao.modificar(new Proveedor(id, nombre, email));
            Mensajes.ok("Proveedor " + id + " modificado correctamente.");
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    private void eliminar() {
        System.out.println("\nELIMINAR PROVEEDOR");
        System.out.println("----------------------------------------");
        int id = LectorConsola.leerEntero("ID del proveedor a eliminar (0 para volver): ");
        if (id == 0) {
            return;
        }
        try {
            Proveedor proveedor = dao.buscarPorId(id);
            if (proveedor == null) {
                Mensajes.error("No existe ningún proveedor con el ID " + id + ".");
                return;
            }
            System.out.println("ID: " + proveedor.getId());
            System.out.println("Nombre: " + proveedor.getNombre());
            System.out.println("Email: " + mostrarEmail(proveedor.getEmail()));

            if (!LectorConsola.confirmar(amarillo("[AVISO] ¿Confirmas la eliminación?"))) {
                Mensajes.aviso("Eliminación cancelada. No se ha borrado nada.");
                return;
            }
            dao.eliminar(id);
            Mensajes.ok("Proveedor " + id + " eliminado correctamente.");
        } catch (SQLException e) {
            Mensajes.error(ConexionBD.explicarError(e));
        }
    }

    private boolean nombreValido(String nombre) {
        if (nombre.isEmpty()) {
            Mensajes.error("El nombre es obligatorio.");
            return false;
        }
        if (nombre.length() > 100) {
            Mensajes.error("El nombre no puede tener más de 100 caracteres.");
            return false;
        }
        return true;
    }

    private boolean emailValido(String email) {
        if (email.length() > 120) {
            Mensajes.error("El email no puede tener más de 120 caracteres.");
            return false;
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            Mensajes.error("El email no tiene un formato válido (ejemplo: ventas@empresa.com).");
            return false;
        }
        return true;
    }

    private String mostrarEmail(String email) {
        return email == null ? "—" : email;
    }

    private String amarillo(String texto) {
        return ColoresConsola.pintar(texto, ColoresConsola.AMARILLO);
    }
}
