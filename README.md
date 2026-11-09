# DataStock – UT1 (Ficheros) y UT2 (JDBC y MySQL)

**Autor:** Álvaro Monjas Navas
**Repositorio:** https://github.com/A1var0-bot/DataStock1-Alvaro
**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027
**Lenguaje:** Java 21 · **Construcción:** Maven · **Interfaz:** consola

## 1. Descripción

DataStock es una aplicación de consola para gestionar el inventario de productos de una pequeña empresa.
Es **una única aplicación** que se amplía en cada unidad:

- **UT1 – Ficheros:** explora rutas, importa catálogos desde **JSON** (Gson) y **XML** (DOM), consulta los
  productos en memoria, exporta a JSON y XML y guarda/recupera el inventario mediante **serialización** (`.dat`).
  Incluye dos actividades independientes: consultas **XPath** y acceso aleatorio con **RandomAccessFile**.
- **UT2 – Bases de datos:** añade la opción **7. Gestionar base de datos MySQL**, que trabaja directamente con
  MySQL mediante **JDBC**: categorías, proveedores, productos (CRUD), consultas con JOIN, movimientos de stock con
  un **procedimiento almacenado**, historial, importación del inventario en memoria a MySQL con **transacción** y
  una demostración de **commit/rollback**. Incluye además una actividad independiente con **SQLite**.

Las opciones 1–6 siguen trabajando con **la colección de productos en memoria** (`Inventario`).
La opción 7 trabaja con **MySQL**. Los dos almacenes **no se sincronizan solos**: solo la opción 7.8 copia
productos de la memoria a MySQL, y únicamente cuando el usuario lo pide.

## 2. Requisitos previos

- **JDK 21** (`java -version`).
- **Maven**, o un IDE con soporte Maven (VS Code, IntelliJ, Eclipse).
- **MySQL Server 8.0.16 o superior** (por los `CHECK`) y, opcionalmente, **MySQL Workbench**.
- Conexión a Internet la primera vez, para que Maven descargue las dependencias:

  | Dependencia | Versión | Para qué |
  |---|---|---|
  | `com.google.code.gson:gson` | 2.11.0 | JSON (UT1) |
  | `com.mysql:mysql-connector-j` (**Connector/J**) | **9.1.0** | Driver JDBC de MySQL (UT2) |
  | `org.xerial:sqlite-jdbc` | **3.46.1.3** | Driver JDBC de SQLite (actividad SQLite) |

- Consola en UTF-8 para ver bien las tildes y el símbolo € (en Windows: `chcp 65001`).

## 3. Configuración de la base de datos (UT2)

### 3.1. Crear el esquema

Ejecutar `material_referencia/DataStock_BD_MySQL.sql` en MySQL (en Workbench: conectarse a la instancia
local → *File → Open SQL Script* → ⚡). Crea la base de datos `datastock`, sus **cuatro tablas**
(`categoria`, `proveedor`, `producto`, `movimiento_stock`) y el procedimiento **`registrar_movimiento`**.
El script se puede ejecutar varias veces: no borra datos existentes.

Comprobación:

```sql
USE datastock;
SHOW TABLES;
SHOW PROCEDURE STATUS WHERE Db = 'datastock';
```

### 3.2. Datos de conexión (fuera del repositorio)

La contraseña **nunca se sube a GitHub**. La clase `configuracion/ConexionBD` busca los datos de conexión así:

1. **Fichero `datastock.properties`** en la raíz del proyecto (junto a `pom.xml`). Está en `.gitignore`.
   Se crea copiando la plantilla `Datastock.properties.ejemplo` y poniendo los datos propios:

   ```properties
   url=jdbc:mysql://localhost:3306/datastock?useSSL=false&allowPublicKeyRetrieval=true
   usuario=root
   clave=TU_CONTRASEÑA
   ```

2. Si ese fichero no existe, usa las **variables de entorno** `JDBC_URL`, `JDBC_USER` y `JDBC_PASSWORD`.
   Ejemplo en PowerShell (solo para la sesión actual):

   ```powershell
   $env:JDBC_URL="jdbc:mysql://localhost:3306/datastock?useSSL=false&allowPublicKeyRetrieval=true"
   $env:JDBC_USER="root"
   $env:JDBC_PASSWORD="TU_CONTRASEÑA"
   ```

Antes de hacer commit conviene comprobar con `git status` que `datastock.properties` **no aparece**.

## 4. Cómo ejecutar

### Aplicación principal (menú)

Desde la carpeta que contiene `pom.xml`:

```bash
mvn clean compile
mvn exec:java
```

Se ejecuta `es.iesisidra.datastock.App`. En VS Code también se puede lanzar con la configuración
**"DataStock (menú principal)"** o con **Run** sobre el `main` de `App`.

> Hay que ejecutar **desde la carpeta del proyecto**: `datastock.properties` y las rutas de `datos/`
> son relativas a la carpeta desde la que se lanza el programa.

### Clases independientes (fuera del menú)

| Clase | Qué hace |
|---|---|
| `complementarias.XPathActividad` | Consultas XPath (UT1) |
| `complementarias.AccesoAleatorioActividad` | RandomAccessFile (UT1) |
| `complementarias.DemoSQLite` | Demostración de SQLite embebido (UT2) |
| `comprobaciones.ComprobarConexion` | Prueba rápida de la conexión a MySQL (UT2) |

Se pueden lanzar con **Run** sobre su `main` en VS Code, o con Maven:

```bash
mvn compile exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite
```

En **PowerShell** el parámetro va entre comillas:

```powershell
mvn compile exec:java "-Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite"
```

Para desactivar los colores ANSI: `-Ddatastock.color=false` o la variable de entorno `NO_COLOR=1`.
Los mensajes llevan siempre el prefijo `[OK]`, `[ERROR]` o `[AVISO]`, así que se entienden también sin color.

## 5. Menús

### Menú principal

```text
1. Explorar ficheros y directorios     RF-FIC-01
2. Importar productos desde JSON       RF-FIC-02
3. Importar productos desde XML        RF-FIC-03
4. Consultar productos                 RF-FIC-04
5. Exportar productos                  RF-FIC-05
6. Guardar y recuperar inventario      RF-FIC-06
7. Gestionar base de datos MySQL       UT2
0. Salir
```

### Submenú 7 – Gestión MySQL (UT2)

```text
1. Comprobar conexión y mostrar información de la BD   RF-JDBC-01
2. Gestionar categorías                                RF-JDBC-02
   1. Alta · 2. Listar · 3. Modificar nombre · 4. Eliminar · 0. Volver
3. Gestionar proveedores                               RF-JDBC-02
   1. Alta · 2. Listar · 3. Modificar · 4. Eliminar · 0. Volver
4. Gestionar productos (CRUD)                          RF-JDBC-03
   1. Alta · 2. Consultar por código · 3. Modificar · 4. Eliminar · 0. Volver
5. Consultar y buscar productos                        RF-JDBC-04
   1. Listar todos · 2. Por código · 3. Por nombre · 4. Stock inferior a un umbral · 0. Volver
6. Registrar entrada o salida de stock                 RF-JDBC-05
   1. ENTRADA · 2. SALIDA · 0. Volver
7. Consultar historial de movimientos                  RF-JDBC-06
   1. Todos · 2. De un producto · 0. Volver
8. Importar productos de memoria a MySQL               RF-JDBC-07
9. Demostración de transacción con commit/rollback     RF-JDBC-08
0. Volver al menú principal
```

Los menús (paquete `ui`) solo **piden datos, muestran resultados y llaman a otras clases**.
El SQL está en las clases del paquete `dao` (separación entre consola y acceso a datos).
Todas las opciones piden los datos de uno en uno, permiten volver (Intro o 0) y piden confirmación S/N antes
de modificar o borrar. Una operación fallida muestra el error y vuelve al menú, sin cerrar la aplicación.

## 6. Requisitos implementados – UT1 (ficheros)

### RF-FIC-01 · Explorar rutas, ficheros y directorios
**Clase:** `ficheros/ExploradorRutas` (usa `java.io.File`)
- **Fichero:** muestra nombre, ruta absoluta y tamaño en bytes.
- **Directorio:** lista sus hijos inmediatos (`[FICHERO]` con su tamaño o `[DIR]`), primero los ficheros y después los directorios, y muestra el total de cada tipo.
- Informa de ruta vacía, ruta inexistente, directorio vacío o sin permisos, sin cerrar la aplicación.

### RF-FIC-02 · Importar productos desde JSON
**Clase:** `ficheros/ImportarJson` (usa **Gson**)
- Formato: objeto raíz con la clave `productos`, que contiene un array.
- Funciona en **tres fases**:
  1. **Fichero:** comprueba que existe, que no es un directorio y que se puede leer.
  2. **Documento:** lee **todo** el JSON (`JsonParser`) y comprueba la estructura general. Si está mal formado o la estructura no es correcta, lanza `ImporExeption` y **el inventario no cambia**.
  3. **Productos:** para cada registro comprueba que los campos numéricos lo son, lo valida con `ValidadorProducto` y rechaza los **códigos duplicados** (ya en memoria o repetidos en el mismo fichero).
- Los productos válidos se **añaden**; los incorrectos se rechazan indicando el motivo.
- Al final muestra el resumen (`ResultadoImportacion`): encontrados, importados, rechazados e incidencias.
- Admite ficheros UTF-8 con marca BOM.

### RF-FIC-03 · Importar productos desde XML
**Clase:** `ficheros/ImportarXml` (usa **DOM**)
- Formato: raíz `<catalogo>` con elementos `<producto>`.
- Mismas tres fases y **las mismas validaciones** que JSON (ambos usan `ValidadorProducto`).
- **Protección XXE:** el analizador prohíbe cualquier `DOCTYPE` y desactiva las entidades externas y XInclude. Un XML con DTD se rechaza completo.
- Solo procesa los elementos `<producto>` hijos directos de `<catalogo>` (se ignoran los nodos de texto y espacios).
- Si la raíz no es `<catalogo>` o el XML está mal formado, la importación se cancela sin modificar el inventario.

### RF-FIC-04 · Consultar productos
**Clase:** `ficheros/ConsultarProductos`
- **Listar:** todos los productos ordenados por código. Se ordena una **copia** para no alterar la colección.
- **Buscar por código:** coincidencia exacta sin distinguir mayúsculas ni espacios extremos (`tec-001` encuentra `TEC-001`).
- **Buscar por nombre:** coincidencia parcial sin distinguir mayúsculas (`teclado` encuentra `Teclado USB`).
- Mensajes claros si el inventario está vacío, si no se escribe nada o si no hay coincidencias.

### RF-FIC-05 · Exportar a JSON y XML
**Clase:** `ficheros/ExportarProductos`
- **JSON** con Gson: `{ "productos": [ ... ] }` con sangría (pretty printing).
- **XML** con DOM: `catalogo / producto / campos`, escrito con `Transformer` en UTF-8 y con sangría.
- Exporta todos los productos ordenados por código y sin alterar sus datos.
- Comprueba que el inventario no está vacío, que existe el directorio de destino y que hay permisos de escritura.
- Si el fichero ya existe, **pide confirmación antes de sobrescribirlo**; si se cancela, el original queda intacto.
- Informa de la ruta, el formato y el número de productos exportados.
- Lo exportado se puede volver a importar y se obtienen los mismos productos.

### RF-FIC-06 · Guardar y recuperar el inventario (serialización)
**Clase:** `ficheros/ControlarInventario`
- **Guardar:** serializa la colección completa en un `.dat` con `ObjectOutputStream`.
  No crea ningún fichero si el inventario está vacío y pide confirmación antes de sobrescribir.
- **Recuperar:** lee con `ObjectInputStream` y **sustituye** el inventario:
  - Un **filtro de deserialización** (`ObjectInputFilter`) solo admite las clases de DataStock
    (`ArrayList`, `Producto`, `BigDecimal`...), para no deserializar ficheros de procedencia desconocida.
  - Comprueba que lo leído es una lista, que cada elemento es un `Producto` válido y que no hay códigos repetidos.
  - **Primero** lee y valida todo; si hay productos en memoria, pide confirmación; **solo entonces** sustituye la colección.
  - Un fichero dañado o ajeno se rechaza y la memoria no cambia.

### RF-FIC-07 · Consultas XPath (actividad independiente)
**Clase:** `complementarias/XPathActividad` (con su propio `main`, no está en el menú)
- Pide la ruta de un XML, lo abre con el mismo analizador seguro (rechaza DTD) y comprueba que la raíz es `<catalogo>`.
- Sobre el documento ya abierto permite repetir cuatro consultas hasta pulsar 0:
  1. **Por código:** selecciona `/catalogo/producto` y compara el código en Java. El texto del usuario **no se concatena** en la expresión XPath.
  2. **Por categoría:** `/catalogo/producto[categoriaId = N]`, después de validar que N es un entero positivo.
  3. **Precio superior:** `/catalogo/producto[precio > X]`, después de validar que X es un número. Un producto que cueste exactamente X **no** aparece.
  4. **Contar:** `count(/catalogo/producto)`, que cuenta todos los productos aunque alguno tenga datos incorrectos.
- No modifica el inventario.

### RF-FIC-08 · Acceso aleatorio con RandomAccessFile (actividad independiente)
**Clase:** `complementarias/AccesoAleatorioActividad` (con su propio `main`, no está en el menú)
- Al arrancar pide el fichero binario (Intro = `datos/stock.bin`).
- Menú de la actividad:
  1. Cargar productos en memoria desde un JSON o XML (reutiliza `ImportarJson` / `ImportarXml`).
  2. Generar `stock.bin` a partir de una copia de esos productos, ordenados por código.
  3. Mostrar todos los registros (lectura **secuencial**).
  4. Consultar un registro por posición (acceso **aleatorio**).
  5. Modificar el stock de un registro.
  6. Ver el stock de los productos en memoria.
- **Registro de longitud fija de 36 bytes:**

  | Campo  | Tipo                        | Bytes | Posición dentro del registro |
  |--------|-----------------------------|-------|------------------------------|
  | código | 12 caracteres Java (`char`) | 24    | 0 – 23                       |
  | precio | `double`                    | 8     | 24 – 31                      |
  | stock  | `int`                       | 4     | 32 – 35                      |

- **Consultar:** `seek(posición × 36)`.
- **Modificar solo el stock:** `seek(posición × 36 + 32)` y `writeInt`, sin reescribir el registro.
- El código se guarda siempre con 12 caracteres (relleno con espacios).
- Al generar, si el fichero existe pide confirmación y lo trunca (`setLength(0)`) antes de escribir. Sin productos en memoria no se genera.
- Comprueba que el tamaño del fichero es múltiplo de 36 y rechaza posiciones fuera de rango y stock negativo.
- `stock.bin` es una **copia independiente**: modificarlo no cambia los productos en memoria (la opción 6 lo demuestra).

## 7. Requisitos implementados – UT2 (JDBC y MySQL)

Todas las operaciones usan **`PreparedStatement`** para los datos que escribe el usuario (nunca se concatenan
en el SQL) y **try-with-resources** para cerrar `Connection`, `PreparedStatement`, `Statement`,
`CallableStatement` y `ResultSet`, también cuando hay errores.
Cada operación abre una conexión corta y la cierra al terminar: ninguna conexión queda abierta.

### RF-JDBC-01 · Conectar y comprobar el esquema (7.1)
**Clases:** `configuracion/ConexionBD`, `comprobaciones/ComprobarConexion`
- Muestra en verde la confirmación, el nombre de la base de datos, el gestor y su versión (`DatabaseMetaData`)
  y el número de productos (`SELECT COUNT(*) FROM producto`).
- Los fallos de conexión se traducen a mensajes claros en rojo, **sin mostrar la contraseña**:

  | Situación | Cómo se detecta | Mensaje |
  |---|---|---|
  | Usuario o contraseña incorrectos | código MySQL **1045** | usuario o contraseña incorrectos |
  | No existe la base de datos | código **1049** | ejecuta antes el script |
  | Servidor apagado o URL errónea | SQLState que empieza por **08** | no se puede conectar con el servidor |
  | Falta configuración | (propia) | crea `datastock.properties` o define las variables |

- `DriverManager.setLoginTimeout(5)`: si MySQL está apagado no se queda esperando.

### RF-JDBC-02 · Categorías y proveedores (7.2 y 7.3)
**Clases:** `dao/CategoriaDao`, `dao/ProveedorDao`, `ui/MenuCategorias`, `ui/MenuProveedores`
- **Categorías:** alta (ID positivo y nombre de 1 a 60 caracteres, sin espacios extremos), listado por ID,
  modificación del nombre (el ID no cambia) y baja con confirmación.
  No se admiten IDs ni nombres repetidos (la comparación la hace MySQL, sin distinguir mayúsculas ni tildes).
- **Proveedores:** alta (ID, nombre hasta 100 caracteres y email **opcional** hasta 120 con formato básico),
  listado (sin email se muestra «—»), modificación y baja.
  - Sin email se guarda **NULL** (`setNull`), no un texto vacío: así varios proveedores pueden no tener email
    aunque la columna sea `UNIQUE`.
  - Al modificar: **Intro** conserva el valor actual y **`-`** deja el email sin valor.
  - No se admiten IDs ni emails repetidos.
- Se comprueba el resultado de `executeUpdate()` (filas afectadas) para saber si se ha modificado algo.
- **Baja con productos asociados:** MySQL lo impide por la clave foránea (código **1451**). Se muestra en rojo
  y **no se borra nada**: ni la categoría/proveedor ni sus productos.

### RF-JDBC-03 · CRUD de productos (7.4)
**Clases:** `dao/ProductoDao`, `ui/MenuProductos`
- **Alta:** código (se pasa a mayúsculas), nombre, precio (`BigDecimal`, admite coma o punto), stock inicial,
  categoría y proveedor. Se valida cada dato con `ValidadorProducto` y se comprueba en MySQL que el código no
  existe y que la categoría y el proveedor **sí** existen. Si algo falla, no se inserta.
- **Consulta por código:** muestra todos los datos con el nombre de la categoría y del proveedor.
- **Modificación:** nombre, precio, categoría y proveedor; **Intro conserva** cada valor. El **código y el stock
  no se modifican aquí** (el `UPDATE` no los incluye): el stock solo cambia con los movimientos (7.6).
- **Baja:** con confirmación. Si el producto tiene movimientos, la clave foránea lo impide y se conserva
  el historial (nunca se borran movimientos para forzar la baja).

### RF-JDBC-04 · Consultas con ResultSet (7.5)
**Clases:** `dao/ConsultaProductosDao`, `modelo/ProductoDetalle` (DTO), `ui/MenuConsultasMySQL`
- Todas las consultas usan **JOIN** con `categoria` y `proveedor` para mostrar sus nombres.
- **Listar todos** ordenados por código · **Por código** (exacto) · **Por nombre** con `LIKE ?`
  (los `%` se añaden en Java, no en el SQL) · **Stock inferior a un umbral** (`stock < ?`, ordenado por stock
  y código).
- Cada fila se convierte en un objeto **`ProductoDetalle`** (DTO), no en valores sueltos.
- Son de **solo lectura** (solo `SELECT`). Sin resultados se muestra un aviso, no un error.

### RF-JDBC-05 · Movimientos con procedimiento almacenado (7.6)
**Clases:** `dao/MovimientoDao`, `ui/MenuMovimientos`
- Pide el código (muestra nombre y stock actual), la cantidad (> 0) y confirmación.
- Llama a `{CALL registrar_movimiento(?, ?, ?)}` con **`CallableStatement`**. **Java no hace ningún `UPDATE`
  del stock**: el procedimiento actualiza el stock e inserta el movimiento en una sola transacción
  (`START TRANSACTION`, `SELECT ... FOR UPDATE`, `UPDATE`, `INSERT`, `COMMIT`).
- No se añade otra transacción desde Java para no interferir con la del procedimiento.
- Después consulta el último movimiento y muestra tipo, stock anterior, cantidad y stock posterior.
- Los errores del procedimiento (`SIGNAL SQLSTATE '45000'`: producto inexistente, stock insuficiente…) se
  muestran en rojo; el procedimiento hace `ROLLBACK` y **no cambia ni el stock ni el historial**.

### RF-JDBC-06 · Historial de movimientos (7.7)
**Clases:** `dao/MovimientoDao`, `ui/MenuHistorial`
- **Todos los movimientos** o **los de un producto**, con ID, fecha y hora, producto, tipo, cantidad,
  stock anterior y posterior, ordenados por `fecha, id` (orden estable aunque coincidan fechas).
- Distingue: producto **inexistente** (error), producto **sin movimientos** («Sin movimientos registrados»)
  y producto con movimientos. Muestra también el stock actual para no confundirlo con el histórico.
- Solo lectura.

### RF-JDBC-07 · Importar productos de memoria a MySQL (7.8)
**Clases:** `servicio/ImportadorMySQL`, `comprobaciones/ComprobacionesBD`, `dao/ProductoDao`
- Muestra cuántos productos hay en memoria; si no hay ninguno, avisa y vuelve. Si hay, pide confirmación.
- Usa **una sola conexión** con `setAutoCommit(false)` para todo el lote. Para cada producto:
  1. Valida los datos (`ValidadorProducto`) → si fallan: **rechazado**.
  2. Si el código ya existe en MySQL → **omitido por duplicado**.
  3. Si no existe la categoría o el proveedor → **rechazado** (no se crean automáticamente).
  4. Si todo es correcto → `INSERT` (pendiente hasta el `commit`).
- Al terminar: **`commit()`**. Si salta un error SQL inesperado: **`rollback()`** y no queda insertado
  **ningún** producto del lote.
- Los rechazos por validación no provocan rollback: el resto del lote sigue.
- Resumen final: analizados, insertados, omitidos por duplicado y rechazados.
- Trabaja sobre una **copia normalizada** de cada producto (código en mayúsculas): el inventario en memoria
  **no se modifica**.
- Para la prueba CP-JDBC-18 se puede **forzar un error SQL** al final del lote (reinserta un producto
  duplicado) y comprobar que se hace rollback de todo.

### RF-JDBC-08 · Demostración de commit y rollback (7.9)
**Clase:** `servicio/DemoTransacciones`
- Elige automáticamente IDs de categoría **libres a partir de 9000**, para no afectar a datos reales, explica
  lo que va a hacer y pide confirmación.
- **Escenario A (commit):** `setAutoCommit(false)`, inserta dos categorías, `commit()` y comprueba con
  `SELECT` que existen.
- **Escenario B (rollback):** inserta una categoría y después repite un ID ya existente (error **1062**,
  clave duplicada). Captura la `SQLException`, hace `rollback()` y comprueba con `SELECT` que la primera
  inserción **tampoco** se ha guardado.
- Restaura el `autoCommit` original en un `finally` y cierra la conexión.
- Al final muestra los IDs de prueba que han quedado y cómo borrarlos (7.2 → 4, o
  `DELETE FROM categoria WHERE id IN (...)`).
- Este rollback es de **Java**; no hay que confundirlo con el del procedimiento `registrar_movimiento`.

## 8. Validación de productos

Clase `validacion/ValidadorProducto`:

| Campo         | UT1 – `validar()` (memoria)                       | UT2 – `validarProducto()` (antes de MySQL)                 |
|---------------|---------------------------------------------------|------------------------------------------------------------|
| `codigo`      | Obligatorio, máx. 12, letras, dígitos y guion     | Se pasa a mayúsculas; 1–12 caracteres `A-Z`, `0-9` y `-`   |
| `nombre`      | 3–100 caracteres                                  | Sin espacios extremos, 3–100 caracteres                    |
| `precio`      | Mayor que 0 (`BigDecimal`)                        | Mayor que 0, **máximo 2 decimales** y ≤ 99999999.99 (`DECIMAL(10,2)`) |
| `stock`       | Entero ≥ 0                                        | Entero ≥ 0                                                 |
| `categoriaId` | Entero positivo                                   | Entero positivo **y que exista en MySQL**                  |
| `proveedorId` | Entero positivo                                   | Entero positivo **y que exista en MySQL**                  |

- Las reglas de la UT2 son las mismas que las restricciones `CHECK` del script: se comprueban en Java para dar
  un mensaje claro **antes** de enviar el `INSERT`, y MySQL sigue siendo la última barrera.
- Lo que depende de los datos guardados (código duplicado, existencia de categoría y proveedor) se consulta en MySQL.
- Los validadores **devuelven** el motivo del error (o `null`) en lugar de imprimirlo: así cada parte del programa
  decide qué hacer (mostrarlo, no insertar o contarlo en un resumen).

## 9. Gestión de errores

- **Ficheros (UT1):** `ficheros/ImporExeption` cancela toda la operación (ruta vacía o inexistente, fichero
  ilegible, documento mal formado, estructura incorrecta, error de escritura o `.dat` dañado).
  Un producto incorrecto no lanza excepción: se anota como incidencia y se continúa.
- **Base de datos (UT2):** las `SQLException` se capturan en los menús y se traducen con
  `ConexionBD.explicarError` según el código de MySQL: **1045** (acceso denegado), **1049** (base de datos
  inexistente), **1062** (clave duplicada), **1451** (borrado bloqueado por clave foránea), SQLState **08**
  (sin conexión) y SQLState **45000** (errores lanzados por el procedimiento con `SIGNAL`).
- **Colores (`util/Mensajes`):** `[ERROR]` en rojo, `[OK]` en verde y `[AVISO]` en amarillo (también las
  preguntas S/N). No se muestran contraseñas ni trazas técnicas al usuario.
- Los errores previstos muestran el mensaje y la aplicación vuelve al menú, sin cerrarse.

## 10. Actividad SQLite: gestor embebido frente a gestor independiente

**Clase:** `complementarias/DemoSQLite` (con su propio `main`; no está en ningún menú).
DataStock sigue usando MySQL: esta clase solo demuestra el uso de un gestor **embebido**.

- Crea la carpeta `datos/sqlite` si no existe y abre `jdbc:sqlite:datos/sqlite/productos_demo.db`
  (el fichero se crea solo al conectar). El `.db` está en `.gitignore`.
- Crea la tabla `producto_demo (codigo TEXT PRIMARY KEY, nombre TEXT)` con `Statement` y
  `CREATE TABLE IF NOT EXISTS`.
- Inserta `SQL-001 / Producto demo A` y `SQL-002 / Producto demo B` con `PreparedStatement` e
  **`INSERT OR IGNORE`**: `executeUpdate()` devuelve 1 si inserta y 0 si ya existía, así que en la segunda
  ejecución muestra `Insertados: 0 | Ya existentes: 2` y no se acumulan copias.
- Consulta `SELECT codigo, nombre FROM producto_demo ORDER BY codigo` y muestra las filas.
- Todos los recursos se cierran con try-with-resources; al terminar comprueba que el fichero `.db` sigue existiendo.

**Ejecutar:** Run sobre el `main` de `DemoSQLite`, o
`mvn compile exec:java "-Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite"`.
Para repetir la "primera ejecución", basta con borrar `datos/sqlite/productos_demo.db`.

### Comparativa MySQL y SQLite

| Pregunta | MySQL | SQLite |
|---|---|---|
| ¿Necesita servidor? | **Sí.** Es un gestor **independiente**: un servicio aparte (`MySQL80`) que debe estar arrancado y al que se conectan los programas por red (puerto 3306). | **No.** Es un gestor **embebido**: el motor va dentro del propio programa (en el driver) y no hay que instalar ni arrancar nada. |
| ¿Dónde se guardan los datos? | En los ficheros internos del servidor, que el programa no toca directamente. | En **un único fichero** (`productos_demo.db`) dentro del proyecto. |
| ¿Cómo se conecta por JDBC? | `jdbc:mysql://localhost:3306/datastock` + **usuario y contraseña**. | `jdbc:sqlite:datos/sqlite/productos_demo.db` (solo la ruta, sin usuario ni contraseña). |
| ¿Qué dependencia usa? | `com.mysql:mysql-connector-j` 9.1.0 | `org.xerial:sqlite-jdbc` 3.46.1.3 |
| Ventajas | Muchos usuarios a la vez, gestión de usuarios y permisos, procedimientos almacenados, buen rendimiento con muchos datos, acceso desde otros equipos. | Cero instalación y configuración, muy ligero, la base de datos se copia como un fichero, ideal para pruebas. |
| Inconvenientes | Hay que instalarlo, arrancarlo y configurarlo; credenciales que proteger. | Pensado para un solo usuario o pocos accesos simultáneos; sin usuarios ni permisos; no sirve como servidor central. |
| ¿Para qué lo elegiría? | Aplicaciones con varios usuarios o puestos que comparten los mismos datos, como el inventario de una empresa (DataStock). | Aplicaciones de escritorio o móviles con datos locales, prototipos, pruebas y configuraciones. |

El **código JDBC es prácticamente el mismo** en los dos (`Connection`, `PreparedStatement`, `ResultSet`):
solo cambian el driver y la URL.

### Pool de conexiones

Abrir una conexión con MySQL es lento (red, autenticación…). Un **pool de conexiones** (por ejemplo, HikariCP)
mantiene varias conexiones **ya abiertas** y las presta al programa: al "cerrar" una conexión, en realidad se
devuelve al pool para reutilizarla. Es útil en aplicaciones con **muchos usuarios o peticiones a la vez**, como
un servidor web, donde abrir una conexión nueva en cada petición sería muy costoso.

DataStock **no lo necesita**: es una aplicación de consola con **un solo usuario** que hace una operación cada
vez. Cada operación abre una conexión **corta**, la usa y la **cierra** con try-with-resources, así que nunca hay
conexiones abiertas sin usar ni se agotan los recursos del servidor.

## 11. Estructura del proyecto

```text
DataStock_UT1_Proyecto_Inicial/
├── pom.xml
├── README.md
├── .gitignore
├── Datastock.properties.ejemplo   # Plantilla de conexión (SÍ se sube)
├── datastock.properties           # Conexión real con contraseña (NO se sube)
├── docs/                          # Documentos de pruebas
├── datos/
│   ├── Productos.json             # Productos del recorrido de la UT2 (TEC-001, RAT-002, MON-003)
│   ├── ejemplos/                  # Catálogos de ejemplo del enunciado (no se modifican)
│   ├── pruebas/                   # Ficheros de prueba válidos e inválidos (UT1)
│   ├── salidas/                   # Exportaciones y .dat generados (ignorados por Git)
│   └── sqlite/                    # productos_demo.db de DemoSQLite (ignorado por Git)
├── material_referencia/           # Script SQL (DataStock_BD_MySQL.sql) y diagrama E-R
└── src/main/java/es/iesisidra/datastock/
    ├── App.java                          # Punto de entrada
    ├── modelo/
    │   ├── Producto.java                 # Serializable
    │   ├── Categoria.java
    │   ├── Proveedor.java
    │   ├── MovimientoStock.java          # Con el enum TipoMovimiento (ENTRADA/SALIDA)
    │   └── ProductoDetalle.java          # DTO: producto + nombres de categoría y proveedor
    ├── servicio/
    │   ├── Inventario.java               # Colección compartida en memoria
    │   ├── ResultadoImportacion.java     # Resumen de una importación de fichero
    │   ├── ImportadorMySQL.java          # RF-JDBC-07 (memoria → MySQL con transacción)
    │   └── DemoTransacciones.java        # RF-JDBC-08 (commit y rollback)
    ├── validacion/
    │   └── ValidadorProducto.java        # Reglas de UT1 y de UT2
    ├── configuracion/
    │   └── ConexionBD.java               # Conexión JDBC y traducción de errores
    ├── comprobaciones/
    │   ├── ComprobarConexion.java        # RF-JDBC-01
    │   └── ComprobacionesBD.java         # existeProducto/Categoria/Proveedor con una conexión dada
    ├── dao/
    │   ├── CategoriaDao.java             # RF-JDBC-02
    │   ├── ProveedorDao.java             # RF-JDBC-02
    │   ├── ProductoDao.java              # RF-JDBC-03
    │   ├── ConsultaProductosDao.java     # RF-JDBC-04
    │   └── MovimientoDao.java            # RF-JDBC-05 y RF-JDBC-06
    ├── ficheros/
    │   ├── ImporExeption.java            # Error que cancela una operación
    │   ├── ExploradorRutas.java          # RF-FIC-01
    │   ├── ImportarJson.java             # RF-FIC-02
    │   ├── ImportarXml.java              # RF-FIC-03
    │   ├── ConsultarProductos.java       # RF-FIC-04
    │   ├── ExportarProductos.java        # RF-FIC-05
    │   └── ControlarInventario.java      # RF-FIC-06
    ├── complementarias/
    │   ├── XPathActividad.java           # RF-FIC-07
    │   ├── AccesoAleatorioActividad.java # RF-FIC-08
    │   └── DemoSQLite.java               # Actividad SQLite (UT2)
    ├── ui/
    │   ├── MenuPrincipal.java            # Menú principal y submenús de la UT1
    │   ├── MenuMySQL.java                # Submenú 7 (UT2)
    │   ├── MenuCategorias.java           # 7.2
    │   ├── MenuProveedores.java          # 7.3
    │   ├── MenuProductos.java            # 7.4
    │   ├── MenuConsultasMySQL.java       # 7.5
    │   ├── MenuMovimientos.java          # 7.6
    │   └── MenuHistorial.java            # 7.7
    └── util/
        ├── LectorConsola.java
        ├── ColoresConsola.java
        └── Mensajes.java                 # [OK] verde, [ERROR] rojo, [AVISO] amarillo
```

## 12. Ficheros de prueba

### `datos/ejemplos/` (del enunciado)

| Fichero | Contenido |
|---|---|
| `catalogo_valido.json` | Dos productos válidos |
| `catalogo_valido.xml` | Un producto válido |
| `catalogo_con_errores.json` | Precio negativo y código duplicado |
| `catalogo_malformado.json` | JSON con sintaxis incorrecta |

### `datos/Productos.json` (UT2)

Los tres productos del recorrido del enunciado: `TEC-001` y `RAT-002` (categoría 1, proveedor 1) y `MON-003`
(categoría 2, **proveedor 3**). Sirve para probar la importación a MySQL (7.8): sin el proveedor 3, `MON-003`
se rechaza; tras darlo de alta y repetir, `TEC-001` y `RAT-002` se omiten por duplicados y `MON-003` se inserta.

### `datos/pruebas/` (UT1)

| Fichero | Para qué sirve |
|---|---|
| `json_tres_validos.json` | Tres productos válidos |
| `json_mixto_5.json` | Tres válidos y dos inválidos (precio negativo, código vacío) |
| `json_campos_invalidos.json` | Tipos incorrectos, campos que faltan y duplicado en el mismo fichero |
| `json_raiz_array.json` | La raíz es un array en lugar de un objeto |
| `json_sin_array.json` | Falta la clave `productos` |
| `json_productos_no_array.json` | `productos` no es un array |
| `json_contenido_sobrante.json` | Texto sobrante después del JSON |
| `json_vacio.json` | Fichero vacío |
| `json_con_bom.json` | UTF-8 con BOM (se importa correctamente) |
| `xml_tres_validos.xml` | Tres productos válidos |
| `xml_mixto.xml` | Productos válidos e inválidos |
| `xml_con_dtd.xml` | XML con DTD (ataque XXE): se rechaza |
| `xml_malformado.xml` | Etiquetas mal cerradas |
| `xml_raiz_incorrecta.xml` | Raíz distinta de `<catalogo>` |
| `xml_duplicado_json.xml` | Producto ya importado desde JSON |
| `xml_xpath.xml` | XPath: categorías 1, 2 y 3, un precio de exactamente 100 y un producto con precio incorrecto |

## 13. Decisiones de diseño

**UT1**
- **Importar añade; recuperar `.dat` sustituye.** JSON y XML son formatos de intercambio legibles; la serialización Java es binaria y solo sirve para Java.
- **Se lee todo antes de modificar:** un documento mal formado o un `.dat` dañado nunca dejan el inventario a medias.
- **El precio es `BigDecimal`** para no perder precisión con importes. Solo en `stock.bin` se guarda como `double`, porque así lo define el formato del registro.
- **Códigos:** en memoria se admiten mayúsculas y minúsculas, pero la unicidad y las búsquedas no las distinguen.
- **Campos numéricos:** cada campo se lee como texto y después se comprueba si es un número, igual en JSON que en XML.
- **Lectura JSON:** `JsonParser` de Gson detecta los errores habituales (llaves sin cerrar, comas que faltan, texto sobrante), aunque tolera algunas variantes no estándar como las comillas simples.
- **Una sola excepción propia** (`ImporExeption`) para todos los errores de fichero que cancelan una operación.

**UT2**
- **Separación por capas sencilla:** `ui` (consola) → `dao` (SQL) → MySQL. Los menús no contienen SQL y los DAO no leen del teclado ni imprimen.
- **Conexiones cortas:** cada método DAO abre su conexión y la cierra con try-with-resources. Solo cuando hace falta una transacción (7.8 y 7.9) se usa una única conexión para varias operaciones; por eso existen `ProductoDao.insertar(Connection, …)`, `CategoriaDao.insertar(Connection, …)` y `ComprobacionesBD`, que reciben la conexión.
- **Comprobar antes y dejar a MySQL como última barrera:** se comprueban en Java los duplicados y las claves foráneas para dar mensajes concretos, y las restricciones de la base de datos siguen protegiendo los datos (errores 1062 y 1451).
- **El stock solo cambia con el procedimiento almacenado**, que garantiza que el cambio de stock y su movimiento se guardan juntos o no se guarda ninguno.
- **Memoria y MySQL son almacenes distintos:** no se sincronizan automáticamente; la importación 7.8 copia sin modificar la memoria.
- **Credenciales fuera del código y de Git:** `datastock.properties` local o variables de entorno.

## 14. Pruebas

- **UT1:** los casos de aceptación (CP-FIC, CP-JSON, CP-XML, CP-CON, CP-EXP, CP-SER, CP-XPATH y CP-RAF) están en
  `docs/DataStock_UT1_Registro_Pruebas.docx`, con identificador, datos usados, resultado esperado, resultado
  obtenido y estado.
- **UT2:** los casos **CP-JDBC-01 a CP-JDBC-20** y **CP-SQLITE-01 a CP-SQLITE-03** se documentan en el
  registro de pruebas de la UT2, con capturas de cada ejecución, siguiendo el recorrido del enunciado
  (alta de categorías 1 y 2 y del proveedor 1 → importación de `datos/Productos.json` → reinicio de la
  aplicación → alta del proveedor 3 → nueva importación → consultas, movimientos, historial y transacciones).