package in.pratanumandal.fractalstudio.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

public class ProgressController {

    @FXML private ProgressBar progress;

    @FXML private VBox progressContainer;

    @FXML private Label message;

    @FXML
    private void initialize() {
        progress.prefWidthProperty().bind(progressContainer.widthProperty());
    }

    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void setProgress(double progress) {
        Platform.runLater(() -> {
            if (progress != this.progress.getProgress()) {
                if (progress == ProgressBar.INDETERMINATE_PROGRESS) {
                    this.progress.setVisible(false);
                    this.progress.setProgress(progress);
                    this.progress.setVisible(true);
                } else {
                    this.progress.setProgress(progress);
                }
            }
        });
    }

}
