# Implementación y visualización de rutas en un mapa de calles mediante BFS y DFS

> ⚠️ **Nota:** este README es la base del informe técnico solicitado. Completa los
> campos marcados como `[COMPLETAR]` con la información real del grupo antes de la
> entrega (carátula, integrantes, capturas de pantalla, tabla de resultados y
> conclusiones individuales).

## Carátula

- **Universidad:** [COMPLETAR - nombre de la universidad / logo institucional]
- **Carrera:** Computación
- **Asignatura:** Estructura de Datos
- **Proyecto:** Proyecto Final - Implementación y visualización de rutas en un mapa
  de calles mediante BFS y DFS
- **Docente:** Ing. Pablo Torres
- **Integrantes:**
  - [COMPLETAR - Nombre integrante 1] — [COMPLETAR - correo institucional]
  - [COMPLETAR - Nombre integrante 2] — [COMPLETAR - correo institucional]
  - [COMPLETAR - Nombre integrante 3] — [COMPLETAR - correo institucional]

## Índice

1. [Objetivo](#objetivo)
2. [Descripción del problema](#descripción-del-problema)
3. [Marco teórico](#marco-teórico)
4. [Tecnologías utilizadas](#tecnologías-utilizadas)
5. [Arquitectura y estructura de carpetas](#arquitectura-y-estructura-de-carpetas)
6. [Diagrama UML](#diagrama-uml)
7. [Funcionamiento general](#funcionamiento-general)
8. [Capturas de pantalla](#capturas-de-pantalla)
9. [Ejemplo comentado: BFS](#ejemplo-comentado-bfs)
10. [Tabla comparativa de resultados](#tabla-comparativa-de-resultados)
11. [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
12. [Pruebas realizadas](#pruebas-realizadas)
13. [Conclusiones](#conclusiones)
14. [Recomendaciones y aplicaciones futuras](#recomendaciones-y-aplicaciones-futuras)

## Objetivo

Desarrollar una aplicación en Java que modele un mapa de calles como un grafo,
permita ubicar intersecciones (nodos) y calles (aristas) sobre una imagen de
fondo, e implemente los algoritmos BFS y DFS para encontrar una ruta entre un
punto de inicio (A) y un punto de destino (B), visualizando tanto el proceso de
exploración como la ruta final.

## Descripción del problema

Un mapa de calles puede modelarse como un grafo donde cada intersección es un
vértice y cada calle es una arista (bidireccional si se puede circular en ambos
sentidos, o dirigida si es de un solo sentido). Encontrar una ruta entre dos
intersecciones equivale a encontrar un camino entre dos vértices del grafo. El
proyecto resuelve este problema aplicando dos estrategias clásicas de recorrido:
BFS (que garantiza la ruta con menor número de calles) y DFS (que explora en
profundidad y no garantiza el camino más corto, pero suele ser más económico en
memoria).

## Marco teórico

**Grafos.** Un grafo `G = (V, E)` está compuesto por un conjunto de vértices `V`
y un conjunto de aristas `E` que conectan pares de vértices. En este proyecto se
representa mediante una lista de adyacencia: un mapa donde cada nodo apunta al
conjunto de nodos alcanzables directamente desde él.

**BFS (Breadth-First Search).** Recorre el grafo por niveles, utilizando una
cola (FIFO). Visita primero todos los vecinos directos del nodo de inicio, luego
los vecinos de esos vecinos, y así sucesivamente. Como explora nivel por nivel,
la primera vez que alcanza el destino, la ruta reconstruida es la de menor
cantidad de aristas.

**DFS (Depth-First Search).** Recorre el grafo profundizando en una rama antes
de retroceder. Puede implementarse con recursividad (como en este proyecto) o
con una pila explícita. Cuando una rama no conduce al destino, se hace
*backtracking*: se elimina el último nodo agregado a la ruta actual y se
continúa explorando otra rama.

## Tecnologías utilizadas

- Java (JDK 21), sin librerías externas de grafos ni de algoritmos de búsqueda.
- Swing (`javax.swing`) para la interfaz gráfica de escritorio.
- Persistencia mediante archivo de texto plano en formato CSV, leído y escrito
  con clases estándar de `java.io` / `java.nio`.
- Control de versiones con Git y GitHub.

## Arquitectura y estructura de carpetas

El proyecto aplica el patrón **Modelo-Vista-Controlador (MVC)**, reutilizando
las clases genéricas de grafos (`Graph<T>`, `Node<T>`, `PathFinder<T>`,
`PathResult<T>`) trabajadas en clase, adaptadas para almacenar `MapPoint` como
valor genérico.

```
src/
├── App.java                          # Punto de entrada
├── controllers/
│   └── MapController.java            # Orquesta modelo, algoritmos y persistencia
├── models/
│   ├── MapPoint.java                 # Punto del mapa (id, x, y)
│   └── VisualizationMode.java        # EXPLORATION / FINAL_PATH
├── persistence/
│   ├── GraphRepository.java          # Contrato de persistencia
│   └── FileGraphRepository.java      # Persistencia en archivo CSV
├── structures/
│   ├── node/
│   │   └── Node.java                 # Nodo genérico (equals/hashCode por valor)
│   └── graphs/
│       ├── Graph.java                # Grafo genérico (lista de adyacencia)
│       ├── PathFinder.java           # Contrato de búsqueda de rutas
│       ├── PathResult.java           # Resultado: visitados + ruta
│       └── implementations/
│           ├── BFSPathFinder.java
│           └── DFSPathFinder.java
└── views/
    ├── MainFrame.java                # Ventana principal y controles
    └── MapPanel.java                 # Dibuja el mapa, nodos, aristas y recorridos

resources/
├── maps/
│   └── map.png                       # Imagen de fondo del mapa
└── config/
    └── graph_config.csv              # Se genera/actualiza automáticamente
```

**Responsabilidades por capa:**

- **Modelo** (`models`, `structures`): `MapPoint`, `Graph<MapPoint>`, los
  resultados de búsqueda y la configuración persistida.
- **Vista** (`views`): dibuja el mapa, los puntos, las calles y el recorrido.
  No contiene lógica de BFS/DFS.
- **Controlador** (`controllers`): recibe la interacción del usuario, modifica
  el grafo, ejecuta BFS/DFS a través de `PathFinder`, y delega el guardado a
  `GraphRepository`.

## Diagrama UML

```
 Graph<T> 1 ---- * Node<T> ---- T (valor genérico, en este proyecto MapPoint)

 PathFinder<T>  <<interface>>
        ▲
        |
   ------------------
   |                |
BFSPathFinder<T>  DFSPathFinder<T>

 MapController --uses--> Graph<MapPoint>
 MapController --uses--> PathFinder<MapPoint> (BFS/DFS)
 MapController --uses--> GraphRepository <<interface>>
                              ▲
                              |
                     FileGraphRepository

 MainFrame --uses--> MapController
 MainFrame *-- MapPanel
```

[COMPLETAR: reemplazar este diagrama textual por una imagen UML generada con
una herramienta como draw.io, PlantUML o similar, y explicar brevemente las
relaciones entre clases.]

## Funcionamiento general

1. Al iniciar la aplicación, `FileGraphRepository.load()` lee
   `resources/config/graph_config.csv` (si existe) y reconstruye el grafo.
2. El usuario puede agregar puntos haciendo clic sobre el mapa (modo
   "Agregar punto"), crear calles entre dos puntos (modo "Agregar calle",
   eligiendo si es bidireccional) o eliminar puntos (modo "Eliminar punto").
   Cada cambio se guarda automáticamente mediante
   `FileGraphRepository.save(...)`.
3. El usuario elige un nodo de inicio, un nodo de destino, un algoritmo (BFS o
   DFS) y un modo de visualización (Exploración o Ruta final), y presiona
   "Ejecutar".
4. `MapController.ejecutarBusqueda(...)` invoca al `PathFinder` correspondiente,
   que devuelve un `PathResult` con el orden de nodos visitados y la ruta
   encontrada. La lógica de búsqueda es idéntica sin importar el modo elegido.
5. `MainFrame` anima el resultado sobre `MapPanel`:
   - **Exploración:** revela progresivamente cada nodo visitado y, al
     terminar, resalta la ruta final.
   - **Ruta final:** omite la exploración intermedia y dibuja únicamente la
     ruta reconstruida, de forma progresiva.

## Capturas de pantalla

[COMPLETAR: incluir aquí al menos dos capturas con configuraciones de mapa
diferentes, mostrando nodos, aristas, y los dos modos de visualización.]

## Ejemplo comentado: BFS

```java
public PathResult<T> find(Graph<T> graph, T start, T end) {
    Queue<T> queue = new LinkedList<>();      // nodos pendientes de explorar
    Set<T> visitados = new HashSet<>();       // evita procesar un nodo dos veces
    Set<T> ordenVisita = new LinkedHashSet<>(); // conserva el orden de exploración
    Map<T, T> predecesores = new HashMap<>();  // permite reconstruir la ruta

    queue.add(start);
    visitados.add(start);
    predecesores.put(start, null);

    while (!queue.isEmpty()) {
        T actual = queue.poll();
        ordenVisita.add(actual);

        if (actual.equals(end)) {
            return new PathResult<>(ordenVisita, buildPath(predecesores, end));
        }

        for (Node<T> vecino : graph.getVecinos(actual)) {
            if (!visitados.contains(vecino.getValue())) {
                visitados.add(vecino.getValue());
                predecesores.put(vecino.getValue(), actual);
                queue.add(vecino.getValue());
            }
        }
    }
    return new PathResult<>(ordenVisita, new LinkedHashSet<>()); // no existe ruta
}
```

El algoritmo explora el grafo por niveles gracias a la cola: primero procesa
todos los vecinos directos del nodo de inicio antes de avanzar al siguiente
nivel. El mapa `predecesores` guarda, para cada nodo descubierto, quién lo
descubrió; al llegar al destino, `buildPath` recorre ese mapa hacia atrás para
reconstruir la ruta completa desde el inicio.

## Tabla comparativa de resultados

[COMPLETAR con datos de ejecuciones reales, sin inventar valores.]

| Caso | Algoritmo | Inicio | Destino | Nodos visitados | Aristas en la ruta | Tiempo |
|------|-----------|--------|---------|------------------|---------------------|--------|
| 1    | BFS       |        |         |                  |                     |        |
| 1    | DFS       |        |         |                  |                     |        |
| 2    | BFS       |        |         |                  |                     |        |
| 2    | DFS       |        |         |                  |                     |        |
| 3    | BFS       |        |         |                  |                     |        |
| 3    | DFS       |        |         |                  |                     |        |

**Análisis requerido** [COMPLETAR en base a los resultados reales]:
- ¿Qué diferencias se observaron en el orden de exploración de BFS y DFS?
- ¿BFS encontró siempre la ruta con menor cantidad de aristas?
- ¿DFS encontró rutas diferentes a las obtenidas con BFS?
- ¿Qué algoritmo visitó más nodos en cada caso?
- ¿Cómo influyó la estructura del grafo en el comportamiento de cada algoritmo?
- ¿Qué ventajas aporta separar la lógica del algoritmo de la visualización?
- ¿Qué mejoras podrían implementarse para trabajar con calles ponderadas?

## Cómo ejecutar el proyecto

Requiere JDK 17 o superior.

```bash
# Compilar
javac -d bin -encoding UTF-8 $(find src -name "*.java")

# Ejecutar (debe ejecutarse desde la raíz del proyecto, para que
# resources/maps/map.png y resources/config/graph_config.csv se
# encuentren correctamente)
java -cp bin App
```

También puede abrirse directamente en Visual Studio Code con la extensión
"Extension Pack for Java" y ejecutarse desde `src/App.java`.

## Pruebas realizadas

Se probaron los siguientes escenarios (ver detalle y evidencias en
[COMPLETAR]):

1. Agregar nodo.
2. Evitar identificadores repetidos.
3. Eliminar nodo y sus conexiones.
4. Agregar arista bidireccional.
5. Agregar arista unidireccional.
6. Eliminar arista.
7. Guardar y volver a cargar la configuración.
8. Redimensionar la ventana (la imagen conserva su proporción).
9. Ruta directa entre dos nodos conectados.
10. Ruta con varios nodos intermedios.
11. Grafo con varias rutas posibles.
12. Nodo de inicio igual al nodo destino.
13. Nodo destino sin conexión (no existe ruta).
14. Identificador inexistente.
15. Ejecución en modo exploración.
16. Ejecución en modo ruta final.
17. Carga de configuración válida.
18. Carga de configuración con errores (líneas inválidas se ignoran y se
    reportan por consola).

## Conclusiones

- Conclusión de [COMPLETAR integrante 1]: ____________________________________
- Conclusión de [COMPLETAR integrante 2]: ____________________________________
- Conclusión de [COMPLETAR integrante 3]: ____________________________________

## Recomendaciones y aplicaciones futuras

- Validar la configuración antes de construir el grafo (ya implementado en
  `FileGraphRepository`, que ignora y reporta líneas inválidas).
- Extender el modelo `MapPoint`/`Graph` para soportar calles ponderadas (por
  ejemplo, agregando una distancia o tiempo estimado a cada arista) y así
  poder aplicar Dijkstra o A* en un trabajo futuro.
- Separar por completo la ejecución del algoritmo de las animaciones al medir
  tiempos de ejecución, para no incluir el tiempo de dibujo en la medición.
- Probar el sistema con grafos desconectados y con ciclos, además de los
  casos ya cubiertos.
