package in.pratanumandal.fractalstudio.gui;

import in.pratanumandal.fractalstudio.common.Constants;
import in.pratanumandal.fractalstudio.common.Utils;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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

        primaryStage.setOnCloseRequest((event) -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.YES, ButtonType.NO);
            alert.setTitle(Constants.APPLICATION_NAME);
            alert.setHeaderText("Exit");
            alert.setContentText("Are you sure you want to exit Fractal Studio?\n\n");

            Utils.setDefaultButton(alert, ButtonType.NO);

            alert.initOwner(primaryStage);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                System.exit(0);
            } else {
                event.consume();
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
