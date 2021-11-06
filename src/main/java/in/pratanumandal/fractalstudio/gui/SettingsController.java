package in.pratanumandal.fractalstudio.gui;

import in.pratanumandal.fractalstudio.common.Configuration;
import in.pratanumandal.fractalstudio.common.Mode;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;

public class SettingsController {

    @FXML private Spinner canvasSize;
    @FXML private Spinner threadCount;
    @FXML private ChoiceBox<String> mode;

    @FXML
    private void initialize() {
        canvasSize.getValueFactory().setValue(Configuration.getCanvasSize());
        threadCount.getValueFactory().setValue(Configuration.getThreadCount());

        switch (Configuration.getMode()) {
            default:
            case PERFORMANCE:
                mode.setValue("Performance");
                return;

            case BALANCED:
                mode.setValue("Balanced");
                return;

            case POWER_SAVING:
                mode.setValue("Power Saving");
                return;
        }
    }

    public int getCanvasSize() {
        return (int) canvasSize.getValue();
    }

    public int getThreadCount() {
        return (int) threadCount.getValue();
    }

    public Mode getMode() {
        switch (mode.getValue()) {
            default:
            case "Performance": return Mode.PERFORMANCE;
            case "Balanced": return Mode.BALANCED;
            case "Power Saving": return Mode.POWER_SAVING;
        }
    }

}
