package in.pratanumandal.fractalstudio.gui;

import in.pratanumandal.fractalstudio.core.Configuration;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;

public class SettingsController {

    @FXML private Spinner canvasSize;
    @FXML private Spinner threadCount;

    @FXML
    private void initialize() {
        canvasSize.getValueFactory().setValue(Configuration.getCanvasSize());
        threadCount.getValueFactory().setValue(Configuration.getThreadCount());
    }

    public int getCanvasSize() {
        return (int) canvasSize.getValue();
    }

    public int getThreadCount() {
        return (int) threadCount.getValue();
    }

}
