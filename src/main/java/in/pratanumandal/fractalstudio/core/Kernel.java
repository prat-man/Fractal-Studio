package in.pratanumandal.fractalstudio.core;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;

public class Kernel implements Runnable {

    private Fractal fractal;

    private Point canvasStart;
    private Point canvasEnd;

    private Point plotStart;
    private Point plotEnd;

    private boolean kill;

    private long processed;

    public Kernel(Fractal fractal, Point canvasStart, Point canvasEnd, Point plotStart, Point plotEnd) {
        this.fractal = fractal;

        this.canvasStart = canvasStart;
        this.canvasEnd = canvasEnd;

        this.plotStart = plotStart;
        this.plotEnd = plotEnd;
    }

    public long getProcessed() {
        return processed;
    }

    public void interrupt() {
        this.kill = true;
    }

    @Override
    public void run() {
        double canvasWidth = canvasEnd.x - canvasStart.x;
        double canvasHeight = canvasEnd.y - canvasStart.y;

        double plotWidth = plotEnd.x - plotStart.x;
        double plotHeight = plotEnd.y - plotStart.y;

        WritableImage image = new WritableImage((int) canvasWidth, (int) canvasHeight);
        PixelWriter pw = image.getPixelWriter();

        for (double y = canvasStart.y; y < canvasEnd.y; y++) {
            for (double x = canvasStart.x; x < canvasEnd.x; x++) {
                if (kill) return;

                Complex z = new Complex(plotStart.x + ((x - canvasStart.x) / canvasWidth) * plotWidth,
                        plotStart.y + ((y - canvasStart.y) / canvasHeight) * plotHeight);

                Color color = this.fractal.getColor(z);

                if (color != null) {
                    pw.setColor((int) (x - canvasStart.x), (int) (y - canvasStart.y), color);
                }

                processed++;
            }
        }

        Platform.runLater(() -> {
            GraphicsContext gc = this.fractal.getCanvas().getGraphicsContext2D();
            gc.drawImage(image, canvasStart.x, canvasStart.y);
        });
    }
    
}
