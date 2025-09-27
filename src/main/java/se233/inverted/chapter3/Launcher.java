package se233.inverted.chapter3;
// Imports are omitted
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.FileInputStream;
import javafx.scene.Parent;

public class Launcher extends Application {
    public static Stage primaryStage;
    public static HostServices hs;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        hs = getHostServices();
        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/se233.inverted.chapter3/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Indexer");
        stage.setScene(scene);
        stage.show();
    }

    static void main(String[] args) {
        launch(args);
    }
}