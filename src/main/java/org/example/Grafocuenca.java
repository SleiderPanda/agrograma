package org.example;
import java.util.*;

class GrafoCuenca {
    // Lista de adyacencia: origen -> parcelas/río hacia donde fluye el agua
    private final Map<String, List<String>> adyacencia = new LinkedHashMap<>();

    void agregarCanal(String origen, String destino) {
        adyacencia.computeIfAbsent(origen, k -> new ArrayList<>()).add(destino);
        adyacencia.putIfAbsent(destino, new ArrayList<>());
    }

    Map<String, List<String>> getAdyacencia() {
        return adyacencia;
    }

    // RECURSIVA (DFS): encuentra todo lo que queda "aguas abajo" de un punto
    void aguasAbajo(String nodo, Set<String> visitados) {
        if (!visitados.add(nodo)) {
            return; // ya lo visité, evita ciclos infinitos
        }
        for (String vecino : adyacencia.getOrDefault(nodo, List.of())) {
            aguasAbajo(vecino, visitados);
        }
    }

    // RECURSIVA: ¿existe un camino de A a B? guarda la ruta
    boolean hayCamino(String actual, String destino, Set<String> visitados, List<String> ruta) {
        ruta.add(actual);
        if (actual.equals(destino)) {
            return true;
        }
        visitados.add(actual);
        for (String vecino : adyacencia.getOrDefault(actual, List.of())) {
            if (!visitados.contains(vecino) && hayCamino(vecino, destino, visitados, ruta)) {
                return true;
            }
        }
        ruta.remove(ruta.size() - 1); // backtracking
        return false;
    }
}