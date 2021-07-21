package in.pratanumandal.fractalstudio.gui;

import in.pratanumandal.fractalstudio.common.Configuration;
import in.pratanumandal.fractalstudio.common.Constants;
import in.pratanumandal.fractalstudio.common.Utils;
import in.pratanumandal.fractalstudio.core.Fractal;
import in.pratanumandal.fractalstudio.core.FractalUtils;
import in.pratanumandal.fractalstudio.core.Point;
import in.pratanumandal.fractalstudio.mandelbrot.Mandelbrot;
import in.pratanumandal.fractalstudio.newton.NewtonRaphson;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
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

    @FXML private Button update;

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

    private Fractal fractal;

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
    private void mandelbrot() {
        fractal = new Mandelbrot(canvas);
        this.updateFractal();
    }

    @FXML
    private void newtonRaphson() {
        fractal = new NewtonRaphson(canvas);
        this.updateFractal();
    }

    @FXML
    private void julia() {

    }

    @FXML
    private void updateFractal() {
        update.setDisable(false);

        canvas.setWidth(Configuration.getCanvasSize());
        canvas.setHeight(Configuration.getCanvasSize());

        currentXCenter = Double.valueOf(centerX.getText());
        currentYCenter = Double.valueOf(centerY.getText());

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
    private void resetCenter() {
        centerX.setText("0");
        centerY.setText("0");
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
                    alert.setTitle(Constants.APPLICATION_NAME);
                    alert.setHeaderText("Save");
                    alert.setContentText("Fractal has been saved to file: " + file.getAbsolutePath());
                    alert.initOwner(canvas.getScene().getWindow());
                    alert.showAndWait();
                });
            } catch (IOException e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(Constants.APPLICATION_NAME);
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
        alert.setTitle(Constants.APPLICATION_NAME);
        alert.setHeaderText("Settings");

        ButtonType apply = new ButtonType("Apply", ButtonBar.ButtonData.APPLY);
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(cancel, apply);

        Utils.setDefaultButton(alert, cancel);

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
    private void about() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(Constants.APPLICATION_NAME);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        closeButton.setVisible(false);
        closeButton.setManaged(false);

        VBox vBox = new VBox();
        vBox.getStyleClass().add("about");
        vBox.setAlignment(Pos.CENTER);

        ImageView imageView1 = new ImageView();
        Image image1 = new Image(getClass().getClassLoader().getResourceAsStream("img/icon.png"));
        imageView1.setImage(image1);
        imageView1.setFitWidth(96.0);
        imageView1.setFitHeight(96.0);
        imageView1.getStyleClass().add("icon");
        vBox.getChildren().add(imageView1);

        Label label1 = new Label(Constants.APPLICATION_NAME + " " + Constants.APPLICATION_VERSION);
        label1.getStyleClass().add("title");
        vBox.getChildren().add(label1);

        Hyperlink hyperlink1 = new Hyperlink("https://github.com/prat-man/Fractal-Studio");
        hyperlink1.getStyleClass().add("hyperlink");
        hyperlink1.setOnAction(event -> {
            Utils.browseURL("https://github.com/prat-man/Fractal-Studio");
        });
        vBox.getChildren().add(hyperlink1);

        Label label2 = new Label("from");
        label2.getStyleClass().add("subheading1");
        vBox.getChildren().add(label2);

        Label label3 = new Label("Pratanu Mandal");
        label3.getStyleClass().add("subheading2");
        vBox.getChildren().add(label3);

        Hyperlink hyperlink2 = new Hyperlink("https://pratanumandal.in/");
        hyperlink2.getStyleClass().add("hyperlink");
        hyperlink2.setOnAction(event -> {
            Utils.browseURL("https://pratanumandal.in/");
        });
        vBox.getChildren().add(hyperlink2);

        Label label4 = new Label("with");
        label4.getStyleClass().add("subheading3");
        vBox.getChildren().add(label4);

        ImageView imageView2 = new ImageView();
        Image image2 = new Image(getClass().getClassLoader().getResourceAsStream("img/heart.png"));
        imageView2.setImage(image2);
        imageView2.setFitWidth(48.0);
        imageView2.setFitHeight(48.0);
        vBox.getChildren().add(imageView2);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        Hyperlink hyperlink3 = new Hyperlink("Licensed under GPL v3.0");
        hyperlink3.getStyleClass().add("subheading4");
        hyperlink3.setOnAction(event -> {
            Utils.browseURL("https://github.com/prat-man/Fractal-Studio/blob/master/LICENSE");
        });
        vBox.getChildren().add(hyperlink3);

        AnchorPane pane = new AnchorPane();
        pane.getChildren().add(vBox);

        AnchorPane.setTopAnchor(vBox, 0.0);
        AnchorPane.setRightAnchor(vBox, 0.0);
        AnchorPane.setBottomAnchor(vBox, 0.0);
        AnchorPane.setLeftAnchor(vBox, 0.0);

        dialog.getDialogPane().contentProperty().set(pane);

        dialog.initOwner(canvas.getScene().getWindow());
        dialog.showAndWait();
    }

    @FXML
    private void exit() {
        Window window = canvas.getScene().getWindow();
        window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void progressDialog(Fractal fractal, Thread thread) {
        AtomicReference<Alert> alertReference = new AtomicReference<>(null);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alertReference.set(alert);

            alert.setTitle(Constants.APPLICATION_NAME);
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
