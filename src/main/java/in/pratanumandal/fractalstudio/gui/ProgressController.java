package in.pratanumandal.fractalstudio.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ProgressController {

    @FXML private ProgressBar progress;

    @FXML private VBox progressContainer;

    @FXML
    private void initialize() {
        progress.prefWidthProperty().bind(progressContainer.widthProperty());
    }

    public void setProgress(double progress) {
        Platform.runLater(() -> this.progress.setProgress(progress));
    }

}
