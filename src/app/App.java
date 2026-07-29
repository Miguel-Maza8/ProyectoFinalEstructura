package app;

import javax.swing.SwingUtilities;

import controllers.MapController;
import persistence.FileGraphRepository;
import persistence.GraphRepository;
import views.MainFrame;


public class App {
    public static void main(String[] args) {
        GraphRepository repository = new FileGraphRepository("resources/config/graph_config.csv");
        MapController controller = new MapController(repository);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
