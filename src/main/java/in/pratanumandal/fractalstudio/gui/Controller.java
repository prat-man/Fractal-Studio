package in.pratanumandal.fractalstudio.gui;

import in.pratanumandal.fractalstudio.common.Configuration;
import in.pratanumandal.fractalstudio.common.Constants;
import in.pratanumandal.fractalstudio.core.Fractal;
import in.pratanumandal.fractalstudio.core.FractalUtils;
import in.pratanumandal.fractalstudio.core.Point;
import in.pratanumandal.fractalstudio.mandelbrot.Mandelbrot;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.ToggleSwitch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Controller {

    @FXML private Canvas canvas;
    @FXML private StackPane canvasHolder;
    @FXML private ScrollPane canvasScroll;

    @FXML private ToggleSwitch smooth;
    @FXML private ToggleSwitch inverted;
    @FXML private ToggleSwitch monochrome;
    @FXML private ToggleSwitch showOrigin;
    @FXML private ToggleSwitch showCenter;

    @FXML private TextField centerX;
    @FXML private TextField centerY;
    @FXML private ToggleButton pickCenter;

    @FXML private Spinner<Double> scale;
    @FXML private Spinner<Double> zoom;

    private double currentXCenter;
    private double currentYCenter;

    @FXML
    private void initialize() {
        canvasHolder.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                        canvasScroll.getViewportBounds().getWidth(),
                canvasScroll.viewportBoundsProperty()));

        canvasHolder.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                        canvasScroll.getViewportBounds().getHeight(),
                canvasScroll.viewportBoundsProperty()));

        inverted.selectedProperty().addListener((obs, oldVal, newVal) -> {
            this.monochrome.setDisable(!newVal);
        });

        centerX.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[+-]?\\d*(\\.\\d*)?")) {
                centerX.setText(oldVal);
            }
        });

        centerY.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[+-]?\\d*(\\.\\d*)?")) {
                centerY.setText(oldVal);
            }
        });

        pickCenter.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) canvas.setCursor(Cursor.HAND);
            else canvas.setCursor(Cursor.DEFAULT);
        });

        canvas.setOnMouseClicked(event -> {
            if (pickCenter.isSelected()) {
                double factor = (scale.getValue() == null ? 2.0 : scale.getValue()) / (zoom.getValue() == null ? 1.0 : zoom.getValue());
                double xCenter = FractalUtils.precision(((event.getX() / canvas.getWidth()) - 0.5) * 2.0 * factor + currentXCenter, 11);
                double yCenter = FractalUtils.precision((0.5 - (event.getY() / canvas.getHeight())) * 2.0 * factor + currentYCenter, 11);
                centerX.setText(String.valueOf(xCenter));
                centerY.setText(String.valueOf(yCenter));
                pickCenter.setSelected(false);
            }
        });

        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });
    }

    @FXML
    private void updateFractal() {
        canvas.setWidth(Configuration.getCanvasSize());
        canvas.setHeight(Configuration.getCanvasSize());

        currentXCenter = Double.valueOf(centerX.getText());
        currentYCenter = Double.valueOf(centerY.getText());

        Fractal fractal = new Mandelbrot(canvas);
        fractal.setScale((scale.getValue() == null ? 2.0 : scale.getValue()) / (zoom.getValue() == null ? 1.0 : zoom.getValue()));
        fractal.setSmooth(smooth.isSelected());
        fractal.setInverted(inverted.isSelected());
        fractal.setMonochrome(monochrome.isSelected());
        fractal.setShowOrigin(showOrigin.isSelected());
        fractal.setShowCenter(showCenter.isSelected());
        fractal.setCenter(new Point(currentXCenter, currentYCenter));

        Thread thread = new Thread(fractal);
        this.progressDialog(fractal, thread);
        thread.start();
    }

    @FXML
    private void export() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"));

        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());

        if (file != null) {
            int width = (int) canvas.getWidth();
            int height = (int) canvas.getHeight();

            WritableImage image = new WritableImage(width, height);
            image = canvas.snapshot(null, image);

            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bufferedImage = SwingFXUtils.fromFXImage(image, bufferedImage);

            try {
                ImageIO.write(bufferedImage, FilenameUtils.getExtension(file.getName()), file);

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(Constants.APPLICATION_TITLE);
                    alert.setHeaderText("Save");
                    alert.setContentText("Fractal has been saved to file: " + file.getAbsolutePath());
                    alert.initOwner(canvas.getScene().getWindow());
                    alert.showAndWait();
                });
            } catch (IOException e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(Constants.APPLICATION_TITLE);
                    alert.setHeaderText("Error");
                    alert.setContentText("Failed to save fractal to file: " + file.getAbsolutePath());
                    alert.initOwner(canvas.getScene().getWindow());
                    alert.showAndWait();
                });
            }
        }
    }

    @FXML
    private void settingsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Constants.APPLICATION_TITLE);
        alert.setHeaderText("Settings");

        ButtonType apply = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(cancel, apply);

        Button applyButton = (Button) alert.getDialogPane().lookupButton(apply);
        applyButton.setDefaultButton(true);

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/settings.fxml"));

        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        SettingsController controller = loader.getController();

        alert.getDialogPane().contentProperty().set(root);
        alert.initOwner(canvas.getScene().getWindow());

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == apply) {
            Configuration.setCanvasSize(controller.getCanvasSize());
            Configuration.setThreadCount(controller.getThreadCount());
        }
    }

    @FXML
    private void resetCenter() {
        centerX.setText("0");
        centerY.setText("0");
    }

    private void progressDialog(Fractal fractal, Thread thread) {
        AtomicReference<Alert> alertReference = new AtomicReference<>(null);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alertReference.set(alert);

            alert.setTitle(Constants.APPLICATION_TITLE);
            alert.setHeaderText("Generating fractal");

            ButtonType abort = new ButtonType("Abort", ButtonBar.ButtonData.OTHER);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(abort);

            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/progress.fxml"));

            Parent root;
            try {
                root = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            ProgressController controller = loader.getController();

            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            ScheduledFuture future = service.scheduleAtFixedRate(
                    () -> controller.setProgress(fractal.getProgress()),
                    0, 100, TimeUnit.MILLISECONDS);

            alert.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> event.consume());

            alert.getDialogPane().contentProperty().set(root);
            alert.initOwner(canvas.getScene().getWindow());

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == abort) {
                fractal.interrupt();
            }

            future.cancel(true);
            service.shutdown();
        });

        Thread fractalThread = new Thread(() -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (alertReference.get() != null) {
                Platform.runLater(() -> alertReference.get().close());
            }
        });

        fractalThread.start();
    }

}
