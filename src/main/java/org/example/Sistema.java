package org.example;
import java.io.*;
import java.nio.file.*;
import java.util.*;

class Sistema {
    private static final String ARCHIVO = "datos.csv";

    // Umbrales para generar alertas
    private static final double UMBRAL_PLAGA = 20.0;       // % infestación
    private static final double UMBRAL_AGROQUIMICO = 5.0;  // litros por aplicación
    private static final double UMBRAL_AGUA = 0.5;         // mg/L
    private static final double UMBRAL_SUELO_MIN = 20.0;   // % humedad mínima

    private final List<Parcela> parcelas = new ArrayList<>();
    private final Queue<String> alertas = new ArrayDeque<>();              // COLA
    private final GrafoCuenca cuenca = new GrafoCuenca();                  // GRAFO
    private final Scanner sc = new Scanner(System.in);

    void iniciar() {
        if (Files.exists(Paths.get(ARCHIVO))) {
            cargar();
            System.out.println("Datos cargados desde " + ARCHIVO);
        } else {
            System.out.println("No hay datos guardados. Empiezas desde cero.");
        }

        int op;
        do {
            menu();
            op = leerEntero("Opción: ");
            switch (op) {
                case 1 -> registrarParcela();
                case 2 -> registrarMedicion();
                case 3 -> atenderAlerta();
                case 4 -> resumen();
                case 5 -> simularContaminacion();
                case 6 -> verCuenca();
                case 7 -> { guardar(); System.out.println("Guardado en " + ARCHIVO); }
                case 0 -> { guardar(); System.out.println("Datos guardados. ¡Hasta luego!"); }
                default -> System.out.println("Opción inválida.");
            }
        } while (op != 0);
    }

    private void menu() {
        System.out.println("\n===== MONITOREO VEREDAS DEL RÍO =====");
        System.out.println("1. Registrar parcela");
        System.out.println("2. Registrar medición");
        System.out.println("3. Atender siguiente alerta (cola)");
        System.out.println("4. Resumen de parcelas");
        System.out.println("5. Simular contaminación aguas abajo (grafo + recursividad)");
        System.out.println("6. Ver mapa de la cuenca (grafo)");
        System.out.println("7. Guardar");
        System.out.println("0. Salir");
        System.out.println("Alertas pendientes: " + alertas.size());
    }

    // ---------- 1. Parcelas ----------
    private void registrarParcela() {
        System.out.print("Id de la parcela: ");
        String id = sc.nextLine().trim().toUpperCase();
        if (id.isEmpty() || buscarParcela(id) != null) {
            System.out.println("Id vacío o ya existe.");
            return;
        }
        System.out.print("Cultivo: ");
        String cultivo = sc.nextLine().trim();
        parcelas.add(new Parcela(id, cultivo));

        System.out.print("¿El agua de esta parcela baja hacia otra? Escribe su id, RIO, o ENTER si no: ");
        String destino = sc.nextLine().trim().toUpperCase();
        if (!destino.isEmpty()) {
            cuenca.agregarCanal(id, destino);
        }
        System.out.println("Parcela registrada.");
    }

    // ---------- 2. Mediciones (COLA) ----------
    private void registrarMedicion() {
        System.out.print("Id de la parcela: ");
        Parcela p = buscarParcela(sc.nextLine().trim().toUpperCase());
        if (p == null) {
            System.out.println("Esa parcela no existe.");
            return;
        }
        System.out.println("Tipo: 1=Plaga  2=Agroquímico  3=Agua  4=Suelo");
        int t = leerEntero("Tipo: ");
        if (t < 1 || t > 4) {
            System.out.println("Tipo inválido.");
            return;
        }
        Tipo tipo = Tipo.values()[t - 1];
        double valor = leerDouble("Valor medido: ");
        System.out.print("Detalle: ");
        String detalle = sc.nextLine().trim().replace(";", ",");

        Registro r = new Registro(p.id, tipo, valor, detalle);
        p.registros.add(r);

        String alerta = evaluar(r);
        if (alerta != null) {
            alertas.offer(alerta); // COLA: se atienden en orden de llegada
            System.out.println("Se generó una alerta: " + alerta);
        } else {
            System.out.println("Registro guardado sin alertas.");
        }
    }

    private String evaluar(Registro r) {
        return switch (r.tipo()) {
            case PLAGA -> r.valor() >= UMBRAL_PLAGA
                    ? "Parcela " + r.parcelaId() + ": infestación alta de " + r.detalle() + " (" + r.valor() + "%). Revisar control temprano."
                    : null;
            case AGROQUIMICO -> r.valor() > UMBRAL_AGROQUIMICO
                    ? "Parcela " + r.parcelaId() + ": uso excesivo de " + r.detalle() + " (" + r.valor() + " L). Riesgo para el río."
                    : null;
            case AGUA -> r.valor() > UMBRAL_AGUA
                    ? "Parcela " + r.parcelaId() + ": agua con contaminantes (" + r.valor() + " mg/L)."
                    : null;
            case SUELO -> r.valor() < UMBRAL_SUELO_MIN
                    ? "Parcela " + r.parcelaId() + ": suelo muy seco (" + r.valor() + "% de humedad)."
                    : null;
        };
    }

    // ---------- 3. Atender alerta (COLA) ----------
    private void atenderAlerta() {
        String a = alertas.poll();
        if (a == null) {
            System.out.println("No hay alertas pendientes.");
        } else {
            System.out.println("Atendiendo alerta: " + a);
            System.out.println("Quedan " + alertas.size() + " pendientes.");
        }
    }

    // ---------- 4. Resumen ----------
    private void resumen() {
        if (parcelas.isEmpty()) {
            System.out.println("No hay parcelas.");
            return;
        }
        for (Parcela p : parcelas) {
            System.out.println("\n[" + p.id + "] " + p.cultivo + " (" + p.registros.size() + " registros)");
            for (Registro r : p.registros) {
                System.out.println("   - " + r);
            }
            double sumaPlaga = 0;
            int nPlaga = 0;
            for (Registro r : p.registros) {
                if (r.tipo() == Tipo.PLAGA) {
                    sumaPlaga += r.valor();
                    nPlaga++;
                }
            }
            if (nPlaga > 0) {
                System.out.printf("   Infestación promedio: %.1f%%%n", sumaPlaga / nPlaga);
            }
        }
    }

    // ---------- 5. Contaminación aguas abajo (GRAFO + RECURSIVIDAD) ----------
    private void simularContaminacion() {
        System.out.print("¿Desde qué parcela se contamina? ");
        String origen = sc.nextLine().trim().toUpperCase();
        if (!cuenca.getAdyacencia().containsKey(origen)) {
            System.out.println("Esa parcela no está en el mapa de la cuenca.");
            return;
        }
        Set<String> afectadas = new LinkedHashSet<>();
        cuenca.aguasAbajo(origen, afectadas);
        afectadas.remove(origen);

        if (afectadas.isEmpty()) {
            System.out.println("Nada queda aguas abajo de " + origen + ".");
        } else {
            System.out.println("Quedarían expuestas: " + afectadas);
        }

        List<String> ruta = new ArrayList<>();
        if (cuenca.hayCamino(origen, "RIO", new HashSet<>(), ruta)) {
            System.out.println("Ruta hasta el río: " + String.join(" -> ", ruta));
        } else {
            System.out.println("Desde " + origen + " no hay camino hasta el río.");
        }
    }

    private void verCuenca() {
        System.out.println("\nMapa de la cuenca (origen -> destinos):");
        cuenca.getAdyacencia().forEach((k, v) -> System.out.println("  " + k + " -> " + v));
    }

    // ---------- Persistencia offline ----------
    private void guardar() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) {
            for (Parcela p : parcelas) {
                pw.println("P;" + p.id + ";" + p.cultivo);
            }
            for (Parcela p : parcelas) {
                for (Registro r : p.registros) {
                    pw.println("R;" + r.parcelaId() + ";" + r.tipo() + ";" + r.valor() + ";" + r.detalle());
                }
            }
            cuenca.getAdyacencia().forEach((o, destinos) -> {
                for (String d : destinos) pw.println("C;" + o + ";" + d);
            });
        } catch (IOException e) {
            System.out.println("No se pudo guardar: " + e.getMessage());
        }
    }

    private void cargar() {
        try (BufferedReader br = new BufferedReader(new FileReader(ARCHIVO))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split(";", -1);
                switch (c[0]) {
                    case "P" -> parcelas.add(new Parcela(c[1], c[2]));
                    case "R" -> {
                        Parcela p = buscarParcela(c[1]);
                        if (p != null) {
                            Registro r = new Registro(c[1], Tipo.valueOf(c[2]), Double.parseDouble(c[3]), c[4]);
                            p.registros.add(r);
                        }
                    }
                    case "C" -> cuenca.agregarCanal(c[1], c[2]);
                    default -> { }
                }
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Error leyendo el archivo: " + e.getMessage());
        }
    }

    // ---------- Buscar una parcela por su id ----------
    private Parcela buscarParcela(String id) {
        for (Parcela p : parcelas) {
            if (p.id.equals(id)) {
                return p;
            }
        }
        return null; // no existe
    }

    // ---------- Lectura segura ----------
    private int leerEntero(String msg) {
        while (true) {
            System.out.print(msg);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número entero.");
            }
        }
    }

    private double leerDouble(String msg) {
        while (true) {
            System.out.print(msg);
            try {
                return Double.parseDouble(sc.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número válido.");
            }
        }
    }
}