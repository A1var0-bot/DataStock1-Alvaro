# DataStock – UT1 (Ficheros)

**Autor:** Álvaro Monjas Navas
**Repositorio:** https://github.com/A1var0-bot/DataStock1-Alvaro
**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027
**Lenguaje:** Java 21 · **Construcción:** Maven · **Interfaz:** consola

## 1. Descripción

DataStock es una aplicación de consola para gestionar el inventario de productos de una pequeña empresa.
En la UT1 trabaja **solo con ficheros** (sin base de datos):

- Explora rutas, ficheros y directorios.
- Importa catálogos de productos desde **JSON** (Gson) y **XML** (DOM), validando cada producto.
- Consulta los productos cargados en memoria.
- Exporta el inventario a JSON y XML.
- Guarda y recupera el inventario mediante **serialización** (`.dat`).
- Incluye dos actividades independientes: consultas **XPath** y acceso aleatorio con **RandomAccessFile**.

Todas las opciones del menú comparten **una única colección de productos en memoria** (`Inventario`).

## 2. Requisitos previos

- **JDK 21** (`java -version`).
- **Maven**, o un IDE con soporte Maven (VS Code, IntelliJ, Eclipse).
- Conexión a Internet la primera vez, para descargar **Gson 2.11.0**.
- Consola en UTF-8 para ver bien las tildes y el símbolo €.

## 3. Cómo ejecutar

### Aplicación principal (menú)

Desde la carpeta que contiene `pom.xml`:

```bash
mvn clean compile
mvn exec:java
```

Se ejecuta `es.iesisidra.datastock.App`, que crea el inventario y muestra el menú.
En VS Code también se puede lanzar con la configuración **"DataStock (menú principal)"**.

> El menú completo se arranca **siempre desde `App`**. Las clases del paquete `ficheros`
> tienen además un `main` propio para probar cada funcionalidad por separado.

### Actividades complementarias (fuera del menú)

```bash
mvn compile exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
mvn compile exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
```

En **PowerShell** el parámetro va entre comillas:

```powershell
mvn compile exec:java "-Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad"
```

Para desactivar los colores ANSI: `-Ddatastock.color=false` o la variable de entorno `NO_COLOR=1`.

## 4. Menú principal

```text
1. Explorar ficheros y directorios     RF-FIC-01
2. Importar productos desde JSON       RF-FIC-02
3. Importar productos desde XML        RF-FIC-03
4. Consultar productos                 RF-FIC-04
   1. Listar · 2. Buscar por código · 3. Buscar por nombre · 0. Volver
5. Exportar productos                  RF-FIC-05
   1. Exportar a JSON · 2. Exportar a XML · 0. Volver
6. Guardar y recuperar inventario      RF-FIC-06
   1. Guardar .dat · 2. Recuperar .dat · 0. Volver
0. Salir
```

El menú (`ui/MenuPrincipal`) solo **presenta las opciones y llama a los métodos** de las clases
del paquete `ficheros`. La lógica de lectura, escritura, validación y serialización no está en el `switch`.

## 5. Requisitos implementados

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

## 6. Validación de productos

Clase `validacion/ValidadorProducto`, común a JSON, XML y `.dat`:

| Campo         | Regla                                                            |
|---------------|------------------------------------------------------------------|
| `codigo`      | Obligatorio, máximo 12 caracteres, solo letras, dígitos y guion |
| `nombre`      | Obligatorio, entre 3 y 100 caracteres                            |
| `precio`      | Número mayor que 0 (`BigDecimal`)                                |
| `stock`       | Entero mayor o igual que 0                                       |
| `categoriaId` | Entero positivo                                                  |
| `proveedorId` | Entero positivo                                                  |

- Los códigos **no se duplican**, aunque cambien mayúsculas y minúsculas.
- En UT1 los IDs de categoría y proveedor solo se comprueban como enteros positivos (no se consulta MySQL).

## 7. Gestión de errores

- `ficheros/ImporExeption` es una excepción comprobada que **cancela toda la operación**:
  ruta vacía o inexistente, fichero ilegible, documento mal formado, estructura incorrecta,
  error de escritura o `.dat` dañado.
- Un **producto incorrecto no lanza excepción**: el método devuelve el motivo, se anota como
  incidencia y se continúa con el siguiente.
- Las lecturas y escrituras usan `try-with-resources`, de modo que los ficheros se cierran siempre.
- Los errores previstos muestran un mensaje y la aplicación vuelve al menú, sin cerrarse.

## 8. Estructura del proyecto

```text
DataStock_UT1_Proyecto_Inicial/
├── pom.xml
├── README.md
├── .gitignore
├── datos/
│   ├── ejemplos/        # Catálogos de ejemplo del enunciado (no se modifican)
│   ├── pruebas/         # Ficheros de prueba válidos e inválidos
│   └── salidas/         # Exportaciones y .dat generados (ignorados por Git)
├── material_referencia/ # Modelo E-R y script SQL (para UT2)
└── src/main/java/es/iesisidra/datastock/
    ├── App.java                          # Punto de entrada
    ├── modelo/
    │   ├── Producto.java                 # Serializable
    │   ├── Categoria.java
    │   ├── Proveedor.java
    │   └── MovimientoStock.java
    ├── servicio/
    │   ├── Inventario.java               # Colección compartida en memoria
    │   └── ResultadoImportacion.java     # Resumen de una importación
    ├── validacion/
    │   └── ValidadorProducto.java        # Reglas comunes de un producto
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
    │   └── AccesoAleatorioActividad.java # RF-FIC-08
    ├── ui/
    │   └── MenuPrincipal.java            # Menú principal y submenús
    └── util/
        ├── LectorConsola.java
        └── ColoresConsola.java
```

## 9. Ficheros de prueba

### `datos/ejemplos/` (del enunciado)

| Fichero | Contenido |
|---|---|
| `catalogo_valido.json` | Dos productos válidos |
| `catalogo_valido.xml` | Un producto válido |
| `catalogo_con_errores.json` | Precio negativo y código duplicado |
| `catalogo_malformado.json` | JSON con sintaxis incorrecta |

### `datos/pruebas/`

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

## 10. Decisiones de diseño

- **Importar añade; recuperar `.dat` sustituye.** JSON y XML son formatos de intercambio legibles; la serialización Java es binaria y solo sirve para Java.
- **Se lee todo antes de modificar:** un documento mal formado o un `.dat` dañado nunca dejan el inventario a medias.
- **El precio es `BigDecimal`** para no perder precisión con importes. Solo en `stock.bin` se guarda como `double`, porque así lo define el formato del registro.
- **Códigos:** se admiten mayúsculas y minúsculas, pero la unicidad y las búsquedas no las distinguen.
- **Campos numéricos:** cada campo se lee como texto y después se comprueba si es un número, igual en JSON que en XML.
- **Lectura JSON:** `JsonParser` de Gson detecta los errores habituales (llaves sin cerrar, comas que faltan, texto sobrante), aunque tolera algunas variantes no estándar como las comillas simples.
- **Una sola excepción propia** (`ImporExeption`) para todos los errores de fichero que cancelan una operación (importar, exportar y serializar).

## 11. Pruebas

Los casos de aceptación del enunciado (CP-FIC, CP-JSON, CP-XML, CP-CON, CP-EXP, CP-SER, CP-XPATH y CP-RAF)
están documentados en el documento de pruebas con: identificador, datos usados, resultado esperado,
resultado obtenido y estado.