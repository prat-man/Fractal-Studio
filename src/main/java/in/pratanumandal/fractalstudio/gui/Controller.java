package in.pratanumandal.fractalstudio.gui;

import in.pratanumandal.expr4j.Expression;
import in.pratanumandal.expr4j.exception.Expr4jException;
import in.pratanumandal.fractalstudio.common.Configuration;
import in.pratanumandal.fractalstudio.common.Constants;
import in.pratanumandal.fractalstudio.common.Utils;
import in.pratanumandal.fractalstudio.core.Fractal;
import in.pratanumandal.fractalstudio.core.FractalUtils;
import in.pratanumandal.fractalstudio.core.Point;
import in.pratanumandal.fractalstudio.expression.ComplexParser;
import in.pratanumandal.fractalstudio.fractals.BurningShip;
import in.pratanumandal.fractalstudio.fractals.Julia;
import in.pratanumandal.fractalstudio.fractals.Mandelbrot;
import in.pratanumandal.fractalstudio.fractals.NewtonRaphson;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
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
import org.apache.commons.math3.complex.Complex;
import org.controlsfx.control.ToggleSwitch;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    @FXML private Spinner<Double> iterationLimit;

    @FXML private Label fractalName;
    @FXML private Label fractalFunction;

    @FXML private MenuItem save;
    @FXML private MenuItem export;

    private Fractal fractal;
    private WritableImage image;

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

        AtomicBoolean dragged = new AtomicBoolean(false);
        canvas.setOnMouseDragged(event -> {
            canvas.setCursor(Cursor.MOVE);
            dragged.set(true);
        });

        canvas.setOnMouseReleased(event -> {
            if (pickCenter.isSelected() && !dragged.get()) {
                double factor = (scale.getValue() == null ? 2.0 : scale.getValue()) / (zoom.getValue() == null ? 1.0 : zoom.getValue());

                double xCenter = FractalUtils.precision(((event.getX() / canvas.getWidth()) - 0.5) * 2.0 * factor + currentXCenter, 11);
                double yCenter = FractalUtils.precision((0.5 - (event.getY() / canvas.getHeight())) * 2.0 * factor + currentYCenter, 11);

                DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                df.setMaximumFractionDigits(11);
                centerX.setText(df.format(xCenter));
                centerY.setText(df.format(yCenter));

                pickCenter.setSelected(false);
            }

            if (pickCenter.isSelected()) canvas.setCursor(Cursor.HAND);
            else canvas.setCursor(Cursor.DEFAULT);
            dragged.set(false);
        });

        showOrigin.selectedProperty().addListener((observable, oldValue, newValue) -> this.updatePlot());
        showCenter.selectedProperty().addListener((observable, oldValue, newValue) -> this.updatePlot());

        canvas.setWidth(Configuration.getCanvasSize());
        canvas.setHeight(Configuration.getCanvasSize());

        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });
    }

    @FXML
    private void mandelbrot() {
        fractalName.setText("Mandelbrot");
        fractalFunction.setText("");

        fractal = new Mandelbrot(Configuration.getCanvasSize());
        this.updateFractal();
    }

    @FXML
    private void burningShip() {
        fractalName.setText("Burning Ship");
        fractalFunction.setText("");

        fractal = new BurningShip(Configuration.getCanvasSize());
        this.updateFractal();
    }

    @FXML
    private void julia() {
        Expression<Complex> expression = showFunctionDialog();

        if (expression != null) {
            fractalName.setText("Julia");
            fractalFunction.setText(expression.toString());

            fractal = new Julia(Configuration.getCanvasSize(), expression);
            this.updateFractal();
        }
    }

    @FXML
    private void newtonRaphson() {
        Expression<Complex> expression = showFunctionDialog();

        if (expression != null) {
            fractalName.setText("Newton Raphson");
            fractalFunction.setText(expression.toString());

            fractal = new NewtonRaphson(Configuration.getCanvasSize(), expression);
            this.updateFractal();
        }
    }

    @FXML
    private void updateFractal() {
        update.setDisable(false);

        fractal.setSize(Configuration.getCanvasSize());

        canvas.setWidth(Configuration.getCanvasSize());
        canvas.setHeight(Configuration.getCanvasSize());

        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });

        currentXCenter = Double.valueOf(centerX.getText());
        currentYCenter = Double.valueOf(centerY.getText());

        fractal.setScale(scale.getValue() == null ? 2.0 : scale.getValue());
        fractal.setZoom(zoom.getValue() == null ? 1.0 : zoom.getValue());
        fractal.setSmooth(smooth.isSelected());
        fractal.setInverted(inverted.isSelected());
        fractal.setMonochrome(monochrome.isSelected());
        fractal.setCenter(new Point(currentXCenter, currentYCenter));
        fractal.setIterationLimit(iterationLimit.getValue() == null ? 100.0 : iterationLimit.getValue());

        Progress progress = this.showProgressDialog("Generating fractal", "Please wait. The fractal is being generated.", fractal::interrupt);

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture future = service.scheduleAtFixedRate(
                () -> {
                    if (progress.getController() != null) {
                        progress.getController().setProgress(fractal.getProgress());
                    }
                },
                0, 100, TimeUnit.MILLISECONDS);

        Thread thread = new Thread(fractal);
        thread.start();

        Thread fractalThread = new Thread(() -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            future.cancel(true);
            service.shutdown();

            if (progress.getAlert() != null) {
                Platform.runLater(() -> progress.getAlert().close());
            }

            if (!fractal.isInterrupted()) {
                this.image = fractal.getImage();
                this.updatePlot();

                if (save.isDisable()) save.setDisable(false);
                if (export.isDisable()) export.setDisable(false);
            }
            else {
                if (!save.isDisable()) save.setDisable(true);
                if (!export.isDisable()) export.setDisable(true);
            }
        });
        fractalThread.start();
    }

    @FXML
    private void resetCenter() {
        centerX.setText("0");
        centerY.setText("0");
    }

    @FXML
    private void open() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fractal Studio", "*.fsx"));
        fileChooser.setInitialDirectory(Configuration.getDirectory());

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());

        if (file != null) {
            Configuration.setDirectory(file.getParentFile());

            try {
                this.fractal = Utils.loadFractal(file);
            } catch (JAXBException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(Constants.APPLICATION_NAME);
                    alert.setHeaderText("Error");
                    alert.setContentText("Failed to open fractal from file: " + file.getAbsolutePath());
                    alert.initOwner(canvas.getScene().getWindow());
                    alert.showAndWait();
                });

                return;
            }

            if (fractal instanceof Mandelbrot) {
                fractalName.setText("Mandelbrot");
                fractalFunction.setText("");
            }
            else if (fractal instanceof BurningShip) {
                fractalName.setText("Burning Ship");
                fractalFunction.setText("");
            }
            else if (fractal instanceof Julia) {
                try {
                    Julia julia = (Julia) fractal;

                    Expression<Complex> expression = julia.expression;
                    Map<String, Complex> variables = new HashMap<>();
                    variables.put("z", Complex.NaN);
                    expression.evaluate(variables);

                    fractalName.setText("Julia");
                    fractalFunction.setText(expression.toString());
                }
                catch (Expr4jException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage(), null);
                    errorAlert.initOwner(canvas.getScene().getWindow());
                    errorAlert.showAndWait();

                    return;
                }
                catch (Exception e) {
                    // Do nothing
                }
            }
            else if (fractal instanceof NewtonRaphson) {
                try {
                    NewtonRaphson newtonRaphson = (NewtonRaphson) fractal;

                    Expression<Complex> expression = newtonRaphson.expression;
                    Map<String, Complex> variables = new HashMap<>();
                    variables.put("z", Complex.NaN);
                    expression.evaluate(variables);

                    fractalName.setText("Newton Raphson");
                    fractalFunction.setText(expression.toString());
                }
                catch (Expr4jException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage(), null);
                    errorAlert.initOwner(canvas.getScene().getWindow());
                    errorAlert.showAndWait();

                    return;
                }
                catch (Exception e) {
                    // Do nothing
                }
            }

            Point center = fractal.getCenter();
            DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            df.setMaximumFractionDigits(11);
            centerX.setText(df.format(center.x));
            centerY.setText(df.format(center.y));

            scale.getValueFactory().setValue(fractal.getScale());
            zoom.getValueFactory().setValue(fractal.getZoom());
            smooth.setSelected(fractal.isSmooth());
            inverted.setSelected(fractal.isInverted());
            monochrome.setSelected(fractal.isMonochrome());
            iterationLimit.getValueFactory().setValue(fractal.getIterationLimit());

            this.updateFractal();
        }
    }

    @FXML
    private void save() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Fractal Studio", "*.fsx"));
        fileChooser.setInitialDirectory(Configuration.getDirectory());

        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());

        if (file != null) {
            Configuration.setDirectory(file.getParentFile());

            try {
                Utils.saveFractal(this.fractal, file);

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(Constants.APPLICATION_NAME);
                    alert.setHeaderText("Save");
                    alert.setContentText("Fractal saved to file: " + file.getAbsolutePath());
                    alert.initOwner(canvas.getScene().getWindow());
                    alert.showAndWait();
                });
            } catch (JAXBException e) {
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
    private void export() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp"));
        fileChooser.setInitialDirectory(Configuration.getDirectory());

        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());

        if (file != null) {
            Configuration.setDirectory(file.getParentFile());

            int width = (int) canvas.getWidth();
            int height = (int) canvas.getHeight();

            WritableImage image = new WritableImage(width, height);
            image = canvas.snapshot(null, image);

            WritableImage finalImage = image;
            Thread thread = new Thread(() -> {
                String suffix = FilenameUtils.getExtension(file.getName());
                ImageWriter writer = ImageIO.getImageWritersBySuffix(suffix).next();

                Progress progress = this.showProgressDialog("Exporting image", "Please wait. The image is being saved to file.", writer::abort);

                BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                bufferedImage = SwingFXUtils.fromFXImage(finalImage, bufferedImage);

                AtomicBoolean aborted = new AtomicBoolean(false);

                writer.addIIOWriteProgressListener(new IIOWriteProgressListener() {
                    @Override
                    public void imageStarted(ImageWriter source, int imageIndex) { }

                    @Override
                    public void imageProgress(ImageWriter source, float percentageDone) {
                        if (progress.getController() != null) {
                            progress.getController().setProgress(percentageDone / 100.0);
                        }
                    }

                    @Override
                    public void imageComplete(ImageWriter source) {
                        if (progress.getAlert() != null) {
                            Platform.runLater(() -> progress.getAlert().close());
                        }

                        try {
                            Desktop.getDesktop().open(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void thumbnailStarted(ImageWriter source, int imageIndex, int thumbnailIndex) { }

                    @Override
                    public void thumbnailProgress(ImageWriter source, float percentageDone) { }

                    @Override
                    public void thumbnailComplete(ImageWriter source) { }

                    @Override
                    public void writeAborted(ImageWriter source) {
                        aborted.set(true);
                    }
                });

                try (ImageOutputStream stream = ImageIO.createImageOutputStream(file)) {
                    writer.setOutput(stream);
                    writer.write(bufferedImage);
                } catch (IOException e) {
                    e.printStackTrace();

                    if (progress.getAlert() != null) {
                        Platform.runLater(() -> progress.getAlert().close());
                    }

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(Constants.APPLICATION_NAME);
                        alert.setHeaderText("Error");
                        alert.setContentText("Failed to export fractal to file: " + file.getAbsolutePath());
                        alert.initOwner(canvas.getScene().getWindow());
                        alert.showAndWait();
                    });
                } finally {
                    if (aborted.get()) {
                        file.delete();
                    }
                }
            });

            thread.start();
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

        Utils.setDefaultButton(alert, apply);

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
            Configuration.setMode(controller.getMode());
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

    private Progress showProgressDialog(String header, String message, Function function) {
        AtomicReference<Alert> alertReference = new AtomicReference<>(null);
        AtomicReference<ProgressController> controllerReference = new AtomicReference<>(null);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alertReference.set(alert);

            alert.setTitle(Constants.APPLICATION_NAME);
            alert.setHeaderText(header);

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
            controllerReference.set(controller);

            controller.setMessage(message);

            alert.getDialogPane().getScene().getWindow().setOnCloseRequest(event -> event.consume());

            alert.getDialogPane().contentProperty().set(root);
            alert.initOwner(canvas.getScene().getWindow());

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == abort) {
                function.execute();
            }
        });

        return new Progress(alertReference, controllerReference);
    }

    private Expression<Complex> showFunctionDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setTitle(Constants.APPLICATION_NAME);
        alert.setHeaderText("Function");

        VBox vbox = new VBox();
        vbox.setPrefWidth(360);

        Label label = new Label("Enter the function to generate the fractal.\nUse z as the complex variable.");
        label.setWrapText(true);
        label.setPadding(new Insets(0, 0, 20, 0));

        TextField textField = new TextField();

        vbox.getChildren().addAll(label, textField);

        alert.getDialogPane().contentProperty().set(vbox);

        Node button = alert.getDialogPane().lookupButton(ButtonType.OK);
        button.disableProperty().bind(Bindings.isEmpty(textField.textProperty()));

        alert.initOwner(canvas.getScene().getWindow());
        alert.showAndWait();

        if (alert.getResult() == ButtonType.OK) {
            Expression<Complex> expression = null;

            try {
                ComplexParser parser = new ComplexParser();
                expression = parser.parse(textField.getText());

                Map<String, Complex> variables = new HashMap<>();
                variables.put("z", Complex.NaN);

                expression.evaluate(variables);
            }
            catch (Expr4jException e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, e.getMessage(), null);
                errorAlert.initOwner(canvas.getScene().getWindow());
                errorAlert.showAndWait();

                return null;
            }
            catch (Exception e) {
                // Do nothing
            }

            return expression;
        }

        return null;
    }

    private void updatePlot() {
        boolean showOrigin = this.showOrigin.isSelected();
        boolean showCenter = this.showCenter.isSelected();
        Point center = new Point(currentXCenter, currentYCenter);
        double factor = (scale.getValue() == null ? 2.0 : scale.getValue()) / (zoom.getValue() == null ? 1.0 : zoom.getValue());

        canvas.setWidth(Configuration.getCanvasSize());
        canvas.setHeight(Configuration.getCanvasSize());

        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.drawImage(image, 0, 0);
        });

        if (showOrigin) {
            Platform.runLater(() -> {
                double xCenter = Math.round(canvas.getWidth() * ((factor - (center == null ? 0.0 : center.x)) / (2.0 * factor))) + 0.5;
                double yCenter = Math.round(canvas.getHeight() * ((factor + (center == null ? 0.0 : center.y)) / (2.0 * factor))) + 0.5;

                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setStroke(Color.color(1.0, 1.0, 1.0, 0.7));
                gc.setLineWidth(1.0);
                gc.setLineDashes(0);
                gc.strokeLine(xCenter, 0, xCenter, canvas.getHeight());
                gc.strokeLine(0, yCenter, canvas.getWidth(), yCenter);
            });
        }

        if (showCenter) {
            Platform.runLater(() -> {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setStroke(Color.color(1.0, 1.0, 1.0, 0.7));
                gc.setLineWidth(1.0);
                gc.setLineDashes(2, 4);
                gc.strokeLine(canvas.getWidth() / 2 + 0.5, 0, canvas.getWidth() / 2 + 0.5, canvas.getHeight());
                gc.strokeLine(0, canvas.getHeight() / 2 + 0.5, canvas.getWidth(), canvas.getHeight() / 2 + 0.5);
            });
        }
    }

    private class Progress {
        private AtomicReference<Alert> alert;
        private AtomicReference<ProgressController> controller;

        public Progress(AtomicReference<Alert> alert, AtomicReference<ProgressController> controller) {
            this.alert = alert;
            this.controller = controller;
        }

        public Alert getAlert() {
            return alert.get();
        }

        public ProgressController getController() {
            return controller.get();
        }
    }

    @FunctionalInterface
    private interface Function {
        void execute();
    }

}
