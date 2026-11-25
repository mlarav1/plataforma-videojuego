# Plataforma de Videojuegos (Caso 11) - Estructuras de Datos, Unidad IV

Aplicacion Java que integra un **arbol** y un **grafo** en un mismo sistema.

## Arbol general: catalogo
`Plataforma > Categoria > Genero > Subgenero > Videojuego`. Es un arbol general porque cada nodo puede tener cantidad variable de hijos.
- Operaciones: insertar, buscar, modificar, eliminar, hijos, padre, nivel, altura.
- Eliminacion: `eliminar` rechaza nodos con hijos; `eliminarConDescendientes` borra el subarbol (el menu pregunta al usuario).
- Recorridos: profundidad (preorden) y por niveles (cola).

## Grafo: red de jugadores
No dirigido y ponderado (peso = latencia en ms), con lista de adyacencia `Map<T, List<Edge<T>>>`. Una amistad/partida es mutua, y el peso permite rutas de menor costo.
- Vertices y aristas: agregar/eliminar, vecinos, conexion directa, grados.
- BFS (cola), DFS (pila), camino con menos saltos (BFS), ruta de menor latencia (Dijkstra) y comunidades (componentes conexos).

## Integracion
Cada jugador tiene un videojuego favorito (nodo del arbol), por lo que se puede consultar que jugadores pertenecen a un genero o categoria.

## Ejecucion
```
javac -encoding UTF-8 -d bin (Get-ChildItem src -Recurse -Filter *.java).FullName
java -cp bin Main
```
Datos de prueba: 21 nodos en 5 niveles, 13 jugadores y 19 conexiones.
