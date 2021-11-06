package in.pratanumandal.fractalstudio.core;

import in.pratanumandal.fractalstudio.common.Configuration;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;

import java.util.HashMap;
import java.util.Map;

public abstract class Fractal implements Runnable {

    private Map<Kernel, Thread> kernels;
    private boolean kill;

    private double size;
    private double scale;
    private double zoom;
    private boolean smooth;
    private boolean inverted;
    private boolean monochrome;
    private Point center;
    private double iterationLimit;
    private WritableImage image;

    private boolean indeterminate;

    public Fractal(double size) {
        this.size = size;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
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

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public double getIterationLimit() {
        return iterationLimit;
    }

    public void setIterationLimit(double iterationLimit) {
        this.iterationLimit = iterationLimit;
    }

    public WritableImage getImage() {
        return image;
    }

    @Override
    public void run() {
        kill = false;
        kernels = new HashMap<>();
        indeterminate = false;

        double canvasWidth = this.size;
        double canvasHeight = this.size;

        int threadCount = Configuration.getThreadCount();
        int threadIndex = 0;

        int yIncr = (int) (canvasHeight / threadCount);
        double factor = scale / zoom;

        for (double y = 0; y < canvasHeight; y += yIncr) {
            double yEnd = (++threadIndex == threadCount) ? canvasHeight : y + yIncr;

            Kernel kernel = new Kernel(this,
                    new Point(0, y),
                    new Point(canvasWidth, y + yIncr),
                    new Point(-1.0 * factor + (center == null ? 0.0 : center.x),
                            ((y - canvasHeight / 2.0) / canvasHeight) * 2.0 * factor - (center == null ? 0.0 : center.y)),
                    new Point(1.0 * factor + (center == null ? 0.0 : center.x),
                            ((yEnd - canvasHeight / 2.0) / canvasHeight) * 2.0 * factor - (center == null ? 0.0 : center.y)));

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

        indeterminate = true;

        image = new WritableImage((int) canvasWidth, (int) canvasHeight);
        PixelWriter pw = image.getPixelWriter();

        for (double y = 0; y < canvasHeight; y++) {
            for (double x = 0; x < canvasWidth; x++) {
                if (kill) return;

                Color color = this.getColor(new Point(x, y));

                if (color != null) {
                    pw.setColor((int) x, (int) y, color);
                }
            }
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

    public boolean isInterrupted() {
        return kill;
    }

    public double getProgress() {
        if (indeterminate || kernels == null) return ProgressBar.INDETERMINATE_PROGRESS;

        long processed = 0;
        for (Kernel kernel : kernels.keySet()) {
            processed += kernel.getProcessed();
        }
        return processed / (this.size * this.size);
    }

    public abstract void compute(Point point, Complex z);

    public abstract Color getColor(Point point);

}
