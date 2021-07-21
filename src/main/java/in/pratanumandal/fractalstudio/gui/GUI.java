package in.pratanumandal.fractalstudio.gui;

import in.pratanumandal.fractalstudio.common.Constants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class GUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/fractals.fxml"));

        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("img/icon.png")));

        primaryStage.setTitle(Constants.APPLICATION_TITLE);
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
