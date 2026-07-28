package views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import controllers.MapController;
import models.MapPoint;
import structures.graphs.PathResult;


public class MainFrame extends JFrame {

    private static final int PASO_ANIMACION_MS = 350;

    private final MapController controller;
    private final MapPanel mapPanel;

    private final JComboBox<String> comboInicio = new JComboBox<>();
    private final JComboBox<String> comboDestino = new JComboBox<>();
    private final JComboBox<String> comboAlgoritmo = new JComboBox<>(
            new String[] { "BFS", "DFS", "Voraz (Greedy)", "A*" });
    private final JComboBox<String> comboModo = new JComboBox<>(new String[] { "Exploracion", "Ruta final" });

    private final JButton botonAgregarNodo = new JButton("Agregar punto");
    private final JButton botonAgregarArista = new JButton("Agregar calle");
    private final JButton botonEliminarNodo = new JButton("Eliminar punto");
    private final JButton botonEliminarArista = new JButton("Eliminar calle");
    private final JButton botonEjecutar = new JButton("Ejecutar");
    private final JButton botonLimpiar = new JButton("Limpiar recorrido");

    private final JLabel etiquetaEstado = new JLabel("Seleccione un modo de edicion o ejecute una busqueda.");

    private Timer animacionActiva;
    private String ultimoAlgoritmoEjecutado;
    private double ultimoTiempoEjecucionMs;


    public MainFrame(MapController controller) {
        super("Rutas en el mapa - BFS y DFS");
        this.controller = controller;
        this.mapPanel = new MapPanel(controller);

        configurarVentana();
        construirBarraSuperior();
        actualizarCombosDeNodos();

        mapPanel.setListener(this::actualizarCombosDeNodos);
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(mapPanel, BorderLayout.CENTER);
    }

    private void construirBarraSuperior() {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));

        barra.add(new JLabel("Inicio:"));
        barra.add(comboInicio);
        barra.add(new JLabel("Destino:"));
        barra.add(comboDestino);
        barra.add(new JLabel("Algoritmo:"));
        barra.add(comboAlgoritmo);
        barra.add(new JLabel("Modo:"));
        barra.add(comboModo);

        barra.add(botonEjecutar);
        barra.add(botonLimpiar);
        barra.add(botonAgregarNodo);
        barra.add(botonAgregarArista);
        barra.add(botonEliminarNodo);
        barra.add(botonEliminarArista);

        add(barra, BorderLayout.NORTH);
        add(etiquetaEstado, BorderLayout.SOUTH);

        comboInicio.addActionListener(e -> actualizarSeleccionEnMapa());
        comboDestino.addActionListener(e -> actualizarSeleccionEnMapa());

        botonAgregarNodo.addActionListener(e -> activarModo(MapPanel.EditMode.ADD_NODE,
                "Click en el mapa para agregar un punto."));
        botonAgregarArista.addActionListener(e -> activarModo(MapPanel.EditMode.ADD_EDGE,
                "Click en dos puntos para crear una calle entre ellos."));
        botonEliminarNodo.addActionListener(e -> activarModo(MapPanel.EditMode.REMOVE_NODE,
                "Click en un punto para eliminarlo."));
        botonEliminarArista.addActionListener(e -> activarModo(MapPanel.EditMode.REMOVE_EDGE,
                "Click en dos puntos para eliminar la calle entre ellos."));

        botonEjecutar.addActionListener(e -> ejecutarBusqueda());
        botonLimpiar.addActionListener(e -> {
            detenerAnimacionSiActiva();
            mapPanel.limpiarRecorrido();
            etiquetaEstado.setText("Recorrido limpiado.");
        });
    }

    private void activarModo(MapPanel.EditMode modo, String mensaje) {
        mapPanel.setEditMode(modo);
        etiquetaEstado.setText(mensaje);
    }

    private void actualizarSeleccionEnMapa() {
        String inicio = (String) comboInicio.getSelectedItem();
        String destino = (String) comboDestino.getSelectedItem();
        mapPanel.setSeleccion(inicio, destino);
    }

   
    private void actualizarCombosDeNodos() {
        String inicioPrevio = (String) comboInicio.getSelectedItem();
        String destinoPrevio = (String) comboDestino.getSelectedItem();

        comboInicio.removeAllItems();
        comboDestino.removeAllItems();

        for (String id : controller.getNodeIds()) {
            comboInicio.addItem(id);
            comboDestino.addItem(id);
        }

        if (inicioPrevio != null) {
            comboInicio.setSelectedItem(inicioPrevio);
        }
        if (destinoPrevio != null) {
            comboDestino.setSelectedItem(destinoPrevio);
        }

        actualizarSeleccionEnMapa();
    }

private void ejecutarBusqueda() {
    String inicio = (String) comboInicio.getSelectedItem();
    String destino = (String) comboDestino.getSelectedItem();
    String algoritmo = (String) comboAlgoritmo.getSelectedItem();
    boolean modoExploracion = "Exploracion".equals(comboModo.getSelectedItem());

    if (inicio == null || destino == null) {
        JOptionPane.showMessageDialog(this, "Debe existir al menos un punto de inicio y uno de destino.");
        return;
    }

    detenerAnimacionSiActiva();
    mapPanel.limpiarRecorrido();

    
    long tiempoInicioNs = System.nanoTime();
    PathResult<MapPoint> resultado = controller.ejecutarBusqueda(algoritmo, inicio, destino);
    long tiempoFinNs = System.nanoTime();
    

    if (resultado == null) {
        JOptionPane.showMessageDialog(this, "No se pudo ejecutar la busqueda.");
        return;
    }

    ultimoAlgoritmoEjecutado = algoritmo;
    ultimoTiempoEjecucionMs = (tiempoFinNs - tiempoInicioNs) / 1_000_000.0; // ns -> ms

    if (modoExploracion) {
        animarExploracion(resultado);
    } else {
        animarSoloRuta(resultado);
    }
}

    private void animarExploracion(PathResult<MapPoint> resultado) {
        Iterator<MapPoint> visitados = resultado.getVisitados().iterator();

        animacionActiva = new Timer(PASO_ANIMACION_MS, null);
        animacionActiva.addActionListener(e -> {
            if (visitados.hasNext()) {
                mapPanel.agregarVisitado(visitados.next());
            } else {
                animacionActiva.stop();
                mostrarResultadoFinal(resultado);
                animarRuta(new ArrayList<>(resultado.getPath()));
            }
        });
        animacionActiva.start();
    }

 
    private void animarSoloRuta(PathResult<MapPoint> resultado) {
        mostrarResultadoFinal(resultado);
        animarRuta(new ArrayList<>(resultado.getPath()));
    }

    private void animarRuta(List<MapPoint> ruta) {
        Iterator<MapPoint> iterador = ruta.iterator();
        animacionActiva = new Timer(PASO_ANIMACION_MS, null);
        animacionActiva.addActionListener(e -> {
            if (iterador.hasNext()) {
                mapPanel.agregarPuntoDeRuta(iterador.next());
            } else {
                animacionActiva.stop();
            }
        });
        animacionActiva.start();
    }
        private String formatearIds(Set<MapPoint> puntos) {
        StringBuilder sb = new StringBuilder("[");
        boolean primero = true;
        for (MapPoint p : puntos) {
            if (!primero) {
                sb.append(", ");
            }
            sb.append(p.getId());
            primero = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private void mostrarResultadoFinal(PathResult<MapPoint> resultado) {
            String tiempoTexto = String.format("%.3f ms", ultimoTiempoEjecucionMs);
            String visitadosTexto= formatearIds(resultado.getVisitados());
        if (resultado.encontroRuta()) {
            String rutaTexto = formatearIds(resultado.getPath());
            etiquetaEstado.setText("Algoritmo: " + ultimoAlgoritmoEjecutado
                    + " | Tiempo: " + tiempoTexto
                    + " | Visitados (" + resultado.getVisitados().size() + "): " + visitadosTexto
                    + " | Ruta (" + resultado.getPath().size() + "): " + rutaTexto);
        } else {
            etiquetaEstado.setText("Algoritmo: " + ultimoAlgoritmoEjecutado
                    + " | Tiempo: " + tiempoTexto
                    + " | Visitados (" + resultado.getVisitados().size() + "): " + visitadosTexto
                    + " | No existe ruta entre los puntos seleccionados.");
        }
    }

    private void detenerAnimacionSiActiva() {
        if (animacionActiva != null && animacionActiva.isRunning()) {
            animacionActiva.stop();
        }
    }
}
