package views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

/**
 * Ventana principal de la aplicacion.
 *
 * Contiene los controles de interaccion (selectores de nodo inicio/destino,
 * algoritmo, modo de visualizacion y botones de edicion y ejecucion) y aloja
 * al MapPanel, que es el unico responsable de dibujar el mapa.
 *
 * Esta clase no implementa BFS ni DFS: solo pide al controlador que ejecute
 * la busqueda y le indica a MapPanel, paso a paso mediante un Timer, que
 * puntos debe ir resaltando segun el modo de visualizacion elegido.
 */
public class MainFrame extends JFrame {

    private static final int PASO_ANIMACION_MS = 350;

    private final MapController controller;
    private final MapPanel mapPanel;

    private final JComboBox<String> comboInicio = new JComboBox<>();
    private final JComboBox<String> comboDestino = new JComboBox<>();
    private final JComboBox<String> comboAlgoritmo = new JComboBox<>(new String[] { "BFS", "DFS" });
    private final JComboBox<String> comboModo = new JComboBox<>(new String[] { "Exploracion", "Ruta final" });

    private final JButton botonAgregarNodo = new JButton("Agregar punto");
    private final JButton botonAgregarArista = new JButton("Agregar calle");
    private final JButton botonEliminarNodo = new JButton("Eliminar punto");
    private final JButton botonEjecutar = new JButton("Ejecutar");
    private final JButton botonLimpiar = new JButton("Limpiar recorrido");

    private final JLabel etiquetaEstado = new JLabel("Seleccione un modo de edicion o ejecute una busqueda.");

    private Timer animacionActiva;

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

    /** Refresca los combos de inicio/destino con los ids actuales del grafo. */
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

        PathResult<MapPoint> resultado = controller.ejecutarBusqueda(algoritmo, inicio, destino);
        if (resultado == null) {
            JOptionPane.showMessageDialog(this, "No se pudo ejecutar la busqueda.");
            return;
        }

        if (modoExploracion) {
            animarExploracion(resultado);
        } else {
            animarSoloRuta(resultado);
        }
    }

    /**
     * Modo EXPLORATION: se revela paso a paso cada nodo visitado por el
     * algoritmo y, al terminar, se resalta la ruta final encontrada.
     */
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

    /**
     * Modo FINAL_PATH: no se dibuja la exploracion intermedia, unicamente
     * la ruta final, de forma progresiva desde el inicio hasta el destino.
     */
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

    private void mostrarResultadoFinal(PathResult<MapPoint> resultado) {
        if (resultado.encontroRuta()) {
            etiquetaEstado.setText("Ruta encontrada: " + resultado.getVisitados().size()
                    + " nodos visitados, " + resultado.getPath().size() + " nodos en la ruta.");
        } else {
            etiquetaEstado.setText("No existe una ruta entre los puntos seleccionados ("
                    + resultado.getVisitados().size() + " nodos visitados).");
        }
    }

    private void detenerAnimacionSiActiva() {
        if (animacionActiva != null && animacionActiva.isRunning()) {
            animacionActiva.stop();
        }
    }
}
