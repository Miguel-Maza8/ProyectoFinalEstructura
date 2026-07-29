package views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import controllers.MapController;
import models.MapPoint;
import structures.node.Node;


public class MapPanel extends JPanel {

    
    public enum EditMode {
        NONE,
        ADD_NODE,
        ADD_EDGE,
        REMOVE_NODE,
        REMOVE_EDGE
    }

    private static final int NODE_RADIUS = 14;
    private static final int HIT_RADIUS = 16;

    private final MapController controller;
    private final Image backgroundImage;

    private EditMode editMode = EditMode.NONE;
    private MapPointListener listener;

    private MapPoint origenSeleccionado;

    private String startId;
    private String endId;

   
    private final List<MapPoint> visitadosParaDibujar = new ArrayList<>();
    private final List<MapPoint> rutaParaDibujar = new ArrayList<>();

    private double scale = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;
    public interface MapPointListener {
        void onGraphChanged();
    }

    public MapPanel(MapController controller) {
        this.controller = controller;
        this.backgroundImage = new ImageIcon("resources/maps/map.png").getImage();
        setBackground(Color.DARK_GRAY);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                manejarClick(e.getX(), e.getY());
            }
        };
        addMouseListener(mouseAdapter);
    }

    public void setListener(MapPointListener listener) {
        this.listener = listener;
    }

    public void setEditMode(EditMode modo) {
        this.editMode = modo;
        this.origenSeleccionado = null;
        repaint();
    }

    public void setSeleccion(String startId, String endId) {
        this.startId = startId;
        this.endId = endId;
        repaint();
    }

    
    public void agregarVisitado(MapPoint punto) {
        visitadosParaDibujar.add(punto);
        repaint();
    }

    public void agregarPuntoDeRuta(MapPoint punto) {
        rutaParaDibujar.add(punto);
        repaint();
    }

    public void limpiarRecorrido() {
        visitadosParaDibujar.clear();
        rutaParaDibujar.clear();
        repaint();
    }

   

    private void manejarClick(int screenX, int screenY) {
        double mapX = (screenX - offsetX) / scale;
        double mapY = (screenY - offsetY) / scale;

        MapPoint puntoClicado = buscarNodoCercano(screenX, screenY);

        switch (editMode) {
            case ADD_NODE:
                manejarAgregarNodo(mapX, mapY, puntoClicado);
                break;
            case ADD_EDGE:
                manejarAgregarArista(puntoClicado);
                break;
            case REMOVE_NODE:
                manejarEliminarNodo(puntoClicado);
                break;
            case REMOVE_EDGE:
                manejarEliminarArista(puntoClicado);
                break;
            case NONE:
            default:
               
                break;
        }
    }

    private void manejarAgregarNodo(double mapX, double mapY, MapPoint puntoClicado) {
        if (puntoClicado != null) {
            return; 
        }
        String id = javax.swing.JOptionPane.showInputDialog(this,
                "Identificador del nuevo punto:", "Agregar punto", javax.swing.JOptionPane.QUESTION_MESSAGE);
        if (id == null) {
            return; 
        }
        boolean agregado = controller.addNode(id.trim(), (int) mapX, (int) mapY);
        if (!agregado) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No se pudo agregar el punto (identificador vacio o repetido).",
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        notificarCambio();
    }

    private void manejarAgregarArista(MapPoint puntoClicado) {
        if (puntoClicado == null) {
            return;
        }
        if (origenSeleccionado == null) {
            origenSeleccionado = puntoClicado;
            repaint();
            return;
        }
        if (origenSeleccionado.equals(puntoClicado)) {
            origenSeleccionado = null;
            repaint();
            return;
        }

        int opcion = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿La calle entre " + origenSeleccionado.getId() + " y " + puntoClicado.getId()
                        + " es bidireccional?",
                "Tipo de conexion", javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);

        if (opcion == javax.swing.JOptionPane.YES_OPTION) {
            controller.addEdge(origenSeleccionado.getId(), puntoClicado.getId(), true);
        } else if (opcion == javax.swing.JOptionPane.NO_OPTION) {
            controller.addEdge(origenSeleccionado.getId(), puntoClicado.getId(), false);
        }

        origenSeleccionado = null;
        notificarCambio();
    }

    private void manejarEliminarNodo(MapPoint puntoClicado) {
        if (puntoClicado == null) {
            return;
        }
        controller.removeNode(puntoClicado.getId());
        notificarCambio();
    }

    
    private void manejarEliminarArista(MapPoint puntoClicado) {
        if (puntoClicado == null) {
            return;
        }
        if (origenSeleccionado == null) {
            origenSeleccionado = puntoClicado;
            repaint();
            return;
        }
        if (origenSeleccionado.equals(puntoClicado)) {
            origenSeleccionado = null;
            repaint();
            return;
        }

        int opcion = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Eliminar la calle entre " + origenSeleccionado.getId() + " y " + puntoClicado.getId()
                        + " en ambos sentidos?",
                "Eliminar calle", javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);

        if (opcion == javax.swing.JOptionPane.YES_OPTION) {
            controller.removeEdge(origenSeleccionado.getId(), puntoClicado.getId(), true);
        } else if (opcion == javax.swing.JOptionPane.NO_OPTION) {
            controller.removeEdge(origenSeleccionado.getId(), puntoClicado.getId(), false);
        }

        origenSeleccionado = null;
        notificarCambio();
    }

    private void notificarCambio() {
        limpiarRecorrido();
        if (listener != null) {
            listener.onGraphChanged();
        }
        repaint();
    }

    private MapPoint buscarNodoCercano(int screenX, int screenY) {
        for (Node<MapPoint> node : controller.getGraph().getNodes()) {
            MapPoint punto = node.getValue();
            int px = (int) (offsetX + punto.getX() * scale);
            int py = (int) (offsetY + punto.getY() * scale);
            double distancia = Math.hypot(screenX - px, screenY - py);
            if (distancia <= HIT_RADIUS) {
                return punto;
            }
        }
        return null;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        actualizarTransformacion();
        dibujarFondo(g2);
        dibujarAristas(g2);
        dibujarExploracion(g2);
        dibujarRuta(g2);
        dibujarNodos(g2);
    }

    private void actualizarTransformacion() {
        int imgW = backgroundImage.getWidth(this);
        int imgH = backgroundImage.getHeight(this);
        if (imgW <= 0 || imgH <= 0) {
            return;
        }

        double escalaX = getWidth() / (double) imgW;
        double escalaY = getHeight() / (double) imgH;
        scale = Math.min(escalaX, escalaY);

        offsetX = (int) ((getWidth() - imgW * scale) / 2);
        offsetY = (int) ((getHeight() - imgH * scale) / 2);
    }

    private void dibujarFondo(Graphics2D g2) {
        int imgW = backgroundImage.getWidth(this);
        int imgH = backgroundImage.getHeight(this);
        if (imgW <= 0 || imgH <= 0) {
            return;
        }
        AffineTransform transform = new AffineTransform();
        transform.translate(offsetX, offsetY);
        transform.scale(scale, scale);
        g2.drawImage(backgroundImage, transform, this);
    }

    private void dibujarAristas(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(30, 30, 30));

        Set<String> paresDibujados = new LinkedHashSet<>();

        for (Map.Entry<Node<MapPoint>, Set<Node<MapPoint>>> entry : controller.getGraph().getGraph().entrySet()) {
            MapPoint origen = entry.getKey().getValue();
            for (Node<MapPoint> vecinoNode : entry.getValue()) {
                MapPoint destino = vecinoNode.getValue();

                boolean esBidireccional = controller.getGraph().getVecinos(destino)
                        .contains(new Node<>(origen));

                String clave = esBidireccional
                        ? claveOrdenada(origen.getId(), destino.getId())
                        : origen.getId() + "->" + destino.getId();

                if (paresDibujados.contains(clave)) {
                    continue;
                }
                paresDibujados.add(clave);

                int x1 = (int) (offsetX + origen.getX() * scale);
                int y1 = (int) (offsetY + origen.getY() * scale);
                int x2 = (int) (offsetX + destino.getX() * scale);
                int y2 = (int) (offsetY + destino.getY() * scale);

                g2.drawLine(x1, y1, x2, y2);

                if (!esBidireccional) {
                    dibujarFlecha(g2, x1, y1, x2, y2);
                }
            }
        }
    }

    private String claveOrdenada(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "-" + b : b + "-" + a;
    }

    private void dibujarFlecha(Graphics2D g2, int x1, int y1, int x2, int y2) {
        double angulo = Math.atan2(y2 - y1, x2 - x1);
        int largoFlecha = 10;

        int puntaX = (int) (x1 + (x2 - x1) * 0.6);
        int puntaY = (int) (y1 + (y2 - y1) * 0.6);

        int alaX1 = (int) (puntaX - largoFlecha * Math.cos(angulo - Math.PI / 6));
        int alaY1 = (int) (puntaY - largoFlecha * Math.sin(angulo - Math.PI / 6));
        int alaX2 = (int) (puntaX - largoFlecha * Math.cos(angulo + Math.PI / 6));
        int alaY2 = (int) (puntaY - largoFlecha * Math.sin(angulo + Math.PI / 6));

        g2.drawLine(puntaX, puntaY, alaX1, alaY1);
        g2.drawLine(puntaX, puntaY, alaX2, alaY2);
    }

    private void dibujarExploracion(Graphics2D g2) {
        g2.setColor(new Color(255, 165, 0, 160));
        for (MapPoint punto : visitadosParaDibujar) {
            int px = (int) (offsetX + punto.getX() * scale);
            int py = (int) (offsetY + punto.getY() * scale);
            g2.fillOval(px - NODE_RADIUS - 4, py - NODE_RADIUS - 4, (NODE_RADIUS + 4) * 2, (NODE_RADIUS + 4) * 2);
        }
    }

    private void dibujarRuta(Graphics2D g2) {
        if (rutaParaDibujar.size() < 1) {
            return;
        }
        g2.setColor(new Color(0, 150, 0));
        g2.setStroke(new BasicStroke(4f));

        for (int i = 0; i < rutaParaDibujar.size() - 1; i++) {
            MapPoint a = rutaParaDibujar.get(i);
            MapPoint b = rutaParaDibujar.get(i + 1);
            int x1 = (int) (offsetX + a.getX() * scale);
            int y1 = (int) (offsetY + a.getY() * scale);
            int x2 = (int) (offsetX + b.getX() * scale);
            int y2 = (int) (offsetY + b.getY() * scale);
            g2.drawLine(x1, y1, x2, y2);
        }

        for (MapPoint punto : rutaParaDibujar) {
            int px = (int) (offsetX + punto.getX() * scale);
            int py = (int) (offsetY + punto.getY() * scale);
            g2.fillOval(px - NODE_RADIUS / 2, py - NODE_RADIUS / 2, NODE_RADIUS, NODE_RADIUS);
        }
    }

    private void dibujarNodos(Graphics2D g2) {
        for (Node<MapPoint> node : controller.getGraph().getNodes()) {
            MapPoint punto = node.getValue();
            int px = (int) (offsetX + punto.getX() * scale);
            int py = (int) (offsetY + punto.getY() * scale);

            g2.setColor(colorParaNodo(punto));
            g2.fillOval(px - NODE_RADIUS, py - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            g2.setColor(Color.BLACK);
            g2.drawOval(px - NODE_RADIUS, py - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            g2.drawString(punto.getId(), px - NODE_RADIUS, py - NODE_RADIUS - 4);
        }
    }

    private Color colorParaNodo(MapPoint punto) {
        if (punto.equals(origenSeleccionado)) {
            return Color.YELLOW;
        }
        if (startId != null && punto.getId().equals(startId)) {
            return new Color(0, 120, 255);
        }
        if (endId != null && punto.getId().equals(endId)) {
            return Color.RED;
        }
        return Color.WHITE;
    }
}

