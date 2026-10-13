package org.example;
record Registro(String parcelaId, Tipo tipo, double valor, String detalle) {
    @Override
    public String toString() {
        String unidad = switch (tipo) {
            case PLAGA -> "% infestación";
            case AGROQUIMICO -> "litros";
            case AGUA -> "mg/L contaminantes";
            case SUELO -> "% humedad";
        };
        return tipo + " | " + valor + " " + unidad + " | " + detalle;
    }
}