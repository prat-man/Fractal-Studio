package in.pratanumandal.fractalstudio.gui;

import in.pratanumandal.fractalstudio.common.Constants;
import in.pratanumandal.fractalstudio.common.Utils;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GUI extends Application {

    public static HostServices hostServices;

    @Override
    public void init() throws Exception {
        super.init();

        // do this before anything else
        Utils.setDockIconIfMac();

        hostServices = getHostServices();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/main.fxml"));

        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/icon.png")));

        primaryStage.setTitle(Constants.APPLICATION_NAME);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.sizeToScene();

        Font.loadFont(getClass().getClassLoader().getResourceAsStream("fonts/OpenSans-Regular.ttf"), 12);

        primaryStage.getScene().getStylesheets().add(getClass().getClassLoader().getResource("css/style.css").toExternalForm());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
