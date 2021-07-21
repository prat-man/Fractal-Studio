package in.pratanumandal.fractalstudio.core;

import in.pratanumandal.fractalstudio.common.Configuration;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;

import java.util.HashMap;
import java.util.Map;

public abstract class Fractal implements Runnable {

    private Map<Kernel, Thread> kernels;
    private boolean kill;

    private Canvas canvas;
    private double scale;
    private boolean smooth;
    private boolean inverted;
    private boolean monochrome;
    private boolean showOrigin;
    private boolean showCenter;
    private Point center;

    public Fractal(Canvas canvas) {
        this.canvas = canvas;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("Scale must be greater than zero");
        }

        this.scale = scale;
    }

    public boolean isSmooth() {
        return smooth;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public boolean isMonochrome() {
        return monochrome;
    }

    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    public boolean isShowOrigin() {
        return showOrigin;
    }

    public void setShowOrigin(boolean showOrigin) {
        this.showOrigin = showOrigin;
    }

    public boolean isShowCenter() {
        return showCenter;
    }

    public void setShowCenter(boolean showCenter) {
        this.showCenter = showCenter;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    @Override
    public final void run() {
        kill = false;
        kernels = new HashMap<>();

        Platform.runLater(() -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        int threadCount = Configuration.getThreadCount();
        int threadIndex = 0;

        int yIncr = (int) (canvasHeight / threadCount);

        for (double y = 0; y < canvasHeight; y += yIncr) {
            double yEnd = (++threadIndex == threadCount) ? canvasHeight : y + yIncr;

            Kernel kernel = new Kernel(this,
                    new Point(0, y),
                    new Point(canvasWidth, y + yIncr),
                    new Point(-1.0 * scale + (center == null ? 0.0 : center.x),
                            ((y - canvasHeight / 2.0) / canvasHeight) * 2.0 * scale - (center == null ? 0.0 : center.y)),
                    new Point(1.0 * scale + (center == null ? 0.0 : center.x),
                            ((yEnd - canvasHeight / 2.0) / canvasHeight) * 2.0 * scale - (center == null ? 0.0 : center.y)));

            Thread thread = new Thread(kernel);
            thread.start();

            kernels.put(kernel, thread);
        }

        if (kill) return;

        for (Thread thread : kernels.values()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (kill) return;

        if (showOrigin) {
            Platform.runLater(() -> {
                double xCenter = Math.round(canvas.getWidth() * ((scale - (center == null ? 0.0 : center.x)) / (2.0 * scale))) + 0.5;
                double yCenter = Math.round(canvas.getHeight() * ((scale + (center == null ? 0.0 : center.y)) / (2.0 * scale))) + 0.5;

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

    public void interrupt() {
        if (kill == true) return;

        kill = true;

        if (kernels != null) {
            for (Map.Entry<Kernel, Thread> entry : kernels.entrySet()) {
                entry.getKey().interrupt();
                try {
                    entry.getValue().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public double getProgress() {
        long processed = 0;
        for (Kernel kernel : kernels.keySet()) {
            processed += kernel.getProcessed();
        }
        return processed / (canvas.getWidth() * canvas.getHeight());
    }

    public abstract Color getColor(Complex z);

}
