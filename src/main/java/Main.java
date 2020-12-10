import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ui_controllers.MainController;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_window.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Hello World");
        Scene scene = new Scene(root, 1200, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        // получу доступ к контроллеру
        final MainController controller = loader.getController();
        controller.init(primaryStage);
        controller.setStage(primaryStage);
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/applicationIcon.png")));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
