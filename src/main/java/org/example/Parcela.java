package org.example;
import java.util.*;

class Parcela {
    final String id;
    final String cultivo;
    final List<Registro> registros = new ArrayList<>();

    Parcela(String id, String cultivo) {
        this.id = id;
        this.cultivo = cultivo;
    }
}