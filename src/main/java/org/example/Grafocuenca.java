package org.example;
import java.util.*;

class GrafoCuenca {

    private final Map<String, List<String>> adyacencia = new LinkedHashMap<>();

    void agregarCanal(String origen, String destino) {
        adyacencia.computeIfAbsent(origen, k -> new ArrayList<>()).add(destino);
        adyacencia.putIfAbsent(destino, new ArrayList<>());
    }

    Map<String, List<String>> getAdyacencia() {
        return adyacencia;
    }


    void aguasAbajo(String nodo, Set<String> visitados) {
        if (!visitados.add(nodo)) {
            return;
        }
        for (String vecino : adyacencia.getOrDefault(nodo, List.of())) {
            aguasAbajo(vecino, visitados);
        }
    }


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