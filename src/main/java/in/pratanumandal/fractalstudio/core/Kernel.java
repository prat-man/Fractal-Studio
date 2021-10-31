package in.pratanumandal.fractalstudio.core;

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

        for (double y = canvasStart.y; y < canvasEnd.y; y++) {
            for (double x = canvasStart.x; x < canvasEnd.x; x++) {
                if (kill) return;

                Complex z = new Complex(plotStart.x + ((x - canvasStart.x) / canvasWidth) * plotWidth,
                        plotStart.y + ((y - canvasStart.y) / canvasHeight) * plotHeight);

                this.fractal.compute(new Point(x, y), z);

                processed++;
            }
        }
    }
    
}
