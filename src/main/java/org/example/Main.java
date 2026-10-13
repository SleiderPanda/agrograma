package org.example;
/**
 * Monitoreo ambiental y productivo en el territorio rural (Veredas del Río)
 * Herramienta offline: todo se guarda en un archivo local (datos.csv).
 *
 * Estructuras de datos usadas y para qué:
 *  - MAP   (LinkedHashMap)  -> parcelas por id
 *  - COLA  (ArrayDeque)     -> alertas pendientes (la primera en llegar se atiende primero)
 *  - PILA  (ArrayDeque)     -> historial de registros para "deshacer" el último
 *  - GRAFO (GrafoCuenca)    -> cómo corre el agua entre parcelas hasta el río
 *  - RECURSIVIDAD           -> recorrer el grafo (DFS) y buscar rutas
 */
public class Main {
    public static void main(String[] args) {
        new Sistema().iniciar();
    }
}